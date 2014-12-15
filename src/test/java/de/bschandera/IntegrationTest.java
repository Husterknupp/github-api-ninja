package de.bschandera;

import com.google.common.primitives.UnsignedInteger;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class IntegrationTest {

    @Test
    public void run() {
        Map<String, BigDecimal> bytesPerLanguage = new HashMap<>();

        GithubApi api = new GithubApi(UnsignedInteger.valueOf(5l));
        for (Repository repository : api.getPublicRepositories()) {
            for (Language language : repository.getLanguages()) {
                bytesPerLanguage.put(language.getName(), BigDecimal.ZERO);
            }
        }
        System.out.println("RUN");
        System.out.println("bytesPerLanguage: " + bytesPerLanguage);

        for (Repository repository : api.getPublicRepositories()) {
            for (Language language : repository.getLanguages()) {
                BigDecimal cumulate = bytesPerLanguage.get(language.getName()).add(language.getBytes());
                bytesPerLanguage.put(language.getName(), cumulate);
            }
        }
        System.out.println("bytesPerLanguage: " + bytesPerLanguage);

        BigDecimal bytesTotal = BigDecimal.ZERO;
        for (BigDecimal bytes : bytesPerLanguage.values()) {
            bytesTotal = bytesTotal.add(bytes);
        }
        System.out.println("bytesTotal: " + bytesTotal);

        StringBuilder slave = new StringBuilder();
        for (String language : bytesPerLanguage.keySet()) {
            final BigDecimal percentage = bytesPerLanguage.get(language).divide(bytesTotal, 20,
                    BigDecimal.ROUND_HALF_UP);
            slave.append(language + ": " + percentage.multiply(BigDecimal.valueOf(100l)) + " %\n");
        }

        System.out.println(slave);
    }
}
