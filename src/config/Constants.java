package config;

/**
 * Framework configuration constants.
 * 
 * Centralized configuration for:
 * - Framework settings
 * - File paths
 * - Timeouts
 */
public final class Constants {

    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    /*=========================================================
     * HEADERS
     *=========================================================*/

    public static final String AUTHORIZATION = "Authorization";
    public static final String TENANT_ID = "x-tenantid";
    public static final String APPLICATION_JSON = "application/json";
    public static final String BEARER = "Bearer ";

    /*=========================================================
     * FILES
     *=========================================================*/

    public static final String CONFIG_FILE = "resources/config.properties";
    public static final String DEV_STAGE_SETUP_JSON = "DevStageSetup.json";
    public static final String DATA_INGESTION_SETUP_JSON = "DataIngestionSetup.json";

}
