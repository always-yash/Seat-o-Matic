package com.lostfound.member2_items.service;

import java.util.ArrayList;
import java.util.List;

public class AIAnalysisService {

    public String analyze(String title, String description, String location) {

        String text = (title + " " + description).toLowerCase();

        List<String> keywords = new ArrayList<>();

        String[] words = {
            "black", "white", "blue", "red", "green", "pink",
            "wallet", "mobile", "laptop", "bag", "watch",
            "keys", "book", "id card", "earbuds", "headphones",
            "leather", "metal", "plastic", "wood"
        };

        for (String word : words) {
            if (text.contains(word)) {
                keywords.add(word);
            }
        }

        if (keywords.isEmpty()) {
            keywords.add("campus");
            keywords.add("item");
        }

        String readiness;

        if (description.length() >= 50 && !location.isBlank()) {
            readiness = "HIGH";
        } else if (description.length() >= 25) {
            readiness = "MEDIUM";
        } else {
            readiness = "LOW";
        }

        return "Suggested Keywords: " + String.join(", ", keywords)
                + " | Item: " + title
                + " | Location: " + location
                + " | Match Readiness: " + readiness;
    }
}