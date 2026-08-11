package pro.helper;

import config.Config;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utils.JsonUtils;

import java.util.Map;

public class PortfolioApiHelper {
    private static final String E2E_JSON = Config.testDataPath + "E2E.json";

    public JSONObject loadPortfolioTestData() {
        JSONObject portfolio = JsonUtils.readSection(E2E_JSON, "portfolio");
        JSONObject configuration = JsonUtils.readSection(E2E_JSON, "proConfiguration");
        configuration.put("portfolioDescription", portfolio.get("description"));
        configuration.put("portfolioStrategy", portfolio.get("strategy"));
        Map<String, Object> user = user();
        JSONObject stakeholder = new JSONObject();
        stakeholder.put("name", user.get("firstName") + " " + user.get("lastName"));
        stakeholder.put("roleName", "OWNER");
        stakeholder.put("email", user.get("email"));
        stakeholder.put("username", user.get("username"));

        JSONArray stakeholders = new JSONArray();
        stakeholders.add(stakeholder);

        JSONObject request = new JSONObject();
        request.put("title", portfolio.get("titlePrefix"));
        request.put("description", configuration.get("portfolioDescription"));
        request.put("strategy", configuration.get("portfolioStrategy"));
        request.put("priority", "LOW");
        request.put("currency", "USD");
        request.put("value", 100);
        request.put("stakeholders", stakeholders);
        return request;
    }

    public String getUniquePortfolioName(JSONObject data) {
        return getUniquePortfolioName(data.get("title").toString());
    }

    public String getUniquePortfolioName(String titlePrefix) {
        return titlePrefix + String.valueOf(System.currentTimeMillis()).substring(7);
    }

    public void updateRuntimeData(String title, String portfolioId) {
        JsonUtils.update(E2E_JSON, "portfolio.title", title);
        JsonUtils.update(E2E_JSON, "portfolio.portfolioId", portfolioId);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> user() {
        return JsonUtils.readSection(E2E_JSON, "proConfiguration.user");
    }
}
