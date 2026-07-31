package com.example.myrajourney.common.messaging;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Cache for patient context data to improve chatbot response performance
 * Manages memory-efficient storage and retrieval of patient information
 */
public class PatientContextCache {
    
    private Map<String, PatientContext> contextCache;
    private Map<String, Long> cacheTimestamps;
    
    // Cache configuration
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 minutes
    private static final int MAX_CACHE_SIZE = 100; // Maximum number of cached contexts
    
    public PatientContextCache() {
        this.contextCache = new ConcurrentHashMap<>();
        this.cacheTimestamps = new ConcurrentHashMap<>();
    }
    
    /**
     * Store patient context in cache
     */
    public void putContext(String userId, PatientContext context) {
        if (userId == null || context == null) {
            return;
        }
        
        // Clean up expired entries before adding new one
        cleanupExpiredEntries();
        
        // If cache is full, remove oldest entry
        if (contextCache.size() >= MAX_CACHE_SIZE) {
            removeOldestEntry();
        }
        
        contextCache.put(userId, context);
        cacheTimestamps.put(userId, System.currentTimeMillis());
        
        android.util.Log.d("PatientContextCache", "Cached context for user: " + userId);
    }
    
    /**
     * Retrieve patient context from cache
     */
    public PatientContext getContext(String userId) {
        if (userId == null) {
            return null;
        }
        
        PatientContext context = contextCache.get(userId);
        Long timestamp = cacheTimestamps.get(userId);
        
        // Check if context exists and is not expired
        if (context != null && timestamp != null) {
            long age = System.currentTimeMillis() - timestamp;
            
            if (age <= CACHE_DURATION) {
                android.util.Log.d("PatientContextCache", "Retrieved cached context for user: " + userId);
                return context;
            } else {
                // Context is expired, remove it
                removeContext(userId);
                android.util.Log.d("PatientContextCache", "Expired context removed for user: " + userId);
            }
        }
        
        return null;
    }
    
    /**
     * Remove specific user context from cache
     */
    public void removeContext(String userId) {
        if (userId != null) {
            contextCache.remove(userId);
            cacheTimestamps.remove(userId);
            android.util.Log.d("PatientContextCache", "Removed context for user: " + userId);
        }
    }
    
    /**
     * Clear all cached contexts
     */
    public void clearAll() {
        int size = contextCache.size();
        contextCache.clear();
        cacheTimestamps.clear();
        android.util.Log.d("PatientContextCache", "Cleared all cached contexts (" + size + " entries)");
    }
    
    /**
     * Check if context exists in cache (regardless of expiration)
     */
    public boolean hasContext(String userId) {
        return userId != null && contextCache.containsKey(userId);
    }
    
    /**
     * Check if context exists and is valid (not expired)
     */
    public boolean hasValidContext(String userId) {
        if (userId == null) {
            return false;
        }
        
        Long timestamp = cacheTimestamps.get(userId);
        if (timestamp == null) {
            return false;
        }
        
        long age = System.currentTimeMillis() - timestamp;
        return age <= CACHE_DURATION && contextCache.containsKey(userId);
    }
    
    /**
     * Get cache statistics for monitoring
     */
    public CacheStatistics getStatistics() {
        cleanupExpiredEntries(); // Clean up before calculating stats
        
        CacheStatistics stats = new CacheStatistics();
        stats.totalEntries = contextCache.size();
        stats.maxCapacity = MAX_CACHE_SIZE;
        stats.cacheDurationMs = CACHE_DURATION;
        
        // Calculate hit rate (would need to track hits/misses for accurate calculation)
        stats.utilizationPercentage = (double) stats.totalEntries / stats.maxCapacity * 100;
        
        return stats;
    }
    
    /**
     * Update existing context with new data
     */
    public void updateContext(String userId, PatientContext updatedContext) {
        if (userId != null && updatedContext != null && hasContext(userId)) {
            contextCache.put(userId, updatedContext);
            cacheTimestamps.put(userId, System.currentTimeMillis());
            android.util.Log.d("PatientContextCache", "Updated context for user: " + userId);
        }
    }
    
    /**
     * Refresh context timestamp to extend cache life
     */
    public void refreshContext(String userId) {
        if (userId != null && hasContext(userId)) {
            cacheTimestamps.put(userId, System.currentTimeMillis());
            android.util.Log.d("PatientContextCache", "Refreshed context timestamp for user: " + userId);
        }
    }
    
    /**
     * Clean up expired cache entries
     */
    private void cleanupExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        int removedCount = 0;
        
        // Create a copy of keys to avoid concurrent modification
        String[] userIds = cacheTimestamps.keySet().toArray(new String[0]);
        
        for (String userId : userIds) {
            Long timestamp = cacheTimestamps.get(userId);
            if (timestamp != null && (currentTime - timestamp) > CACHE_DURATION) {
                contextCache.remove(userId);
                cacheTimestamps.remove(userId);
                removedCount++;
            }
        }
        
        if (removedCount > 0) {
            android.util.Log.d("PatientContextCache", "Cleaned up " + removedCount + " expired entries");
        }
    }
    
    /**
     * Remove the oldest cache entry to make room for new ones
     */
    private void removeOldestEntry() {
        if (cacheTimestamps.isEmpty()) {
            return;
        }
        
        // Find the oldest entry
        String oldestUserId = null;
        long oldestTimestamp = Long.MAX_VALUE;
        
        for (Map.Entry<String, Long> entry : cacheTimestamps.entrySet()) {
            if (entry.getValue() < oldestTimestamp) {
                oldestTimestamp = entry.getValue();
                oldestUserId = entry.getKey();
            }
        }
        
        if (oldestUserId != null) {
            removeContext(oldestUserId);
            android.util.Log.d("PatientContextCache", "Removed oldest entry for user: " + oldestUserId);
        }
    }
    
    /**
     * Get all cached user IDs (for debugging/monitoring)
     */
    public String[] getCachedUserIds() {
        cleanupExpiredEntries();
        return contextCache.keySet().toArray(new String[0]);
    }
    
    /**
     * Get context age in milliseconds
     */
    public long getContextAge(String userId) {
        if (userId == null) {
            return -1;
        }
        
        Long timestamp = cacheTimestamps.get(userId);
        if (timestamp == null) {
            return -1;
        }
        
        return System.currentTimeMillis() - timestamp;
    }
    
    /**
     * Check if cache needs cleanup (more than 80% full or has expired entries)
     */
    public boolean needsCleanup() {
        if (contextCache.size() > (MAX_CACHE_SIZE * 0.8)) {
            return true;
        }
        
        // Check for expired entries
        long currentTime = System.currentTimeMillis();
        for (Long timestamp : cacheTimestamps.values()) {
            if ((currentTime - timestamp) > CACHE_DURATION) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Perform maintenance on the cache
     */
    public void performMaintenance() {
        android.util.Log.d("PatientContextCache", "Performing cache maintenance...");
        
        int sizeBefore = contextCache.size();
        cleanupExpiredEntries();
        int sizeAfter = contextCache.size();
        
        android.util.Log.d("PatientContextCache", 
            "Cache maintenance completed. Size: " + sizeBefore + " -> " + sizeAfter);
    }
    
    /**
     * Cache statistics for monitoring and optimization
     */
    public static class CacheStatistics {
        public int totalEntries;
        public int maxCapacity;
        public long cacheDurationMs;
        public double utilizationPercentage;
        
        @Override
        public String toString() {
            return String.format(
                "CacheStatistics{entries=%d/%d (%.1f%%), duration=%dms}",
                totalEntries, maxCapacity, utilizationPercentage, cacheDurationMs
            );
        }
    }
}