rootProject.name = "MumbleLink"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            plugin("shadow", "com.gradleup.shadow").version("9.0.0-rc1")
            plugin("loom", "fabric-loom").version("1.11-SNAPSHOT")
        }
    }
}