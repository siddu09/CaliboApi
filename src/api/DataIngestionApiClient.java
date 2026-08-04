package api;

import common.RequestSpecProvider;
import config.Config;
import io.restassured.http.Method;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

/** Executes one recorded data-ingestion HTTP transaction. */
public final class DataIngestionApiClient {

    public Response execute(String method, String relativeUri, String body) {
        int attempts = isSafeToRetry(method, relativeUri) ? Config.retryCount + 1 : 1;
        Response response = null;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            response = executeOnce(method, relativeUri, body);
            if (!isRetryableGatewayFailure(response) || attempt == attempts) return response;
            waitBeforeRetry();
        }
        return response;
    }

    private boolean isSafeToRetry(String method, String relativeUri) {
        return "GET".equals(method)
                || ("POST".equals(method)
                && relativeUri.startsWith("/databricks/config/clusterWhlMapping"));
    }

    private Response executeOnce(String method, String relativeUri, String body) {
        var request = given().spec(RequestSpecProvider.get());
        if (body != null && !body.isBlank()) request.body(body);
        return request.request(Method.valueOf(method), relativeUri).then().extract().response();
    }

    private boolean isRetryableGatewayFailure(Response response) {
        return response.statusCode() == 502
                || response.statusCode() == 503
                || response.statusCode() == 504;
    }

    private void waitBeforeRetry() {
        try {
            Thread.sleep(Config.retryInterval);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while retrying Data Ingestion API call", exception);
        }
    }
}
