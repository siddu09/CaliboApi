package pro.helper;

import config.Config;
import org.json.simple.JSONObject;
import utils.JsonUtils;

public class PortfolioApiHelper {

    public JSONObject loadPortfolioTestData() {
        return JsonUtils.readJson(Config.testDataPath + "SimplePortfolio.json");
    }

    public String getUniquePortfolioName(JSONObject data) {
        return data.get("title") + String.valueOf(System.currentTimeMillis()).substring(7);
    }
}
