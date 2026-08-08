package DPS.BuildingBlocks.DataPipeline;

import DPS.Helpers.DpsContext;
import DPS.Helpers.WorkflowHelper.WorkflowHelper;

public final class Workflow {
    private final DpsContext context = new DpsContext();
    private final WorkflowHelper helper = new WorkflowHelper(context);

    public DpsContext setup() { helper.setup(); return context; }
    public void runAndVerify() { helper.runAndVerify(); }
}
