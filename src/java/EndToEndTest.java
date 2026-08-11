package tests;

import base.BaseTest;
import org.testng.annotations.Test;

public class EndToEndTest extends BaseTest {

    @Test
    public void endToEndTest() {
        SimplePortfolioTest portfolio = new SimplePortfolioTest();
        SimpleProductTest product = new SimpleProductTest();
        SimpleFeatureTest feature = new SimpleFeatureTest();

        portfolio.createPortfolio();

        product.createProduct("product1");
        product.createProduct("product2");

        feature.createFeature("product1", "feature1");
        feature.createFeature("product1", "feature2");
        feature.createFeature("product2", "feature1");
        feature.createFeature("product2", "feature2");
    }
}
