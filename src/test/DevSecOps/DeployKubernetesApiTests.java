package DevSecOps;

import DevSecOps.BuildingBlocks.KubernetesDeployment.KubernetesDeployment;
import base.BaseTest;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

public class DeployKubernetesApiTests extends BaseTest {
    @Test(groups = "deploy-stage")
    public void deployKubernetes() {
        KubernetesDeployment deployment = new KubernetesDeployment(new LinkedHashMap<>());
        deployment.createStage();
        deployment.verifyCreatedStage();
        deployment.addNewTechnology();
    }
}
