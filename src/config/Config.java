package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Framework Configuration - Loads and stores all config values.
 * 
 * Combines configuration loading and storage in one place.
 */
public final class Config {

    private Config() {
        throw new IllegalStateException("Utility class");
    }

    /*=========================================================
     * FRAMEWORK
     *=========================================================*/

    public static volatile boolean initialized = false;

    /*=========================================================
     * ENVIRONMENT
     *=========================================================*/

    public static String baseUrl;
    public static String loginUrl;

    /*=========================================================
     * CREDENTIALS
     *=========================================================*/

    public static String username;
    public static String password;

    /*=========================================================
     * TENANT
     *=========================================================*/

    public static String tenantName;
    public static String tenantId;

    /*=========================================================
     * AUTHENTICATION
     *=========================================================*/

    public static String accessToken;

    /*=========================================================
     * BROWSER
     *=========================================================*/

    public static boolean headless;

    /*=========================================================
     * TIMEOUTS
     *=========================================================*/

    public static int explicitWait;
    public static int retryCount;
    public static int retryInterval;

    /*=========================================================
     * TEST DATA
     *=========================================================*/

    public static String testDataPath;
    public static String usersJson;

    /*=========================================================
     * CONFIGURATION LOADER
     *=========================================================*/

    /**
     * Loads configuration from config.properties file.
     * Called once during suite initialization.
     */
    public static synchronized void load() {

        if (initialized) {
            return;
        }

        Properties props = new Properties();

        try (InputStream input = Config.class
                .getClassLoader()
                .getResourceAsStream(Constants.CONFIG_FILE)) {

            if (input == null) {
                throw new RuntimeException(
                        "config.properties not found in resources folder");
            }

            props.load(input);

            // Environment
            baseUrl = getRequired(props, "base.url");
            loginUrl = getRequired(props, "login.url");

            // Credentials
            username = getSecret(props, "username", "calibo.username", "CALIBO_USERNAME");
            password = getSecret(props, "password", "calibo.password", "CALIBO_PASSWORD");

            // Tenant
            tenantName = getRequired(props, "tenant.name");
            tenantId = props.getProperty("tenant.id", "");

            // Browser
            headless = Boolean.parseBoolean(System.getProperty(
                    "headless", props.getProperty("headless", "false")));

            // Timeouts
            explicitWait = Integer.parseInt(
                    props.getProperty("explicit.wait", "60"));
            retryCount = Integer.parseInt(
                    props.getProperty("retry.count", "2"));
            retryInterval = Integer.parseInt(
                    props.getProperty("retry.interval", "3000"));

            // Test Data
            testDataPath = props.getProperty(
                    "test.data.path", "resources/");
            usersJson = props.getProperty(
                    "users.json", "users.json");

            initialized = true;

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to load configuration", e);
        }
    }

    private static String getRequired(Properties props, String key) {
        String value = props.getProperty(key);
        if (Objects.isNull(value) || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing required property: " + key);
        }
        return value.trim();
    }

    private static String getSecret(Properties props, String key,
                                    String systemProperty, String environmentVariable) {
        String value = System.getProperty(systemProperty);
        if (value == null || value.isBlank()) value = System.getenv(environmentVariable);
        if (value == null || value.isBlank()) value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing secret: set -D" + systemProperty
                    + " or environment variable " + environmentVariable);
        }
        return value.trim();
    }
}
