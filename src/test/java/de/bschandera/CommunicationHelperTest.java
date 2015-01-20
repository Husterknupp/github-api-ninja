package de.bschandera;

import org.junit.Ignore;
import org.junit.Test;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CommunicationHelperTest {

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

    @Ignore
    @Test
    public void testGetResponseAsJson_failedMocking() {
        OAuthRequest request = mock(OAuthRequest.class);
        Response response = mock(Response.class);
        /* TODO does not work: request.send(); */
        stub(request.send()).toReturn(response);
        System.out.println(request.send());
    }

    @Test
    public void testHasStillApiCallsLeft() throws Exception {

    }

    @Test
    public void testUrlIsAvailable() throws Exception {

    }
}
