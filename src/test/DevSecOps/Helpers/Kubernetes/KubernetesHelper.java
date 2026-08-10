package DevSecOps.Helpers.Kubernetes;

import java.util.Map;

public final class KubernetesHelper {
    private final KubernetesDeploymentHelper deployment;

    public KubernetesHelper(Map<String, Object> state) {
        deployment = new KubernetesDeploymentHelper(state);
    }

    public void configureKubernetes() { deployment.configureKubernetes(); }
}
