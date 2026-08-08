package DPS.Helpers.DataIntegrationHelper;

import DPS.BuildingBlocks.Crawler.Crawler;
import DPS.Helpers.DpsContext;
import common.RequestSpecProvider;
import io.restassured.response.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.Assert;

import java.util.UUID;
import java.util.Map;

import static endpoints.ApiEndpoints.*;
import static io.restassured.RestAssured.given;

public final class DataIntegrationHelper {
    private final DpsContext context;
    public DataIntegrationHelper(Crawler crawler) { context = crawler.context(); }

    @SuppressWarnings("unchecked") // json-simple exposes raw Map/List APIs.
    public void addDataIntegrationStageWithDatabricksNode() {
        JSONObject stage = stage("DATA_INTEGRATION", 1);
        JSONObject node = new JSONObject();
        context.integrationNodeId(UUID.randomUUID().toString());
        node.put("id", context.integrationNodeId()); node.put("displayLabel", "Databricks 1");
        node.put("label", "Databricks"); node.put("providerCode", "DATABRICKS");
        node.put("techStackId", "84"); node.put("laneIndex", "1");
        node.put("type", "DATA_INTEGRATION"); node.put("icon", "/databrick.png");
        node.put("repoId", ""); node.put("isTechStackConfigurable", true);
        ((JSONArray) context.draft().get("stages")).add(stage);
        ((JSONArray) context.draft().get("nodes")).add(node);
        edge(context.dataSourceNodeId(), context.integrationNodeId());
        save();
    }

    @SuppressWarnings("unchecked") // json-simple exposes raw Map/List APIs.
    public void configureDatabricksJob() {
        Response settings = given().spec(RequestSpecProvider.get())
                .queryParam("configCode", "DATA_INTEGRATION").get(DPS_CLIENT_SETTINGS);
        Assert.assertEquals(settings.statusCode(), 200, settings.asString());
        Map<String, Object> databricks = settings.jsonPath().getList("$", Map.class).stream()
                .filter(item -> "Databricks_Qa_Cluster".equals(item.get("name"))).findFirst()
                .orElseThrow(() -> new IllegalStateException("Databricks_Qa_Cluster setting not found"));
        String instanceId = String.valueOf(databricks.get("id"));

        JSONObject repo = repository(instanceId);
        Response response = given().spec(RequestSpecProvider.get()).body(repo.toJSONString()).post(DPS_PROJECT_REPOSITORIES);
        Assert.assertEquals(response.statusCode(), 201, response.asString());
        context.repositoryId(response.jsonPath().getString("[0].id"));

        Response templates = given().spec(RequestSpecProvider.get())
                .queryParam("workstreamId", context.workstreamId()).queryParam("releaseId", context.releaseId())
                .get(DPS_DATABRICKS_TEMPLATES);
        Assert.assertEquals(templates.statusCode(), 200, templates.asString());
        String templateId = templates.jsonPath().getList("$", Map.class).stream()
                .filter(item -> "SNOWFLAKE".equalsIgnoreCase(String.valueOf(item.get("targetType")))
                        && ("RDBMS".equalsIgnoreCase(String.valueOf(item.get("sourceType")))
                        || String.valueOf(item.get("name")).toLowerCase().contains("sql server")))
                .map(item -> String.valueOf(item.get("id"))).findFirst()
                .orElseThrow(() -> new IllegalStateException("MSSQL to Snowflake template not found: " + templates.asString()));

        JSONArray instances = new JSONArray(); instances.add(instanceId);
        Response clusters = given().spec(RequestSpecProvider.get()).body(instances.toJSONString())
                .post(DPS_DATABRICKS_CLUSTER_WHL_MAPPING);
        Assert.assertEquals(clusters.statusCode(), 200, clusters.asString());
        String clusterId = clusters.jsonPath().getString("'" + instanceId + "'.detail.clusterId");

        JSONObject job = (JSONObject) context.payload().get("databricksTemplateJobInput");
        job.put("projectId", context.projectId()); job.put("projectName", context.projectName());
        job.put("featureName", context.workstreamName()); job.put("repositoryId", context.repositoryId());
        job.put("pipelineDetailsId", context.pipelineDetailsId());
        job.put("templateId", templateId); job.put("clusterId", clusterId);
        job.put("name", "Job_DataIngestion_" + context.suffix());
        JSONObject source = (JSONObject) job.get("source");
        source.put("nodeId", context.dataSourceNodeId());
        source.put("configType", "DATA_CATALOG");
        source.put("attributes", node(context.dataSourceNodeId()).get("configuration"));
        JSONObject target = (JSONObject) job.get("target");
        target.put("nodeId", context.dataLakeNodeId());
        JSONObject selected = (JSONObject) ((JSONArray) node(context.dataLakeNodeId()).get("configuration")).get(0);
        target.put("attributes", selected.get("attributes"));
        JSONObject firstTargetAttribute = (JSONObject) ((JSONArray) selected.get("attributes")).get(0);
        target.put("id", firstTargetAttribute.get("dataStoreId"));
        target.put("name", "Snowflake_Automation");
        JSONObject mapping = (JSONObject) job.get("sourceToTargetMapping");
        JSONObject map = (JSONObject) ((JSONArray) mapping.get("mapDetails")).get(0);
        map.put("sourceName", context.catalogTable().get("tableName"));
        map.put("tableName", context.projectName().toUpperCase());

        String uri = DPS_DATABRICKS_TEMPLATE.replace("{templateId}", templateId)
                + "?workstreamId=" + context.workstreamId() + "&pipelineDetailsId=" + context.pipelineDetailsId()
                + "&stageDetailsId=" + context.stageDetailsId() + "&releaseId=" + context.releaseId();
        response = given().spec(RequestSpecProvider.get()).body(job.toJSONString()).post(uri);
        Assert.assertEquals(response.statusCode(), 200, response.asString());
        context.jobId(response.asString().replace("\"", "").trim());

        JSONObject integration = node(context.integrationNodeId());
        integration.put("repoId", context.repositoryId()); integration.put("multiInstanceId", instanceId);
        integration.put("isTechStackSchedulable", "true");
        JSONObject configuration = new JSONObject();
        configuration.put("jobCreationType", "DATA_INTEGRATION"); configuration.put("jobSubType", "TEMPLATE");
        configuration.put("id", context.jobId()); configuration.put("sourceNodeId", context.dataSourceNodeId());
        configuration.put("targetNodeId", context.dataLakeNodeId()); integration.put("configuration", configuration);
        save();
    }

    @SuppressWarnings("unchecked") // json-simple exposes raw Map/List APIs.
    private JSONObject repository(String instanceId) {
        JSONObject body = new JSONObject();
        body.put("releaseName", "Default Release"); body.put("workstreamId", context.workstreamId());
        JSONArray ids = new JSONArray(); ids.add("84"); body.put("techStackIds", ids);
        body.put("title", context.projectName()); body.put("projectName", context.projectName());
        body.put("projectId", context.projectId()); body.put("workstreamName", context.workstreamName());
        body.put("releaseId", context.releaseId()); body.put("portfolioId", "6b6c1fe7-e0e7-47b8-bf75-14f8fb7eb3da");
        body.put("portfolioName", "DefaultAutomationPL"); body.put("isCalledFromDis", true);
        body.put("createRepositories", "pending");
        JSONObject item = new JSONObject(); item.put("repoUrl", ""); item.put("uid", context.suffix());
        item.put("skipRepoCreation", true); item.put("projectKey", ""); item.put("repoName", "DATABRICKS");
        item.put("selectedRepo", new JSONObject()); item.put("isMultiRepoSupported", true); item.put("repoCode", "");
        item.put("sourceCodeRepoTitle", "DATABRICKS"); item.put("multiInstanceId", instanceId); item.put("techstackId", "84");
        JSONArray repos = new JSONArray(); repos.add(item); body.put("techstackRepos", repos); return body;
    }

    private JSONObject node(String id) {
        for (Object value : (JSONArray) context.draft().get("nodes")) {
            JSONObject node = (JSONObject) value;
            if (id.equals(node.get("id"))) return node;
        }
        throw new IllegalStateException("Pipeline node not found: " + id);
    }

    @SuppressWarnings("unchecked") // json-simple exposes raw Map/List APIs.
    private JSONObject stage(String type, int lane) {
        JSONObject stage = new JSONObject(); stage.put("name", type); stage.put("type", type);
        stage.put("icon", "/data_source.png"); stage.put("laneIndex", lane); return stage;
    }
    @SuppressWarnings("unchecked") // json-simple exposes raw Map/List APIs.
    private void edge(String from, String to) {
        JSONObject edge = new JSONObject(); edge.put("from", from); edge.put("to", to);
        ((JSONArray) context.draft().get("edges")).add(edge);
    }
    private void save() {
        Response response = given().spec(RequestSpecProvider.get()).body(context.draft().toJSONString()).post(DPS_DRAFT);
        Assert.assertEquals(response.statusCode(), 201, response.asString());
    }
}
