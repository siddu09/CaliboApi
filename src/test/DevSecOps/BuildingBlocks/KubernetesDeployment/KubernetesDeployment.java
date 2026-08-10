package DevSecOps.BuildingBlocks.KubernetesDeployment;

import DevSecOps.Helpers.DeploymentStage.DeployStageValidationHelper;
import DevSecOps.Helpers.DeploymentStage.DeploymentStageHelper;
import DevSecOps.Helpers.Kubernetes.KubernetesHelper;
import DevSecOps.Helpers.Pipeline.DeploymentPipelineHelper;
import DevSecOps.Helpers.Validation.DeploymentValidationHelper;

import java.util.Map;

public final class KubernetesDeployment {
    private final DeploymentStageHelper stage;
    private final DeployStageValidationHelper stageValidation;
    private final KubernetesHelper kubernetes;
    private final DeploymentPipelineHelper pipeline;
    private final DeploymentValidationHelper validation;

    public KubernetesDeployment(Map<String, Object> state) {
        stage = new DeploymentStageHelper(state);
        stageValidation = new DeployStageValidationHelper(state);
        kubernetes = new KubernetesHelper(state);
        pipeline = new DeploymentPipelineHelper(state);
        validation = new DeploymentValidationHelper(state);
    }

    public void createStage() {
        stage.loadTestData();
        stage.addOrUpdateStage();
    }

    public void verifyCreatedStage() {
        stageValidation.verifySetup();
        stageValidation.verifyCreatedStage();
    }

    public void addNewTechnology() {
        kubernetes.configureKubernetes();
        pipeline.runCiPipelines();
        pipeline.waitForCiSuccess();
        pipeline.deployPipelines();
        pipeline.waitForDeploymentSuccess();
        validation.verifyCicdLogs();
        validation.validateLiveUrl();
    }
}
