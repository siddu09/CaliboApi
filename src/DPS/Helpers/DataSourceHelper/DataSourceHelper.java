package DPS.Helpers.DataSourceHelper;

import DPS.BuildingBlocks.Crawler.Crawler;
import DPS.Helpers.DpsContext;
import common.RequestSpecProvider;
import io.restassured.response.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.Assert;

import java.util.UUID;

import static endpoints.ApiEndpoints.DPS_DRAFT;
import static io.restassured.RestAssured.given;

public final class DataSourceHelper {
    private final DpsContext context;

    public DataSourceHelper(Crawler crawler) { context = crawler.context(); }

    @SuppressWarnings("unchecked") // json-simple exposes raw Map/List APIs.
    public void addDataSourceStageWithMsSqlNode() {
        JSONObject table = new JSONObject(context.catalogTable());
        table.put("columns", table.remove("fields"));
        JSONObject node = node("Microsoft SQL Server 1", "RDBMS", "78", "MS_SQL_SERVER", 0);
        JSONArray configuration = new JSONArray();
        configuration.add(attribute("catalogName", "CatalogMSSQLS_" + context.suffix()));
        configuration.add(attribute("catalogId", context.catalogId()));
        configuration.add(attribute("catalogSchema", "dbo"));
        JSONArray tables = new JSONArray(); tables.add(table);
        configuration.add(attribute("tables", tables));
        node.put("configuration", configuration);
        node.put("configurationType", "DATA_CATALOG");
        add("DATA_SOURCES", node);
        context.dataSourceNodeId(String.valueOf(node.get("id")));
    }

    @SuppressWarnings("unchecked") // json-simple exposes raw Map/List APIs.
    private void add(String type, JSONObject node) {
        JSONObject stage = new JSONObject();
        stage.put("name", type); stage.put("type", type);
        stage.put("icon", "/data_source.png"); stage.put("laneIndex", 0);
        ((JSONArray) context.draft().get("stages")).add(stage);
        ((JSONArray) context.draft().get("nodes")).add(node);
        save();
    }

    @SuppressWarnings("unchecked") // json-simple exposes raw Map/List APIs.
    private JSONObject node(String display, String provider, String techStack, String subType, int lane) {
        JSONObject node = new JSONObject();
        node.put("id", UUID.randomUUID().toString()); node.put("displayLabel", display);
        node.put("label", "Microsoft SQL Server"); node.put("providerCode", provider);
        node.put("providerCodeSubType", subType); node.put("techStackId", techStack);
        node.put("laneIndex", String.valueOf(lane)); node.put("type", "DATA_SOURCES");
        node.put("icon", "/microsoft_sql_server.png"); node.put("repoId", "");
        node.put("isTechStackConfigurable", true);
        return node;
    }

    @SuppressWarnings("unchecked") // json-simple exposes raw Map/List APIs.
    private JSONObject attribute(String name, Object value) {
        JSONObject item = new JSONObject(); item.put("attributeName", name); item.put("attributeValue", value); return item;
    }

    private void save() {
        Response response = given().spec(RequestSpecProvider.get()).body(context.draft().toJSONString()).post(DPS_DRAFT);
        Assert.assertEquals(response.statusCode(), 201, response.asString());
    }
}
