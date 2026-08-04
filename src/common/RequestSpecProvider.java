package common;

import config.Config;
import config.Constants;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static io.restassured.config.LogConfig.logConfig;

public class RequestSpecProvider {

    private static RequestSpecification requestSpecification;
    private static PrintStream apiLogStream;
    private static Path apiLogPath;

    public static synchronized void initialize() {

        initializeApiLogging();

        RestAssuredConfig restAssuredConfig = RestAssuredConfig.config()
                .logConfig(logConfig()
                        .blacklistHeader(Constants.AUTHORIZATION)
                        .blacklistHeader("Cookie")
                        .blacklistHeader("Set-Cookie"));

        requestSpecification =
                new RequestSpecBuilder()
                        .setBaseUri(Config.baseUrl)
                        .setConfig(restAssuredConfig)
                        .addHeader(Constants.AUTHORIZATION,
                                Constants.BEARER + Config.accessToken)
                        .addHeader(Constants.TENANT_ID,
                                Config.tenantId)
                        .addHeader("Accept", Constants.APPLICATION_JSON)
                        .setContentType(ContentType.JSON)
                        .addFilter(new RequestLoggingFilter(LogDetail.ALL, true, apiLogStream,
                                true, Set.of(Constants.AUTHORIZATION, "Cookie", "Set-Cookie")))
                        .addFilter(new ResponseLoggingFilter(apiLogStream))
                        .build();

        System.out.println("[RequestSpec] Initialized for " + Config.baseUrl);
        System.out.println("[RequestSpec] API log: " + apiLogPath.toAbsolutePath());
    }

    public static RequestSpecification get() {
        if (requestSpecification == null) {
            throw new IllegalStateException("Request specification has not been initialized");
        }
        return new RequestSpecBuilder().addRequestSpecification(requestSpecification).build();
    }

    public static Path getApiLogPath() {
        return apiLogPath;
    }

    public static synchronized void closeApiLogging() {
        if (apiLogStream != null) {
            apiLogStream.flush();
            apiLogStream.close();
            apiLogStream = null;
        }
    }

    private static void initializeApiLogging() {
        try {
            Path logDirectory = Path.of("target", "api-logs");
            Files.createDirectories(logDirectory);
            String timestamp = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String testName = System.getProperty("test", "api-execution")
                    .replaceAll("[^A-Za-z0-9_.-]", "_");
            apiLogPath = logDirectory.resolve(testName + "-api-" + timestamp + ".log");
            apiLogStream = new PrintStream(Files.newOutputStream(apiLogPath), true);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to initialize API request/response logging", exception);
        }
    }
}
