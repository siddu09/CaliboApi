package DPS.Helpers.Crawlers;

import DPS.Helpers.DpsContext;
import common.RequestSpecProvider;
import io.restassured.response.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.Assert;

import java.util.List;
import java.util.Map;

import static endpoints.ApiEndpoints.*;
import static io.restassured.RestAssured.given;

public final class CrawlerHelper {
    private final DpsContext context;

    public CrawlerHelper(DpsContext context) { this.context = context; }

    @SuppressWarnings("unchecked") // json-simple exposes raw Map/List APIs.
    public void create() {
        Response stores = given().spec(RequestSpecProvider.get()).get(DPS_DATA_STORES);
        Assert.assertEquals(stores.statusCode(), 200, stores.asString());
        List<Map<String, Object>> all = stores.jsonPath().getList("$");
        Map<String, Object> store = all.stream()
                .filter(item -> "MSSQL_Automation".equals(item.get("name"))).findFirst()
                .orElseThrow(() -> new IllegalStateException("MSSQL_Automation datastore not found"));

        JSONObject body = new JSONObject();
        body.put("name", "MSSQLCrawlerS_" + context.suffix());
        body.put("sourceType", "RDBMS");
        body.put("subType", "MS_SQL_SERVER");
        JSONArray attributes = new JSONArray();
        for (Map<String, Object> attribute : (List<Map<String, Object>>) store.get("attributes")) {
            String name = String.valueOf(attribute.get("attributeName"));
            if (List.of("activeSecurityProvider", "host", "databaseName", "port").contains(name)) {
                attributes.add(new JSONObject(attribute));
            }
        }
        body.put("attributes", attributes);

        Response response = given().spec(RequestSpecProvider.get()).queryParams(baseParams())
                .body(body.toJSONString()).post(DPS_CRAWLERS);
        Assert.assertEquals(response.statusCode(), 200, response.asString());
        context.crawlerId(response.jsonPath().getString("id"));
        Assert.assertNotNull(context.crawlerId(), "Crawler ID is missing");

        Response run = given().spec(RequestSpecProvider.get())
                .queryParam("crawlerId", context.crawlerId())
                .queryParam("pipelineDetailsId", context.pipelineDetailsId())
                .queryParam("projectId", context.projectId()).body("{}")
                .put(DPS_CRAWLER_RUN);
        Assert.assertEquals(run.statusCode(), 200, run.asString());
    }

    public void waitUntilComplete() {
        for (int attempt = 1; attempt <= 180; attempt++) {
            Response response = given().spec(RequestSpecProvider.get()).queryParams(baseParams())
                    .get(DPS_CRAWLER.replace("{crawlerId}", context.crawlerId()));
            Assert.assertEquals(response.statusCode(), 200, response.asString());
            String status = response.jsonPath().getString("status");
            if ("SUCCESS".equals(status)) return;
            if ("FAILED".equals(status)) Assert.fail("Crawler failed: " + response.asString());
            try { Thread.sleep(1000); }
            catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Crawler wait interrupted", exception);
            }
        }
        Assert.fail("Crawler did not complete within 180 seconds");
    }

    public Response details() {
        Response response = given().spec(RequestSpecProvider.get()).queryParams(baseParams())
                .get(DPS_CRAWLER_DETAILS.replace("{crawlerId}", context.crawlerId()));
        Assert.assertEquals(response.statusCode(), 200, response.asString());
        return response;
    }

    private Map<String, ?> baseParams() {
        return Map.of("pipelineDetailsId", context.pipelineDetailsId(),
                "projectId", context.projectId(), "releaseId", context.releaseId(),
                "workstreamId", context.workstreamId());
    }
}
