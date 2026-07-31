package com.example.myrajourney.doctor.education;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.example.myrajourney.R;

public class LifestyleActivity extends AppCompatActivity {

    private WebView webView;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lifestyle);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        webView = findViewById(R.id.webView);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Lifestyle Recommendations");
        }

        // Setup WebView
        setupWebView();

        // Load lifestyle content
        loadLifestyleContent();
    }

    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setWebViewClient(new WebViewClient());

        // Enable responsive design
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
    }

    private void loadLifestyleContent() {
        String htmlContent = generateLifestyleContent();
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
    }

    private String generateLifestyleContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <style>\n");
        html.append("        body { font-family: 'Roboto', sans-serif; line-height: 1.6; margin: 0; padding: 20px; background-color: #f8f9fa; color: #333; }\n");
        html.append("        .container { max-width: 100%; margin: 0 auto; background: white; padding: 30px; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }\n");
        html.append("        h1, h2, h3 { color: #2c3e50; margin-top: 30px; margin-bottom: 15px; }\n");
        html.append("        h1 { border-bottom: 3px solid #9b59b6; padding-bottom: 10px; text-align: center; font-size: 28px; }\n");
        html.append("        h2 { color: #34495e; border-left: 4px solid #9b59b6; padding-left: 15px; font-size: 24px; }\n");
        html.append("        h3 { color: #7f8c8d; font-size: 20px; }\n");
        html.append("        .highlight { background-color: #e8f4fd; padding: 15px; border-left: 4px solid #3498db; margin: 20px 0; border-radius: 0 8px 8px 0; }\n");
        html.append("        .success { background-color: #d4edda; padding: 15px; border-left: 4px solid #28a745; margin: 20px 0; border-radius: 0 8px 8px 0; }\n");
        html.append("        ul, ol { margin: 15px 0; padding-left: 25px; }\n");
        html.append("        li { margin: 8px 0; }\n");
        html.append("        .section { margin-bottom: 40px; padding: 20px; background: #f8f9fa; border-radius: 8px; border-left: 4px solid #9b59b6; }\n");
        html.append("        .subsection { margin: 20px 0; padding: 15px; background: white; border-radius: 6px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <h1>ðŸƒâ€â™€ï¸ Lifestyle Recommendations for RA</h1>\n");
        html.append("\n");
        html.append("        <div class=\"highlight\">\n");
        html.append("            <strong>Balance is Key:</strong> Living well with RA means finding the right balance between activity and rest, exercise and recovery.\n");
        html.append("        </div>\n");
        html.append("\n");
        html.append("        <div class=\"section\">\n");
        html.append("            <h2>ðŸ’ª Exercise Guidelines</h2>\n");
        html.append("            <div class=\"subsection\">\n");
        html.append("                <h3>Low-Impact Activities:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li><strong>Swimming:</strong> Excellent for joint mobility without impact</li>\n");
        html.append("                    <li><strong>Walking:</strong> Start slow, build gradually, use supportive shoes</li>\n");
        html.append("                    <li><strong>Cycling:</strong> Stationary bike for controlled movement</li>\n");
        html.append("                    <li><strong>Water Aerobics:</strong> Reduces joint stress while building strength</li>\n");
        html.append("                </ul>\n");
        html.append("\n");
        html.append("                <h3>Strength Training:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li><strong>Light Weights:</strong> 2-3 lb weights, 8-12 repetitions</li>\n");
        html.append("                    <li><strong>Resistance Bands:</strong> Gentle resistance for muscle strengthening</li>\n");
        html.append("                    <li><strong>Bodyweight Exercises:</strong> Wall push-ups, chair stands</li>\n");
        html.append("                    <li><strong>Isometric Exercises:</strong> Static holds to build stability</li>\n");
        html.append("                </ul>\n");
        html.append("\n");
        html.append("                <h3>Flexibility and Balance:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li><strong>Yoga:</strong> Gentle styles like Hatha or Restorative</li>\n");
        html.append("                    <li><strong>Tai Chi:</strong> Slow, flowing movements for balance</li>\n");
        html.append("                    <li><strong>Stretching:</strong> Daily gentle stretches for all major muscle groups</li>\n");
        html.append("                    <li><strong>Pilates:</strong> Modified for joint protection</li>\n");
        html.append("                </ul>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("\n");
        html.append("        <div class=\"section\">\n");
        html.append("            <h2>ðŸ›¡ï¸ Joint Protection Strategies</h2>\n");
        html.append("            <div class=\"subsection\">\n");
        html.append("                <h3>Daily Activities:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li><strong>Use Larger Joints:</strong> Carry items with forearms instead of fingers</li>\n");
        html.append("                    <li><strong>Take Breaks:</strong> Rest every 15-20 minutes during repetitive tasks</li>\n");
        html.append("                    <li><strong>Use Assistive Devices:</strong> Jar openers, reachers, button hooks</li>\n");
        html.append("                    <li><strong>Maintain Good Posture:</strong> Proper alignment reduces joint stress</li>\n");
        html.append("                </ul>\n");
        html.append("\n");
        html.append("                <h3>Energy Conservation:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li><strong>Pace Yourself:</strong> Break large tasks into smaller steps</li>\n");
        html.append("                    <li><strong>Plan Ahead:</strong> Organize your environment for efficiency</li>\n");
        html.append("                    <li><strong>Delegate Tasks:</strong> Ask for help when needed</li>\n");
        html.append("                    <li><strong>Use Tools:</strong> Electric can openers, lightweight cookware</li>\n");
        html.append("                </ul>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("\n");
        html.append("        <div class=\"section\">\n");
        html.append("            <h2>ðŸ˜´ Sleep and Rest</h2>\n");
        html.append("            <div class=\"subsection\">\n");
        html.append("                <h3>Sleep Hygiene:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li><strong>Consistent Schedule:</strong> Go to bed and wake up at the same time</li>\n");
        html.append("                    <li><strong>Sleep Environment:</strong> Cool, dark, quiet room</li>\n");
        html.append("                    <li><strong>Comfortable Bedding:</strong> Supportive mattress and pillows</li>\n");
        html.append("                    <li><strong>Relaxation Techniques:</strong> Deep breathing, meditation before bed</li>\n");
        html.append("                </ul>\n");
        html.append("\n");
        html.append("                <h3>Rest Periods:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li><strong>Power Naps:</strong> 20-30 minute naps if needed</li>\n");
        html.append("                    <li><strong>Scheduled Breaks:</strong> Plan rest periods throughout the day</li>\n");
        html.append("                    <li><strong>Active Recovery:</strong> Gentle movement between rest periods</li>\n");
        html.append("                </ul>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("\n");
        html.append("        <div class=\"success\">\n");
        html.append("            <strong>ðŸ’¡ Remember:</strong> Listen to your body. Exercise should make you feel better, not worse. If you experience increased pain, stop and consult your healthcare provider.\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        return html.toString();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}






