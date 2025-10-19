package servlets;

/**
 * Constants for all API endpoints used in the auth application.
 * This replaces the old ServletHandler URL constants.
 */
public final class ApiConstants {

    private ApiConstants() {
        // Utility class - prevent instantiation
    }

    public static final String TEST_URL = "/v1/test"; // POST

    // --- Send Mail URLs ---
    public static final String SEND_MAIL_URL = "/v1/emails/send"; // POST
}
