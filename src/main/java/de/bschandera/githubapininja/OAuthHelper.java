package de.bschandera.githubapininja;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.DefaultApi20;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;
import org.scribe.utils.Preconditions;

import java.util.Scanner;

public class OAuthHelper {
    private static final Token EMPTY_TOKEN = null;
    private static final String CALLBACK_URL = "https://github.com/login/oauth/authorize";

    private OAuthService oAuthService;
    private String code;
    private Token token;

    public OAuthRequest getoAuthSignedRequest(String uri) {
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

    private static class GitHubOAuthImpl extends DefaultApi20
    {
        private static final String AUTHORIZE_URL = "https://github.com/login/oauth/authorize?client_id=%s&redirect_uri=%s";
        private static final String SCOPED_AUTHORIZE_URL = AUTHORIZE_URL + "&scope=%s";
        @Override
        public String getAccessTokenEndpoint()
        {
            return "https://github.com/login/oauth/access_token";
        }
        @Override
        public String getAuthorizationUrl(OAuthConfig config)
        {
            Preconditions.checkValidUrl(config.getCallback(), "Must provide a valid url as callback. Github does not support OOB");
            // Append scope if present
            if(config.hasScope())
            {
                return String.format(SCOPED_AUTHORIZE_URL, config.getApiKey(), OAuthEncoder.encode(config.getCallback()), OAuthEncoder.encode(config.getScope()));
            }
            else
            {
                return String.format(AUTHORIZE_URL, config.getApiKey(), OAuthEncoder.encode(config.getCallback()));
            }
        }
    }
}
