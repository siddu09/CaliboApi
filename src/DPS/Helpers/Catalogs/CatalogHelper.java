package DPS.Helpers.Catalogs;

import DPS.Helpers.Crawlers.CrawlerHelper;
import DPS.Helpers.DpsContext;
import common.RequestSpecProvider;
import io.restassured.response.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;

import java.util.Map;
import java.util.UUID;

import static endpoints.ApiEndpoints.DPS_CATALOG;
import static endpoints.ApiEndpoints.DPS_CATALOGS;
import static io.restassured.RestAssured.given;

public final class CatalogHelper {
    private final DpsContext context;
    private final CrawlerHelper crawler;

    public CatalogHelper(DpsContext context, CrawlerHelper crawler) {
        this.context = context;
        this.crawler = crawler;
    }

    @SuppressWarnings("unchecked") // json-simple exposes raw Map/List APIs.
    public void create() {
        JSONObject name = new JSONObject();
        name.put("name", "CatalogMSSQLS_" + context.suffix());
        Response response = given().spec(RequestSpecProvider.get()).queryParams(params())
                .body(name.toJSONString()).post(DPS_CATALOGS);
        Assert.assertEquals(response.statusCode(), 200, response.asString());
        context.catalogId(response.jsonPath().getString("id"));
        Assert.assertNotNull(context.catalogId(), "Catalog ID is missing");

        try {
            JSONArray crawled = (JSONArray) new JSONParser().parse(crawler.details().asString());
            JSONObject schema = null;
            for (Object item : crawled) if ("dbo".equals(((JSONObject) item).get("schema"))) schema = (JSONObject) item;
            if (schema == null) throw new IllegalStateException("dbo schema was not crawled");
            JSONArray tables = (JSONArray) schema.get("tables");
            JSONObject table = null;
            for (Object item : tables) if ("patients_5k".equals(((JSONObject) item).get("tableName"))) table = (JSONObject) item;
            if (table == null) throw new IllegalStateException("patients_5k table was not crawled");
            context.tableId(String.valueOf(table.get("id")));
            context.catalogTable(table);
            schema.put("id", UUID.randomUUID().toString().replace("-", "").substring(0, 9));
            JSONArray selectedTables = new JSONArray();
            selectedTables.add(table);
            schema.put("tables", selectedTables);
            table.put("schemaIndex", 0);
            table.put("name", table.get("tableName"));
            table.put("isChecked", true);
            JSONArray fields = (JSONArray) table.get("fields");
            for (int index = 0; index < fields.size(); index++) {
                JSONObject field = (JSONObject) fields.get(index);
                field.put("sequenceNo", index);
                field.put("isChecked", true);
                field.put("conditionAdded", false);
            }
            JSONArray selected = new JSONArray();
            selected.add(schema);
            Response update = given().spec(RequestSpecProvider.get()).queryParams(params())
                    .body(selected.toJSONString())
                    .put(DPS_CATALOG.replace("{catalogId}", context.catalogId()));
            Assert.assertEquals(update.statusCode(), 200, update.asString());
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to build catalog from crawler details", exception);
        }
    }

    public void verify() {
        Response response = given().spec(RequestSpecProvider.get()).queryParams(params())
                .get(DPS_CATALOG.replace("{catalogId}", context.catalogId()));
        Assert.assertEquals(response.statusCode(), 200, response.asString());
        Assert.assertEquals(response.jsonPath().getString("id"), context.catalogId());
    }

    private Map<String, ?> params() {
        return Map.of("projectId", context.projectId(), "releaseId", context.releaseId(),
                "workstreamId", context.workstreamId(), "crawlerId", context.crawlerId());
    }
}
