package tests;

import base.BaseTest;
import org.json.simple.JSONObject;
import org.testng.annotations.Test;
import pro.building_blocks.ProductApiBuildingBlock;
import pro.helper.ProductApiHelper;

public class SimpleProductTest extends BaseTest {
    @Test
    @SuppressWarnings("unchecked")
    public void createProduct() {
        createProduct("product1");
    }

    @SuppressWarnings("unchecked")
    public void createProduct(String productKey) {
        ProductApiHelper helper = new ProductApiHelper();
        ProductApiBuildingBlock product = new ProductApiBuildingBlock();
        JSONObject request = helper.loadProductTestData(productKey);
        request.put("title", helper.getUniqueProductName(request));
        product.verifyProduct(product.addNewProduct(request));
        helper.updateRuntimeData(productKey, request.get("title").toString(), product.getProductId());
        System.out.println("Product ID: " + product.getProductId());
    }
}
