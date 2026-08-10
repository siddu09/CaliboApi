package DevSecOps.Helpers.Kubernetes;

import DevSecOps.Helpers.DeploymentStage.DeployStageRequestHelper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class KubernetesConfigurationHelper {
    private final Map<String, Object> state;

    public KubernetesConfigurationHelper(Map<String, Object> state) { this.state = state; }

    @SuppressWarnings("unchecked")
    public JSONArray requests() {
        JSONObject setup = json("setup"), data = json("testData"), stage = json("stage");
        JSONArray requests = new JSONArray();
        Object pipelineData = stage.get("pipelineData");
        if (!(pipelineData instanceof List<?> pipelines))
            throw new IllegalStateException("Stage pipelineData is missing");
        List<String> pipelineIds = new ArrayList<>();
        Map<String, String> techIds = (Map<String, String>) required("techStackIds");
        for (Object value : (JSONArray) setup.get("techStacks")) {
            JSONObject input = (JSONObject) value;
            JSONObject pipeline = pipeline(pipelines, techIds.get(input.get("name").toString()));
            pipelineIds.add(String.valueOf(pipeline.get("pipelineDetailsId")));
            requests.add(DeployStageRequestHelper.technology(pipeline, input, setup,
                    required("stageDetailsId").toString(), data.get("portfolioId").toString(),
                    data.get("portfolioName").toString(), required("kubernetesSettingId").toString(),
                    ((JSONObject) setup.get("configurationNames")).get("KUBERNETES").toString(),
                    required("suffix").toString()));
        }
        state.put("pipelineIds", pipelineIds);
        return requests;
    }

    private JSONObject pipeline(List<?> pipelines, String techId) {
        for (Object value : pipelines) {
            if (!(value instanceof Map<?, ?> map)) continue;
            JSONObject pipeline = new JSONObject(map);
            if (techId.equalsIgnoreCase(String.valueOf(pipeline.get("techStackId")))) return pipeline;
        }
        throw new IllegalStateException("No pipeline generated for tech stack " + techId);
    }

    private JSONObject json(String key) { return (JSONObject) required(key); }
    private Object required(String key) { Object value = state.get(key); if (value == null) throw new IllegalStateException("Missing DevSecOps runtime value: " + key); return value; }
}
