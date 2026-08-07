package DPS.Helpers.CrawlersCatalogs;

import config.Config;
import config.Constants;
import org.json.simple.JSONObject;
import utils.JsonUtils;

public final class CrawlerConfugurations {
    public JSONObject configureMsSqlCrawlerWithExistingDatastore() {
        JSONObject setup = JsonUtils.readJson(Config.testDataPath + Constants.DATA_INGESTION_SETUP_JSON);
        JSONObject payload = JsonUtils.readJson(Config.testDataPath + setup.get("payloadFile"));
        return (JSONObject) payload.get("crawlerInPutJson");
    }
}
