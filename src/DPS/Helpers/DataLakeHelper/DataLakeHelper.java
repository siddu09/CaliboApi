package DPS.Helpers.DataLakeHelper;

import DPS.BuildingBlocks.Crawler.Crawler;
import DPS.Helpers.DpsContext;
import common.RequestSpecProvider;
import io.restassured.response.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.Assert;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static endpoints.ApiEndpoints.DPS_DATA_STORES;
import static endpoints.ApiEndpoints.DPS_DRAFT;
import static io.restassured.RestAssured.given;

public final class DataLakeHelper {
    private final DpsContext context;
    public DataLakeHelper(Crawler crawler) { context = crawler.context(); }

    @SuppressWarnings("unchecked") // json-simple exposes raw Map/List APIs.
    public void addDataLakeStageWithSnowflakeNode() {
        Response stores = given().spec(RequestSpecProvider.get()).get(DPS_DATA_STORES);
        Assert.assertEquals(stores.statusCode(), 200, stores.asString());
        List<Map<String, Object>> allStores = stores.jsonPath().getList("$");
        Map<String, Object> store = allStores.stream()
                .filter(item -> "Snowflake_Automation".equals(item.get("name"))).findFirst()
                .orElseThrow(() -> new IllegalStateException("Snowflake_Automation datastore not found"));

        JSONObject stage = new JSONObject(); stage.put("name", "DATA_LAKE"); stage.put("type", "DATA_LAKE");
        stage.put("icon", "/data_source.png"); stage.put("laneIndex", 2);
        JSONObject node = new JSONObject(); context.dataLakeNodeId(UUID.randomUUID().toString());
        node.put("id", context.dataLakeNodeId()); node.put("displayLabel", "Snowflake 1");
        node.put("label", "Snowflake"); node.put("providerCode", "SNOWFLAKE");
        node.put("techStackId", "26"); node.put("laneIndex", "2"); node.put("type", "DATA_LAKE");
        node.put("icon", "/snowflake.png"); node.put("repoId", ""); node.put("isTechStackConfigurable", true);
        node.put("configurationType", "EXISTING_DATASTORE");
        JSONArray attributes = new JSONArray();
        for (Map<String, Object> attribute : (List<Map<String, Object>>) store.get("attributes"))
            attributes.add(new JSONObject(attribute));
        JSONObject selected = new JSONObject(); selected.put("attributes", attributes);
        JSONArray configuration = new JSONArray(); configuration.add(selected); node.put("configuration", configuration);

        ((JSONArray) context.draft().get("stages")).add(stage);
        ((JSONArray) context.draft().get("nodes")).add(node);
        JSONObject edge = new JSONObject(); edge.put("from", context.integrationNodeId()); edge.put("to", context.dataLakeNodeId());
        ((JSONArray) context.draft().get("edges")).add(edge);
        Response response = given().spec(RequestSpecProvider.get()).body(context.draft().toJSONString()).post(DPS_DRAFT);
        Assert.assertEquals(response.statusCode(), 201, response.asString());
    }
}
