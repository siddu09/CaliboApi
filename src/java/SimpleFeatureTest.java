package tests;

import base.BaseTest;
import org.json.simple.JSONObject;
import org.testng.annotations.Test;
import pro.building_blocks.FeatureApiBuildingBlock;
import pro.helper.FeatureApiHelper;

public class SimpleFeatureTest extends BaseTest {
    @Test
    @SuppressWarnings("unchecked")
    public void createFeature() {
        createFeature("product1", "feature1");
    }

    @SuppressWarnings("unchecked")
    public void createFeature(String productKey, String featureKey) {
        FeatureApiHelper helper = new FeatureApiHelper();
        FeatureApiBuildingBlock feature = new FeatureApiBuildingBlock();
        JSONObject request = helper.loadFeatureTestData(productKey, featureKey);
        JSONObject data = helper.feature(request);
        data.put("title", helper.getUniqueFeatureName(data));
        feature.verifyFeature(feature.addNewFeature(request), data.get("projectId").toString());
        helper.updateRuntimeData(productKey, featureKey, data.get("title").toString(),
                feature.getFeatureId(), feature.getReleaseId());
        System.out.println("Feature ID: " + feature.getFeatureId());
    }
}
