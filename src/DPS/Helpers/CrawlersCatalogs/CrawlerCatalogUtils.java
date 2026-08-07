package DPS.Helpers.CrawlersCatalogs;

import config.Config;
import org.json.simple.JSONObject;
import utils.JsonUtils;

public final class CrawlerCatalogUtils {
    public String generateUniqueCrawlerName() { return "MSSQL_Crawler_" + System.currentTimeMillis(); }
    public String generateUniqueCatalogName() { return "MSSQL_Catalog_" + System.currentTimeMillis(); }
    public JSONObject buildExpectedTableMetadata() {
        return (JSONObject) JsonUtils.readJson(Config.testDataPath + "DPL_MSSQLCatalogDbSf.json")
                .get("dataCrawlerDetailsExpected");
    }
    public void verifyTableMetadata(JSONObject actual) {
        if (!buildExpectedTableMetadata().equals(actual))
            throw new IllegalStateException("Crawler table metadata does not match");
    }
}
