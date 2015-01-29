package de.bschandera;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Ignore;
import org.junit.Test;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;

import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CommunicationHelperTest {

    public static final int LIMIT_ARBITRARY = 1234;

    @Test
    public void testGetResponseAsJson() throws Exception {
        CommunicationHelper communicationHelper = new CommunicationHelper();
        final Response response = mock(Response.class);
        when(response.isSuccessful()).thenReturn(true);
        when(response.getHeader("X-RateLimit-Remaining")).thenReturn("1");
        when(response.getBody()).thenReturn(
                "{\n" +
                        "  \"Body\": 0123456789\n" +
                        "}\n");

        assertThat(communicationHelper.getResponseAsJson(response).isPresent()).isTrue();
    }

    @Test
    public void testGetResponseAsJsonWhenRequestNotSuccessful() throws Exception {
        CommunicationHelper communicationHelper = new CommunicationHelper();
        final Response response = mock(Response.class);
        when(response.isSuccessful()).thenReturn(false);
        assertThat(communicationHelper.getResponseAsJson(response).isPresent()).isFalse();
    }

    @Test
    public void testHasStillApiCallsLeft() throws Exception {
        CommunicationHelper communicationHelper = new CommunicationHelper(10, HttpClientBuilder.create().build());
        assertThat(communicationHelper.hasStillApiCallsLeft()).isTrue();

        communicationHelper = new CommunicationHelper(0, HttpClientBuilder.create().build());
        assertThat(communicationHelper.hasStillApiCallsLeft()).isFalse();
    }

    @Test
    public void testUrlIsAvailable() throws IOException {
        final HttpClient httpClient = mock(HttpClient.class);

        final HttpResponse response = mock(HttpResponse.class);
        when(httpClient.execute(any(HttpGet.class))).thenReturn(response);

        final Header rateHeader = mock(Header.class);
        when(rateHeader.getValue()).thenReturn("0");
        Header[] headers = new Header[1];
        headers[0] = rateHeader;
        when(response.getHeaders("X-RateLimit-Remaining")).thenReturn(headers);

        final StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(200);
        when(response.getStatusLine()).thenReturn(statusLine);

        CommunicationHelper communicationHelper = new CommunicationHelper(LIMIT_ARBITRARY, httpClient);
        assertThat(communicationHelper.urlIsAvailable("http://url.com")).isTrue(); // TODO this should be a separate test case
        assertThat(communicationHelper.hasStillApiCallsLeft()).isFalse();
    }

    @Test
    public void testUrlIsNotAvailable() throws IOException {
        /*
        TEST CASE IRRELEVANT INFORMATION
         */
        final HttpClient httpClient = mock(HttpClient.class);
        final HttpResponse response = mock(HttpResponse.class);
        when(httpClient.execute(any(HttpGet.class))).thenReturn(response);

        final Header rateHeader = mock(Header.class);
        when(rateHeader.getValue()).thenReturn("0");
        Header[] headers = new Header[1];
        headers[0] = rateHeader;
        when(response.getHeaders("X-RateLimit-Remaining")).thenReturn(headers);

        /*
        TEST CASE RELEVANT INFORMATION
         */
        final StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(500);
        when(response.getStatusLine()).thenReturn(statusLine);

        CommunicationHelper communicationHelper = new CommunicationHelper(LIMIT_ARBITRARY, httpClient);
        assertThat(communicationHelper.urlIsAvailable("http://url.com")).isFalse();
    }

    @Test(expected = RuntimeException.class)
    public void testUrlIsAvailable_brokenConnection() throws IOException {
        final HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.execute(any(HttpGet.class))).thenThrow(new IOException());

        CommunicationHelper communicationHelper = new CommunicationHelper(LIMIT_ARBITRARY, httpClient);
        communicationHelper.urlIsAvailable("http://url.com");
    }

    @Test
    public void testUrlIsAvailable_readsHeaderRateLimit() throws IOException {
        final HttpClient httpClient = mock(HttpClient.class);
        final HttpResponse response = mock(HttpResponse.class);
        when(httpClient.execute(any(HttpGet.class))).thenReturn(response);

        final Header rateHeader = mock(Header.class);
        when(rateHeader.getValue()).thenReturn("0");
        Header[] headers = new Header[1];
        headers[0] = rateHeader;
        when(response.getHeaders("X-RateLimit-Remaining")).thenReturn(headers);

        final StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(200);
        when(response.getStatusLine()).thenReturn(statusLine);

        CommunicationHelper communicationHelper = new CommunicationHelper(LIMIT_ARBITRARY, httpClient);
        communicationHelper.urlIsAvailable("http://url.com");
        assertThat(communicationHelper.hasStillApiCallsLeft()).isFalse();
    }

    @Ignore
    @Test
    public void testGetResponseAsJson_failedMocking() {
        OAuthRequest request = mock(OAuthRequest.class);
        Response response = mock(Response.class);
        /* TODO does not work: request.send(); */
        stub(request.send()).toReturn(response);
        System.out.println(request.send());
    }
}
