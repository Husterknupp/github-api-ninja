package de.bschandera;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;

import java.io.IOException;
import java.util.Scanner;

public class CommunicationHelper {
    private static final JsonParser PARSER = new JsonParser();
    private static final Token EMPTY_TOKEN = null;
    private static final String CALLBACK_URL = "https://github.com/login/oauth/authorize";

    private final OAuthService oAuthService;
    private final String code;
    private final Token token;
    private final HttpClient httpClient;
    private final int maxApiCalls;
    private int doneApiCalls;

    public CommunicationHelper(int maxApiCalls) {
        this(maxApiCalls, HttpClientBuilder.create().build());
    }

    public CommunicationHelper(int maxApiCalls, HttpClient httpClient) {
        this.maxApiCalls = maxApiCalls;
        this.doneApiCalls = 0;
        this.httpClient = httpClient;
        oAuthService = getoAuthService(readApiKey(), readApiSecret());
        code = generateAuthCode();
        token = generateToken(oAuthService, code);
    }

    public int getMaxApiCalls() {
        return maxApiCalls;
    }

    public int getDoneApiCalls() {
        return doneApiCalls;
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
     * Make REST call against the GitHub API. Parse result to JSON. Request will be signed with a token. So all requests
     * are less restricted.
     *
     * @param uri
     * @return
     */
    public JsonElement getResponseAsJson(String uri) {
        doneApiCalls++;
        OAuthRequest request = new OAuthRequest(Verb.GET, uri);
        oAuthService.signRequest(token, request);
        Response response = request.send();
        return PARSER.parse(response.getBody());
    }

    // TODO document
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

    // TODO document
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

    // TODO document
    private static Token generateToken(OAuthService service, final String code) {
        // I followed that link and found the code in the redirect url https://github.com/login/oauth/authorize?code=...
        Verifier verifier = new Verifier(code);
        // Trade the Request Token and Verfier for the Access Token
        return service.getAccessToken(EMPTY_TOKEN, verifier);
    }

    // TODO document
    public boolean allApiCallsAreComsumed() {
        return getDoneApiCalls() == getMaxApiCalls();
    }

    // TODO document (does a GET)
    public boolean urlIsAvailable(String url) {
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
