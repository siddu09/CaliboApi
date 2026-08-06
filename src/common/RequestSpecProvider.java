package common;

import config.Config;
import config.Constants;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.nio.file.Path;

public final class RequestSpecProvider {

    private static RequestSpecification requestSpecification;

    private RequestSpecProvider() {
    }

    public static synchronized void initialize() {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(Config.baseUrl)
                .addHeader(Constants.AUTHORIZATION, Constants.BEARER + Config.accessToken)
                .addHeader(Constants.TENANT_ID, Config.tenantId)
                .setContentType(ContentType.JSON);
        ApiLogger.addTo(builder);
        requestSpecification = builder.build();
    }

    public static RequestSpecification get() {
        if (requestSpecification == null) {
            throw new IllegalStateException("Request specification has not been initialized");
        }
        return new RequestSpecBuilder().addRequestSpecification(requestSpecification).build();
    }

    public static Path getApiLogPath() {
        return ApiLogger.getLogPath();
    }

    public static synchronized void closeApiLogging() {
        ApiLogger.close();
    }
}
