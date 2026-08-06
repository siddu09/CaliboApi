package pro.building_blocks;

import common.RequestSpecProvider;
import endpoints.ApiEndpoints;
import io.restassured.response.Response;
import org.json.simple.JSONObject;
import org.testng.Assert;

import static io.restassured.RestAssured.given;

public class ProductApiBuildingBlock {
    private String productId;

    public Response addNewProduct(JSONObject request) {
        return given().spec(RequestSpecProvider.get()).body(request.toJSONString())
                .post(ApiEndpoints.PROJECTS_V2);
    }

    public void verifyProduct(Response response) {
        Assert.assertEquals(response.statusCode(), 201, response.asString());
        productId = response.jsonPath().getString("id");
        Assert.assertNotNull(productId, "Product ID is missing");
    }

    public String getProductId() {
        return productId;
    }
}
