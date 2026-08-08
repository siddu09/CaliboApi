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

        JSONObject stage = new JSONObject();
        stage.put("loadBalancerType", "PUBLIC");
        stage.put("stageType", "Dev");
        stage.put("stageName", "Dev");
        stage.put("stageOrder", 0);
        stage.put("releaseName", "Default Release");
        stage.put("releaseId", context.releaseId());
        stage.put("projectId", context.projectId());
        stage.put("projectName", context.projectName());
        stage.put("workstreamId", context.workstreamId());
        stage.put("workstreamName", context.workstreamName());
        stage.put("portfolioId", product.get("portfolioId"));
        stage.put("portfolioName", product.get("portfolioTitle"));
        stage.put("portfolioTitle", product.get("portfolioTitle"));
        stage.put("enableCi", false);
        stage.put("loadBalancerCreationMode", "AUTO");
        stage.put("cloudConfigId", "");
        stage.put("deploymentModes", java.util.List.of("EC2", "KUBERNETES", "TERRAFORM", "OPENSHIFT"));
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
        JSONObject publish = new JSONObject();
        publish.put("draftId", context.draftId());
        publish.put("pipelineDetailsId", context.pipelineDetailsId());
        publish.put("stageOrder", 0);
        publish.put("projectId", context.projectId());
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
            try { Thread.sleep(seconds * 1000L); }
            catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Workflow polling interrupted", exception);
            }
        }
        throw new IllegalStateException("DPS workflow did not complete within " + attempts * seconds + " seconds");
    }

    @SuppressWarnings("unchecked") // json-simple exposes raw Map/List APIs.
    private JSONObject ids() {
        JSONObject body = new JSONObject();
        body.put("projectId", context.projectId());
        body.put("workstreamId", context.workstreamId());
        body.put("releaseId", context.releaseId());
        body.put("stageDetailsId", context.stageDetailsId());
        if (context.pipelineDetailsId() != null) body.put("pipelineDetailsId", context.pipelineDetailsId());
        return body;
    }

    private String query() {
        return "?projectId=" + context.projectId() + "&releaseId=" + context.releaseId()
                + "&stageDetailsId=" + context.stageDetailsId() + "&workstreamId=" + context.workstreamId()
                + "&pipelineDetailsId=" + context.pipelineDetailsId();
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
