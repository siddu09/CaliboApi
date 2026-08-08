package DPS.Helpers;

import config.Config;
import org.json.simple.JSONObject;
import utils.JsonUtils;

import java.util.UUID;

/** Runtime identifiers shared by the DPS building blocks. */
public final class DpsContext {
    private final String suffix = UUID.randomUUID().toString().substring(0, 6);
    private final String projectName = "Auto_Proj_DataIngestion_" + suffix;
    private final String workstreamName = "AutoWS_DataIngestion_" + suffix;
    private final JSONObject payload = JsonUtils.readJson(Config.testDataPath + "dataIngestionCatalog.json.json");
    private final JSONObject draft = new JSONObject();
    private String projectId, workstreamId, releaseId, stageDetailsId, pipelineDetailsId;
    private String crawlerId, catalogId, tableId, draftId, dataSourceNodeId;
    private String integrationNodeId, dataLakeNodeId, repositoryId, jobId;
    private JSONObject catalogTable;

    public String suffix() { return suffix; }
    public String projectName() { return projectName; }
    public String workstreamName() { return workstreamName; }
    public JSONObject payload() { return payload; }
    public JSONObject draft() { return draft; }
    public JSONObject catalogTable() { return catalogTable; }
    public void catalogTable(JSONObject value) { catalogTable = value; }
    public String projectId() { return projectId; }
    public void projectId(String value) { projectId = value; }
    public String workstreamId() { return workstreamId; }
    public void workstreamId(String value) { workstreamId = value; }
    public String releaseId() { return releaseId; }
    public void releaseId(String value) { releaseId = value; }
    public String stageDetailsId() { return stageDetailsId; }
    public void stageDetailsId(String value) { stageDetailsId = value; }
    public String pipelineDetailsId() { return pipelineDetailsId; }
    public void pipelineDetailsId(String value) { pipelineDetailsId = value; }
    public String crawlerId() { return crawlerId; }
    public void crawlerId(String value) { crawlerId = value; }
    public String catalogId() { return catalogId; }
    public void catalogId(String value) { catalogId = value; }
    public String tableId() { return tableId; }
    public void tableId(String value) { tableId = value; }
    public String draftId() { return draftId; }
    public void draftId(String value) { draftId = value; }
    public String dataSourceNodeId() { return dataSourceNodeId; }
    public void dataSourceNodeId(String value) { dataSourceNodeId = value; }
    public String integrationNodeId() { return integrationNodeId; }
    public void integrationNodeId(String value) { integrationNodeId = value; }
    public String dataLakeNodeId() { return dataLakeNodeId; }
    public void dataLakeNodeId(String value) { dataLakeNodeId = value; }
    public String repositoryId() { return repositoryId; }
    public void repositoryId(String value) { repositoryId = value; }
    public String jobId() { return jobId; }
    public void jobId(String value) { jobId = value; }
}
