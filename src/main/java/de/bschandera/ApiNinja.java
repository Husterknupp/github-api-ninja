package de.bschandera;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class ApiNinja {

    private static final BigDecimal _100 = BigDecimal.valueOf(100);
    private static final int SCALE_6 = 6;

    public static void main(String[] args) {

        GitHubApi gitHub;
        if (args.length == 1) {
            gitHub = new GitHubApi(Integer.parseInt(args[0]));
            System.out.println("I'll do at most " + Integer.parseInt(args[0]) + " api calls (" +
                    (Integer.parseInt(args[0]) - 2) + " repos). Promise.");
        } else {
            gitHub = new GitHubApi(12);
            System.out.println("I'll do at most 12 api calls (10 repos). Promise.");
        }
        if (!available(gitHub)) return;
        System.out.println("GitHub's status is all fine. Let the show begin.");
        System.out.println();

        List<Language> languagesOfPublicRepos = gitHub.aggregateLanguagesOfPublicRepos();
        BigDecimal bytesTotal = sumUpBytesTotal(languagesOfPublicRepos);
        System.out.println("bytesTotal: " + bytesTotal);

        String result = formatResult(languagesOfPublicRepos, bytesTotal);
        System.out.println(result);
    }

    private static boolean available(GitHubApi gitHub) {
        try {
            if (!gitHub.isAlive()) {
                System.out.println("GitHub API currently unavailable.");
                System.out.println("Since you are connected to the internet, probably GitHub faces some real problems... Try again later");
                return false;
            }
        } catch (IOException e) {
            System.out.println("GitHub API currently unavailable. Are you connected to this internet thingy?");
            e.printStackTrace(System.err);
            return false;
        }
        return true;
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
        return bytes.divide(bytesTotal, SCALE_6, BigDecimal.ROUND_HALF_UP);
    }

}
