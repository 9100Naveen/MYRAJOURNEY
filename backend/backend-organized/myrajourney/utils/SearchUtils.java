package com.example.myrajourney.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enhanced search utility for all app modules
 */
public class SearchUtils {
    
    /**
     * Generic search interface
     */
    public interface Searchable {
        String getSearchableText();
        boolean matchesQuery(String query);
    }
    
    /**
     * Perform fuzzy search on a list of searchable items
     */
    public static <T extends Searchable> List<T> search(List<T> items, String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(items);
        }
        
        String searchQuery = query.toLowerCase().trim();
        
        return items.stream()
            .filter(item -> item.matchesQuery(searchQuery))
            .collect(Collectors.toList());
    }
    
    /**
     * Search with multiple keywords (AND operation)
     */
    public static <T extends Searchable> List<T> searchWithKeywords(List<T> items, String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(items);
        }
        
        String[] keywords = query.toLowerCase().trim().split("\\s+");
        
        return items.stream()
            .filter(item -> {
                String searchText = item.getSearchableText().toLowerCase();
                for (String keyword : keywords) {
                    if (!searchText.contains(keyword)) {
                        return false;
                    }
                }
                return true;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Search with scoring (relevance-based)
     */
    public static <T extends Searchable> List<T> searchWithScoring(List<T> items, String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(items);
        }
        
        String searchQuery = query.toLowerCase().trim();
        
        return items.stream()
            .filter(item -> item.matchesQuery(searchQuery))
            .sorted((item1, item2) -> {
                int score1 = calculateRelevanceScore(item1.getSearchableText(), searchQuery);
                int score2 = calculateRelevanceScore(item2.getSearchableText(), searchQuery);
                return Integer.compare(score2, score1); // Higher score first
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate relevance score for search results
     */
    private static int calculateRelevanceScore(String text, String query) {
        if (text == null || query == null) return 0;
        
        String lowerText = text.toLowerCase();
        String lowerQuery = query.toLowerCase();
        
        int score = 0;
        
        // Exact match gets highest score
        if (lowerText.equals(lowerQuery)) {
            score += 100;
        }
        
        // Starts with query gets high score
        if (lowerText.startsWith(lowerQuery)) {
            score += 50;
        }
        
        // Contains query gets medium score
        if (lowerText.contains(lowerQuery)) {
            score += 25;
        }
        
        // Word boundary matches get bonus
        String[] words = lowerText.split("\\s+");
        for (String word : words) {
            if (word.startsWith(lowerQuery)) {
                score += 15;
            }
            if (word.contains(lowerQuery)) {
                score += 5;
            }
        }
        
        return score;
    }
    
    /**
     * Highlight search terms in text
     */
    public static String highlightSearchTerms(String text, String query) {
        if (text == null || query == null || query.trim().isEmpty()) {
            return text;
        }
        
        String[] keywords = query.trim().split("\\s+");
        String result = text;
        
        for (String keyword : keywords) {
            if (keyword.length() > 0) {
                // Case-insensitive highlighting
                result = result.replaceAll("(?i)(" + keyword + ")", "<b>$1</b>");
            }
        }
        
        return result;
    }
    
    /**
     * Get search suggestions based on partial input
     */
    public static <T extends Searchable> List<String> getSearchSuggestions(List<T> items, String partialQuery, int maxSuggestions) {
        if (partialQuery == null || partialQuery.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String query = partialQuery.toLowerCase().trim();
        
        return items.stream()
            .map(Searchable::getSearchableText)
            .distinct()
            .filter(text -> text.toLowerCase().startsWith(query))
            .limit(maxSuggestions)
            .collect(Collectors.toList());
    }
    
    /**
     * Check if text matches search query with fuzzy matching
     */
    public static boolean fuzzyMatch(String text, String query) {
        if (text == null || query == null) return false;
        
        String lowerText = text.toLowerCase();
        String lowerQuery = query.toLowerCase();
        
        // Direct contains match
        if (lowerText.contains(lowerQuery)) {
            return true;
        }
        
        // Character-by-character fuzzy matching
        int textIndex = 0;
        int queryIndex = 0;
        
        while (textIndex < lowerText.length() && queryIndex < lowerQuery.length()) {
            if (lowerText.charAt(textIndex) == lowerQuery.charAt(queryIndex)) {
                queryIndex++;
            }
            textIndex++;
        }
        
        return queryIndex == lowerQuery.length();
    }
    
    /**
     * Remove special characters for cleaner search
     */
    public static String cleanSearchText(String text) {
        if (text == null) return "";
        
        return text.replaceAll("[^a-zA-Z0-9\\s]", "").trim();
    }
    
    /**
     * Create search-friendly version of text
     */
    public static String createSearchableText(String... textParts) {
        StringBuilder sb = new StringBuilder();
        
        for (String part : textParts) {
            if (part != null && !part.trim().isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(cleanSearchText(part));
            }
        }
        
        return sb.toString().toLowerCase();
    }
}