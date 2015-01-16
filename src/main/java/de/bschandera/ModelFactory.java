package de.bschandera;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.sf.qualitycheck.Check;

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
     * Parses a payload that holds multiple languages into regarding Language objects. See the example payload.
     * <p>{
     * "Scala": 1222,
     * "Java": 305360
     * }
     * </p>
     *
     * @param languagesPayload
     * @return Immutable list of {@linkplain de.bschandera.Language}.
     */
    public static List<Language> parseLanguages(JsonObject languagesPayload) {
        Check.notNull(languagesPayload, "languagesPayload");
        ImmutableList.Builder result = new ImmutableList.Builder();
        for (Map.Entry<String, JsonElement> languageOccurrence : languagesPayload.entrySet()) {
            final String name = languageOccurrence.getKey();
            final BigDecimal bytes = BigDecimal.valueOf(languageOccurrence.getValue().getAsLong());
            result.add(new Language(name, bytes));
        }
        return result.build();
    }

    /**
     * {@linkplain de.bschandera.Repository}s that only have their name and their language id. No languages are contended, yet.
     * Please use {@linkplain #parseLanguages(com.google.gson.JsonObject)} for this task. See example payload.
     * <p>{
     * "id": 27962218,
     * "name": "github-api-ninja",
     * ...
     * "languages_url": "https://api.github.com/repos/Husterknupp/github-api-ninja/languages",
     * ...}</p>
     *
     * @param allReposPayload
     * @return Repository that does not contain languages.
     */
    public static List<Repository> parseRepos(JsonArray allReposPayload) {
        // TODO accept plain String
        Check.notNull(allReposPayload, "allReposPayload");
        List<Repository> result = new ArrayList<>();
        for (JsonElement repo : allReposPayload.getAsJsonArray()) {
            result.add(new Repository(extractId(repo), extractLanguageURL(repo)));
        }
        return result;
    }

    private static String extractId(JsonElement repoAsJson) {
        return repoAsJson.getAsJsonObject().getAsJsonPrimitive(JSON_MEMBER_ID).getAsString();
    }

    private static String extractLanguageURL(JsonElement repoAsJson) {
        return repoAsJson.getAsJsonObject().getAsJsonPrimitive(JSON_MEMBER_LANGUAGES_URL).getAsString();
    }

}
