package tests;

import base.BaseTest;
import org.json.simple.JSONObject;
import org.testng.annotations.Test;
import pro.building_blocks.PortfolioApiBuildingBlock;
import pro.helper.PortfolioApiHelper;

public class SimplePortfolioTest extends BaseTest {

    @Test
    @SuppressWarnings("unchecked")
    public void createPortfolio() {
        PortfolioApiHelper portfolioHelper = new PortfolioApiHelper();
        PortfolioApiBuildingBlock portfolio = new PortfolioApiBuildingBlock();
        JSONObject request = portfolioHelper.loadPortfolioTestData();
        request.put("title", portfolioHelper.getUniquePortfolioName(request));
        portfolio.verifyProductPortfolio(portfolio.addNewProductPortfolio(request));
        portfolioHelper.updateRuntimeData(portfolio.getPortfolioTitle(), portfolio.getPortfolioId());
        System.out.println("Portfolio ID: " + portfolio.getPortfolioId());
    }
}
