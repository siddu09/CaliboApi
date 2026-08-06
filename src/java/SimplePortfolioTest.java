package tests;

import base.BaseTest;
import io.restassured.response.Response;
import org.json.simple.JSONObject;
import org.testng.annotations.Test;
import pro.building_blocks.PortfolioApiBuildingBlock;
import pro.helper.PortfolioApiHelper;

public class SimplePortfolioTest extends BaseTest {

    @Test
    @SuppressWarnings("unchecked")
    public void createPortfolio() {
        PortfolioApiHelper helper = new PortfolioApiHelper();
        PortfolioApiBuildingBlock portfolio = new PortfolioApiBuildingBlock();

        JSONObject request = helper.loadPortfolioTestData();
        request.put("title", helper.getUniquePortfolioName(request));
        Response response = portfolio.addNewProductPortfolio(request);
        portfolio.verifyProductPortfolio(response);
    }
}
