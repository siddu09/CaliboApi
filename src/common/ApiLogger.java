package common;

import config.Constants;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/** Reusable request/response file logging for Rest Assured clients. */
public final class ApiLogger {
    private static PrintStream stream;
    private static Path logPath;

    private ApiLogger() {
    }

    public static synchronized void addTo(RequestSpecBuilder builder) {
        if (stream == null) open();
        builder.addFilter(new RequestLoggingFilter(LogDetail.ALL, true, stream, true,
                        Set.of(Constants.AUTHORIZATION, "Cookie", "Set-Cookie")))
                .addFilter(new ResponseLoggingFilter(stream));
    }

    public static Path getLogPath() {
        return logPath;
    }

    public static synchronized void close() {
        if (stream != null) {
            stream.close();
            stream = null;
        }
    }

    private static void open() {
        try {
            Path directory = Path.of("target", "api-logs");
            Files.createDirectories(directory);
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String test = System.getProperty("test", "api-execution").replaceAll("[^A-Za-z0-9_.-]", "_");
            logPath = directory.resolve(test + "-api-" + time + ".log");
            stream = new PrintStream(Files.newOutputStream(logPath), true);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize API logging", e);
        }
    }
}
