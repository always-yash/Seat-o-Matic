package com.lostfound.member3_matching.algo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Custom manual implementation of Levenshtein Distance & Text Similarity Algorithm.
 * No external matching libraries used.
 */
public class LevenshteinAlgorithm {

    /**
     * Computes the manual Levenshtein Edit Distance between two character sequences.
     */
    public static int computeDistance(String s1, String s2) {
        if (s1 == null && s2 == null) return 0;
        if (s1 == null) return s2.length();
        if (s2 == null) return s1.length();




        int len1 = s1.length();
        int len2 = s2.length();

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1,       // deletion
                                 dp[i][j - 1] + 1),      // insertion
                        dp[i - 1][j - 1] + cost          // substitution
                );
            }
        }

        return dp[len1][len2];
    }

    /**
     * Normalizes text by trimming, converting to lower case, and collapsing whitespace.
     */
    public static String normalize(String text) {
        if (text == null) return "";
        return text.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    /**
     * Computes raw Levenshtein percentage similarity (0.0 to 100.0).
     */
    public static double computeLevenshteinSimilarity(String text1, String text2) {
        String s1 = normalize(text1);
        String s2 = normalize(text2);

        if (s1.isEmpty() && s2.isEmpty()) return 100.0;
        if (s1.isEmpty() || s2.isEmpty()) return 0.0;

        int maxLength = Math.max(s1.length(), s2.length());
        int distance = computeDistance(s1, s2);

        double similarity = (1.0 - ((double) distance / maxLength)) * 100.0;
        return Math.max(0.0, Math.min(100.0, similarity));
    }

    /**
     * Computes token-based Jaccard word similarity to accurately reward matching keywords
     * (e.g. "Wallet" vs "Black Leather Tommy Hilfiger Wallet").
     */
    public static double computeTokenOverlapSimilarity(String text1, String text2) {
        String s1 = normalize(text1);
        String s2 = normalize(text2);

        if (s1.isEmpty() || s2.isEmpty()) return 0.0;

        Set<String> tokens1 = new HashSet<>(Arrays.asList(s1.split(" ")));
        Set<String> tokens2 = new HashSet<>(Arrays.asList(s2.split(" ")));

        Set<String> intersection = new HashSet<>(tokens1);
        intersection.retainAll(tokens2);

        Set<String> union = new HashSet<>(tokens1);
        union.addAll(tokens2);

        if (union.isEmpty()) return 0.0;

        // If one token set is completely contained in the other (e.g., "wallet" in "black leather wallet")
        if (!intersection.isEmpty()) {
            double containment = (double) intersection.size() / Math.min(tokens1.size(), tokens2.size());
            double jaccard = (double) intersection.size() / union.size();
            return ((containment * 0.70) + (jaccard * 0.30)) * 100.0;
        }

        return 0.0;
    }

    /**
     * Comprehensive text similarity combining normalized Levenshtein and Token overlap.
     * Weights: 60% Token overlap (concept match), 40% character edit distance.
     */
    public static double calculateTextScore(String text1, String text2) {
        if (text1 == null || text2 == null) return 0.0;

        double levSim = computeLevenshteinSimilarity(text1, text2);
        double tokenSim = computeTokenOverlapSimilarity(text1, text2);

        double combined = Math.max(levSim, (tokenSim * 0.65) + (levSim * 0.35));
        return Math.round(combined * 100.0) / 100.0;
    }
}
