package me.kubbidev.mumble.jna;

import com.sun.jna.Library;
import com.sun.jna.Structure;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * The LinkApi interface is designed to interface with Mumble's positional audio system,
 * allowing external applications to interact with the Mumble client.
 * <p>
 * It enables the configuration and updating of context, identity, positional data, and more
 * for Mumble's integration.
 */
public interface LinkApi extends Library {

    /** Represents the fixed length of a vector in a system or calculation */
    int VECTOR_LENGTH = 3;

    /** Represents the maximum allowed length for an identity */
    int MAX_IDENTITY_LENGTH = 256;

    /** Represents the maximum allowed length for a name */
    int MAX_NAME_LENGTH = 256;

    /** A constant representing the maximum allowable length for context data */
    int MAX_CONTEXT_LENGTH = 256;

    /** Specifies the maximum allowable length for a lore */
    int MAX_LORE_LENGTH = 2048;

    /**
     * Initializes the instance with the provided name, description, and UI version.
     * This setup is required before interacting with the system.
     *
     * @param name        the display name of the application
     * @param description a descriptive text explaining the application
     * @param uiVersion   the version of the user interface
     * @return an integer representing the initialization status;
     *         typically used to determine if the initialization was successful or not
     *
     * @implNote <code>int initialize(wchar_t[256], wchar_t[2048], UINT32)</code>
     */
    int initialize(CharBuffer name, CharBuffer description, int uiVersion);

    /**
     * Updates the identity of the user.
     * <p>
     * The identity is used to uniquely identify a user and should be updated
     * when the user identity changes.
     *
     * <p>This method does not update any other data or context information.</p>
     *
     * @param identity a {@code CharBuffer} containing the unique identifier
     *                 for the user.
     * @return a byte indicating the success or failure of the operation.
     *         Typically, a non-zero value indicates failure.
     *
     * @implNote <code>bool updateIdentity(wchar_t[256])</code>
     */
    byte updateIdentity(CharBuffer identity);

    /**
     * Updates the context with provided data.
     * <p>
     * The context string is used to determine which users on a Mumble server
     * should hear each other positionally.
     * <p>
     * If context between two mumble user does not match the positional audio
     * data is stripped server-side and voice will be received as non-positional.
     *
     * @param context     a {@code ByteBuffer} containing the context data to be updated.
     * @param context_len the length of the context (number of active array elements).
     * @return a byte indicating the success or failure of the operation.
     *         Typically, a non-zero value indicates failure.
     *
     * @implNote <code>bool updateContext(unsigned char[256], UINT32)</code>
     */
    byte updateContext(ByteBuffer context, int context_len);

    /**
     * Updates both the identity and context data.
     *
     * @param identity    a {@code CharBuffer} containing the unique identifier for the user.
     * @param context     a {@code ByteBuffer} containing the context data to be updated.
     * @param context_len the length of the context (number of active bytes in the context).
     * @return a byte indicating the success or failure of the operation.
     *         Typically, a non-zero value indicates failure.
     *
     * @implNote <code>bool updateIdentityAndContext(wchar_t[256], unsigned char[256], UINT32)</code>
     * @see #updateIdentity(CharBuffer)
     * @see #updateContext(ByteBuffer, int)
     */
    byte updateIdentityAndContext(CharBuffer identity, ByteBuffer context, int context_len);

    /**
     * Updates the name of the application or user.
     * <p>
     * this name is shown in the mumble interface to indicate which plugin's
     * positional audio is being used (i.e. used for the "XXX linked." message
     * in the mumble log)
     *
     * @param name a {@code CharBuffer} containing the new name to be updated.
     * @return a byte indicating the success or failure of the operation.
     *         Typically, a non-zero value indicates failure.
     *
     * @implNote <code>bool updateName(wchar_t[256])</code>
     */
    byte updateName(CharBuffer name);

    /**
     * Updates the description data.
     *
     * @param description a {@code CharBuffer} containing the new description to be updated.
     * @return a byte indicating the success or failure of the operation.
     *         Typically, a non-zero value indicates failure.
     *
     * @implNote <code>bool updateDescription(wchar_t[2048])</code>
     */
    byte updateDescription(CharBuffer description);

    /**
     * Updates the spatial vectors for both avatar and camera.
     * <p>
     * This method updates the position, front, and top unit vectors
     * for the avatar and camera in the 3D space used by the LinkApi system.
     *
     * <p>These vectors are essential for accurately calculating spatial audio.</p>
     *
     * @param fAvatarPosition the avatar's position vector.
     * @param fAvatarFront the avatar's front vector, indicating the forward
     *                    direction of the avatar.
     * @param fAvatarTop the avatar's top vector, representing the upward direction
     *                  from the avatar's perspective.
     * @param fCameraPosition the camera's position vector.
     * @param fCameraFront the camera's front vector, indicating the forward
     *                    direction of the camera.
     * @param fCameraTop the camera's top vector, representing the upward direction
     *                  from the camera's perspective.
     * @return a byte indicating the success or failure of the operation.
     *
     * @implNote <code>bool updateVectors(float[3], float[3], float[3], float[3], float[3], float[3])</code>
     */
    byte updateVectors(
            FloatBuffer fAvatarPosition,
            FloatBuffer fAvatarFront,
            FloatBuffer fAvatarTop,
            FloatBuffer fCameraPosition,
            FloatBuffer fCameraFront,
            FloatBuffer fCameraTop
    );

    /**
     * Updates the spatial vectors for the avatar in the 3D space.
     *
     * <p>This method updates the position, front, and top unit vectors for the avatar.</p>
     *
     * <p>These vectors are essential for accurately calculating spatial audio effects.</p>
     *
     * @param fAvatarPosition the avatar's position vector, representing its location in 3D space.
     * @param fAvatarFront the avatar's front vector, indicating the forward direction of the avatar.
     * @param fAvatarTop the avatar's top vector, representing the upward direction from the avatar's perspective.
     * @return a byte indicating the success or failure of the update operation.
     *
     * @implNote <code>bool updateVectorsByAvatar(float[3], float[3], float[3])</code>
     */
    byte updateVectorsByAvatar(
            FloatBuffer fAvatarPosition,
            FloatBuffer fAvatarFront,
            FloatBuffer fAvatarTop
    );

    /**
     * Updates the system data with the given {@link LinkedMem} instance.
     * <p>
     * The data includes various fields such as spatial vectors, identity,
     * context, name, and description information, as defined in the {@link LinkedMem} structure.
     *
     * @param source a {@link LinkedMem} object containing the new data to be updated.
     * @return a byte indicating the success or failure of the operation.
     *
     * @implNote <code>bool updateData(LinkedMem*)</code>
     */
    byte updateData(LinkedMem source);

    class LinkedMem extends Structure {

        /** C type : UINT32 */
        public int uiVersion;
        public int uiTick;

        /** C type : float[3] */
        public float[] fAvatarPosition = new float[VECTOR_LENGTH];

        /** C type : float[3] */
        public float[] fAvatarFront = new float[VECTOR_LENGTH];

        /** C type : float[3] */
        public float[] fAvatarTop = new float[VECTOR_LENGTH];

        /** C type : wchar_t[256] */
        public char[] name = new char[MAX_NAME_LENGTH];

        /** C type : float[3] */
        public float[] fCameraPosition = new float[VECTOR_LENGTH];

        /** C type : float[3] */
        public float[] fCameraFront = new float[VECTOR_LENGTH];

        /** C type : float[3] */
        public float[] fCameraTop = new float[VECTOR_LENGTH];

        /** C type : wchar_t[256] */
        public char[] identity = new char[MAX_IDENTITY_LENGTH];

        /** C type : UINT32 */
        public int context_len;

        /** C type : unsigned char[256] */
        public byte[] context = new byte[MAX_CONTEXT_LENGTH];

        /** C type : wchar_t[2048] */
        public char[] lore = new char[MAX_LORE_LENGTH];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(
                    "uiVersion",
                    "uiTick",
                    "fAvatarPosition",
                    "fAvatarFront",
                    "fAvatarTop",
                    "name",
                    "fCameraPosition",
                    "fCameraFront",
                    "fCameraTop",
                    "identity",
                    "context_len",
                    "context",
                    "description");
        }

        public static class ByReference extends LinkedMem implements Structure.ByReference {
        }

        public static class ByValue extends LinkedMem implements Structure.ByValue {
        }

    }

}
