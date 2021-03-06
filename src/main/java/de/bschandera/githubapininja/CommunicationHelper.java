package de.bschandera.githubapininja;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.sf.qualitycheck.Check;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.scribe.model.*;

import java.io.IOException;

/**
 * <p>Helps you to do http calls. Does not know anything about GitHub besides the OAuth workflow.</p>
 * <p>Also this class does is providing oAuth mechanisms to sign api calls. This makes it possible to increase
 * the maximal number of calls drastically.</p>
 */
public class CommunicationHelper {
    private static final JsonParser PARSER = new JsonParser();
    private static final String HEADER_X_RATE_REMAINING = "X-RateLimit-Remaining";

    private final HttpClient httpClient;
    private final OAuthHelper oAuthHelper;
    private int apiCallsRemaining;

    public CommunicationHelper() {
        // if no X-RateLimit-Remaining header can be achieved, 50 should be enough
        this(HttpClientBuilder.create().build());
    }

    public CommunicationHelper(HttpClient httpClient) {
        Check.notNull(httpClient, "httpClient");
        this.httpClient = httpClient;
        oAuthHelper = new OAuthHelper();
        apiCallsRemaining = 50; // default number for protected GitHub resources
    }

    /**
     * Make REST call against the GitHub API. Parse result to JSON. Request will be signed with a token. So all requests
     * are less restricted.
     *
     * @param uri
     * @return
     */
    public Optional<JsonElement> getResponseAsJson(String uri) {
        Check.stateIsTrue(hasStillApiCallsLeft(), "Wanted to call the API but no rate limit remaining anymore.");
        return tryGetResponseAsJson(oAuthHelper.getoAuthSignedRequest(uri).send());
    }

    @VisibleForTesting
    Optional<JsonElement> tryGetResponseAsJson(Response response) {
        if (response.isSuccessful()) {
            adjustRateRemaining(response);
            return Optional.of(PARSER.parse(response.getBody()));
        } else {
            System.out.println("Request call was not successful.");
            System.out.println(response.getCode() + " status code");
            System.out.println("Body:\n" + response.getBody());
            System.out.println();
            return Optional.absent();
        }
    }

    private void adjustRateRemaining(Object response) {
        if (response instanceof Response) {
            apiCallsRemaining = Integer.parseInt(((Response) response).getHeader(HEADER_X_RATE_REMAINING));
        } else if (response instanceof HttpResponse) {
            final Header rateLimitHeader = ((HttpResponse) response).getHeaders(HEADER_X_RATE_REMAINING)[0];
            apiCallsRemaining = Integer.parseInt(rateLimitHeader.getValue());
        }
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
        HttpResponse response = callUrlWithoutoAuth(url);
        return response.getStatusLine().getStatusCode() == 200;
    }

    /**
     * @param url
     * @return
     * @throws java.io.IOException as RuntimeException if e.g., this machine is not connected to the internet.
     */
    private HttpResponse callUrlWithoutoAuth(String url) {
        try {
            HttpResponse response = httpClient.execute(new HttpGet(url));
            adjustRateRemaining(response);
            return response;
        } catch (IOException e) {
            System.out.println("I'm facing some connection problems. Are you connected to this internet thingy?");
            throw new RuntimeException(e);
        }
    }
}
