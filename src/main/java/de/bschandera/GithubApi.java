package de.bschandera;

import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.sf.qualitycheck.Check;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GithubApi {
    private static final JsonParser PARSER = new JsonParser();
    private static final CloseableHttpClient HTTP_CLIENT = HttpClientBuilder.create().build();

    private final UnsignedInteger repoLimit;
    private final List<Repository> repositories;

    public GithubApi(UnsignedInteger repoLimit) {
        Check.notNull(repoLimit, "repoLimit");
        this.repoLimit = repoLimit;
        repositories = new ArrayList<>();
    }

    public List<Repository> getPublicRepositories() {
        if (!repositories.isEmpty()) {
            return Lists.newArrayList(repositories);
        }

        final JsonArray repos = getResponseAsJson("https://api.github.com/repositories").getAsJsonArray();

        if (apiCallsAreLimited()) {
            System.out.println("Api calls are limited to " + repoLimit + ".");
            int callsLeft = repoLimit.intValue();
            for (JsonElement repoAsJson : repos) {
                if (callsLeft > 0) {
                    callsLeft--;
                    final Repository repo = new Repository(extractId(repoAsJson), extractLanguages(repoAsJson));
                    System.out.println("Found repo: " + repo.getId() + ", " + repo.getLanguages());
                    repositories.add(repo);
                }
            }
        } else {
            System.out.println("Api calls are NOT limited.");
            for (JsonElement repoAsJson : repos) {
                final Repository repo = new Repository(extractId(repoAsJson), extractLanguages(repoAsJson));
                System.out.println("Found repo: " + repo.getId() + ", " + repo.getLanguages());
                repositories.add(repo);
            }
        }

        return Lists.newArrayList(repositories);
    }

    private boolean apiCallsAreLimited() {
        return repoLimit.compareTo(UnsignedInteger.ONE) != 0;
    }

    private static JsonElement getResponseAsJson(String uri) {
        String jsonFromGithub = null;
        try {
            HttpResponse response = HTTP_CLIENT.execute(new HttpGet(uri));
            jsonFromGithub = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            System.out.println(e);
        }
        assert jsonFromGithub != null;
        return PARSER.parse(jsonFromGithub);
    }

    /**
     * Extract id of a specific repo.
     *
     * @param repoAsJson
     * @return
     */
    private static String extractId(JsonElement repoAsJson) {
        return repoAsJson.getAsJsonObject().getAsJsonPrimitive("id").getAsString();
    }

    /**
     * Extract languages of a specific repo.
     *
     * @param repoAsJson
     * @return
     */
    private static List<Language> extractLanguages(JsonElement repoAsJson) {
        String languagesUrl = repoAsJson.getAsJsonObject().getAsJsonPrimitive("languages_url").getAsString();
        JsonObject languagesAsJson = getResponseAsJson(languagesUrl).getAsJsonObject();

        List<Language> result = new ArrayList<>();
        for (Map.Entry<String, JsonElement> languageOccurrence : languagesAsJson.entrySet()) {
            final String name = languageOccurrence.getKey();
            final BigDecimal bytes = BigDecimal.valueOf(languageOccurrence.getValue().getAsLong());
            result.add(new Language(name, bytes));
        }
        return result;
    }
}
