#!/usr/bin/env python3
"""Create the two-sheet DevSecOps API workbook using the Pro-UI template."""

from pathlib import Path
import re
import zipfile

from generate_api_structure_workbook import SOURCE, worksheet


OUTPUT = Path(__file__).resolve().parents[1] / "docs" / "DevSecOps-API-Two-Sheet-Review.xlsx"


ENDPOINTS = [
    ("GET - Stage", "/devops/project/{projectId}/stages?workstreamId={workstreamId}&releaseId={releaseId}", "Get or verify DevStage [PROJECT_STAGES]"),
    ("POST - Stage", "/devops/stage", "Create DevStage [DEVOPS_STAGE]"),
    ("POST - Pipeline", "/devops/stage/v2/techStackPipeline?workstreamId={workstreamId}&releaseId={releaseId}", "Configure stage technologies [STAGE_TECH_STACK_PIPELINE]"),
    ("GET - Pipeline", "/devops/pipeline/{pipelineDetailsId}/ci/run", "Start CI pipeline [PIPELINE_CI_RUN]"),
    ("POST - Pipeline", "/devops/pipeline/v3/buildStatus", "Poll CI or deployment status [PIPELINE_BUILD_STATUS]"),
    ("POST - Deployment", "/devops/pipeline/deploy", "Deploy successful pipeline images [PIPELINE_DEPLOY]"),
    ("POST - Validation", "/devops/project/pipelines/v2/stages", "Retrieve and verify pipeline stage logs [PIPELINE_STAGE_LOGS]"),
]


def endpoint_rows():
    rows = [
        [("devsecops", 7), ("devsecops", 2), ("endpoints", 3)],
        [("devstage", 8), ("api", 4), ("DevSecOps", 2)],
        [("Method / Area", 9), ("Endpoint", 5), ("Purpose / Constant", 5)],
    ]
    rows.extend([list(item) for item in ENDPOINTS])
    return rows


def structure_rows():
    return [
        [("devsecops", 7), ("devsecops", 2), ("tests", 3)],
        [("devstage", 8), ("devstage", 4), ("devsecops", 2)],
        [("helper", 8), ("building_blocks", 4), ("devstage", 4)],
        [("DevStageApiHelper", 9), ("DevStageApiBuildingBlock", 5), ("DevStageApiTests", 5)],
        [("loadDevStageTestData()", 10), ("getStageTemplate()", 1), ("createDevStage", 1)],
        [("createRuntimeStageData()", 10), ("createOrReuseStage()", 1), ("", 1)],
        [("buildInitialKubernetesStageRequest()", 10), ("verifyCreatedStage()", 1), ("", 1)],
        [("buildKubernetesStageRequest()", 10), ("configureStageTechnologies()", 1), ("", 1)],
        [("findStage()", 10), ("runCiPipelines()", 1), ("", 1)],
        [("findPipelineByTechStack()", 10), ("waitForCiSuccess()", 1), ("", 1)],
        [("buildTechnologyRequests()", 10), ("deploySuccessfulPipelines()", 1), ("", 1)],
        [("collectPipelineIds()", 10), ("waitForDeploymentSuccess()", 1), ("", 1)],
        [("parsePipelineStatus()", 10), ("retrievePipelineStages()", 1), ("", 1)],
        [("buildDeployRequest()", 10), ("verifyPipelineLogs()", 1), ("", 1)],
        [("buildPipelineLogRequest()", 10), ("createAndVerifyDevStage()", 1), ("", 1)],
    ]


def main():
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(SOURCE) as source_zip:
        files = {name: source_zip.read(name) for name in source_zip.namelist()}

    files["xl/worksheets/sheet1.xml"] = worksheet(
        endpoint_rows(), [30.71, 88, 48.57], autofilter=f"A3:C{len(ENDPOINTS) + 3}"
    )
    files["xl/worksheets/sheet2.xml"] = worksheet(
        structure_rows(), [30.71, 41.71, 48.57], freeze=False
    )

    workbook = files["xl/workbook.xml"]
    sheets = (
        '<sheets>'
        '<sheet name="DevSecOps Valid Endpoints" sheetId="1" r:id="rId1"/>'
        '<sheet name="DevSecOps API Folder" sheetId="2" r:id="rId2"/>'
        '</sheets>'
    ).encode()
    files["xl/workbook.xml"] = re.sub(br"<sheets>.*?</sheets>", sheets, workbook, flags=re.DOTALL)

    with zipfile.ZipFile(OUTPUT, "w", zipfile.ZIP_DEFLATED) as output_zip:
        for name, content in files.items():
            output_zip.writestr(name, content)
    print(OUTPUT)


if __name__ == "__main__":
    main()
