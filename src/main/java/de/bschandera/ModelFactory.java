package de.bschandera;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides methods to make model objects, e.g., {@linkplain de.bschandera.Language}, {@linkplain de.bschandera.Repository}
 * out of JSON objects.
 */
public class ModelFactory {
    private static final String JSON_MEMBER_ID = "id";
    private static final String JSON_MEMBER_LANGUAGES_URL = "languages_url";

    /**
     * {@linkplain de.bschandera.Repository}s that only have their name and their language id. No languages are contended, yet.
     * Please use {@linkplain #getLanguages(com.google.gson.JsonObject)} for this task.
     *
     * @param allReposPayload
     * @return
     */
    public static List<Repository> getReposWithoutLanguages(JsonArray allReposPayload) {
        // TODO accept plain String
        List<Repository> result = new ArrayList<>();
        for (JsonElement repo : allReposPayload.getAsJsonArray()) {
            result.add(new Repository(extractId(repo), extractLanguageURL(repo)));
        }
        return result;
    }

    /**
     * Extract id of a specific repo.
     *
     * @param repoAsJson
     * @return
     */
    private static String extractId(JsonElement repoAsJson) {
        return repoAsJson.getAsJsonObject().getAsJsonPrimitive(JSON_MEMBER_ID).getAsString();
    }

    // TODO document
    private static String extractLanguageURL(JsonElement repoAsJson) {
        return repoAsJson.getAsJsonObject().getAsJsonPrimitive(JSON_MEMBER_LANGUAGES_URL).getAsString();
    }

    // TODO document
    public static List<Language> getLanguages(JsonObject languagesPayload) {
        List<Language> result = new ArrayList<>();
        for (Map.Entry<String, JsonElement> languageOccurrence : languagesPayload.entrySet()) {
            final String name = languageOccurrence.getKey();
            final BigDecimal bytes = BigDecimal.valueOf(languageOccurrence.getValue().getAsLong());
            result.add(new Language(name, bytes));
        }
        return result;
    }

}
