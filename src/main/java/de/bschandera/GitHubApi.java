package de.bschandera;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.sf.qualitycheck.Check;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Provide a convenient way to handle the public GitHub REST API v3. Hide away HTTP calls and also the necessary JSON
 * handling. Make data accessible within good looking Java types instead.
 */
public class GitHubApi {
    private static final String URL_API_GITHUB_COM = "https://api.github.com";
    private static final String URL_REPOSITORIES = "/repositories";
    private static final JsonParser PARSER = new JsonParser();
    private static final Token EMPTY_TOKEN = null;
    private static final String CALLBACK_URL = "https://github.com/login/oauth/authorize";
    private static final String JSON_MEMBER_ID = "id";
    private static final String JSON_MEMBER_LANGUAGES_URL = "languages_url";

    private final CloseableHttpClient httpClient;
    private final int maxApiCalls;
    private int doneApiCalls;
    private final OAuthService oAuthService;
    private final Token token;

    /*
    TODO move JSON mapping stuff into separate class
     */

    public GitHubApi(Integer maxApiCalls) {
        this(maxApiCalls, HttpClientBuilder.create().build());
    }

    public GitHubApi(Integer maxApiCalls, CloseableHttpClient httpClient) {
        Check.notNegative(maxApiCalls, "maxApiCalls");
        this.maxApiCalls = maxApiCalls;
        this.httpClient = httpClient;
        this.doneApiCalls = 0;
        oAuthService = getoAuthService(readApiKey(), readApiSecret());
        final String code = generateAuthCode();
        token = generateToken(oAuthService, code);
    }

    private static String readApiKey() {
        System.out.println("API key... we need your API key:");
        return new Scanner(System.in).nextLine();
    }

    private static String readApiSecret() {
        System.out.println("And - tadaaa - your API secret:");
        return new Scanner(System.in).nextLine();
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
                getResponseAsJson(URL_API_GITHUB_COM + URL_REPOSITORIES).getAsJsonArray());
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
     * @return Status of {@linkplain #URL_API_GITHUB_COM} == 200 ?
     * @throws java.lang.RuntimeException when something with the HTTP connection is bad.
     */
    public boolean isAvailable() {
        CloseableHttpResponse statusResponse;
        try {
            statusResponse = httpClient.execute(new HttpGet(URL_API_GITHUB_COM));
        } catch (IOException e) {
            System.out.println("GitHub API currently unavailable. Are you connected to this internet thingy?");
            throw new RuntimeException(e);
        }
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
        // TODO accept plain String
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

     /*
    ==================================

    HTTP COMMUNICATION STUFF

    ==================================
     */

    /**
     * Make REST call against the GitHub API. Parse result to JSON. Request will be signed with a token. So all requests
     * are less restricted.
     *
     * @param uri
     * @return
     */
    private JsonElement getResponseAsJson(String uri) {
        doneApiCalls++;
        OAuthRequest request = new OAuthRequest(Verb.GET, uri);
        oAuthService.signRequest(token, request);
        Response response = request.send();
        return PARSER.parse(response.getBody());
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

    private static String extractLanguageURL(JsonElement repoAsJson) {
        return repoAsJson.getAsJsonObject().getAsJsonPrimitive(JSON_MEMBER_LANGUAGES_URL).getAsString();
    }

    private static OAuthService getoAuthService(String apiKey, String apiSecret) {
        // Replace these with your own api key and secret (found on https://github.com/settings/applications/155857)
        return new ServiceBuilder()
                .provider(GitHubOAuthImpl.class)
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                        // callback as described here https://developer.github.com/v3/oauth/#web-application-flow #1
                .callback(CALLBACK_URL)
                .build();
    }

    private String generateAuthCode() {
        // TODO authorize automatically (required code is provided when calling the authorizationUrl)
        String authorizationUrl = oAuthService.getAuthorizationUrl(EMPTY_TOKEN);
        System.out.println("Got the Authorization URL!");
        System.out.println("Now go and authorize Scribe here:");
        System.out.println(authorizationUrl);
        // I followed that link and found the code in the redirect url https://github.com/login/oauth/authorize?code=cf37d19cec7e91f0de33
        System.out.println("And paste the authorization code here");
        System.out.print(">>");
        return new Scanner(System.in).nextLine();
    }

    private static Token generateToken(OAuthService service, final String code) {
        // I followed that link and found the code in the redirect url https://github.com/login/oauth/authorize?code=...
        Verifier verifier = new Verifier(code);
        // Trade the Request Token and Verfier for the Access Token
        return service.getAccessToken(EMPTY_TOKEN, verifier);
    }

}
