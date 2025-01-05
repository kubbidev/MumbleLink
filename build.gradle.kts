import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.io.ByteArrayOutputStream

plugins {
    id("java")
    id("java-library")
    alias(libs.plugins.shadow)
    alias(libs.plugins.loom)
}

group = "me.kubbidev"
version = "1.0-SNAPSHOT"

base {
    archivesName.set("mumblelink")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

fun determinePatchVersion(): Int {
    // get the name of the last tag
    val tagInfo = ByteArrayOutputStream()
    exec {
        commandLine("git", "describe", "--tags")
        standardOutput = tagInfo
    }
    val tagString = String(tagInfo.toByteArray())
    if (tagString.contains("-")) {
        return tagString.split("-")[1].toInt()
    }
    return 0
}

val majorVersion = "1"
val minorVersion = "0"
val patchVersion = determinePatchVersion()
val releaseVersion = "$majorVersion.$minorVersion"
val projectVersion = "$releaseVersion.$patchVersion"

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:1.20.4")
    mappings("net.fabricmc:yarn:1.20.4+build.3:v2")
    modImplementation("net.fabricmc:fabric-loader:0.16.9")

    val apiModules = listOf(
        "fabric-api"
    )

    apiModules.forEach {
        modImplementation(fabricApi.module(it, "0.97.2+1.20.4"))
    }

    // tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
    testImplementation("org.testcontainers:junit-jupiter:1.19.8")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Test>().configureEach {
    testLogging {
        events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
    }
}

tasks.shadowJar {
    archiveFileName = "mumblelink-${projectVersion}-dev.jar"

    mergeServiceFiles()
    dependencies {
        exclude(dependency("net.fabricmc:.*"))
        include(dependency("me.kubbidev:.*"))

        // we don't want to include the mappings in the jar do we?
        exclude("/mappings/*")
    }
}

val remappedShadowJar by tasks.registering(RemapJarTask::class) {
    dependsOn(tasks.shadowJar)

    inputFile = tasks.shadowJar.flatMap {
        it.archiveFile
    }
    addNestedDependencies = true
    archiveFileName = "MumbleLink-Fabric-${projectVersion}.jar"
}

tasks.assemble {
    dependsOn(remappedShadowJar)
}

artifacts {
    archives(remappedShadowJar)
    archives(tasks.shadowJar)
}