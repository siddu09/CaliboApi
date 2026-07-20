package constants;

/**
 * Framework level constants.
 *
 * Contains only common reusable constants used throughout the framework.
 */
public final class FrameworkConstants {

    private FrameworkConstants() {
        throw new IllegalStateException("Utility class");
    }

    /*=========================================================
     * Framework
     *=========================================================*/

    public static final String FRAMEWORK_NAME =
            "Calibo API Automation Framework";

    public static final String FRAMEWORK_VERSION =
            "1.0.0";

    /*=========================================================
     * Configuration
     *=========================================================*/

    public static final String CONFIG_FILE =
            "resources/config.properties";

    /*=========================================================
     * Browser
     *=========================================================*/

    public static final String CHROME =
            "chrome";

    public static final String EDGE =
            "edge";

    /*=========================================================
     * Content Type
     *=========================================================*/

    public static final String APPLICATION_JSON =
            "application/json";

    /*=========================================================
     * Headers
     *=========================================================*/

    public static final String AUTHORIZATION =
            "Authorization";

    public static final String TENANT_ID =
            "x-tenantid";

    public static final String CONTENT_TYPE =
            "Content-Type";

    public static final String BEARER =
            "Bearer ";

    /*=========================================================
     * Cookies
     *=========================================================*/

    public static final String ACCESS_TOKEN =
            "access_token";

    public static final String REFRESH_TOKEN =
            "refresh_token";

    /*=========================================================
     * Timeouts
     *=========================================================*/

    public static final int DEFAULT_EXPLICIT_WAIT =
            60;

    public static final int DEFAULT_PAGE_LOAD_TIMEOUT =
            120;

    /*=========================================================
     * Retry
     *=========================================================*/

    public static final int DEFAULT_RETRY_COUNT =
            2;

    public static final int DEFAULT_RETRY_INTERVAL =
            3000;

    /*=========================================================
     * File Locations
     *=========================================================*/

    public static final String TEST_RESOURCES =
            "src/test/resources/";

    public static final String JSON_FOLDER =
            TEST_RESOURCES + "json/";

    public static final String CONFIG_FOLDER =
            TEST_RESOURCES;

    /*=========================================================
     * JSON Files
     *=========================================================*/

    public static final String USERS_JSON =
            "users.json";

    public static final String PROJECT_JSON =
            "project.json";

    public static final String IDEAS_JSON =
            "ideas.json";

    /*=========================================================
     * Request Specification
     *=========================================================*/

    public static final String DEFAULT_CHARSET =
            "UTF-8";

}