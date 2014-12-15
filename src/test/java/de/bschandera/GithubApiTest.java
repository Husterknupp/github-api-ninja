package de.bschandera;

import com.google.common.primitives.UnsignedInteger;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class GithubApiTest {

    @Test
    public void testGetPublicRepositories() {
        GithubApi gitHub = new GithubApi(UnsignedInteger.valueOf(5l));
        assertThat(gitHub.getPublicRepositories()).isNotEmpty();
    }

}
