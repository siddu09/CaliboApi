package DevSecOps.Helpers.DeploymentStage;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

public final class DeployStageRequestHelper {
    private static final List<String> REQUIRED = List.of("projectId", "projectName", "portfolioId",
            "portfolioName", "releaseId", "releaseName", "workstreamId", "workstreamName",
            "stageName", "stageType", "stageOrder");
    private static final List<String> STAGE_RESPONSE_FIELDS = List.of("artifactoryEnv",
            "containerScanProvider", "libraryArtifactConfigId", "qualysConfigId", "workflowTemplate",
            "loadBalancerCreationMode", "loadBalancerType", "terraformConfiguration");
    private static final List<String> CLUSTER_RESPONSE_FIELDS = List.of("ingressClass", "ingressType",
            "ingressAddress", "hostname", "albGroupName", "ingressOverridden");
    private static final List<String> PIPELINE_RESPONSE_FIELDS = List.of("appServerIp", "appServerPort",
            "appServerName", "appServerUrl", "appServerPublicIp", "appServerPrivateIp",
            "appServerInstanceId", "instanceName", "applicationUrl", "applicationLogUrl", "codeNowUrl",
            "ciCdUrl", "ciUrl", "ciBuildNumber", "buildNumber", "instanceOrder", "instanceType",
            "instanceState", "ram", "cpu", "cloudConfigId", "stageInstanceNetworkId", "allowContainer",
            "clusterUrl", "clusterName", "kubernetesCustomClusterId", "configured", "proxyType",
            "loadBalancerId", "loadBalancerArn", "loadBalancerDns", "deploymentConfig", "helmConfig",
            "testcaseData", "workflowId", "workflowStatus", "artifactoryRepoUri", "roleName", "imageTag",
            "customHelmChartDetails", "ciSchedules", "cdSchedules", "clusterIp", "k8sServicePort",
            "k8sClusterDns", "latestPromotionWorkflowId", "latestPromotionWorkflowStatus",
            "libraryTechstack", "serverlessTechstack", "artifactRepository", "customDns", "runnerName",
            "snykProjectName", "snykSeverity", "samExecutionDetails", "ciRunNo", "volumeId",
            "jenkinsAgent", "promotable", "hostname", "serviceAccountName");

    public DeployStageRequestHelper() { }

    public JSONObject setup(Map<String, Object> runtime) {
        Object value = runtime.get("setup");
        if (!(value instanceof JSONObject setup))
            throw new IllegalStateException("Deployment test data has not been loaded");
        return setup;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject runtimeStageData(JSONObject setup, String projectId, String projectName,
                                              String workstreamId, String workstreamName,
                                              String releaseId, String portfolioId) {
        JSONObject data = new JSONObject();
        data.put("projectId", projectId); data.put("projectName", projectName);
        data.put("portfolioId", portfolioId); data.put("portfolioName", setup.get("portfolioName"));
        data.put("releaseId", releaseId); data.put("releaseName", setup.get("releaseName"));
        data.put("workstreamId", workstreamId); data.put("workstreamName", workstreamName);
        data.put("stageName", setup.get("stageName")); data.put("stageType", setup.get("stageName"));
        data.put("stageOrder", 0L); data.put("enableCi", true);
        REQUIRED.forEach(field -> require(data, field));
        return data;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject kubernetesStage(JSONObject stage, JSONObject data) {
        if (stage == null) throw new IllegalArgumentException("Existing stage template is missing");
        STAGE_RESPONSE_FIELDS.forEach(stage::remove);
        Object value = stage.get("kubernetesClusters");
        if (!(value instanceof JSONArray clusters) || clusters.isEmpty())
            throw new IllegalArgumentException("Existing stage has no Kubernetes cluster");
        JSONObject cluster = (JSONObject) clusters.get(0);
        CLUSTER_RESPONSE_FIELDS.forEach(cluster::remove);
        stage.put("deploymentModes", array("KUBERNETES"));
        copy(stage, data);
        require(stage, "cloudConfigId"); require(stage, "devopsConfigId");
        return stage;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject initialKubernetesStage(JSONObject data, Map<String, String> ids,
                                                    String clusterName) {
        JSONObject stage = new JSONObject(); copy(stage, data);
        stage.put("cloudConfigId", ids.get("CLOUD")); stage.put("devopsConfigId", ids.get("DEV_OPS"));
        stage.put("sonarConfigId", ids.get("SECURITY_ASSESSMENT"));
        stage.put("artifactoryConfigId", ids.get("ARTIFACTORY_MANAGEMENT"));
        stage.put("pipelineData", new JSONArray()); stage.put("cloudTags", new JSONObject());
        stage.put("deploymentModes", array("KUBERNETES"));
        JSONObject cluster = new JSONObject(); cluster.put("settingId", ids.get("KUBERNETES"));
        cluster.put("clusterName", clusterName); cluster.put("clusterProvider", "KUBERNETES");
        stage.put("kubernetesClusters", array(cluster)); return stage;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject technology(JSONObject pipeline, JSONObject input, JSONObject setup,
                                        String stageId, String portfolioId, String portfolioName,
                                        String kubernetesId, String clusterName, String suffix) {
        JSONObject request = new JSONObject(); request.putAll(pipeline);
        PIPELINE_RESPONSE_FIELDS.forEach(request::remove);
        String context = input.get("contextPrefix") + suffix;
        request.put("contextPath", "/" + context); request.put("kubernetesSettingId", kubernetesId);
        request.put("clusterName", clusterName); request.put("deploymentType", "KUBERNETES");
        request.put("ciTriggerType", array("POLL_SCM")); request.put("cdTriggerType", array("AFTER_CI"));
        request.put("useOwnHelm", false); request.put("helmReleaseName", context.toLowerCase());
        request.put("newBranch", false); request.put("portfolioName", portfolioName);
        request.put("portfolioId", portfolioId); request.put("stageDetailsId", stageId);
        request.put("repoType", "NPM"); request.put("deploymentConfig", deploymentConfig());
        request.put("subSection", input.get("subSection")); request.put("namespace", setup.get("namespace"));
        request.put("branch", setup.get("branch")); return request;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject deploy(JSONObject data, String stageId, List<String> ids, String imageTag) {
        JSONObject request = new JSONObject();
        for (String field : List.of("portfolioId", "releaseId", "workstreamId", "projectName",
                "projectId", "workstreamName", "portfolioName")) request.put(field, data.get(field));
        JSONArray pipelines = new JSONArray();
        for (String id : ids) { JSONObject pipeline = new JSONObject(); pipeline.put("id", id);
            pipeline.put("imageTag", imageTag); pipelines.add(pipeline); }
        request.put("pipelineDetailsId", pipelines); request.put("stageId", stageId); return request;
    }

    @SuppressWarnings("unchecked") private static void copy(JSONObject target, JSONObject data) {
        REQUIRED.forEach(field -> target.put(field, data.get(field)));
        target.put("enableCi", data.getOrDefault("enableCi", false));
    }
    private static void require(JSONObject value, String field) {
        Object item = value.get(field);
        if (item == null || item.toString().isBlank()) throw new IllegalArgumentException("Missing " + field);
    }
    @SuppressWarnings("unchecked") private static JSONObject deploymentConfig() {
        JSONObject value = new JSONObject(); value.put("requestMemory", 256L); value.put("replicas", 1L);
        value.put("limitCpu", 500L); value.put("requestCpu", 200L); value.put("limitMemory", 1500L); return value;
    }
    @SuppressWarnings("unchecked") private static JSONArray array(Object... values) {
        JSONArray array = new JSONArray(); for (Object value : values) array.add(value); return array;
    }
}




