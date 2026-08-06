package pro.building_blocks;

import common.RequestSpecProvider;
import endpoints.ApiEndpoints;
import io.restassured.response.Response;
import org.json.simple.JSONObject;
import org.testng.Assert;

import static io.restassured.RestAssured.given;

public class FeatureApiBuildingBlock {
    private String featureId;

    public Response addNewFeature(JSONObject request) {
        return given().spec(RequestSpecProvider.get()).body(request.toJSONString())
                .post(ApiEndpoints.WORKSTREAMS_V2);
    }

    public void verifyFeature(Response response, String productId) {
        Assert.assertEquals(response.statusCode(), 201, response.asString());
        featureId = response.jsonPath().getString("id");
        Assert.assertNotNull(featureId, "Feature ID is missing");
        Assert.assertEquals(response.jsonPath().getString("projectId"), productId);
    }

    public String getFeatureId() {
        return featureId;
    }
}
