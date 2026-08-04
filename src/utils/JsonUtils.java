package utils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

/**
 * Utility for reading JSON files.
 */
public final class JsonUtils {

    private JsonUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Reads a JSON file and returns it as JSONObject.
     * @param filePath Path to the JSON file
     * @return JSONObject parsed from file
     */
    public static JSONObject readJson(String filePath) {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(filePath)) {
            return (JSONObject) parser.parse(reader);
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Failed to read JSON file: " + filePath, e);
        }
    }
}
