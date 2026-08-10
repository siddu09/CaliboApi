package DPS.Helpers.WorkflowHelper;

import DPS.Helpers.DpsContext;
import common.RequestSpecProvider;
import config.Config;
import config.Constants;
import io.restassured.response.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utils.JsonUtils;

import static endpoints.ApiEndpoints.*;
import static io.restassured.RestAssured.given;

public final class WorkflowHelper {
    private final DpsContext context;

    public WorkflowHelper(DpsContext context) { this.context = context; }

    @SuppressWarnings("unchecked") // json-simple exposes raw Map/List APIs.
    public void setup() {
        JSONObject product = JsonUtils.readJson(Config.testDataPath + "SimpleProduct.json");
        product.put("title", context.projectName());
        Response response = post(PROJECTS_V2, product, 201);
        context.projectId(required(response, "id"));

        JSONObject feature = JsonUtils.readJson(Config.testDataPath + "SimpleFeature.json");
        JSONObject item = (JSONObject) ((JSONArray) feature.get("workstreamsRequest")).get(0);
        item.put("title", context.workstreamName());
        item.put("projectId", context.projectId());
        response = post(WORKSTREAMS_V2, feature, 201);
        context.workstreamId(required(response, "id"));
        context.releaseId(required(response, "releaseId"));

        JSONObject stage = json(
                "loadBalancerType", "PUBLIC", "stageType", "Dev", "stageName", "Dev", "stageOrder", 0,
                "releaseName", "Default Release", "releaseId", context.releaseId(),
                "projectId", context.projectId(), "projectName", context.projectName(),
                "workstreamId", context.workstreamId(), "workstreamName", context.workstreamName(),
                "portfolioId", product.get("portfolioId"), "portfolioName", product.get("portfolioTitle"),
                "portfolioTitle", product.get("portfolioTitle"), "enableCi", false,
                "loadBalancerCreationMode", "AUTO", "cloudConfigId", "",
                "deploymentModes", java.util.List.of("EC2", "KUBERNETES", "TERRAFORM", "OPENSHIFT"));
        response = post(DPS_STAGE.replace("{stageName}", "Dev"), stage, 200);
        context.stageDetailsId(required(response, "stageDetailsId"));

        JSONObject pipeline = ids();
        pipeline.put("pipelineIndex", 0);
        pipeline.put("isDefault", true);
        pipeline.put("name", context.workstreamName() + "One");
        response = post(DPS_PIPELINE_DETAILS, pipeline, 200);
        context.pipelineDetailsId(response.asString().replace("\"", "").trim());

        JSONObject draft = context.draft();
        draft.putAll(ids());
        draft.put("nodes", new JSONArray());
        draft.put("stages", new JSONArray());
        draft.put("edges", new JSONArray());
        response = post(DPS_DRAFT, draft, 201);
        context.draftId(required(response, "id"));
        context.draft().putAll(draft);
        context.draft().put("draftId", context.draftId());

        JSONObject status = ids();
        status.put("status", "EDITING");
        expect(given().spec(RequestSpecProvider.get()).body(status.toJSONString()).put(DPS_DRAFT_STATUS), 204);
    }

    @SuppressWarnings("unchecked") // json-simple exposes raw Map/List APIs.
    public void runAndVerify() {
        JSONObject publish = json("draftId", context.draftId(), "pipelineDetailsId", context.pipelineDetailsId(),
                "stageOrder", 0, "projectId", context.projectId());
        post(DPS_DRAFT_PUBLISH_V3 + "?projectId=" + context.projectId(), publish, 201);

        String query = query();
        expect(given().spec(RequestSpecProvider.get()).put(DPS_WORKFLOW_INITIATE + query), 200);
        JSONObject setup = JsonUtils.readJson(Config.testDataPath + Constants.DATA_INGESTION_SETUP_JSON);
        int attempts = ((Number) setup.get("workflowMaxAttempts")).intValue();
        int seconds = ((Number) setup.get("workflowPollSeconds")).intValue();
        for (int attempt = 1; attempt <= attempts; attempt++) {
            Response response = given().spec(RequestSpecProvider.get()).get(DPS_WORKFLOW_STATUS + query);
            expect(response, 200);
            String status = response.jsonPath().getString("workflowStatus");
            System.out.println("[DPS] Workflow status attempt " + attempt + ": " + status);
            if ("COMPLETED".equalsIgnoreCase(status)) return;
            if ("FAILED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status))
                throw new IllegalStateException("DPS workflow ended with " + status + ": " + response.asString());
            sleep(seconds);
        }
        throw new IllegalStateException("DPS workflow did not complete within " + attempts * seconds + " seconds");
    }

    @SuppressWarnings("unchecked") // json-simple exposes raw Map/List APIs.
    private JSONObject ids() {
        JSONObject body = json("projectId", context.projectId(), "workstreamId", context.workstreamId(),
                "releaseId", context.releaseId(), "stageDetailsId", context.stageDetailsId());
        if (context.pipelineDetailsId() != null) body.put("pipelineDetailsId", context.pipelineDetailsId());
        return body;
    }

    private String query() {
        return "?projectId=%s&releaseId=%s&stageDetailsId=%s&workstreamId=%s&pipelineDetailsId=%s".formatted(
                context.projectId(), context.releaseId(), context.stageDetailsId(),
                context.workstreamId(), context.pipelineDetailsId());
    }

    @SuppressWarnings("unchecked")
    private JSONObject json(Object... values) {
        JSONObject body = new JSONObject();
        for (int index = 0; index < values.length; index += 2) body.put(values[index], values[index + 1]);
        return body;
    }

    private void sleep(int seconds) {
        try { Thread.sleep(seconds * 1000L); }
        catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Workflow polling interrupted", exception);
        }
    }

    private Response post(String uri, JSONObject body, int status) {
        return expect(given().spec(RequestSpecProvider.get()).body(body.toJSONString()).post(uri), status);
    }

    private Response expect(Response response, int status) {
        if (response.statusCode() != status)
            throw new IllegalStateException("Expected HTTP " + status + " but received "
                    + response.statusCode() + ": " + response.asString());
        return response;
    }

    private String required(Response response, String path) {
        String value = response.jsonPath().getString(path);
        if (value == null || value.isBlank()) throw new IllegalStateException("Missing response field: " + path);
        return value;
    }
}
