#!/usr/bin/env python3
"""Create the two-sheet Pro API workbook requested for first-round review."""

from pathlib import Path
import re
import zipfile

from generate_api_structure_workbook import SOURCE, worksheet


OUTPUT = Path(__file__).resolve().parents[1] / "docs" / "Pro-API-Final-Document.xlsx"


ENDPOINTS = [
    ("POST - Portfolio", "/elab/portfolios", "Create Portfolio (endpoint confirmed by user)"),
    ("GET - Portfolio", "/elab/portfolios?limit={limit}&offset={offset}&sort={sort}&search={portfolioName}", "Verify Portfolio and obtain portfolioId"),
    ("GET - Product", "/keycloakadapter/users/v2/userInfo", "Obtain current user for Product owner payload"),
    ("GET - Product", "/rbac/roles/type/project", "Resolve Product owner role"),
    ("GET - Product", "/configuration/settings/fieldvalues?isUsageRequired=false", "Resolve business group and customer values"),
    ("POST - Product", "/elab/v2/projects", "Create Product (API entity: project)"),
    ("PATCH - Product", "/elab/v2/projects/{projectId}", "Complete/update created Product"),
    ("POST - Feature", "/elab/v2/projects/workstreams", "Create Feature (API entity: workstream)"),
    ("GET - Feature", "/elab/projects/workstreams/getAllWorkstreams/{projectId}", "Verify Feature and obtain workstreamId"),
    ("GET - Feature", "/elab/projects/releases?projectId={projectId}", "Verify/obtain default releaseId"),
]


def endpoint_rows():
    rows = [
        [("pro", 7), ("pro", 2), ("endpoints", 3)],
        [("portfolio-product-feature", 8), ("api", 4), ("Pro", 2)],
        [("Method / Area", 9), ("Endpoint", 5), ("Purpose", 5)],
    ]
    rows.extend([list(item) for item in ENDPOINTS])
    return rows


def structure_rows():
    return [
        # Portfolio - exact Pro-UI three-column skeleton.
        [("pro", 7), ("pro", 2), ("tests", 3)],
        [("portfolio", 8), ("portfolio", 4), ("pro", 2)],
        [("helper", 8), ("building_blocks", 4), ("portfolio", 4)],
        [("PortfolioApiHelper", 9), ("PortfolioApiBuildingBlock", 5), ("PortfolioApiTests", 5)],
        [("getUniquePortfolioName()", 10), ("addNewProductPortfolio()", 1), ("createPortfolioWithMandatoryFields", 1)],
        [("buildMandatoryPortfolioRequest()", 10), ("addPortfolioMandatoryDetails()", 1), ("createPortfolioWithAllFields", 1)],
        [("buildAllFieldsPortfolioRequest()", 10), ("addPortfolioCustomFields()", 1), ("", 1)],
        [("extractPortfolioId()", 10), ("verifyProductPortfolio()", 1), ("", 1)],

        # Product - Product is named project by the API.
        [("pro", 7), ("pro", 2), ("tests", 3)],
        [("product", 8), ("product", 4), ("pro", 2)],
        [("helper", 8), ("building_blocks", 4), ("product", 4)],
        [("ProductApiHelper", 9), ("ProductApiBuildingBlock", 5), ("ProductApiTests", 5)],
        [("getUniqueProductName()", 10), ("addNewProduct()", 1), ("createProductWithMandatoryFields", 1)],
        [("buildMandatoryProductRequest()", 10), ("addProductOverviewDetails()", 1), ("createProductWithAllFields", 1)],
        [("buildAllFieldsProductRequest()", 10), ("addProductCustomFields()", 1), ("", 1)],
        [("resolveOwnerAndFieldValues()", 10), ("updateProduct()", 1), ("", 1)],
        [("extractProjectId()", 10), ("verifyProduct()", 1), ("", 1)],

        # Feature - Feature is named workstream by the API.
        [("pro", 7), ("pro", 2), ("tests", 3)],
        [("feature", 8), ("feature", 4), ("pro", 2)],
        [("helper", 8), ("building_blocks", 4), ("feature", 4)],
        [("FeatureApiHelper", 9), ("FeatureApiBuildingBlock", 5), ("FeatureApiTests", 5)],
        [("getUniqueFeatureName()", 10), ("addNewFeature()", 1), ("createFeatureWithMandatoryFields", 1)],
        [("buildMandatoryFeatureRequest()", 10), ("addFeatureOverview()", 1), ("createFeatureWithAllFields", 1)],
        [("buildAllFieldsFeatureRequest()", 10), ("addFeatureCustomFields()", 1), ("", 1)],
        [("extractWorkstreamId()", 10), ("verifyFeature()", 1), ("", 1)],
        [("extractReleaseId()", 10), ("verifyDefaultRelease()", 1), ("", 1)],

        # Define - preserved from Pro-API Folder Ramining.
        [("pro", 7), ("pro", 2), ("tests", 3)],
        [("design", 8), ("design", 4), ("pro", 2)],
        [("helper", 8), ("building_blocks", 4), ("design", 4)],
        [("DefineApiHelper", 9), ("DefineApiBuildingBlock", 5), ("DefineApiTests", 5)],
        [("createBusinessRequirementWithConfluence()", 10), ("addNewUserStory()", 1), ("createNewUserStoryWithMandatoryFields", 1)],
        [("createBusinessRequirementWithoutConfluence()", 10), ("addNewUserFeedback()", 1), ("createNewUserFeedbackWithMandatoryFields", 1)],
        [("addUserStoryToBusinessRequirement()", 10), ("addNewBusinessRequirement()", 1), ("createNewBusinessRequirementWithMandatoryFields", 1)],
        [("", 10), ("convertUserFeedbackToBusinessRequirement()", 1), ("convertUserFeedbackToBusinessRequirement", 1)],
        [("", 10), ("addComments()", 1), ("", 1)],
        [("", 10), ("addNewConfluencePage()", 1), ("", 1)],
        [("", 10), ("replyToComment()", 1), ("", 1)],
        [("", 10), ("updateUserFeedback()", 1), ("", 1)],
        [("", 10), ("updateBusinessRequirement()", 1), ("", 1)],
        [("", 10), ("deleteUserFeedback()", 1), ("", 1)],
        [("", 10), ("deleteBusinessRequirement()", 1), ("", 1)],

        # Design - preserved from Pro-API Folder Ramining.
        [("pro", 7), ("pro", 2), ("tests", 3)],
        [("design", 8), ("design", 4), ("pro", 2)],
        [("helper", 8), ("building_blocks", 4), ("design", 4)],
        [("DesignApiHelper", 9), ("DesignApiBuildingBlock", 5), ("DesignApiTests", 5)],
        [("buildMandatoryDesignRequest()", 10), ("addNewDesign()", 1), ("createNewDesignWithMandatoryFields", 1)],
        [("buildAllFieldsDesignRequest()", 10), ("addComments()", 1), ("createNewDesignWithAllFields", 1)],
        [("extractDesignId()", 10), ("updateDesign()", 1), ("", 1)],
        [("", 10), ("deleteDesign()", 1), ("", 1)],

        # Release - preserved from Pro-API Folder Ramining.
        [("pro", 7), ("pro", 2), ("tests", 3)],
        [("release", 17), ("release", 17), ("pro", 17)],
        [("helper", 18), ("building_blocks", 19), ("release", 19)],
        [("ReleaseApiHelper", 5), ("ReleaseApiBuildingBlock", 5), ("ReleaseApiTests", 5)],
        [("buildMandatoryReleaseRequest()", 10), ("addNewReleaseTrain()", 1), ("createReleaseWithMandatoryFields", 1)],
        [("buildAllFieldsReleaseRequest()", 10), ("addNewRelease()", 1), ("createReleaseWithAllFields", 1)],
        [("extractReleaseId()", 10), ("updateRelease()", 1), ("", 1)],
        [("extractReleaseTrainId()", 10), ("updateReleaseTrain()", 1), ("", 1)],
        [("", 10), ("deleteRelease()", 1), ("", 1)],
        [("", 10), ("deleteReleaseTrain()", 1), ("", 1)],
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
        '<sheet name="Pro Valid Endpoints" sheetId="1" r:id="rId1"/>'
        '<sheet name="Final Pro API Folder" sheetId="2" r:id="rId2"/>'
        '</sheets>'
    ).encode()
    files["xl/workbook.xml"] = re.sub(br"<sheets>.*?</sheets>", sheets, workbook, flags=re.DOTALL)

    with zipfile.ZipFile(OUTPUT, "w", zipfile.ZIP_DEFLATED) as output_zip:
        for name, content in files.items():
            output_zip.writestr(name, content)
    print(OUTPUT)


if __name__ == "__main__":
    main()
