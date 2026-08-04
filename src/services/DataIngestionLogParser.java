package services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Parses request/response transactions emitted by the legacy MainUtils logger. */
public final class DataIngestionLogParser {

    private static final Pattern INLINE_REQUEST = Pattern.compile(
            "MainUtils: (GET|POST|PUT|PATCH|DELETE) (https://\\S+)");
    private static final Pattern REQUEST_METHOD = Pattern.compile("Request Method => (GET|POST|PUT|PATCH|DELETE)");
    private static final Pattern STATUS = Pattern.compile("Response Status => HTTP/[^ ]+ (\\d{3})");
    private static final Pattern TIMESTAMP = Pattern.compile("^\\d{2} [A-Z][a-z]{2} \\d{4} ");
    private static final DateTimeFormatter LOG_TIME = DateTimeFormatter.ofPattern(
            "dd MMM yyyy HH:mm:ss,SSS", Locale.ENGLISH);

    private DataIngestionLogParser() {
    }

    public static List<Transaction> parse(Path logPath) {
        try {
            return parseLines(Files.readAllLines(logPath));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read ingestion reference log: " + logPath, exception);
        }
    }

    private static List<Transaction> parseLines(List<String> lines) {
        List<Transaction> transactions = new ArrayList<>();
        int index = 0;
        LocalDateTime previousRequestTime = null;
        while (index < lines.size()) {
            RequestStart start = requestStart(lines, index);
            if (start == null) {
                index++;
                continue;
            }

            int statusIndex = findStatus(lines, start.nextIndex());
            if (statusIndex < 0) break;
            int expectedStatus = status(lines.get(statusIndex));
            String body = extractBlock(lines, start.nextIndex(), statusIndex, "Request Body =>");

            int nextRequest = findNextRequest(lines, statusIndex + 1);
            int responseEnd = nextRequest < 0 ? lines.size() : nextRequest;
            String expectedResponse = extractBlock(lines, statusIndex + 1, responseEnd, "Response Body =>");

            long delayBeforeMillis = previousRequestTime == null ? 0L
                    : Math.max(0L, java.time.Duration.between(previousRequestTime, start.time()).toMillis());
            transactions.add(new Transaction(start.method(), relativeUri(start.uri()), body,
                    expectedStatus, expectedResponse, delayBeforeMillis));
            previousRequestTime = start.time();
            index = nextRequest < 0 ? lines.size() : nextRequest;
        }
        return transactions;
    }

    private static RequestStart requestStart(List<String> lines, int index) {
        String line = lines.get(index);
        Matcher inline = INLINE_REQUEST.matcher(line);
        if (inline.find()) return new RequestStart(inline.group(1), inline.group(2), index + 1, time(line));

        Matcher method = REQUEST_METHOD.matcher(line);
        if (method.find()) {
            for (int next = index + 1; next < Math.min(lines.size(), index + 5); next++) {
                int marker = lines.get(next).indexOf("Request URI => ");
                if (marker >= 0) {
                    return new RequestStart(method.group(1),
                            lines.get(next).substring(marker + "Request URI => ".length()).trim(),
                            next + 1, time(line));
                }
            }
        }
        return null;
    }

    private static int findStatus(List<String> lines, int from) {
        for (int index = from; index < lines.size(); index++) {
            if (STATUS.matcher(lines.get(index)).find()) return index;
            if (index > from && requestStart(lines, index) != null) return -1;
        }
        return -1;
    }

    private static int findNextRequest(List<String> lines, int from) {
        for (int index = from; index < lines.size(); index++) {
            if (requestStart(lines, index) != null) return index;
        }
        return -1;
    }

    private static int status(String line) {
        Matcher matcher = STATUS.matcher(line);
        if (!matcher.find()) throw new IllegalStateException("Missing response status: " + line);
        return Integer.parseInt(matcher.group(1));
    }

    private static String extractBlock(List<String> lines, int from, int to, String marker) {
        for (int index = from; index < to; index++) {
            int markerIndex = lines.get(index).indexOf(marker);
            if (markerIndex >= 0) {
                StringBuilder block = new StringBuilder();
                String sameLine = lines.get(index).substring(markerIndex + marker.length()).trim();
                if (!sameLine.isEmpty()) block.append(sameLine);
                for (int value = index + 1; value < to; value++) {
                    if (TIMESTAMP.matcher(lines.get(value)).find()) break;
                    if (!block.isEmpty()) block.append(System.lineSeparator());
                    block.append(lines.get(value));
                }
                return block.toString().trim();
            }
        }
        return null;
    }

    private static String relativeUri(String absoluteUri) {
        int scheme = absoluteUri.indexOf("://");
        if (scheme < 0) return absoluteUri;
        int path = absoluteUri.indexOf('/', scheme + 3);
        return path < 0 ? "/" : absoluteUri.substring(path);
    }

    private static LocalDateTime time(String line) {
        return LocalDateTime.parse(line.substring(0, 24), LOG_TIME);
    }

    private record RequestStart(String method, String uri, int nextIndex, LocalDateTime time) {}

    public record Transaction(String method, String uri, String body,
                              int expectedStatus, String expectedResponse, long delayBeforeMillis) {}
}
