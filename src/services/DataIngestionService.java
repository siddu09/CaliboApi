package services;

import api.DataIngestionApiClient;
import config.Config;
import config.Constants;
import io.restassured.response.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import utils.JsonUtils;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Replays the successful ingestion API flow while replacing all runtime identifiers. */
public final class DataIngestionService {

    private final DataIngestionApiClient apiClient = new DataIngestionApiClient();
    private final Map<String, String> replacements = new LinkedHashMap<>();

    public void runEndToEndIngestion() {
        JSONObject setup = JsonUtils.readJson(Config.testDataPath + Constants.DATA_INGESTION_SETUP_JSON);
        JSONObject payloadData = JsonUtils.readJson(
                Config.testDataPath + setup.get("payloadFile").toString());
        validatePayloadContract(setup, payloadData);
        List<DataIngestionLogParser.Transaction> transactions = DataIngestionLogParser.parse(
                resolveReferenceLog(setup.get("referenceLog").toString()));
        if (transactions.isEmpty()) throw new IllegalStateException("No API transactions found in reference log");

        String suffix = UUID.randomUUID().toString().substring(0, 6);
        String sourceProject = setup.get("sourceProjectName").toString();
        String runtimeProject = setup.get("runtimeProjectPrefix") + suffix;
        String sourceWorkstream = setup.get("sourceWorkstreamName").toString();
        String runtimeWorkstream = setup.get("runtimeWorkstreamPrefix") + suffix;
        String recordedSuffix = sourceRunSuffix(sourceProject);
        replacements.put(sourceProject, runtimeProject);
        replacements.put(sourceProject.toUpperCase(Locale.ROOT), runtimeProject.toUpperCase(Locale.ROOT));
        replacements.put(sourceWorkstream, runtimeWorkstream);
        replacements.put(sourceWorkstream.toUpperCase(Locale.ROOT), runtimeWorkstream.toUpperCase(Locale.ROOT));
        replacements.put(recordedSuffix, suffix);
        replacements.put(recordedSuffix.toUpperCase(Locale.ROOT), suffix.toUpperCase(Locale.ROOT));

        int executed = 0;
        boolean workflowVerified = false;
        boolean retainCreatedResources = Boolean.TRUE.equals(setup.get("retainCreatedResources"));
        for (DataIngestionLogParser.Transaction transaction : transactions) {
            if (transaction.uri().contains("/keycloakadapter/userLogout")) continue;

            if (Boolean.TRUE.equals(setup.get("preserveRecordedTiming"))) {
                waitBeforeTransaction(transaction.delayBeforeMillis());
            }

            String uri = normalizeUri(replace(transaction.uri()));
            String body = replace(transaction.body());

            if (uri.contains("/datapipeline/project/dataflow/v3/status")) {
                if (!workflowVerified) {
                    waitForWorkflowSuccess(setup, uri);
                    workflowVerified = true;
                    executed++;
                }
                continue;
            }

            if (workflowVerified && retainCreatedResources && isCleanupStart(transaction, body)) {
                System.out.println("[DataIngestion] Cleanup disabled; created DPS resources are retained");
                break;
            }

            Response response = apiClient.execute(transaction.method(), uri, body);
            if (response.statusCode() != transaction.expectedStatus()
                    && !isIgnorableCleanupFailure(transaction, response)
                    && !isOptionalDiscoveryFailure(transaction, response)) {
                throw new IllegalStateException("Transaction " + (executed + 1) + " failed: "
                        + transaction.method() + " " + uri + "; expected HTTP "
                        + transaction.expectedStatus() + " but received " + response.statusCode()
                        + ": " + response.asString());
            }
            if (isOptionalDiscoveryFailure(transaction, response)) {
                System.out.println("[DataIngestion] Optional Databricks metadata lookup unavailable; "
                        + "continuing with configured job payload");
                executed++;
                continue;
            }
            learnRuntimeIdentifiers(transaction.expectedResponse(), response.asString());
            executed++;
        }

        if (!workflowVerified) {
            throw new IllegalStateException("DPS workflow status endpoint was not executed");
        }

        System.out.println("[DataIngestion] Replayed " + executed + " API transactions successfully");
        System.out.println("[DataIngestion] Product: "
                + replacements.get(setup.get("sourceProjectName").toString()));
        System.out.println("[DataIngestion] Workstream: "
                + replacements.get(setup.get("sourceWorkstreamName").toString()));
    }

    private void validatePayloadContract(JSONObject setup, JSONObject payloadData) {
        JSONObject crawler = (JSONObject) payloadData.get("crawlerInPutJson");
        if (crawler == null
                || !setup.get("sourceType").equals(crawler.get("sourceType"))
                || !setup.get("sourceSubType").equals(crawler.get("subType"))) {
            throw new IllegalStateException("Crawler payload does not match DataIngestionSetup source contract");
        }
        if (payloadData.get("MSSQLConfigureInput") == null
                || payloadData.get("configureInputSnowflakeDataLake") == null
                || payloadData.get("databricksTemplateJobInput") == null
                || payloadData.get("dataCrawlerDetailsExpected") == null) {
            throw new IllegalStateException("DPL_MSSQLCatalogDbSf.json is missing required payload sections");
        }
    }

    private Path resolveReferenceLog(String configuredPath) {
        Path path = Path.of(configuredPath);
        if (path.isAbsolute() || path.toFile().exists()) return path;
        Path parentPath = Path.of("..").resolve(path).normalize();
        if (parentPath.toFile().exists()) return parentPath;
        throw new IllegalStateException("Reference log not found: " + configuredPath);
    }

    private void waitBeforeTransaction(long delayMillis) {
        if (delayMillis <= 0) return;
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Data-ingestion replay interrupted", exception);
        }
    }

    private String replace(String value) {
        if (value == null) return null;
        String replaced = value;
        List<Map.Entry<String, String>> entries = replacements.entrySet().stream()
                .sorted((left, right) -> Integer.compare(right.getKey().length(), left.getKey().length()))
                .toList();
        for (int index = 0; index < entries.size(); index++) {
            replaced = replaced.replace(entries.get(index).getKey(), "__DPS_VALUE_" + index + "__");
        }
        for (int index = 0; index < entries.size(); index++) {
            replaced = replaced.replace("__DPS_VALUE_" + index + "__", entries.get(index).getValue());
        }
        return replaced;
    }

    private String normalizeUri(String uri) {
        if (uri == null) return null;
        return normalizeQueryIds(uri.replace("/datapipeline/catalogs//", "/datapipeline/catalogs/"));
    }

    private String normalizeQueryIds(String uri) {
        Matcher parameters = Pattern.compile("([?&][A-Za-z]+Id=)([^&]+)").matcher(uri);
        StringBuffer normalized = new StringBuffer();
        while (parameters.find()) {
            String value = parameters.group(2);
            if (value.length() > 36) {
                Pattern uuid = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
                String last = null;
                for (int index = 0; index < value.length(); index++) {
                    Matcher candidate = uuid.matcher(value).region(index, value.length());
                    if (candidate.lookingAt()) last = candidate.group();
                }
                if (last != null) value = last;
            }
            parameters.appendReplacement(normalized, Matcher.quoteReplacement(parameters.group(1) + value));
        }
        parameters.appendTail(normalized);
        return normalized.toString();
    }

    private String sourceRunSuffix(String sourceProjectName) {
        int separator = sourceProjectName.lastIndexOf('_');
        if (separator < 0 || separator == sourceProjectName.length() - 1) {
            throw new IllegalStateException("sourceProjectName must end with a reusable run suffix");
        }
        return sourceProjectName.substring(separator + 1);
    }

    private boolean isIgnorableCleanupFailure(DataIngestionLogParser.Transaction transaction,
                                               Response response) {
        return "DELETE".equals(transaction.method())
                && transaction.uri().startsWith("/databricks/v1/jobs/")
                && response.statusCode() == 400
                && response.asString().contains("does not exist");
    }

    private boolean isOptionalDiscoveryFailure(DataIngestionLogParser.Transaction transaction,
                                               Response response) {
        return (("GET".equals(transaction.method())
                && transaction.uri().startsWith("/databricks/v1/template?"))
                || ("POST".equals(transaction.method())
                && transaction.uri().startsWith("/databricks/config/clusterWhlMapping")))
                && response.statusCode() == 502;
    }

    private boolean isCleanupStart(DataIngestionLogParser.Transaction transaction, String body) {
        return "PUT".equals(transaction.method())
                && transaction.uri().contains("/datapipeline/project/dataflow/v2/draft/status")
                && body != null
                && body.contains("\"status\": \"EDITING\"");
    }

    private void waitForWorkflowSuccess(JSONObject setup, String statusUri) {
        String expected = setup.get("workflowSuccessStatus").toString();
        int maxAttempts = ((Number) setup.get("workflowMaxAttempts")).intValue();
        int pollSeconds = ((Number) setup.get("workflowPollSeconds")).intValue();
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Response response = apiClient.execute("GET", statusUri, null);
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Workflow status request failed with HTTP "
                        + response.statusCode() + ": " + response.asString());
            }
            String status = response.jsonPath().getString("workflowStatus");
            System.out.println("[DataIngestion] Workflow status attempt " + attempt + ": " + status);
            if (expected.equalsIgnoreCase(status)) return;
            if ("FAILED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
                throw new IllegalStateException("DPS workflow ended with " + status + ": "
                        + response.asString());
            }
            waitBeforeTransaction(pollSeconds * 1000L);
        }
        throw new IllegalStateException("DPS workflow did not reach " + expected
                + " within " + maxAttempts + " attempts");
    }

    private void learnRuntimeIdentifiers(String expectedBody, String actualBody) {
        if (expectedBody == null || actualBody == null || expectedBody.isBlank() || actualBody.isBlank()) return;
        try {
            JSONParser parser = new JSONParser();
            Object expected = parser.parse(expectedBody);
            Object actual = parser.parse(actualBody);
            align(expected, actual, null);
        } catch (Exception ignored) {
            String expected = expectedBody.trim().replace("\"", "");
            String actual = actualBody.trim().replace("\"", "");
            if ((isUuid(expected) && isUuid(actual))
                    || (isNumericIdentifier(expected) && isNumericIdentifier(actual))) {
                replacements.putIfAbsent(expected, actual);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void align(Object expected, Object actual, String fieldName) {
        if (expected instanceof JSONObject expectedObject && actual instanceof Map<?, ?> actualObject) {
            for (Object keyValue : expectedObject.keySet()) {
                String key = keyValue.toString();
                if (actualObject.containsKey(keyValue)) {
                    align(expectedObject.get(keyValue), actualObject.get(keyValue), key);
                }
            }
        } else if (expected instanceof JSONArray expectedArray && actual instanceof List<?> actualList) {
            for (int index = 0; index < expectedArray.size(); index++) {
                Object expectedItem = expectedArray.get(index);
                Object actualItem = findMatchingItem(expectedItem, actualList);
                if (actualItem == null && index < actualList.size()) actualItem = actualList.get(index);
                if (actualItem != null) align(expectedItem, actualItem, fieldName);
            }
        } else if (expected != null && actual != null && !expected.toString().equals(actual.toString())
                && isIdentifierField(fieldName, expected.toString(), actual.toString())) {
            replacements.putIfAbsent(expected.toString(), actual.toString());
        }
    }

    private Object findMatchingItem(Object expectedItem, List<?> actualItems) {
        if (!(expectedItem instanceof JSONObject expectedObject)) return null;
        String[] stableKeys = {"tableName", "name", "schema", "attributeName", "taskId"};
        for (String key : stableKeys) {
            Object expectedValue = expectedObject.get(key);
            if (expectedValue == null) continue;
            for (Object actualItem : actualItems) {
                if (actualItem instanceof Map<?, ?> actualObject
                        && expectedValue.equals(actualObject.get(key))) {
                    return actualItem;
                }
            }
        }
        return null;
    }

    private boolean isIdentifierField(String fieldName, String expected, String actual) {
        String normalized = fieldName == null ? "" : fieldName.toLowerCase();
        boolean identifierName = normalized.equals("id")
                || normalized.endsWith("id") || normalized.endsWith("ids");
        return (identifierName && (isUuid(expected) || isNumericIdentifier(expected)))
                || (isUuid(expected) && isUuid(actual))
                || (fieldName == null && isNumericIdentifier(expected) && isNumericIdentifier(actual));
    }

    private boolean isNumericIdentifier(String value) {
        return value != null && value.length() >= 8 && value.chars().allMatch(Character::isDigit);
    }

    private boolean isUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
