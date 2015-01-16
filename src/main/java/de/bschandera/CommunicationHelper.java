package de.bschandera;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.sf.qualitycheck.Check;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;

import java.io.IOException;
import java.util.Scanner;

/**
 * <p>Helps you to do http calls. Does not know anything about GitHub besides the OAuth workflow.</p>
 * <p>Also this class does is providing oAuth mechanisms to sign api calls. This makes it possible to increase
 * the maximal number of calls drastically.</p>
 */
public class CommunicationHelper {
    private static final JsonParser PARSER = new JsonParser();
    private static final Token EMPTY_TOKEN = null;
    private static final String CALLBACK_URL = "https://github.com/login/oauth/authorize";

    private OAuthService oAuthService;
    private String code;
    private Token token;
    private final HttpClient httpClient;
    private final int maxApiCalls; // Todo generate automatically regarding ??-header
    private int doneApiCalls;

    public CommunicationHelper(int maxApiCalls) {
        this(maxApiCalls, HttpClientBuilder.create().build());
    }

    public CommunicationHelper(int maxApiCalls, HttpClient httpClient) {
        Check.notNull(httpClient, "httpClient");
        this.maxApiCalls = maxApiCalls;
        this.doneApiCalls = 0;
        this.httpClient = httpClient;
    }

    /**
     * @return Number of api calls that can be done against the api.
     */
    public int getMaxApiCalls() {
        return maxApiCalls;
    }

    /**
     * @return Number of api calls that are already done within this application run.
     */
    public int getDoneApiCalls() {
        return doneApiCalls;
    }

    /**
     * Make REST call against the GitHub API. Parse result to JSON. Request will be signed with a token. So all requests
     * are less restricted.
     *
     * @param uri
     * @return
     */
    public JsonElement getResponseAsJson(String uri) {
        Check.notNull(uri, "uri");
        doneApiCalls++;
        OAuthRequest request = getoAuthSignedRequest(uri);
        Response response = request.send();
        return PARSER.parse(response.getBody());
    }

    private OAuthRequest getoAuthSignedRequest(String uri) {
        if (!initialized()) {
            init();
        }
        OAuthRequest request = new OAuthRequest(Verb.GET, uri);
        oAuthService.signRequest(token, request);
        return request;
    }

    private boolean initialized() {
        return oAuthService != null && code != null && token != null;
    }

    private void init() {
        oAuthService = getoAuthService(readApiKey(), readApiSecret());
        code = generateAuthCode();
        token = generateToken(oAuthService, code);
    }

    private static String readApiKey() {
        System.out.println("API key - we need your API key. >>");
        return new Scanner(System.in).nextLine();
    }

    private static String readApiSecret() {
        System.out.println("And your - API secret - surprise! >>");
        return new Scanner(System.in).nextLine();
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

    /**
     * @return {@code true} if and only if the same number of api calls is made as is allowed by {@linkplain #getMaxApiCalls()}.
     */
    public boolean allApiCallsAreConsumed() {
        return getDoneApiCalls() == getMaxApiCalls();
    }

    /**
     * <p>Use a http client to execute a {@linkplain org.apache.http.client.methods.HttpGet} towards the given url.</p>
     * <p>In case an {@linkplain java.io.IOException} occurs (e.g., if you are not connected to the internet) the result
     * will be false. The exception's stack trace can be found in {@code java.lang.System.out}.</p>
     *
     * @param url
     * @return true if and only if the url can be requested and the response status code is 200.
     */
    public boolean urlIsAvailable(String url) {
        Check.notNull(url, "url");
        try {
            HttpResponse statusResponse = httpClient.execute(new HttpGet(url));
            doneApiCalls++;
            return statusResponse.getStatusLine().getStatusCode() == 200;
        } catch (IOException e) {
            System.out.println("I'm facing some connection problems. Are you connected to this internet thingy?");
            System.out.println(e);
            return false;
        }
    }
}
