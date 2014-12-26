package de.bschandera;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class GitHubApiTest {

    private GitHubApi gitHub;

    @Before
    public void setup() {
        gitHub = new GitHubApi(0);
    }

    @Test
    public void testSerializingOfRepos() {
        JsonArray repoPayload = new JsonParser().parse(repoPayload_firstTwoRepos()).getAsJsonArray();
        List<Repository> repositories = GitHubApi.getReposWithoutLanguages(repoPayload);

        assertThat(repositories).hasSize(2);
        assertThat(repositories.get(0).getId()).isEqualTo("1");
        assertThat(repositories.get(0).getLanguages()).hasSize(0);
        assertThat(repositories.get(0).getLanguagesURL()).isEqualTo("https://api.github.com/repos/mojombo/grit/languages");
        assertThat(repositories.get(1).getId()).isEqualTo("26");
        assertThat(repositories.get(1).getLanguages()).hasSize(0);
        assertThat(repositories.get(1).getLanguagesURL()).isEqualTo("https://api.github.com/repos/wycats/merb-core/languages");
    }

    @Test
    public void testGetLanguages() {
        JsonObject languagesPayload = new JsonParser().parse(languagePayload()).getAsJsonObject();
        List<Language> languages = GitHubApi.getLanguages(languagesPayload);

        assertThat(languages).hasSize(2);
        assertThat(languages.get(0).getName()).isEqualTo("JavaScript");
        assertThat(languages.get(0).getBytes()).isEqualTo(BigDecimal.valueOf(8925));
        assertThat(languages.get(1).getName()).isEqualTo("Ruby");
        assertThat(languages.get(1).getBytes()).isEqualTo(BigDecimal.valueOf(948883));
    }

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

    private static String languagePayload() {
        // https://api.github.com/repos/wycats/merb-core/languages (id=26 see below)
        return "{\n" +
                "  \"JavaScript\": 8925,\n" +
                "  \"Ruby\": 948883\n" +
                "}\n";
    }

    private static String repoPayload_firstTwoRepos() {
        // https://api.github.com/repositories
        return "[\n" +
                "  {\n" +
                "    \"id\": 1,\n" +
                "    \"name\": \"grit\",\n" +
                "    \"full_name\": \"mojombo/grit\",\n" +
                "    \"owner\": {\n" +
                "      \"login\": \"mojombo\",\n" +
                "      \"id\": 1,\n" +
                "      \"avatar_url\": \"https://avatars.githubusercontent.com/u/1?v=3\",\n" +
                "      \"gravatar_id\": \"\",\n" +
                "      \"url\": \"https://api.github.com/users/mojombo\",\n" +
                "      \"html_url\": \"https://github.com/mojombo\",\n" +
                "      \"followers_url\": \"https://api.github.com/users/mojombo/followers\",\n" +
                "      \"following_url\": \"https://api.github.com/users/mojombo/following{/other_user}\",\n" +
                "      \"gists_url\": \"https://api.github.com/users/mojombo/gists{/gist_id}\",\n" +
                "      \"starred_url\": \"https://api.github.com/users/mojombo/starred{/owner}{/repo}\",\n" +
                "      \"subscriptions_url\": \"https://api.github.com/users/mojombo/subscriptions\",\n" +
                "      \"organizations_url\": \"https://api.github.com/users/mojombo/orgs\",\n" +
                "      \"repos_url\": \"https://api.github.com/users/mojombo/repos\",\n" +
                "      \"events_url\": \"https://api.github.com/users/mojombo/events{/privacy}\",\n" +
                "      \"received_events_url\": \"https://api.github.com/users/mojombo/received_events\",\n" +
                "      \"type\": \"User\",\n" +
                "      \"site_admin\": false\n" +
                "    },\n" +
                "    \"private\": false,\n" +
                "    \"html_url\": \"https://github.com/mojombo/grit\",\n" +
                "    \"description\": \"**Grit is no longer maintained. Check out libgit2/rugged.** Grit gives you object oriented read/write access to Git repositories via Ruby.\",\n" +
                "    \"fork\": false,\n" +
                "    \"url\": \"https://api.github.com/repos/mojombo/grit\",\n" +
                "    \"forks_url\": \"https://api.github.com/repos/mojombo/grit/forks\",\n" +
                "    \"keys_url\": \"https://api.github.com/repos/mojombo/grit/keys{/key_id}\",\n" +
                "    \"collaborators_url\": \"https://api.github.com/repos/mojombo/grit/collaborators{/collaborator}\",\n" +
                "    \"teams_url\": \"https://api.github.com/repos/mojombo/grit/teams\",\n" +
                "    \"hooks_url\": \"https://api.github.com/repos/mojombo/grit/hooks\",\n" +
                "    \"issue_events_url\": \"https://api.github.com/repos/mojombo/grit/issues/events{/number}\",\n" +
                "    \"events_url\": \"https://api.github.com/repos/mojombo/grit/events\",\n" +
                "    \"assignees_url\": \"https://api.github.com/repos/mojombo/grit/assignees{/user}\",\n" +
                "    \"branches_url\": \"https://api.github.com/repos/mojombo/grit/branches{/branch}\",\n" +
                "    \"tags_url\": \"https://api.github.com/repos/mojombo/grit/tags\",\n" +
                "    \"blobs_url\": \"https://api.github.com/repos/mojombo/grit/git/blobs{/sha}\",\n" +
                "    \"git_tags_url\": \"https://api.github.com/repos/mojombo/grit/git/tags{/sha}\",\n" +
                "    \"git_refs_url\": \"https://api.github.com/repos/mojombo/grit/git/refs{/sha}\",\n" +
                "    \"trees_url\": \"https://api.github.com/repos/mojombo/grit/git/trees{/sha}\",\n" +
                "    \"statuses_url\": \"https://api.github.com/repos/mojombo/grit/statuses/{sha}\",\n" +
                "    \"languages_url\": \"https://api.github.com/repos/mojombo/grit/languages\",\n" +
                "    \"stargazers_url\": \"https://api.github.com/repos/mojombo/grit/stargazers\",\n" +
                "    \"contributors_url\": \"https://api.github.com/repos/mojombo/grit/contributors\",\n" +
                "    \"subscribers_url\": \"https://api.github.com/repos/mojombo/grit/subscribers\",\n" +
                "    \"subscription_url\": \"https://api.github.com/repos/mojombo/grit/subscription\",\n" +
                "    \"commits_url\": \"https://api.github.com/repos/mojombo/grit/commits{/sha}\",\n" +
                "    \"git_commits_url\": \"https://api.github.com/repos/mojombo/grit/git/commits{/sha}\",\n" +
                "    \"comments_url\": \"https://api.github.com/repos/mojombo/grit/comments{/number}\",\n" +
                "    \"issue_comment_url\": \"https://api.github.com/repos/mojombo/grit/issues/comments/{number}\",\n" +
                "    \"contents_url\": \"https://api.github.com/repos/mojombo/grit/contents/{+path}\",\n" +
                "    \"compare_url\": \"https://api.github.com/repos/mojombo/grit/compare/{base}...{head}\",\n" +
                "    \"merges_url\": \"https://api.github.com/repos/mojombo/grit/merges\",\n" +
                "    \"archive_url\": \"https://api.github.com/repos/mojombo/grit/{archive_format}{/ref}\",\n" +
                "    \"downloads_url\": \"https://api.github.com/repos/mojombo/grit/downloads\",\n" +
                "    \"issues_url\": \"https://api.github.com/repos/mojombo/grit/issues{/number}\",\n" +
                "    \"pulls_url\": \"https://api.github.com/repos/mojombo/grit/pulls{/number}\",\n" +
                "    \"milestones_url\": \"https://api.github.com/repos/mojombo/grit/milestones{/number}\",\n" +
                "    \"notifications_url\": \"https://api.github.com/repos/mojombo/grit/notifications{?since,all,participating}\",\n" +
                "    \"labels_url\": \"https://api.github.com/repos/mojombo/grit/labels{/name}\",\n" +
                "    \"releases_url\": \"https://api.github.com/repos/mojombo/grit/releases{/id}\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 26,\n" +
                "    \"name\": \"merb-core\",\n" +
                "    \"full_name\": \"wycats/merb-core\",\n" +
                "    \"owner\": {\n" +
                "      \"login\": \"wycats\",\n" +
                "      \"id\": 4,\n" +
                "      \"avatar_url\": \"https://avatars.githubusercontent.com/u/4?v=3\",\n" +
                "      \"gravatar_id\": \"\",\n" +
                "      \"url\": \"https://api.github.com/users/wycats\",\n" +
                "      \"html_url\": \"https://github.com/wycats\",\n" +
                "      \"followers_url\": \"https://api.github.com/users/wycats/followers\",\n" +
                "      \"following_url\": \"https://api.github.com/users/wycats/following{/other_user}\",\n" +
                "      \"gists_url\": \"https://api.github.com/users/wycats/gists{/gist_id}\",\n" +
                "      \"starred_url\": \"https://api.github.com/users/wycats/starred{/owner}{/repo}\",\n" +
                "      \"subscriptions_url\": \"https://api.github.com/users/wycats/subscriptions\",\n" +
                "      \"organizations_url\": \"https://api.github.com/users/wycats/orgs\",\n" +
                "      \"repos_url\": \"https://api.github.com/users/wycats/repos\",\n" +
                "      \"events_url\": \"https://api.github.com/users/wycats/events{/privacy}\",\n" +
                "      \"received_events_url\": \"https://api.github.com/users/wycats/received_events\",\n" +
                "      \"type\": \"User\",\n" +
                "      \"site_admin\": false\n" +
                "    },\n" +
                "    \"private\": false,\n" +
                "    \"html_url\": \"https://github.com/wycats/merb-core\",\n" +
                "    \"description\": \"Merb Core: All you need. None you don't.\",\n" +
                "    \"fork\": false,\n" +
                "    \"url\": \"https://api.github.com/repos/wycats/merb-core\",\n" +
                "    \"forks_url\": \"https://api.github.com/repos/wycats/merb-core/forks\",\n" +
                "    \"keys_url\": \"https://api.github.com/repos/wycats/merb-core/keys{/key_id}\",\n" +
                "    \"collaborators_url\": \"https://api.github.com/repos/wycats/merb-core/collaborators{/collaborator}\",\n" +
                "    \"teams_url\": \"https://api.github.com/repos/wycats/merb-core/teams\",\n" +
                "    \"hooks_url\": \"https://api.github.com/repos/wycats/merb-core/hooks\",\n" +
                "    \"issue_events_url\": \"https://api.github.com/repos/wycats/merb-core/issues/events{/number}\",\n" +
                "    \"events_url\": \"https://api.github.com/repos/wycats/merb-core/events\",\n" +
                "    \"assignees_url\": \"https://api.github.com/repos/wycats/merb-core/assignees{/user}\",\n" +
                "    \"branches_url\": \"https://api.github.com/repos/wycats/merb-core/branches{/branch}\",\n" +
                "    \"tags_url\": \"https://api.github.com/repos/wycats/merb-core/tags\",\n" +
                "    \"blobs_url\": \"https://api.github.com/repos/wycats/merb-core/git/blobs{/sha}\",\n" +
                "    \"git_tags_url\": \"https://api.github.com/repos/wycats/merb-core/git/tags{/sha}\",\n" +
                "    \"git_refs_url\": \"https://api.github.com/repos/wycats/merb-core/git/refs{/sha}\",\n" +
                "    \"trees_url\": \"https://api.github.com/repos/wycats/merb-core/git/trees{/sha}\",\n" +
                "    \"statuses_url\": \"https://api.github.com/repos/wycats/merb-core/statuses/{sha}\",\n" +
                "    \"languages_url\": \"https://api.github.com/repos/wycats/merb-core/languages\",\n" +
                "    \"stargazers_url\": \"https://api.github.com/repos/wycats/merb-core/stargazers\",\n" +
                "    \"contributors_url\": \"https://api.github.com/repos/wycats/merb-core/contributors\",\n" +
                "    \"subscribers_url\": \"https://api.github.com/repos/wycats/merb-core/subscribers\",\n" +
                "    \"subscription_url\": \"https://api.github.com/repos/wycats/merb-core/subscription\",\n" +
                "    \"commits_url\": \"https://api.github.com/repos/wycats/merb-core/commits{/sha}\",\n" +
                "    \"git_commits_url\": \"https://api.github.com/repos/wycats/merb-core/git/commits{/sha}\",\n" +
                "    \"comments_url\": \"https://api.github.com/repos/wycats/merb-core/comments{/number}\",\n" +
                "    \"issue_comment_url\": \"https://api.github.com/repos/wycats/merb-core/issues/comments/{number}\",\n" +
                "    \"contents_url\": \"https://api.github.com/repos/wycats/merb-core/contents/{+path}\",\n" +
                "    \"compare_url\": \"https://api.github.com/repos/wycats/merb-core/compare/{base}...{head}\",\n" +
                "    \"merges_url\": \"https://api.github.com/repos/wycats/merb-core/merges\",\n" +
                "    \"archive_url\": \"https://api.github.com/repos/wycats/merb-core/{archive_format}{/ref}\",\n" +
                "    \"downloads_url\": \"https://api.github.com/repos/wycats/merb-core/downloads\",\n" +
                "    \"issues_url\": \"https://api.github.com/repos/wycats/merb-core/issues{/number}\",\n" +
                "    \"pulls_url\": \"https://api.github.com/repos/wycats/merb-core/pulls{/number}\",\n" +
                "    \"milestones_url\": \"https://api.github.com/repos/wycats/merb-core/milestones{/number}\",\n" +
                "    \"notifications_url\": \"https://api.github.com/repos/wycats/merb-core/notifications{?since,all,participating}\",\n" +
                "    \"labels_url\": \"https://api.github.com/repos/wycats/merb-core/labels{/name}\",\n" +
                "    \"releases_url\": \"https://api.github.com/repos/wycats/merb-core/releases{/id}\"\n" +
                "  }\n" +
                "]";
    }

}
