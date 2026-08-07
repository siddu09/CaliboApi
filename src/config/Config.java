package config;

import java.io.InputStream;
import java.util.Properties;

public final class Config {
    public static final String testDataPath = "resources/";
    public static String baseUrl, loginUrl, username, password, tenantName, tenantId, accessToken;
    public static boolean headless;
    public static int explicitWait, retryCount, retryInterval;
    private static boolean loaded;

    private Config() {}

    public static synchronized void load() {
        if (loaded) return;
        Properties properties = new Properties();
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream(Constants.CONFIG_FILE)) {
            if (input == null) throw new IllegalStateException("Missing " + Constants.CONFIG_FILE);
            properties.load(input);
            baseUrl = properties.getProperty("base.url");
            loginUrl = properties.getProperty("login.url");
            username = properties.getProperty("username");
            password = properties.getProperty("password");
            tenantName = properties.getProperty("tenant.name");
            tenantId = properties.getProperty("tenant.id");
            headless = Boolean.parseBoolean(System.getProperty("headless", properties.getProperty("headless")));
            explicitWait = Integer.parseInt(properties.getProperty("explicit.wait", "60"));
            retryCount = Integer.parseInt(properties.getProperty("retry.count", "2"));
            retryInterval = Integer.parseInt(properties.getProperty("retry.interval", "3000"));
            loaded = true;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load configuration", e);
        }
    }
}
