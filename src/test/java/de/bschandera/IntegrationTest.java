package de.bschandera;

import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

public class IntegrationTest {

    private static final BigDecimal _100 = BigDecimal.valueOf(100l);

    @Ignore
    @Test
    public void run() {
        List<Language> languages = new GitHubApi(5).aggregateLanguagesOfPublicRepos();

        BigDecimal bytesTotal = BigDecimal.ZERO;
        for (Language language : languages) {
            bytesTotal = bytesTotal.add(language.getBytes());
        }
        System.out.println("bytesTotal: " + bytesTotal);

        StringBuilder result = new StringBuilder();
        for (Language language : languages) {
            final BigDecimal percentage = asPercentage(language.getBytes(), bytesTotal);
            result.append(language.getName()).append(": ").append(percentage.multiply(_100)).append(" %\n");
        }

        System.out.println(result);
    }

    private BigDecimal asPercentage(BigDecimal bytes, BigDecimal bytesTotal) {
        return bytes.divide(bytesTotal, 20, BigDecimal.ROUND_HALF_UP);
    }

}
