package services;

import api.PortfolioProductFeatureApiClient;
import config.Config;
import io.restassured.response.Response;
import org.json.simple.JSONObject;
import utils.JsonUtils;
import validators.PortfolioProductFeatureValidator;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

public final class PortfolioProductFeatureService {

    private static final String DATA_FILE = "PortfolioProductFeatureSetup.json";
    private final PortfolioProductFeatureApiClient api = new PortfolioProductFeatureApiClient();
    private final PortfolioProductFeatureValidator validator = new PortfolioProductFeatureValidator();

    public void createPortfolioProductAndFeature() {
        CreationContext context = createPortfolioAndProductContext();
        JSONObject data = context.data;
        Map<String, Object> user = context.user;
        String ownerRoleId = context.ownerRoleId;
        String suffix = context.suffix;
        long startsOn = context.startsOn;
        long endsOn = context.endsOn;
        String productId = context.productId;

        try {
            String featureTitle = data.get("featurePrefix") + suffix;
            Response featureResponse = api.createFeature(PortfolioProductFeatureRequestBuilder.feature(
                    data, user, ownerRoleId, productId, featureTitle, startsOn, endsOn));
            String featureId = validator.createdEntity(featureResponse, featureTitle, "create feature");
            validator.featureBelongsToProduct(featureResponse, productId);

            System.out.printf("[PortfolioFlow] Feature: %s (%s)%n", featureTitle, featureId);
        } finally {
            validator.successful(api.deleteProduct(productId,
                    PortfolioProductFeatureRequestBuilder.cleanup()), 204, "cleanup product");
            System.out.printf("[PortfolioFlow] Cleaned up product: %s%n", productId);
        }
    }

    private CreationContext createPortfolioAndProductContext() {
        JSONObject data = JsonUtils.readJson(Config.testDataPath + DATA_FILE);
        Map<String, Object> user = currentUser();
        String ownerRoleId = ownerRoleId(data.get("ownerRoleKey").toString());
        String suffix = UUID.randomUUID().toString().substring(0, 6);
        long startsOn = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        long endsOn = Instant.now().plus(30, ChronoUnit.DAYS).toEpochMilli();

        String portfolioTitle = data.get("portfolioPrefix") + suffix;
        String portfolioId = validator.createdEntity(api.createPortfolio(
                PortfolioProductFeatureRequestBuilder.portfolio(data, user, portfolioTitle)),
                portfolioTitle, "create portfolio");

        String productTitle = data.get("productPrefix") + suffix;
        String productId = validator.createdEntity(api.createProduct(
                PortfolioProductFeatureRequestBuilder.product(data, user, ownerRoleId,
                        portfolioId, portfolioTitle, productTitle, startsOn, endsOn)),
                productTitle, "create product");

        System.out.printf("[PortfolioFlow] Portfolio: %s (%s)%n", portfolioTitle, portfolioId);
        System.out.printf("[PortfolioFlow] Product: %s (%s)%n", productTitle, productId);
        return new CreationContext(data, user, ownerRoleId, suffix, startsOn, endsOn, productId);
    }

    private Map<String, Object> currentUser() {
        Response response = api.getCurrentUser();
        validator.successful(response, 200, "get current user");
        Map<String, Object> user = response.jsonPath().getMap("");
        for (String field : new String[]{"id", "databaseId", "username", "email", "firstName", "lastName"}) {
            validator.required(String.valueOf(user.get(field)), "userInfo." + field);
        }
        return user;
    }

    private String ownerRoleId(String ownerRoleKey) {
        Response response = api.getProjectRoles();
        validator.successful(response, 200, "get project roles");
        return validator.required(response.jsonPath().getString(
                "find { it.key == '" + ownerRoleKey + "' }.id"), "owner role id");
    }

    private record CreationContext(JSONObject data, Map<String, Object> user, String ownerRoleId,
                                   String suffix, long startsOn, long endsOn, String productId) {}
}
