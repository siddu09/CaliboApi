package tests;

import base.BaseTest;
import common.RequestSpecProvider;
import endpoints.ApiEndpoints;
import io.restassured.response.Response;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;
import pro.building_blocks.ProductApiBuildingBlock;
import pro.helper.ProductApiHelper;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class ProductDependencyTest extends BaseTest {
    @Test(description = "Create two products, approve their dependency notification, and verify the relationship")
    @SuppressWarnings("unchecked")
    public void approveProductDependency() {
        ProductApiHelper helper = new ProductApiHelper();
        String suffix = String.valueOf(System.currentTimeMillis());
        String firstId = createProduct(helper, suffix + "A");
        String secondId = createProduct(helper, suffix + "B");
        String portfolio = helper.loadProductTestData().get("portfolioTitle").toString();
        List<String> existingNotifications = notificationIds(notifications(secondId));

        Response dependency = given().spec(RequestSpecProvider.get())
                .pathParam("sourceProjectId", firstId).pathParam("targetProjectId", secondId)
                .queryParam("portfolioName", portfolio).patch(ApiEndpoints.PROJECT_DEPENDENCY);
        Assert.assertTrue(dependency.statusCode() == 200 || dependency.statusCode() == 204,
                "Add dependency failed: " + dependency.asString());

        Response notifications = notifications(secondId);
        Assert.assertEquals(notifications.statusCode(), 200, notifications.asString());
        String notificationId = notificationIds(notifications).stream()
                .filter(id -> !existingNotifications.contains(id)).findFirst().orElse(null);
        Assert.assertNotNull(notificationId, "Dependency notification ID is missing");

        Response approval = given().spec(RequestSpecProvider.get()).pathParam("notificationId", notificationId)
                .patch(ApiEndpoints.PROJECT_NOTIFICATION_STATUS);
        Assert.assertTrue(approval.statusCode() == 200 || approval.statusCode() == 204,
                "Notification approval failed: " + approval.asString());

        Response verified = notifications(secondId);
        Assert.assertEquals(verified.statusCode(), 200, verified.asString());
        System.out.printf("Product dependency approved: %s -> %s%n", firstId, secondId);
    }

    @SuppressWarnings("unchecked")
    private String createProduct(ProductApiHelper helper, String suffix) {
        JSONObject request = helper.loadProductTestData();
        request.put("title", request.get("title") + suffix);
        ProductApiBuildingBlock product = new ProductApiBuildingBlock();
        product.verifyProduct(product.addNewProduct(request));
        return product.getProductId();
    }

    private List<String> notificationIds(Response response) {
        Assert.assertEquals(response.statusCode(), 200, response.asString());
        List<Map<String, Object>> notifications = response.jsonPath().getList("");
        return notifications.stream().map(item -> String.valueOf(item.get("id"))).toList();
    }

    private Response notifications(String productId) {
        return given().spec(RequestSpecProvider.get()).pathParam("projectId", productId)
                .get(ApiEndpoints.PROJECT_NOTIFICATIONS);
    }
}
