package de.bschandera;

import java.math.BigDecimal;
import java.util.List;

public class ApiNinja {

    private static final BigDecimal _100 = BigDecimal.valueOf(100);
    private static final int BIG_DECIMAL_SCALE_6 = 6;

    // TODO be less restrictive: broken api calls make the ninja fail :(
    public static void main(String[] args) {
        int maxApiCalls;
        if (args.length == 1) {
            maxApiCalls = Integer.parseInt(args[0]);
            System.out.println("I'll do at most " + maxApiCalls + " api calls (" + (maxApiCalls - 2) + " repos). Promise.");
        } else {
            maxApiCalls = 12;
            System.out.println("I'll do at most 12 api calls (10 repos). Promise.");
        }

        GitHubApi gitHub = new GitHubApi(maxApiCalls);
        if (gitHub.isAvailable()) {
            System.out.println("GitHub's status is all fine. Let the show begin.");
            System.out.println();
        } else {
            System.out.println("I cannot reach GitHub... Please try again later");
            return;
        }

        List<Language> languagesOfPublicRepos = gitHub.aggregateLanguagesOfPublicRepos();
        BigDecimal bytesTotal = sumUpBytesTotal(languagesOfPublicRepos);
        System.out.println("bytesTotal: " + bytesTotal);

        String result = formatResult(languagesOfPublicRepos, bytesTotal);
        System.out.println(result);
    }

    private static BigDecimal sumUpBytesTotal(List<Language> languagesOfPublicRepos) {
        BigDecimal bytesTotal = BigDecimal.ZERO;
        for (Language language : languagesOfPublicRepos) {
            bytesTotal = bytesTotal.add(language.getBytes());
        }
        return bytesTotal;
    }

    private static String formatResult(List<Language> languagesOfPublicRepos, BigDecimal bytesTotal) {
        StringBuilder result = new StringBuilder();
        for (Language language : languagesOfPublicRepos) {
            final BigDecimal percentage = asPercentage(language.getBytes(), bytesTotal);
            result.append(language.getName()).append(": ").append(percentage.multiply(_100)).append(" %\n");
        }
        return result.toString();
    }

    private static BigDecimal asPercentage(BigDecimal bytes, BigDecimal bytesTotal) {
        return bytes.divide(bytesTotal, BIG_DECIMAL_SCALE_6, BigDecimal.ROUND_HALF_UP);
    }

}
