package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import services.PortfolioProductFeatureService;

public class PortfolioProductFeatureTests extends BaseTest {

    @Test(description = "Create one portfolio, product, and feature, then clean up the product")
    public void createPortfolioProductAndFeature() {
        new PortfolioProductFeatureService().createPortfolioProductAndFeature();
    }
}
