#!/usr/bin/env python3
"""Generate the API automation architecture workbook from the approved UI template."""

from pathlib import Path
from xml.sax.saxutils import escape
import re
import zipfile


SOURCE = Path("/Users/sid/Desktop/devSecops/folder Strucure/API Automation-FolderStructures.xlsx")
OUTPUT = Path(__file__).resolve().parents[1] / "docs" / "API Automation-FolderStructures-Updated.xlsx"


PRO_ENDPOINTS = [
    ("GET", "/configuration/settings/client?configCode={code}", "Shared prerequisite", "SETTINGS_BY_CONFIG_CODE", "Resolve configuration IDs", "code"),
    ("GET", "/keycloakadapter/users/v2/userInfo", "Pro", "CURRENT_USER_INFO", "Resolve current owner/user", "-"),
    ("GET", "/rbac/roles/type/project", "Pro", "PROJECT_ROLES", "Resolve product owner role", "-"),
    ("GET", "/elab/portfolios?limit={limit}&offset={offset}&sort={sort}&search={name}", "Pro / Portfolio", "PORTFOLIOS", "Find portfolio by name", "limit, offset, sort, name"),
    ("GET", "/configuration/settings/fieldvalues?isUsageRequired=false", "Pro / Product", "DROPDOWN_VALUES", "Resolve business group and customer IDs", "isUsageRequired"),
    ("POST", "/elab/v2/projects", "Pro / Product", "PROJECTS_V2", "Create Product (API project)", "request body"),
    ("PATCH", "/elab/v2/projects/{projectId}", "Pro / Product", "PROJECT_BY_ID_V2", "Update Product", "projectId, request body"),
    ("POST", "/elab/v2/projects/workstreams", "Pro / Feature", "WORKSTREAMS_V2", "Create Feature (API workstream)", "request body"),
    ("GET", "/elab/projects/workstreams/getAllWorkstreams/{projectId}", "Pro / Feature", "PROJECT_WORKSTREAMS", "Verify Feature", "projectId"),
    ("GET", "/elab/projects/releases?projectId={projectId}", "Pro / Feature", "PROJECT_RELEASES", "Resolve default release", "projectId"),
    ("GET", "/configuration/settings/techStack/ordered?projectId={projectId}&isSelected=true", "Pro / Repository", "PROJECT_TECH_STACKS", "Resolve selected technology stacks", "projectId, isSelected"),
    ("GET", "/elab/projects/repositories/groups/list?projectId={projectId}", "Pro / Repository", "PROJECT_REPOSITORY_GROUPS", "Resolve repository group", "projectId"),
    ("POST", "/elab/projects/repositories", "Pro / Repository", "PROJECT_REPOSITORIES", "Create technology repository", "request body"),
    ("GET", "/elab/projects/repositories/createRepoStatus/{projectId}/{workstreamId}", "Pro / Repository", "REPOSITORY_CREATION_STATUS", "Poll repository creation", "projectId, workstreamId"),
]

DEVSTAGE_ENDPOINTS = [
    ("GET", "/devops/project/{projectId}/stages?workstreamId={workstreamId}&releaseId={releaseId}", "DevStage / Stage", "PROJECT_STAGES", "Read or verify deployment stages", "projectId, workstreamId, releaseId"),
    ("POST", "/devops/stage", "DevStage / Stage", "DEVOPS_STAGE", "Create deployment stage", "request body"),
    ("POST", "/devops/stage/v2/techStackPipeline?workstreamId={workstreamId}&releaseId={releaseId}", "DevStage / Pipeline", "STAGE_TECH_STACK_PIPELINE", "Configure stage technologies", "workstreamId, releaseId, request body"),
    ("GET", "/devops/pipeline/{pipelineDetailsId}/ci/run", "DevStage / Pipeline", "PIPELINE_CI_RUN", "Start CI pipeline", "pipelineDetailsId"),
    ("POST", "/devops/pipeline/v3/buildStatus", "DevStage / Pipeline", "PIPELINE_BUILD_STATUS", "Poll CI/deployment status", "pipelineIds"),
    ("POST", "/devops/pipeline/deploy", "DevStage / Deployment", "PIPELINE_DEPLOY", "Deploy successful pipeline images", "request body"),
    ("POST", "/devops/project/pipelines/v2/stages", "DevStage / Validation", "PIPELINE_STAGE_LOGS", "Retrieve and verify pipeline stage logs", "pipelineIds"),
]


PRO_STRUCTURE = [
    ("portfolio", "PortfolioApiHelper", "findPortfolioIdByName(); requirePortfolioId()", "PortfolioApiBuildingBlock", "getPortfolioByName(); resolvePortfolioContext()", "ProPortfolioApiTests", "resolveExistingPortfolio"),
    ("product (API: project)", "ProductApiHelper", "uniqueProductName(); buildProductRequest(); extractProjectId(); resolveProductLookups()", "ProductApiBuildingBlock", "createProduct(); updateProduct(); createAndUpdateProduct()", "ProProductApiTests", "createProductWithMandatoryFields; createProductWithAllFields"),
    ("feature (API: workstream)", "FeatureApiHelper", "uniqueFeatureName(); buildFeatureRequest(); extractWorkstreamId(); extractReleaseId()", "FeatureApiBuildingBlock", "createFeature(); verifyFeature(); verifyDefaultRelease()", "ProFeatureApiTests", "createFeatureWithMandatoryFields; createProductAndFeature"),
    ("repository prerequisite", "RepositoryApiHelper", "findTechStackId(); findRepositoryGroupId(); requireActiveStatus()", "RepositoryApiBuildingBlock", "createRepository(); waitForRepositoryActive(); createRepositoriesForTechStacks()", "ProRepositoryApiTests", "createRepositoriesForFeature"),
]

DEV_STRUCTURE = [
    ("stage", "DevStageApiHelper", "buildInitialStageRequest(); buildStageRequest(); findStage(); requireStageDetailsId()", "DevStageApiBuildingBlock", "getStages(); createStage(); createOrReuseStage(); verifyStage()", "DevStageApiTests", "createKubernetesStage"),
    ("technology pipeline", "PipelineApiHelper", "findPipelineByTechStack(); buildTechnologyRequests(); collectPipelineIds()", "PipelineApiBuildingBlock", "configureTechnologies(); runCiForAll()", "DevStagePipelineApiTests", "configureAndRunCi"),
    ("status polling", "PipelineStatusHelper", "parseStatuses(); classifyTerminalState(); preserveInterrupt()", "PipelineStatusBuildingBlock", "waitForCiSuccess(); waitForDeploymentSuccess()", "DevStagePipelineApiTests", "waitUntilPipelinesSucceed"),
    ("deployment", "DeploymentApiHelper", "buildDeployRequest(); validateDeployablePipelines()", "DeploymentApiBuildingBlock", "deploySuccessfulPipelines(); verifyDeployment()", "DevStageDeploymentApiTests", "deployStage"),
    ("pipeline validation", "PipelineLogHelper", "buildPipelineLogRequest(); requireLogsForEveryPipeline()", "PipelineLogBuildingBlock", "retrievePipelineStages(); verifyPipelineLogs()", "DevStageDeploymentApiTests", "validateEndToEndPipelineLogs"),
    ("shared", "ApiResponseHelper", "requireStatus(); requiredPath(); findValue(); redactSecrets()", "ApiContextBuildingBlock", "resolveProContext(); pass IDs to DevStageContext", "DevStageEndToEndTests", "productId -> projectId; featureId -> workstreamId"),
]


def col_name(index):
    result = ""
    while index:
        index, remainder = divmod(index - 1, 26)
        result = chr(65 + remainder) + result
    return result


def cell(ref, value, style=1):
    value = "" if value is None else str(value)
    preserve = ' xml:space="preserve"' if value != value.strip() else ""
    return f'<c r="{ref}" s="{style}" t="inlineStr"><is><t{preserve}>{escape(value)}</t></is></c>'


def worksheet(rows, widths, freeze=True, autofilter=None):
    max_cols = max((len(row) for row in rows), default=1)
    max_rows = len(rows)
    cols = "".join(
        f'<col min="{i}" max="{i}" width="{width}" customWidth="1"/>'
        for i, width in enumerate(widths, 1)
    )
    data = []
    for r, row in enumerate(rows, 1):
        cells = []
        for c, item in enumerate(row, 1):
            value, style = item if isinstance(item, tuple) else (item, 1)
            cells.append(cell(f"{col_name(c)}{r}", value, style))
        data.append(f'<row r="{r}" spans="1:{max_cols}">{"".join(cells)}</row>')
    pane = '<pane ySplit="1" topLeftCell="A2" activePane="bottomLeft" state="frozen"/>' if freeze else ""
    selection = '<selection pane="bottomLeft" activeCell="A2" sqref="A2"/>' if freeze else ""
    filter_xml = f'<autoFilter ref="{autofilter}"/>' if autofilter else ""
    return (
        '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
        '<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" '
        'xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">'
        f'<dimension ref="A1:{col_name(max_cols)}{max_rows}"/>'
        f'<sheetViews><sheetView workbookViewId="0">{pane}{selection}</sheetView></sheetViews>'
        f'<sheetFormatPr defaultRowHeight="18"/><cols>{cols}</cols><sheetData>{"".join(data)}</sheetData>'
        f'{filter_xml}<pageMargins left="0.7" right="0.7" top="0.75" bottom="0.75" header="0.3" footer="0.3"/>'
        '</worksheet>'
    ).encode()


def endpoint_rows(title, endpoints):
    rows = [[(title, 3), ("Source: DevStage API log (actual recorded requests)", 3), ("Naming: Product=project; Feature=workstream; Portfolio=portfolio", 3), ("", 3), ("", 3), ("", 3)]]
    rows.append([("Method", 5), ("Endpoint template", 5), ("Owner", 5), ("Constants.java", 5), ("Purpose", 5), ("Inputs", 5)])
    rows.extend([[method, endpoint, owner, constant, purpose, inputs] for method, endpoint, owner, constant, purpose, inputs in endpoints])
    return rows


def structure_rows(title, structures):
    rows = [[(title, 3), ("helpers", 3), ("building_blocks", 3), ("tests", 3)]]
    rows.append([("Naming mapping", 4), ("Product = API project", 4), ("Feature = API workstream", 4), ("Portfolio = API portfolio", 4)])
    rows.append([("Module", 5), ("Helper class / methods", 5), ("Building-block class / methods", 5), ("Test class / flows", 5)])
    for module, helper_class, helper_methods, block_class, block_methods, test_class, tests in structures:
        rows.append([(module, 2), (helper_class, 4), (block_class, 4), (test_class, 4)])
        rows.append([("responsibilities", 1), (helper_methods, 1), (block_methods, 1), (tests, 1)])
    return rows


def devsecops_folder_rows():
    return structure_rows("DevSecOps-API Folder", DEV_STRUCTURE)


def inject_sheet(workbook_xml, name, sheet_id, relationship_id):
    tag = f'<sheet name="{escape(name)}" sheetId="{sheet_id}" r:id="{relationship_id}"/>'
    return workbook_xml.replace(b"</sheets>", tag.encode() + b"</sheets>")


def main():
    if not SOURCE.exists():
        raise FileNotFoundError(SOURCE)
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(SOURCE, "r") as source_zip:
        files = {name: source_zip.read(name) for name in source_zip.namelist()}

    files["xl/worksheets/sheet1.xml"] = worksheet(
        endpoint_rows("DevStage Endpoints", DEVSTAGE_ENDPOINTS), [12, 80, 24, 34, 44, 40], autofilter=f"A2:F{len(DEVSTAGE_ENDPOINTS)+2}"
    )
    files["xl/worksheets/sheet3.xml"] = worksheet(devsecops_folder_rows(), [28, 62, 66, 54], freeze=False)
    files["xl/worksheets/sheet4.xml"] = worksheet(
        endpoint_rows("Pro Endpoints", PRO_ENDPOINTS), [12, 88, 24, 36, 46, 42], autofilter=f"A2:F{len(PRO_ENDPOINTS)+2}"
    )
    files["xl/worksheets/sheet5.xml"] = worksheet(
        structure_rows("Pro-API Structure", PRO_STRUCTURE), [30, 66, 72, 56], freeze=False
    )
    files["xl/worksheets/sheet6.xml"] = worksheet(
        structure_rows("DevStage-API Structure", DEV_STRUCTURE), [30, 66, 72, 56], freeze=False
    )

    workbook = files["xl/workbook.xml"]
    workbook = inject_sheet(workbook, "Pro-API Structure", 5, "rId8")
    workbook = inject_sheet(workbook, "DevStage-API Structure", 6, "rId9")
    files["xl/workbook.xml"] = workbook

    rels = files["xl/_rels/workbook.xml.rels"]
    additions = (
        '<Relationship Id="rId8" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet5.xml"/>'
        '<Relationship Id="rId9" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet6.xml"/>'
    ).encode()
    files["xl/_rels/workbook.xml.rels"] = rels.replace(b"</Relationships>", additions + b"</Relationships>")

    types = files["[Content_Types].xml"]
    additions = (
        '<Override PartName="/xl/worksheets/sheet5.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>'
        '<Override PartName="/xl/worksheets/sheet6.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>'
    ).encode()
    files["[Content_Types].xml"] = types.replace(b"</Types>", additions + b"</Types>")

    with zipfile.ZipFile(OUTPUT, "w", zipfile.ZIP_DEFLATED) as output_zip:
        for name, content in files.items():
            output_zip.writestr(name, content)
    print(OUTPUT)


if __name__ == "__main__":
    main()
