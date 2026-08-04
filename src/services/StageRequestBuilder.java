package services;

import config.Config;
import config.Constants;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utils.JsonUtils;

import java.util.List;
import java.util.Map;

/** Builds the stage request from test data and the server's existing stage template. */
public final class StageRequestBuilder {

    private static final List<String> REQUIRED_FIELDS = List.of(
            "projectId", "projectName", "portfolioId", "portfolioName",
            "releaseId", "releaseName", "workstreamId", "workstreamName",
            "stageName", "stageType", "stageOrder"
    );

    private static final List<String> RESPONSE_ONLY_STAGE_FIELDS = List.of(
            "artifactoryEnv", "containerScanProvider", "libraryArtifactConfigId",
            "qualysConfigId", "workflowTemplate", "loadBalancerCreationMode",
            "loadBalancerType", "terraformConfiguration"
    );

    private static final List<String> RESPONSE_ONLY_CLUSTER_FIELDS = List.of(
            "ingressClass", "ingressType", "ingressAddress", "hostname",
            "albGroupName", "ingressOverridden"
    );

    private StageRequestBuilder() {
    }

    public static JSONObject loadDevStageTestData() {
        return JsonUtils.readJson(Config.testDataPath + Constants.DEV_STAGE_SETUP_JSON);
    }

    @SuppressWarnings("unchecked")
    public static JSONObject createRuntimeStageData(JSONObject setup, String projectId,
                                                    String projectName, String workstreamId,
                                                    String workstreamName, String releaseId,
                                                    String portfolioId) {
        JSONObject testData = new JSONObject();
        testData.put("projectId", projectId);
        testData.put("projectName", projectName);
        testData.put("portfolioId", portfolioId);
        testData.put("portfolioName", setup.get("portfolioName"));
        testData.put("releaseId", releaseId);
        testData.put("releaseName", setup.get("releaseName"));
        testData.put("workstreamId", workstreamId);
        testData.put("workstreamName", workstreamName);
        testData.put("stageName", setup.get("stageName"));
        testData.put("stageType", setup.get("stageName"));
        testData.put("stageOrder", 0L);
        testData.put("enableCi", true);

        for (String field : REQUIRED_FIELDS) {
            Object value = testData.get(field);
            if (value == null || (value instanceof String text && text.isBlank())) {
                throw new IllegalArgumentException(
                        "DevStageSetup.json is missing required field: " + field);
            }
        }

        return testData;
    }

    /** Mirrors DeploySetupParent.addStageToProjectWorkStreamWithKubernates. */
    @SuppressWarnings("unchecked")
    public static JSONObject createKubernetesStageRequest(
            JSONObject existingStage, JSONObject testData) {

        if (existingStage == null) {
            throw new IllegalArgumentException(
                    "No existing stage template was returned for this project/workstream/release");
        }

        RESPONSE_ONLY_STAGE_FIELDS.forEach(existingStage::remove);

        Object clustersValue = existingStage.get("kubernetesClusters");
        if (!(clustersValue instanceof JSONArray clusters) || clusters.isEmpty()) {
            throw new IllegalArgumentException(
                    "The existing stage has no kubernetesClusters. Configure an AWS Kubernetes "
                            + "cluster for this project before running DevStageTests");
        }

        JSONObject cluster = (JSONObject) clusters.get(0);
        RESPONSE_ONLY_CLUSTER_FIELDS.forEach(cluster::remove);
        clusters.set(0, cluster);

        JSONArray deploymentModes = new JSONArray();
        deploymentModes.add("KUBERNETES");
        existingStage.put("deploymentModes", deploymentModes);

        copyTestData(existingStage, testData);
        requireRuntimeConfiguration(existingStage, "cloudConfigId", "AWS cloud configuration");
        requireRuntimeConfiguration(existingStage, "devopsConfigId", "Jenkins configuration");

        return existingStage;
    }

    /** Creates the first stage when no repository-generated stage template exists yet. */
    @SuppressWarnings("unchecked")
    public static JSONObject createInitialKubernetesStageRequest(
            JSONObject testData, Map<String, String> configurationIds,
            String kubernetesClusterName) {
        JSONObject stage = new JSONObject();
        copyTestData(stage, testData);
        stage.put("cloudConfigId", configurationIds.get("CLOUD"));
        stage.put("devopsConfigId", configurationIds.get("DEV_OPS"));
        stage.put("sonarConfigId", configurationIds.get("SECURITY_ASSESSMENT"));
        stage.put("artifactoryConfigId", configurationIds.get("ARTIFACTORY_MANAGEMENT"));
        stage.put("pipelineData", new JSONArray());
        stage.put("cloudTags", new JSONObject());

        JSONArray deploymentModes = new JSONArray();
        deploymentModes.add("KUBERNETES");
        stage.put("deploymentModes", deploymentModes);

        JSONObject cluster = new JSONObject();
        cluster.put("settingId", configurationIds.get("KUBERNETES"));
        cluster.put("clusterName", kubernetesClusterName);
        cluster.put("clusterProvider", "KUBERNETES");
        JSONArray clusters = new JSONArray();
        clusters.add(cluster);
        stage.put("kubernetesClusters", clusters);
        return stage;
    }

    @SuppressWarnings("unchecked")
    private static void copyTestData(JSONObject stage, JSONObject testData) {
        for (String field : REQUIRED_FIELDS) {
            stage.put(field, testData.get(field));
        }
        stage.put("enableCi", testData.getOrDefault("enableCi", Boolean.FALSE));
    }

    private static void requireRuntimeConfiguration(
            JSONObject stage, String field, String description) {
        Object value = stage.get(field);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException(
                    "Existing stage template is missing " + field + " (" + description + ")");
        }
    }

    @SuppressWarnings("unchecked")
    public static JSONObject technologyRequest(JSONObject pipeline, JSONObject techInput,
                                               JSONObject setup, String stageDetailsId,
                                               String portfolioId, String portfolioName,
                                               String kubernetesSettingId, String clusterName,
                                               String suffix) {
        JSONObject request = new JSONObject();
        request.putAll(pipeline);
        RESPONSE_ONLY_PIPELINE_FIELDS.forEach(request::remove);

        String context = techInput.get("contextPrefix") + suffix;
        request.put("contextPath", "/" + context);
        request.put("kubernetesSettingId", kubernetesSettingId);
        request.put("clusterName", clusterName);
        request.put("deploymentType", "KUBERNETES");
        request.put("ciTriggerType", arrayOf("POLL_SCM"));
        request.put("cdTriggerType", arrayOf("AFTER_CI"));
        request.put("useOwnHelm", false);
        request.put("helmReleaseName", context.toLowerCase());
        request.put("newBranch", false);
        request.put("portfolioName", portfolioName);
        request.put("portfolioId", portfolioId);
        request.put("stageDetailsId", stageDetailsId);
        request.put("repoType", "NPM");
        request.put("deploymentConfig", defaultDeploymentConfig());
        request.put("subSection", techInput.get("subSection"));
        request.put("namespace", setup.get("namespace"));
        request.put("branch", setup.get("branch"));
        return request;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject deployRequest(JSONObject testData, String stageDetailsId,
                                           List<String> pipelineIds, String imageTag) {
        JSONObject request = new JSONObject();
        for (String field : List.of("portfolioId", "releaseId", "workstreamId", "projectName",
                "projectId", "workstreamName", "portfolioName")) {
            request.put(field, testData.get(field));
        }
        JSONArray pipelines = new JSONArray();
        for (String id : pipelineIds) {
            JSONObject pipeline = new JSONObject();
            pipeline.put("id", id);
            pipeline.put("imageTag", imageTag);
            pipelines.add(pipeline);
        }
        request.put("pipelineDetailsId", pipelines);
        request.put("stageId", stageDetailsId);
        return request;
    }

    @SuppressWarnings("unchecked")
    private static JSONObject defaultDeploymentConfig() {
        JSONObject config = new JSONObject();
        config.put("requestMemory", 256L);
        config.put("replicas", 1L);
        config.put("limitCpu", 500L);
        config.put("requestCpu", 200L);
        config.put("limitMemory", 1500L);
        return config;
    }

    @SuppressWarnings("unchecked")
    private static JSONArray arrayOf(Object value) {
        JSONArray array = new JSONArray();
        array.add(value);
        return array;
    }

    private static final List<String> RESPONSE_ONLY_PIPELINE_FIELDS = List.of(
            "appServerIp", "appServerPort", "appServerName", "appServerUrl",
            "appServerPublicIp", "appServerPrivateIp", "appServerInstanceId", "instanceName",
            "applicationUrl", "applicationLogUrl", "codeNowUrl", "ciCdUrl", "ciUrl",
            "ciBuildNumber", "buildNumber", "instanceOrder", "instanceType", "instanceState",
            "ram", "cpu", "cloudConfigId", "stageInstanceNetworkId", "allowContainer",
            "clusterUrl", "clusterName", "kubernetesCustomClusterId", "configured", "proxyType",
            "loadBalancerId", "loadBalancerArn", "loadBalancerDns", "deploymentConfig",
            "helmConfig", "testcaseData", "workflowId", "workflowStatus", "artifactoryRepoUri",
            "roleName", "imageTag", "customHelmChartDetails", "ciSchedules", "cdSchedules",
            "clusterIp", "k8sServicePort", "k8sClusterDns", "latestPromotionWorkflowId",
            "latestPromotionWorkflowStatus", "libraryTechstack", "serverlessTechstack",
            "artifactRepository", "customDns", "runnerName", "snykProjectName", "snykSeverity",
            "samExecutionDetails", "ciRunNo", "volumeId", "jenkinsAgent", "promotable",
            "hostname", "serviceAccountName"
    );
}
