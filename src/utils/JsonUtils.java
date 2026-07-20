package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class for reading and writing JSON files.
 */
public final class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Reads a JSON file as JSONObject.
     *
     * @param filePath JSON file path
     * @return JSONObject
     */
    public static JSONObject readJson(String filePath) {

        try {

            JSONParser parser = new JSONParser();

            return (JSONObject) parser.parse(new FileReader(filePath));

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to read JSON file : " + filePath,
                    e);

        }

    }

    /**
     * Reads a JSON Array file.
     */
    public static JSONArray readJsonArray(String filePath) {

        try {

            JSONParser parser = new JSONParser();

            return (JSONArray) parser.parse(new FileReader(filePath));

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to read JSON Array : " + filePath,
                    e);

        }

    }

    /**
     * Deep copy of JSONObject.
     */
    public static JSONObject clone(JSONObject jsonObject) {

        return readJsonFromString(jsonObject.toJSONString());

    }

    /**
     * Converts String to JSONObject.
     */
    public static JSONObject readJsonFromString(String json) {

        try {

            JSONParser parser = new JSONParser();

            return (JSONObject) parser.parse(json);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to parse JSON string.",
                    e);

        }

    }

    /**
     * Converts any Java Object to JSONObject.
     */
    @SuppressWarnings("unchecked")
    public static JSONObject convert(Object object) {

        return OBJECT_MAPPER.convertValue(
                object,
                JSONObject.class);

    }

    /**
     * Writes JSONObject to file.
     */
    public static void writeJson(String filePath,
                                 JSONObject jsonObject) {

        try {

            Files.writeString(
                    Path.of(filePath),
                    jsonObject.toJSONString());

        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to write JSON : " + filePath,
                    e);

        }

    }

    /**
     * Pretty print JSON.
     */
    public static String pretty(JSONObject jsonObject) {

        try {

            Object object =
                    OBJECT_MAPPER.readValue(
                            jsonObject.toJSONString(),
                            Object.class);

            return OBJECT_MAPPER
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(object);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to pretty print JSON.",
                    e);

        }

    }

}