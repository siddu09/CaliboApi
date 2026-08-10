package DevSecOps.Helpers.Pipeline;

import java.util.Map;

public final class DeploymentPipelineHelper {
    private final PipelineHelper pipeline;
    private final PipelineStatusHelper status;

    public DeploymentPipelineHelper(Map<String, Object> state) {
        pipeline = new PipelineHelper(state);
        status = new PipelineStatusHelper(state);
    }

    public void runCiPipelines() { pipeline.runCiPipeline(); }
    public void waitForCiSuccess() { status.waitForCiSuccess(); }
    public void deployPipelines() { pipeline.runDeploymentPipeline(); }
    public void waitForDeploymentSuccess() { status.waitForDeploymentSuccess(); }
}
