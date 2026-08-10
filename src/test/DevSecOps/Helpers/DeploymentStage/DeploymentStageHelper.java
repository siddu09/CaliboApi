package DevSecOps.Helpers.DeploymentStage;

import java.util.Map;

public final class DeploymentStageHelper {
    private final DeploySetupHelper setup;
    private final DeployStageHelper stage;

    public DeploymentStageHelper(Map<String, Object> state) {
        setup = new DeploySetupHelper(state);
        stage = new DeployStageHelper(state);
    }

    public void loadTestData() { setup.loadTestData(); }

    public void addOrUpdateStage() {
        setup.createProjectAndWorkstream();
        stage.createOrUpdateStage();
    }
}
