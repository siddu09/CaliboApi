package DevSecOps.Helpers.DeploymentStage;

import java.util.Map;

public final class DeployStageValidationHelper {
    private final Map<String, Object> state;

    public DeployStageValidationHelper(Map<String, Object> state) { this.state = state; }

    public void verifySetup() {
        for (String key : java.util.List.of("testData", "techStackIds", "kubernetesSettingId"))
            if (state.get(key) == null) throw new IllegalStateException("Deployment setup is missing " + key);
    }

    public void verifyCreatedStage() {
        if (state.get("stageDetailsId") == null) throw new IllegalStateException("Dev stage was not created");
    }
}



