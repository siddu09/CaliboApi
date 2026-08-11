package pro.building_blocks;

import common.RequestSpecProvider;
import endpoints.ApiEndpoints;
import io.restassured.response.Response;
import org.json.simple.JSONObject;
import org.testng.Assert;

import static io.restassured.RestAssured.given;

public class FeatureApiBuildingBlock {
    private String featureId;
    private String releaseId;

    public Response addNewFeature(JSONObject request) {
        return given().spec(RequestSpecProvider.get()).body(request.toJSONString())
                .post(ApiEndpoints.WORKSTREAMS_V2);
    }

    public void verifyFeature(Response response, String productId) {
        Assert.assertEquals(response.statusCode(), 201, response.asString());
        featureId = response.jsonPath().getString("id");
        releaseId = response.jsonPath().getString("releaseId");
        Assert.assertNotNull(featureId, "Feature ID is missing");
        Assert.assertNotNull(releaseId, "Release ID is missing");
        Assert.assertEquals(response.jsonPath().getString("projectId"), productId);
    }

    public String getFeatureId() {
        return featureId;
    }

    public String getReleaseId() {
        return releaseId;
    }
}
