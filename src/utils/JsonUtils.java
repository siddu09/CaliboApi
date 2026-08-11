package utils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
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

    public static JSONObject readSection(String filePath, String path) {
        Object value = value(readJson(filePath), path);
        if (value instanceof JSONObject object) return object;
        throw new IllegalStateException("JSON section is missing: " + path);
    }

    public static String readString(String filePath, String path) {
        Object value = value(readJson(filePath), path);
        if (value == null || value.toString().isBlank())
            throw new IllegalStateException("JSON value is blank: " + path);
        return value.toString();
    }

    @SuppressWarnings("unchecked")
    public static void update(String filePath, String path, Object value) {
        JSONObject root = readJson(filePath);
        int separator = path.lastIndexOf('.');
        JSONObject parent = separator < 0 ? root
                : (JSONObject) value(root, path.substring(0, separator));
        parent.put(separator < 0 ? path : path.substring(separator + 1), value);
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(root.toJSONString());
            writer.write(System.lineSeparator());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to update JSON file: " + filePath, exception);
        }
    }

    private static Object value(JSONObject root, String path) {
        Object value = root;
        for (String key : path.split("\\.")) {
            if (!(value instanceof JSONObject object) || !object.containsKey(key))
                throw new IllegalStateException("JSON value is missing: " + path);
            value = object.get(key);
        }
        return value;
    }
}
