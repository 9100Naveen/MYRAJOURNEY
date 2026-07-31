package com.example.myrajourney.core.network;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myrajourney.R;

public class NetworkDiagnosticsActivity extends AppCompatActivity {
    private static final String TAG = "NetworkDiagnostics";
    
    private TextView statusText;
    private TextView detailsText;
    private ProgressBar progressBar;
    private Button runDiagnosticsButton;
    private Button fixConfigButton;
    
    private NetworkDiagnosticsManager diagnosticsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create a simple layout programmatically since we don't have XML layouts defined
        createLayout();
        
        diagnosticsManager = NetworkDiagnosticsManager.getInstance();
        
        runDiagnosticsButton.setOnClickListener(v -> runDiagnostics());
        fixConfigButton.setOnClickListener(v -> attemptConfigFix());
        
        // Run diagnostics automatically on start
        runDiagnostics();
    }
    
    private void createLayout() {
        // Create a simple vertical layout programmatically
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        
        // Title
        TextView title = new TextView(this);
        title.setText("Network Diagnostics");
        title.setTextSize(24);
        title.setPadding(0, 0, 0, 32);
        layout.addView(title);
        
        // Status text
        statusText = new TextView(this);
        statusText.setText("Ready to run diagnostics...");
        statusText.setTextSize(16);
        statusText.setPadding(0, 0, 0, 16);
        layout.addView(statusText);
        
        // Progress bar
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(android.view.View.GONE);
        layout.addView(progressBar);
        
        // Details text
        detailsText = new TextView(this);
        detailsText.setText("");
        detailsText.setTextSize(14);
        detailsText.setPadding(0, 16, 0, 32);
        detailsText.setTypeface(android.graphics.Typeface.MONOSPACE);
        layout.addView(detailsText);
        
        // Run diagnostics button
        runDiagnosticsButton = new Button(this);
        runDiagnosticsButton.setText("Run Diagnostics");
        android.widget.LinearLayout.LayoutParams buttonParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.setMargins(0, 0, 0, 16);
        runDiagnosticsButton.setLayoutParams(buttonParams);
        layout.addView(runDiagnosticsButton);
        
        // Fix config button
        fixConfigButton = new Button(this);
        fixConfigButton.setText("Attempt Auto-Fix");
        fixConfigButton.setLayoutParams(buttonParams);
        fixConfigButton.setVisibility(android.view.View.GONE);
        layout.addView(fixConfigButton);
        
        setContentView(layout);
    }
    
    private void runDiagnostics() {
        statusText.setText("Running network diagnostics...");
        progressBar.setVisibility(android.view.View.VISIBLE);
        runDiagnosticsButton.setEnabled(false);
        fixConfigButton.setVisibility(android.view.View.GONE);
        
        diagnosticsManager.runFullDiagnostics(this, new NetworkDiagnosticsManager.DiagnosticsCallback() {
            @Override
            public void onDiagnosticsComplete(DiagnosticsResult result) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    runDiagnosticsButton.setEnabled(true);
                    
                    if (result.isAllHealthy()) {
                        statusText.setText("✅ All diagnostics passed! Network is healthy.");
                        statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    } else {
                        statusText.setText("❌ Network issues detected");
                        statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        fixConfigButton.setVisibility(android.view.View.VISIBLE);
                    }
                    
                    // Show detailed results
                    StringBuilder details = new StringBuilder();
                    details.append("=== DIAGNOSTIC RESULTS ===\n\n");
                    details.append("API URL: ").append(result.getApiBaseUrl()).append("\n");
                    details.append("Server IP: ").append(result.getCurrentIpAddress()).append("\n");
                    details.append("Server Port: ").append(result.getCurrentPort()).append("\n\n");
                    
                    details.append("Network Available: ").append(result.isNetworkAvailable() ? "✅" : "❌").append("\n");
                    details.append("IP Reachable: ").append(result.isIpReachable() ? "✅" : "❌").append("\n");
                    details.append("Port Open: ").append(result.isPortOpen() ? "✅" : "❌").append("\n");
                    details.append("API Responding: ").append(result.isApiResponding() ? "✅" : "❌").append("\n");
                    
                    if (result.getResponseTime() > 0) {
                        details.append("Response Time: ").append(result.getResponseTime()).append("ms\n");
                    }
                    
                    if (!result.getIssues().isEmpty()) {
                        details.append("\n=== ISSUES FOUND ===\n");
                        for (String issue : result.getIssues()) {
                            details.append("• ").append(issue).append("\n");
                        }
                    }
                    
                    if (result.getRecommendedAction() != null) {
                        details.append("\n=== RECOMMENDED ACTION ===\n");
                        details.append(result.getRecommendedAction()).append("\n");
                    }
                    
                    // Add network info
                    details.append("\n=== NETWORK INFO ===\n");
                    details.append(NetworkUtils.getNetworkInfo(NetworkDiagnosticsActivity.this));
                    
                    detailsText.setText(details.toString());
                });
            }
            
            @Override
            public void onDiagnosticsProgress(String message) {
                runOnUiThread(() -> {
                    statusText.setText(message);
                });
            }
        });
    }
    
    private void attemptConfigFix() {
        Toast.makeText(this, "Attempting to fix configuration...", Toast.LENGTH_SHORT).show();
        
        // Simple fix: check if we can reach the current IP and suggest alternatives
        String currentIp = getString(R.string.api_base_ip);
        
        // Test common development IPs
        String[] testIps = {
            "10.0.2.2",       // Android emulator host (XAMPP)
            "127.0.0.1",      // Localhost
            "192.168.1.100",  // Common home network IP
            "192.168.1.100",  // Common router IP range
            "192.168.0.100"   // Another common range
        };
        
        for (String testIp : testIps) {
            NetworkUtils.isPortOpen(testIp, 8000).thenAccept(isOpen -> {
                if (isOpen) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Found working server at " + testIp + "/backend/public/index.php", Toast.LENGTH_LONG).show();
                        Log.i(TAG, "Working server found at " + testIp + "/backend/public/index.php");
                        
                        // Here you would update the configuration
                        // For now, just show the user what they need to do
                        new android.app.AlertDialog.Builder(this)
                            .setTitle("Configuration Fix Available")
                            .setMessage("Found a working server at " + testIp + "/backend/public/index.php\n\n" +
                                      "Your app should now work correctly!\n\n" +
                                      "To fix the issue, update your network_config.xml file:\n" +
                                      "Change api_base_ip to: " + testIp + "\n" +
                                      "Change api_base_url to: http://" + testIp + "/backend/public/index.php/api/v1/")
                            .setPositiveButton("OK", null)
                            .show();
                    });
                    return;
                }
            });
        }
        
        // If no working server found, show manual fix instructions
        new android.app.AlertDialog.Builder(this)
            .setTitle("Manual Fix Required")
            .setMessage("No working server found automatically.\n\n" +
                      "Please check:\n" +
                      "1. Is your backend server running?\n" +
                      "2. Is it running on port 8000?\n" +
                      "3. Is the IP address in network_config.xml correct?\n" +
                      "4. Are you on the same network as the server?")
            .setPositiveButton("OK", null)
            .show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (diagnosticsManager != null) {
            diagnosticsManager.shutdown();
        }
    }
}