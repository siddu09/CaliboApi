package pro.building_blocks;

import common.RequestSpecProvider;
import endpoints.ApiEndpoints;
import io.restassured.response.Response;
import org.json.simple.JSONObject;
import org.testng.Assert;

import static io.restassured.RestAssured.given;

public class PortfolioApiBuildingBlock {

    public Response addNewProductPortfolio(JSONObject request) {
        return given()
                .spec(RequestSpecProvider.get())
                .body(request.toJSONString())
                .post(ApiEndpoints.PORTFOLIOS);
    }

    public void verifyProductPortfolio(Response response) {
        Assert.assertEquals(response.statusCode(), 201, response.asString());
        Assert.assertNotNull(response.jsonPath().getString("id"), "Portfolio ID is missing");
    }
}
