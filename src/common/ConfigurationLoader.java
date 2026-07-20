package common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Loads framework configuration from config.properties.
 *
 * This class should be executed only once before the test suite starts.
 */
public final class ConfigurationLoader {

    private static final String CONFIG_FILE = "resources/config.properties";

    private ConfigurationLoader() {
    }

    /**
     * Loads configuration into Configuration class.
     */
    public static synchronized void load() {

        if (Configuration.initialized) {
            return;
        }

        Properties properties = new Properties();

        try (InputStream inputStream = ConfigurationLoader.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {

            if (inputStream == null) {
                throw new RuntimeException(
                        "Unable to find " + CONFIG_FILE + " under resources folder.");
            }

            properties.load(inputStream);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to load configuration file.", e);
        }

        loadEnvironment(properties);
        loadUrls(properties);
        loadCredentials(properties);
        loadTenant(properties);
        loadBrowser(properties);
        loadTimeouts(properties);
        loadApi(properties);

        Configuration.initialized = true;
    }

    /*-------------------------------------------------------
     * Environment
     *-------------------------------------------------------*/

    private static void loadEnvironment(Properties properties) {

        Configuration.environment =
                getRequired(properties, "environment");
    }

    /*-------------------------------------------------------
     * URLs
     *-------------------------------------------------------*/

    private static void loadUrls(Properties properties) {

        Configuration.baseUrl =
                getRequired(properties, "base.url");

        Configuration.loginUrl =
                getRequired(properties, "login.url");
    }

    /*-------------------------------------------------------
     * Credentials
     *-------------------------------------------------------*/

    private static void loadCredentials(Properties properties) {

        Configuration.username =
                getRequired(properties, "username");

        Configuration.password =
                getRequired(properties, "password");
    }

    /*-------------------------------------------------------
     * Tenant
     *-------------------------------------------------------*/

    private static void loadTenant(Properties properties) {

        Configuration.tenantName =
                getRequired(properties, "tenant.name");

        Configuration.tenantId =
                properties.getProperty("tenant.id", "");
    }

    /*-------------------------------------------------------
     * Browser
     *-------------------------------------------------------*/

    private static void loadBrowser(Properties properties) {

        Configuration.headless =
                Boolean.parseBoolean(
                        properties.getProperty("headless", "false"));
    }

    /*-------------------------------------------------------
     * Timeouts
     *-------------------------------------------------------*/

    private static void loadTimeouts(Properties properties) {

        Configuration.explicitWait =
                Integer.parseInt(
                        properties.getProperty("explicit.wait", "60"));

        Configuration.pageLoadTimeout =
                Integer.parseInt(
                        properties.getProperty("page.load.timeout", "120"));
    }

    /*-------------------------------------------------------
     * API
     *-------------------------------------------------------*/

    private static void loadApi(Properties properties) {

        Configuration.contentType =
                properties.getProperty(
                        "content.type",
                        "application/json");
    }

    /**
     * Returns required property.
     */
    private static String getRequired(Properties properties,
                                      String key) {

        String value = properties.getProperty(key);

        if (Objects.isNull(value) || value.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Missing required property : " + key);
        }

        return value.trim();
    }

}