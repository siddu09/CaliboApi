package common;

/**
 * Stores framework configuration and runtime values.
 *
 * Values are loaded once during suite initialization by
 * ConfigurationLoader and reused throughout the framework.
 */
public final class Configuration {

    private Configuration() {
        throw new IllegalStateException("Utility class");
    }

    /*=========================================================
     * Framework
     *=========================================================*/

    /**
     * Indicates whether the framework has been initialized.
     */
    public static boolean initialized = false;

    /*=========================================================
     * Environment
     *=========================================================*/

    public static String environment;

    /*=========================================================
     * Application URLs
     *=========================================================*/

    public static String baseUrl;

    public static String loginUrl;

    /*=========================================================
     * User Credentials
     *=========================================================*/

    public static String username;

    public static String password;

    /*=========================================================
     * Tenant Information
     *=========================================================*/

    public static String tenantName;

    public static String tenantId;

    /*=========================================================
     * Authentication Tokens
     *=========================================================*/

    /**
     * Captured after Selenium login.
     */
    public static String accessToken;

    /**
     * Captured after Selenium login.
     */
    public static String refreshToken;

    /*=========================================================
     * Browser
     *=========================================================*/

    public static String browser;

    public static boolean headless;

    /*=========================================================
     * Selenium Timeouts
     *=========================================================*/

    public static int explicitWait;

    public static int pageLoadTimeout;

    /*=========================================================
     * API
     *=========================================================*/

    public static String contentType;

    /*=========================================================
     * Retry Configuration
     *=========================================================*/

    public static int retryCount;

    public static int retryInterval;

    /*=========================================================
     * Logging
     *=========================================================*/

    public static boolean consoleLogs;

    public static boolean captureRequest;

    public static boolean captureResponse;

    /*=========================================================
     * Execution
     *=========================================================*/

    public static boolean parallelExecution;

    public static int threadCount;

    /*=========================================================
     * Test Data
     *=========================================================*/

    public static String testDataPath;

    public static String usersJson;

    public static String projectJson;

    public static String ideasJson;

}