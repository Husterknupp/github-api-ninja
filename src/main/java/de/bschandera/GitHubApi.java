package de.bschandera;

import net.sf.qualitycheck.Check;

import java.math.BigDecimal;
import java.util.*;

import static de.bschandera.ModelFactory.getLanguages;
import static de.bschandera.ModelFactory.getReposWithoutLanguages;

/**
 * Provide a convenient way to handle the public GitHub REST API v3. Hide away HTTP calls and also the necessary JSON
 * handling. Make data accessible within good looking Java types instead.
 */
public class GitHubApi {
    private static final String URL_API_GITHUB_COM = "https://api.github.com";
    private static final String URL_REPOSITORIES = "/repositories";

    private CommunicationHelper communicationHelper;

    public GitHubApi(Integer maxApiCalls) {
        Check.notNegative(maxApiCalls, "maxApiCalls");
        communicationHelper = new CommunicationHelper(maxApiCalls);
    }

    /**
     * Aggregated view on public repositories and their regarding languages. List every {@linkplain de.bschandera.Language}
     * only once. The contained number of bytes reflects the sum of all bytes that are written in this specific
     * language, regarding all repos of {@linkplain #getPublicRepositories()}.
     *
     * @return
     */
    public List<Language> aggregateLanguagesOfPublicRepos() {
        return aggregateLanguagesOfRepos(getPublicRepositories());
    }

    /**
     * Aggregated view on the given repositories and their languages. List every {@linkplain de.bschandera.Language}
     * only once. The contained number of bytes reflects the sum of all bytes that are written in this specific
     * language, regarding all given repos.
     *
     * @return
     */
    public static List<Language> aggregateLanguagesOfRepos(Collection<Repository> repositories) {
        Check.noNullElements(repositories, "repositories");

        Map<String, BigDecimal> bytesPerLanguage = cumulateBytesPerLanguage(repositories);

        List<Language> result = new ArrayList<>();
        for (String languageName : bytesPerLanguage.keySet()) {
            result.add(new Language(languageName, bytesPerLanguage.get(languageName)));
        }
        return result;
    }

    private static Map<String, BigDecimal> cumulateBytesPerLanguage(Collection<Repository> repositories) {
        Map<String, BigDecimal> bytesPerLanguage = initializeWithZeros(repositories);
        for (Repository repository : repositories) {
            for (Language language : repository.getLanguages()) {
                BigDecimal cumulate = bytesPerLanguage.get(language.getName()).add(language.getBytes());
                bytesPerLanguage.put(language.getName(), cumulate);
            }
        }
        return bytesPerLanguage;
    }

    private static Map<String, BigDecimal> initializeWithZeros(Collection<Repository> repositories) {
        Map<String, BigDecimal> bytesPerLanguage = new HashMap<>();
        for (Repository repository : repositories) {
            for (Language language : repository.getLanguages()) {
                bytesPerLanguage.put(language.getName(), BigDecimal.ZERO);
            }
        }
        return bytesPerLanguage;
    }

    /**
     * Ask GitHub API for public repositories. A {@linkplain de.bschandera.Repository} is less detailed view on
     * the data provided by GitHub. Given an api call limit, this method only returns at most limit - 1 repos.
     *
     * @return
     */
    public List<Repository> getPublicRepositories() {
        List<Repository> reposWithoutLanguages = getReposWithoutLanguages(
                // TODO make call easier (i.e., give it into this method as a parameter)
                communicationHelper.getResponseAsJson(URL_API_GITHUB_COM + URL_REPOSITORIES).getAsJsonArray());
        List<Repository> result = new ArrayList<>();
        for (Repository repo : reposWithoutLanguages) {
            if (communicationHelper.allApiCallsAreComsumed()) {
                break;
            }
            List<Language> languages = getLanguages(communicationHelper.getResponseAsJson(repo.getLanguagesURL()).getAsJsonObject());
            repo.setLanguages(languages);
            result.add(repo);
        }
        return result;
    }

    /**
     * @return Status of {@linkplain #URL_API_GITHUB_COM} == 200 ?
     * @throws java.lang.RuntimeException when something with the HTTP connection is bad.
     */
    public boolean isAvailable() {
        return communicationHelper.urlIsAvailable(URL_API_GITHUB_COM);
    }

    /**
     * Ask GitHub how many calls still can be done with this IP until new calls will be rejected. See X-RateLimit-Remaining
     * header of GitHub api response.
     *
     * @return
     */
    public Integer apiCallsLeft() {
        throw new UnsupportedOperationException();
    }

    public String createSession() {
        throw new UnsupportedOperationException();
    }
}
