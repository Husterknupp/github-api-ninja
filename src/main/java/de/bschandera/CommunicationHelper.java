package de.bschandera;

import com.google.common.base.Optional;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.sf.qualitycheck.Check;
import org.apache.http.Header;
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
    private static final String HEADER_X_RATE_REMAINING = "X-RateLimit-Remaining";

    private OAuthService oAuthService;
    private String code;
    private Token token;
    private final HttpClient httpClient;
    private int apiCallsRemaining;

    public CommunicationHelper() {
        // if no X-RateLimit-Remaining header can be achieved, 50 should be enough
        this(50, HttpClientBuilder.create().build());
    }

    public CommunicationHelper(int maxApiCalls, HttpClient httpClient) {
        Check.notNull(httpClient, "httpClient");
        this.httpClient = httpClient;
        apiCallsRemaining = maxApiCalls;
    }

    /**
     * Make REST call against the GitHub API. Parse result to JSON. Request will be signed with a token. So all requests
     * are less restricted.
     *
     * @param uri
     * @return
     */
    public Optional<JsonElement> getResponseAsJson(String uri) {
        Check.notNull(uri, "uri");
        Check.stateIsTrue(hasStillApiCallsLeft(), "Wanted to call the API but no rate limit remaining anymore.");

        OAuthRequest request = getoAuthSignedRequest(uri);
        Response response = request.send();
        adjustRateRemaining(response);
        if (response.isSuccessful()) {
            return Optional.of(PARSER.parse(response.getBody()));
        } else {
            System.out.println(uri + " was called");
            System.out.println(response.getCode() + " status code");
            System.out.println("Body:\n" + response.getBody());
            System.out.println();
            return Optional.absent();
        }
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

    private void adjustRateRemaining(Object response) {
        if (response instanceof Response) {
            apiCallsRemaining = Integer.parseInt(((Response) response).getHeader(HEADER_X_RATE_REMAINING));
        } else if (response instanceof HttpResponse) {
            final Header rateLimitHeader = ((HttpResponse) response).getHeaders(HEADER_X_RATE_REMAINING)[0];
            apiCallsRemaining = Integer.parseInt(rateLimitHeader.getValue());
        }
    }

    private static String readApiKey() {
        System.out.println("API key - we need your API key.");
        System.out.print(">>");
        return new Scanner(System.in).nextLine();
    }

    private static String readApiSecret() {
        System.out.println("And your - API secret - surprise!");
        System.out.print(">>");
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
        final String code = new Scanner(System.in).nextLine();
        System.out.println();
        return code;
    }

    private static Token generateToken(OAuthService service, final String code) {
        // I followed that link and found the code in the redirect url https://github.com/login/oauth/authorize?code=...
        Verifier verifier = new Verifier(code);
        // Trade the Request Token and Verfier for the Access Token
        return service.getAccessToken(EMPTY_TOKEN, verifier);
    }

    /**
     * @return {@code true} if and only if there are still calls allowed against the api - according to either the
     * constructor parameter or the {@linkplain #HEADER_X_RATE_REMAINING} header of done requests.
     */
    public boolean hasStillApiCallsLeft() {
        return apiCallsRemaining > 0;
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
        Check.stateIsTrue(hasStillApiCallsLeft(), "Wanted to call the API but no rate limit remaining anymore.");
        try {
            HttpResponse response = httpClient.execute(new HttpGet(url));
            adjustRateRemaining(response);
            return response.getStatusLine().getStatusCode() == 200;
        } catch (IOException e) {
            System.out.println("I'm facing some connection problems. Are you connected to this internet thingy?");
            System.out.println(e);
            System.out.println();
            return false;
        }
    }
}
