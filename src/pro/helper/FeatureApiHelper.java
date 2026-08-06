package pro.helper;

import config.Config;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utils.JsonUtils;

public class FeatureApiHelper {
    public JSONObject loadFeatureTestData() {
        return JsonUtils.readJson(Config.testDataPath + "SimpleFeature.json");
    }

    public String getUniqueFeatureName(JSONObject data) {
        return data.get("title") + String.valueOf(System.currentTimeMillis()).substring(7);
    }

    public JSONObject feature(JSONObject request) {
        return (JSONObject) ((JSONArray) request.get("workstreamsRequest")).get(0);
    }
}
