package de.bschandera;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.sf.qualitycheck.Check;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class GitHubApi {
    private static final JsonParser PARSER = new JsonParser();

    private final CloseableHttpClient httpClient;
    private final int repoLimit;
    private final List<Repository> repositories;

    public GitHubApi(Integer repoLimit) {
        this(repoLimit, HttpClientBuilder.create().build());
    }

    public GitHubApi(Integer repoLimit, CloseableHttpClient httpClient) {
        Check.notNegative(repoLimit, "repoLimit");
        this.repoLimit = repoLimit;
        repositories = new ArrayList<>();
        this.httpClient = httpClient;
    }

    /**
     * Ask GitHub API for the first 100 repositories. A {@linkplain de.bschandera.Repository} is much less detailed view
     * on the data provided from GitHub.
     *
     * @return
     */
    public List<Repository> getPublicRepositories() {
        if (!repositories.isEmpty()) {
            return Lists.newArrayList(repositories);
        }

        final JsonArray repos = getResponseAsJson("https://api.github.com/repositories").getAsJsonArray();

        if (apiCallsAreLimited()) {
            System.out.println("Api calls are limited to " + repoLimit + ".");
            int callsLeft = repoLimit;
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

    private JsonElement getResponseAsJson(String uri) {
        return getResponseAsJson(uri, this.httpClient);
    }

    /**
     * Make REST call against the GitHub API. Parse result to JSON.
     *
     * @param uri
     * @return
     */
    private static JsonElement getResponseAsJson(String uri, HttpClient httpClient) {
        String jsonFromGitHub = null;
        try {
            HttpResponse response = httpClient.execute(new HttpGet(uri));
            jsonFromGitHub = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            System.out.println(e);
        }
        assert jsonFromGitHub != null;
        return PARSER.parse(jsonFromGitHub);
    }

    private boolean apiCallsAreLimited() {
        return repoLimit != 0;
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

    private List<Language> extractLanguages(JsonElement repoAsJson) {
        return extractLanguages(repoAsJson, this.httpClient);
    }

    /**
     * Extract languages of a specific repo.
     *
     * @param repoAsJson
     * @return
     */
    private static List<Language> extractLanguages(JsonElement repoAsJson, HttpClient httpClient) {
        String languagesUrl = repoAsJson.getAsJsonObject().getAsJsonPrimitive("languages_url").getAsString();
        JsonObject languagesAsJson = getResponseAsJson(languagesUrl, httpClient).getAsJsonObject();

        List<Language> result = new ArrayList<>();
        for (Map.Entry<String, JsonElement> languageOccurrence : languagesAsJson.entrySet()) {
            final String name = languageOccurrence.getKey();
            final BigDecimal bytes = BigDecimal.valueOf(languageOccurrence.getValue().getAsLong());
            result.add(new Language(name, bytes));
        }
        return result;
    }

    /**
     * Aggregated view on all public repositories and their regarding languages. Cached repositories are used here. Thus,
     * {@linkplain #getPublicRepositories()} must be called beforehand.
     *
     * @return
     */
    public List<Language> getBytesPerLanguage() {
        return aggregateLanguagesOfRepos(repositories);
    }

    /**
     * Aggregated view on all public repositories and their regarding languages.
     *
     * @return
     */
    public static List<Language> aggregateLanguagesOfRepos(Collection<Repository> repositories) {
        Check.noNullElements(repositories, "repositories");
        Map<String, BigDecimal> bytesPerLanguage = new HashMap<>();
        for (Repository repository : repositories) {
            for (Language language : repository.getLanguages()) {
                bytesPerLanguage.put(language.getName(), BigDecimal.ZERO);
            }
        }
        System.out.println("RUN");
        System.out.println("bytesPerLanguage: " + bytesPerLanguage);

        for (Repository repository : repositories) {
            for (Language language : repository.getLanguages()) {
                BigDecimal cumulate = bytesPerLanguage.get(language.getName()).add(language.getBytes());
                bytesPerLanguage.put(language.getName(), cumulate);
            }
        }
        System.out.println("bytesPerLanguage: " + bytesPerLanguage);

        List<Language> result = new ArrayList<>();
        for (String languageName : bytesPerLanguage.keySet()) {
            result.add(new Language(languageName, bytesPerLanguage.get(languageName)));
        }
        return result;
    }

}
