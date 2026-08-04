package validators;

import io.restassured.response.Response;
import org.testng.Assert;

public final class PortfolioProductFeatureValidator {

    public void successful(Response response, int expectedStatus, String operation) {
        Assert.assertEquals(response.statusCode(), expectedStatus,
                operation + " failed: " + response.asString());
    }

    public String createdEntity(Response response, String expectedTitle, String operation) {
        successful(response, 201, operation);
        String id = response.jsonPath().getString("id");
        Assert.assertTrue(id != null && !id.isBlank(), operation + " response has no id");
        Assert.assertEquals(response.jsonPath().getString("title"), expectedTitle);
        return id;
    }

    public String required(String value, String field) {
        Assert.assertTrue(value != null && !value.isBlank() && !"null".equals(value),
                field + " is missing");
        return value;
    }

    public void featureBelongsToProduct(Response response, String productId) {
        Assert.assertEquals(response.jsonPath().getString("projectId"), productId,
                "Created feature does not belong to the created product");
    }
}
