package DevSecOps.Helpers.Pipeline;

import common.RequestSpecProvider;
import endpoints.ApiEndpoints;
import io.restassured.response.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public final class PipelineStatusHelper {
    private final Map<String, Object> state;

    public PipelineStatusHelper(Map<String, Object> state) { this.state = state; }

    public void waitForCiSuccess() {
        state.put("successfulPipelineIds", waitFor("ciPipelineStatus", List.of("SUCCESS"), ids("pipelineIds")));
    }

    public void waitForDeploymentSuccess() {
        waitFor("pipelineStatus", List.of("DEPLOYED", "SUCCESS"), ids("successfulPipelineIds"));
        JSONObject data = json("testData");
        for (int attempt = 1; attempt <= 12; attempt++) {
            Response response = ok(given().spec(RequestSpecProvider.get()).pathParam("projectId", data.get("projectId"))
                    .queryParam("workstreamId", data.get("workstreamId")).queryParam("releaseId", data.get("releaseId"))
                    .get(ApiEndpoints.PROJECT_STAGES), "retrieve deployed stage");
            String url = liveUrl(parse(response.asString()));
            if (url != null) { state.put("liveUrl", url); return; }
            pause(10);
        }
        throw new IllegalStateException("Deployment completed but no application URL was generated");
    }

    @SuppressWarnings("unchecked")
    private List<String> waitFor(String field, List<String> success, List<String> pipelineIds) {
        List<String> pending = new ArrayList<>(pipelineIds), completed = new ArrayList<>();
        JSONObject setup = json("setup");
        int attempts = ((Number) setup.get("pipelineMaxAttempts")).intValue();
        for (int attempt = 1; attempt <= attempts && !pending.isEmpty(); attempt++) {
            JSONObject body = new JSONObject(); JSONArray values = new JSONArray(); values.addAll(pending); body.put("pipelineIds", values);
            JSONArray statuses = (JSONArray) parse(ok(given().spec(RequestSpecProvider.get()).body(body.toJSONString())
                    .post(ApiEndpoints.PIPELINE_BUILD_STATUS), "pipeline build status").asString());
            for (Object value : statuses) {
                JSONObject status = (JSONObject) value;
                String id = String.valueOf(status.get("pipelineDetailsId"));
                String current = String.valueOf(status.get(field));
                if (success.stream().anyMatch(item -> item.equalsIgnoreCase(current))) { pending.remove(id); completed.add(id); }
                else if ("FAILED".equalsIgnoreCase(current) || "CREATION_FAILED".equalsIgnoreCase(current))
                    throw new IllegalStateException("Pipeline " + id + " failed with " + field + "=" + current);
            }
            if (!pending.isEmpty()) pause(((Number) setup.get("pipelinePollSeconds")).intValue());
        }
        if (!pending.isEmpty()) throw new IllegalStateException("Pipelines timed out: " + pending);
        return completed;
    }

    private Object parse(String json) { try { return new JSONParser().parse(json); } catch (ParseException exception) { throw new IllegalStateException("Invalid pipeline response", exception); } }
    private String liveUrl(Object value) { if (value instanceof Map<?, ?> map) { for (String key : List.of("applicationUrl", "appServerUrl")) if (map.get(key) != null && map.get(key).toString().matches("https?://.+")) return map.get(key).toString(); for (Object nested : map.values()) { String url = liveUrl(nested); if (url != null) return url; } } else if (value instanceof List<?> list) for (Object nested : list) { String url = liveUrl(nested); if (url != null) return url; } return null; }
    private Response ok(Response response, String activity) { if (response.statusCode() != 200) throw new IllegalStateException(activity + " failed: HTTP " + response.statusCode() + ": " + response.asString()); return response; }
    @SuppressWarnings("unchecked") private List<String> ids(String key) { return (List<String>) required(key); }
    private JSONObject json(String key) { return (JSONObject) required(key); }
    private Object required(String key) { Object value = state.get(key); if (value == null) throw new IllegalStateException("Missing DevSecOps runtime value: " + key); return value; }
    private void pause(int seconds) { try { Thread.sleep(seconds * 1000L); } catch (InterruptedException exception) { Thread.currentThread().interrupt(); throw new IllegalStateException("Pipeline wait interrupted", exception); } }
}

