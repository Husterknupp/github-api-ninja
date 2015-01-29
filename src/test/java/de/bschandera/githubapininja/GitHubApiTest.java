package de.bschandera.githubapininja;

import de.bschandera.githubapininja.GitHubApi;
import de.bschandera.githubapininja.Language;
import de.bschandera.githubapininja.Repository;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class GitHubApiTest {

    @Test
    public void testAggregateLanguagesOfRepos() {
        BigDecimal _100 = BigDecimal.valueOf(100);
        BigDecimal _200 = BigDecimal.valueOf(200);
        BigDecimal _300 = BigDecimal.valueOf(300);
        BigDecimal _400 = BigDecimal.valueOf(400);
        BigDecimal _600 = BigDecimal.valueOf(600);

        List<Repository> repositories = new ArrayList<>();
        repositories.add(new Repository("id1", Arrays.asList(
                new Language("Java", _100),
                new Language("Scala", _100))));
        assertThat(GitHubApi.aggregateLanguagesOfRepos(repositories)).containsOnly(new Language("Java", _100), new Language("Scala", _100));

        repositories = new ArrayList<>();
        repositories.add(new Repository("id1", Arrays.asList(
                new Language("Java", _100),
                new Language("Scala", _100))));
        repositories.add(new Repository("id2", Arrays.asList(
                new Language("Scala", _100),
                new Language("Haskell", _100))));
        repositories.add(new Repository("id3", Arrays.asList(
                new Language("Haskell", _200),
                new Language("Java", _300),
                new Language("Scala", _400))));
        assertThat(GitHubApi.aggregateLanguagesOfRepos(repositories)).
                containsOnly(new Language("Java", _400), new Language("Scala", _600), new Language("Haskell", _300));
    }

    @Test
    public void testAggregateLanguagesOfReposWithEmptyInput() {
        assertThat(GitHubApi.aggregateLanguagesOfRepos(new ArrayList<Repository>())).isEmpty();
    }

    @Test
    public void testAggregateLanguagesOfRepos_multipleEqualRepos() {
        BigDecimal _100 = BigDecimal.valueOf(100);
        BigDecimal _200 = BigDecimal.valueOf(200);
        List<Repository> repositories = new ArrayList<>();
        repositories.add(new Repository("id1", Arrays.asList(new Language("Java", _100))));
        repositories.add(new Repository("id1", Arrays.asList(new Language("Java", _100))));

        assertThat(GitHubApi.aggregateLanguagesOfRepos(repositories)).containsOnly(new Language("Java", _200));
    }

}
