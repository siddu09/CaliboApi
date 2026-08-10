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
        Response response = checked(given().spec(RequestSpecProvider.get()).queryParams(params())
                .body(name.toJSONString()).post(DPS_CATALOGS));
        context.catalogId(response.jsonPath().getString("id"));
        Assert.assertNotNull(context.catalogId(), "Catalog ID is missing");
        try {
            JSONArray crawled = (JSONArray) new JSONParser().parse(crawler.details().asString());
            JSONObject schema = find(crawled, "schema", "dbo");
            JSONObject table = find((JSONArray) schema.get("tables"), "tableName", "patients_5k");
            context.tableId(String.valueOf(table.get("id")));
            context.catalogTable(table);
            schema.put("id", UUID.randomUUID().toString().replace("-", "").substring(0, 9));
            schema.put("tables", array(table));
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
            checked(given().spec(RequestSpecProvider.get()).queryParams(params()).body(array(schema).toJSONString())
                    .put(catalogUri()));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to build catalog from crawler details", exception);
        }
    }

    public void verify() {
        Response response = checked(given().spec(RequestSpecProvider.get()).queryParams(params()).get(catalogUri()));
        Assert.assertEquals(response.jsonPath().getString("id"), context.catalogId());
    }

    private JSONObject find(JSONArray items, String key, String value) {
        for (Object item : items) if (value.equals(((JSONObject) item).get(key))) return (JSONObject) item;
        throw new IllegalStateException(value + " was not crawled");
    }

    @SuppressWarnings("unchecked")
    private JSONArray array(Object item) { JSONArray array = new JSONArray(); array.add(item); return array; }
    private String catalogUri() { return DPS_CATALOG.replace("{catalogId}", context.catalogId()); }
    private Response checked(Response response) {
        Assert.assertEquals(response.statusCode(), 200, response.asString());
        return response;
    }

    private Map<String, ?> params() {
        return Map.of("projectId", context.projectId(), "releaseId", context.releaseId(),
                "workstreamId", context.workstreamId(), "crawlerId", context.crawlerId());
    }
}
