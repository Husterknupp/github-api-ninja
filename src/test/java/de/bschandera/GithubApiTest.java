package de.bschandera;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class GithubApiTest {

    @Test
    public void testGetPublicRepositories() {
        GithubApi gitHub = new GithubApi(5);
        assertThat(gitHub.getPublicRepositories()).isNotEmpty();
    }

    @Test
    public void testGetBytesPerLanguage() {
        // TODO test method probably with mock
    }

}
