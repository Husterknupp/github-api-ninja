package de.bschandera;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.sf.qualitycheck.Check;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Provide a convenient way to handle the public GitHub REST API v3. Hide away HTTP calls and also the necessary JSON
 * handling. Make data accessible within good looking Java types instead.
 */
public class GitHubApi {
    private static final JsonParser PARSER = new JsonParser();
    private static final String API_GITHUB_COM = "https://api.github.com";
    private static final String REPOSITORIES = "/repositories";

    private final CloseableHttpClient httpClient;
    private final int maxApiCalls;
    private int doneApiCalls;

    public GitHubApi(Integer maxApiCalls) {
        this(maxApiCalls, HttpClientBuilder.create().build());
    }

    public GitHubApi(Integer maxApiCalls, CloseableHttpClient httpClient) {
        Check.notNegative(maxApiCalls, "maxApiCalls");
        this.maxApiCalls = maxApiCalls;
        this.httpClient = httpClient;
        this.doneApiCalls = 0;
    }

    /**
     * Aggregated view on public repositories and their regarding languages. List every {@linkplain de.bschandera.Language}
     * only once. The contained number of bytes reflects the sum of all bytes that are written in this specific
     * language, regarding all repos of {@linkplain #getPublicRepositories()}.
     *
     * @return
     */
    public List<Language> aggregateLanguagesOfPublicRepos() {
        return aggregateLanguagesOfRepos(getPublicRepositories());
    }

    /**
     * Aggregated view on the given repositories and their languages. List every {@linkplain de.bschandera.Language}
     * only once. The contained number of bytes reflects the sum of all bytes that are written in this specific
     * language, regarding all given repos.
     *
     * @return
     */
    public static List<Language> aggregateLanguagesOfRepos(Collection<Repository> repositories) {
        Check.noNullElements(repositories, "repositories");

        Map<String, BigDecimal> bytesPerLanguage = cumulateBytesPerLanguage(repositories);

        List<Language> result = new ArrayList<>();
        for (String languageName : bytesPerLanguage.keySet()) {
            result.add(new Language(languageName, bytesPerLanguage.get(languageName)));
        }
        return result;
    }

    private static Map<String, BigDecimal> cumulateBytesPerLanguage(Collection<Repository> repositories) {
        Map<String, BigDecimal> bytesPerLanguage = initializeWithZeros(repositories);
        for (Repository repository : repositories) {
            for (Language language : repository.getLanguages()) {
                BigDecimal cumulate = bytesPerLanguage.get(language.getName()).add(language.getBytes());
                bytesPerLanguage.put(language.getName(), cumulate);
            }
        }
        return bytesPerLanguage;
    }

    private static Map<String, BigDecimal> initializeWithZeros(Collection<Repository> repositories) {
        Map<String, BigDecimal> bytesPerLanguage = new HashMap<>();
        for (Repository repository : repositories) {
            for (Language language : repository.getLanguages()) {
                bytesPerLanguage.put(language.getName(), BigDecimal.ZERO);
            }
        }
        return bytesPerLanguage;
    }

    /**
     * Ask GitHub API for public repositories. A {@linkplain de.bschandera.Repository} is less detailed view on
     * the data provided by GitHub. Given an api call limit, this method only returns at most limit - 1 repos.
     *
     * @return
     */
    public List<Repository> getPublicRepositories() {
        List<Repository> reposWithoutLanguages = getReposWithoutLanguages(
                getResponseAsJson(API_GITHUB_COM + REPOSITORIES).getAsJsonArray());
        List<Repository> result = new ArrayList<>();
        for (Repository repo : reposWithoutLanguages) {
            if (doneApiCalls == maxApiCalls) {
                break;
            }
            List<Language> languages = getLanguages(getResponseAsJson(repo.getLanguagesURL()).getAsJsonObject());
            repo.setLanguages(languages);
            result.add(repo);
        }
        return result;
    }

    /**
     * @return Status of {@linkplain #API_GITHUB_COM} == 200 ?
     * @throws java.io.IOException when something with the HTTP connection is bad.
     */
    public boolean isAlive() throws IOException {
        CloseableHttpResponse statusResponse = httpClient.execute(new HttpGet(API_GITHUB_COM));
        doneApiCalls++;
        return statusResponse.getStatusLine().getStatusCode() == 200;
    }

    /**
     * Ask GitHub how many calls still can be done with this IP until new calls will be rejected. See X-RateLimit-Remaining
     * header of GitHub api response.
     *
     * @return
     */
    public Integer apiCallsLeft() {
        throw new UnsupportedOperationException();
    }

    public String createSession() {
        throw new UnsupportedOperationException();
    }

    /*
    ==================================

    JSON PARSING STUFF

    ==================================
     */

    /**
     * {@linkplain de.bschandera.Repository}s that only have their name and their language id. No languages are contended, yet.
     * Please use {@linkplain #getLanguages(com.google.gson.JsonObject)} for this task.
     *
     * @param allReposPayload
     * @return
     */
    public static List<Repository> getReposWithoutLanguages(JsonArray allReposPayload) {
        List<Repository> result = new ArrayList<>();
        for (JsonElement repo : allReposPayload.getAsJsonArray()) {
            result.add(new Repository(extractId(repo), extractLanguageURL(repo)));
        }
        return result;
    }

    public static List<Language> getLanguages(JsonObject languagesPayload) {
        List<Language> result = new ArrayList<>();
        for (Map.Entry<String, JsonElement> languageOccurrence : languagesPayload.entrySet()) {
            final String name = languageOccurrence.getKey();
            final BigDecimal bytes = BigDecimal.valueOf(languageOccurrence.getValue().getAsLong());
            result.add(new Language(name, bytes));
        }
        return result;
    }

    private JsonElement getResponseAsJson(String uri) {
        doneApiCalls++;
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

    /**
     * Extract id of a specific repo.
     *
     * @param repoAsJson
     * @return
     */
    private static String extractId(JsonElement repoAsJson) {
        return repoAsJson.getAsJsonObject().getAsJsonPrimitive("id").getAsString();
    }

    private static String extractLanguageURL(JsonElement repoAsJson) {
        return repoAsJson.getAsJsonObject().getAsJsonPrimitive("languages_url").getAsString();
    }

}
