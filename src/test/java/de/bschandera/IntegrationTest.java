package de.bschandera;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

public class IntegrationTest {

    @Test
    public void run() {
        Map<Language, BigDecimal> bytesPerLanguage = new GithubApi(5).getBytesPerLanguage();

        BigDecimal bytesTotal = BigDecimal.ZERO;
        for (BigDecimal bytes : bytesPerLanguage.values()) {
            bytesTotal = bytesTotal.add(bytes);
        }
        System.out.println("bytesTotal: " + bytesTotal);

        StringBuilder result = new StringBuilder();
        for (Language language : bytesPerLanguage.keySet()) {
            final BigDecimal percentage = asPercentage(bytesPerLanguage.get(language), bytesTotal);
            result.append(language).append(": ").append(percentage.multiply(BigDecimal.valueOf(100l))).append(" %\n");
        }

        System.out.println(result);
    }

    private BigDecimal asPercentage(BigDecimal bytes, BigDecimal bytesTotal) {
        return bytes.divide(bytesTotal, 20, BigDecimal.ROUND_HALF_UP);
    }

}
