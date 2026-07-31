package com.example.myrajourney.common.messaging;

import android.content.Context;
import android.util.Log;

import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.core.session.SessionManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatBot {
    private static final String TAG = "ChatBot";
    private Context context;
    private SessionManager sessionManager;
    private ApiService apiService;
    private String currentSessionId;

    public ChatBot(Context context) {
        this.context = context;
        this.sessionManager = SessionManager.getInstance(context);
        this.apiService = ApiClient.getApiService(context);
        this.currentSessionId = generateSessionId();
    }

    public interface ResponseCallback {
        void onResponse(String response);
        
        default void onError(String error) {
            onResponse("I apologize, I'm having trouble connecting right now. Please try again.");
        }
    }

    /**
     * Get AI response asynchronously using ChatGPT-like API
     */
    public void getResponseAsync(String userMessage, ResponseCallback callback) {
        Log.d(TAG, "Getting AI response for: " + userMessage);

        // Prepare request data
        Map<String, String> request = new HashMap<>();
        request.put("message", userMessage);
        request.put("session_id", currentSessionId);
        
        // Add user context if available
        String userId = sessionManager.getUserId();
        String userRole = sessionManager.getRole();
        request.put("user_id", userId);
        request.put("user_role", userRole);

        // Make API call to backend ChatGPT service
        Call<ApiResponse<Map<String, Object>>> call = apiService.sendChat(request);
        
        call.enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, 
                                 Response<ApiResponse<Map<String, Object>>> response) {
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> data = response.body().getData();
                    if (data != null && data.containsKey("response")) {
                        String aiResponse = (String) data.get("response");
                        Log.d(TAG, "AI Response received: " + aiResponse.substring(0, Math.min(100, aiResponse.length())));
                        callback.onResponse(aiResponse);
                        return;
                    }
                }
                
                // Fallback to local response if API fails
                Log.w(TAG, "API response failed, using fallback");
                String fallbackResponse = getLocalFallbackResponse(userMessage);
                callback.onResponse(fallbackResponse);
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                Log.e(TAG, "ChatBot API call failed", t);
                
                // Provide fallback response
                String fallbackResponse = getLocalFallbackResponse(userMessage);
                callback.onResponse(fallbackResponse);
            }
        });
    }

    /**
     * Synchronous method for getting response (for backward compatibility)
     */
    public CompletableFuture<String> getResponse(String userMessage) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        getResponseAsync(userMessage, new ResponseCallback() {
            @Override
            public void onResponse(String response) {
                future.complete(response);
            }

            @Override
            public void onError(String error) {
                future.complete(getLocalFallbackResponse(userMessage));
            }
        });
        
        return future;
    }

    /**
     * Local fallback responses when API is unavailable
     */
    private String getLocalFallbackResponse(String userMessage) {
        String message = userMessage.toLowerCase().trim();
        
        // Pain-related responses
        if (message.contains("pain") || message.contains("hurt") || message.contains("ache")) {
            return "I understand you're experiencing pain. Here are some immediate steps:\n\n" +
                   "• Apply heat or cold therapy (15-20 minutes)\n" +
                   "• Take prescribed pain medication if it's time\n" +
                   "• Try gentle stretching or movement\n" +
                   "• Rest the affected joints\n\n" +
                   "If pain is severe (7-10/10) or sudden, contact your doctor immediately.";
        }
        
        // Medication-related responses
        if (message.contains("medication") || message.contains("medicine") || message.contains("pill")) {
            if (message.contains("forgot") || message.contains("missed")) {
                return "For missed RA medications:\n\n" +
                       "• If same day: Take when you remember\n" +
                       "• If next day: Skip and resume normal schedule\n" +
                       "• Never double dose\n\n" +
                       "For methotrexate specifically, if you remember the same day, take it. " +
                       "If it's the next day, skip and take your next scheduled dose.";
            } else {
                return "RA medications work best when taken consistently:\n\n" +
                       "• DMARDs (like methotrexate) prevent joint damage\n" +
                       "• Take exactly as prescribed\n" +
                       "• Don't stop without consulting your doctor\n" +
                       "• Report side effects promptly\n\n" +
                       "Set reminders to help maintain your medication schedule.";
            }
        }
        
        // Fatigue-related responses
        if (message.contains("tired") || message.contains("fatigue") || message.contains("exhausted")) {
            return "RA fatigue is very real and challenging. Here's what can help:\n\n" +
                   "• Take short 20-30 minute naps\n" +
                   "• Pace your activities throughout the day\n" +
                   "• Prioritize essential tasks\n" +
                   "• Stay hydrated and eat regularly\n" +
                   "• Gentle exercise can boost energy\n\n" +
                   "Severe fatigue may indicate active inflammation - discuss with your rheumatologist.";
        }
        
        // Stiffness-related responses
        if (message.contains("stiff") || message.contains("stiffness")) {
            return "Morning stiffness is common with RA. Try these strategies:\n\n" +
                   "• Take a warm shower or bath\n" +
                   "• Do gentle range-of-motion exercises\n" +
                   "• Use heating pads on stiff joints\n" +
                   "• Move slowly and give yourself extra time\n\n" +
                   "If stiffness lasts more than 2 hours, contact your rheumatologist.";
        }
        
        // Flare-related responses
        if (message.contains("flare") || message.contains("worse") || message.contains("worsening")) {
            return "RA flares need prompt attention:\n\n" +
                   "• Rest affected joints immediately\n" +
                   "• Apply ice to hot, swollen joints\n" +
                   "• Take prescribed flare medications\n" +
                   "• Contact your rheumatologist within 24-48 hours\n\n" +
                   "Early flare treatment prevents joint damage. Don't wait it out.";
        }
        
        // Exercise-related responses
        if (message.contains("exercise") || message.contains("workout") || message.contains("activity")) {
            return "Exercise is important for RA management:\n\n" +
                   "• Swimming and water aerobics are excellent\n" +
                   "• Walking, yoga, and tai chi are gentle options\n" +
                   "• Exercise when stiffness improves (mid-morning)\n" +
                   "• Stop if joints become more painful\n\n" +
                   "Consider working with a physical therapist experienced with RA.";
        }
        
        // Diet-related responses
        if (message.contains("diet") || message.contains("food") || message.contains("eat")) {
            return "Anti-inflammatory diet can help with RA:\n\n" +
                   "• Eat fatty fish, leafy greens, berries\n" +
                   "• Use olive oil and include nuts/seeds\n" +
                   "• Limit processed foods and sugar\n" +
                   "• Stay well hydrated\n\n" +
                   "No single food cures RA, but healthy eating patterns can reduce inflammation.";
        }
        
        // Sleep-related responses
        if (message.contains("sleep") || message.contains("insomnia") || message.contains("tired")) {
            return "RA can disrupt sleep. Try these tips:\n\n" +
                   "• Take a warm bath before bed\n" +
                   "• Use supportive pillows for joints\n" +
                   "• Keep room cool and dark\n" +
                   "• Time pain medication for nighttime coverage\n\n" +
                   "If sleep problems persist, discuss with your doctor.";
        }
        
        // Weather-related responses
        if (message.contains("weather") || message.contains("cold") || message.contains("rain")) {
            return "Weather sensitivity is real with RA:\n\n" +
                   "• Layer clothing in cold weather\n" +
                   "• Use heating pads proactively\n" +
                   "• Plan lighter activities on weather-sensitive days\n" +
                   "• Keep joints moving with indoor exercises\n\n" +
                   "Track your symptoms vs. weather patterns to identify triggers.";
        }
        
        // Greeting responses
        if (message.contains("hello") || message.contains("hi") || message.contains("hey")) {
            return "Hello! I'm your RA health assistant. I can help with questions about:\n\n" +
                   "• Pain and symptom management\n" +
                   "• Medication guidance\n" +
                   "• Exercise and lifestyle tips\n" +
                   "• Flare management\n\n" +
                   "What's going on with your RA today?";
        }
        
        // Default response
        return "I'm here to help with your RA management. I can provide specific guidance on:\n\n" +
               "• **Pain relief** - \"I'm having joint pain\"\n" +
               "• **Medications** - \"I forgot my methotrexate\"\n" +
               "• **Symptoms** - \"I'm feeling very tired\"\n" +
               "• **Flares** - \"My joints are more swollen\"\n" +
               "• **Exercise** - \"What activities are safe?\"\n" +
               "• **Diet** - \"What foods help with inflammation?\"\n\n" +
               "What specific aspect of your RA would you like help with today?";
    }

    /**
     * Generate a unique session ID for conversation tracking
     */
    private String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" + sessionManager.getUserId();
    }

    /**
     * Get chat history from backend
     */
    public void getChatHistory(int limit, ResponseCallback callback) {
        Call<ApiResponse<java.util.List<Map<String, String>>>> call = apiService.getChatHistory(limit);
        
        call.enqueue(new Callback<ApiResponse<java.util.List<Map<String, String>>>>() {
            @Override
            public void onResponse(Call<ApiResponse<java.util.List<Map<String, String>>>> call, 
                                 Response<ApiResponse<java.util.List<Map<String, String>>>> response) {
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Process chat history
                    java.util.List<Map<String, String>> history = response.body().getData();
                    StringBuilder historyText = new StringBuilder("Recent conversations:\n\n");
                    
                    for (Map<String, String> chat : history) {
                        historyText.append("You: ").append(chat.get("message")).append("\n");
                        historyText.append("AI: ").append(chat.get("response")).append("\n\n");
                    }
                    
                    callback.onResponse(historyText.toString());
                } else {
                    callback.onError("Unable to retrieve chat history");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<java.util.List<Map<String, String>>>> call, Throwable t) {
                callback.onError("Failed to load chat history: " + t.getMessage());
            }
        });
    }

    /**
     * Check if the AI service is available
     */
    public boolean isAIServiceAvailable() {
        // This could be enhanced to ping the backend service
        return true;
    }

    /**
     * Get information about the current AI provider
     */
    public String getProviderInfo() {
        return "ChatGPT-like AI Assistant specialized in Rheumatoid Arthritis care";
    }
}