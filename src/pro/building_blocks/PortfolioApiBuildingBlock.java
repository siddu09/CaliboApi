package pro.building_blocks;

import common.RequestSpecProvider;
import endpoints.ApiEndpoints;
import io.restassured.response.Response;
import org.json.simple.JSONObject;
import org.testng.Assert;

import static io.restassured.RestAssured.*;

public class PortfolioApiBuildingBlock {
    private String portfolioId;
    private String portfolioTitle;

    public Response addNewProductPortfolio(JSONObject request) {
        return given()
                .spec(RequestSpecProvider.get())
                .body(request.toJSONString())
                .post(ApiEndpoints.PORTFOLIOS);
    }

    public void verifyProductPortfolio(Response response) {
        Assert.assertEquals(response.statusCode(), 201, response.asString());
        portfolioId = response.jsonPath().getString("id");
        portfolioTitle = response.jsonPath().getString("title");
        Assert.assertNotNull(portfolioId, "Portfolio ID is missing");
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public String getPortfolioTitle() {
        return portfolioTitle;
    }
}
