package validators;

import io.restassured.response.Response;
import org.testng.Assert;

import java.util.List;
import java.util.Map;

/** Assertions for deployment-stage creation and retrieval. */
public final class StageValidator {

    public void validateStagesRetrieved(Response response) {
        Assert.assertEquals(response.statusCode(), 200,
                "Unable to retrieve the existing stage template. Check projectId, "
                        + "workstreamId and releaseId. Response: " + response.asString());

        List<Map<String, Object>> stages = response.jsonPath().getList("stages");
        Assert.assertNotNull(stages, "Response does not contain a stages array");
        Assert.assertFalse(stages.isEmpty(),
                "No stage template exists for the supplied project/workstream/release");
    }

    public void validateStagesApi(Response response) {
        Assert.assertEquals(response.statusCode(), 200,
                "Unable to retrieve stages: " + response.asString());
        Assert.assertNotNull(response.jsonPath().getList("stages"),
                "Response does not contain a stages array");
    }

    public void validateStageCreated(Response response) {
        Assert.assertTrue(
                response.statusCode() == 200 || response.statusCode() == 201,
                "Expected stage creation status 200 or 201, but received "
                        + response.statusCode() + ": " + response.asString());
    }

    public void validateStageExists(
            Response response, String expectedStageName, String expectedDeploymentMode) {
        validateStagesRetrieved(response);

        Map<String, Object> matchingStage = response.jsonPath().getMap(
                "stages.find { it.stageName == '" + expectedStageName + "' }");

        Assert.assertNotNull(matchingStage,
                "Stage was submitted but could not be found: " + expectedStageName);
        Assert.assertNotNull(matchingStage.get("stageDetailsId"),
                "Created stage does not contain stageDetailsId");

        List<String> deploymentModes = response.jsonPath().getList(
                "stages.find { it.stageName == '" + expectedStageName + "' }.deploymentModes");
        Assert.assertTrue(deploymentModes != null && deploymentModes.contains(expectedDeploymentMode),
                "Stage does not contain deployment mode " + expectedDeploymentMode);
    }
}
