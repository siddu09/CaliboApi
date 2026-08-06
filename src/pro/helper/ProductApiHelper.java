package pro.helper;

import config.Config;
import org.json.simple.JSONObject;
import utils.JsonUtils;

public class ProductApiHelper {
    public JSONObject loadProductTestData() {
        return JsonUtils.readJson(Config.testDataPath + "SimpleProduct.json");
    }

    public String getUniqueProductName(JSONObject data) {
        return data.get("title") + String.valueOf(System.currentTimeMillis()).substring(7);
    }

}
