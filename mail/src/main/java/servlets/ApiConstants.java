package servlets;

/**
 * Constants for all API endpoints used in the auth application.
 * This replaces the old ServletHandler URL constants.
 */
public final class ApiConstants {

    private ApiConstants() {
        // Utility class - prevent instantiation
    }

    // --- Authentication URLs ---
    public static final String TEST_URL = "/v1/test"; // GET
}
