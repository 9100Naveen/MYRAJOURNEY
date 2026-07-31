package com.example.myrajourney.doctor.education;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.example.myrajourney.R;

public class WhatIsRAActivity extends AppCompatActivity {

    private WebView webView;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_what_is_ra);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        webView = findViewById(R.id.webView);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("What is Rheumatoid Arthritis?");
        }

        // Setup WebView
        setupWebView();

        // Load RA information content
        loadRAContent();
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

    private void loadRAContent() {
        String htmlContent = generateRAContent();
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
    }

    private String generateRAContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <style>\n");
        html.append("        body { font-family: 'Roboto', sans-serif; line-height: 1.6; margin: 0; padding: 20px; background-color: #f8f9fa; color: #333; }\n");
        html.append("        .container { max-width: 100%; margin: 0 auto; background: white; padding: 30px; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }\n");
        html.append("        h1, h2, h3 { color: #2c3e50; margin-top: 30px; margin-bottom: 15px; }\n");
        html.append("        h1 { border-bottom: 3px solid #3498db; padding-bottom: 10px; text-align: center; font-size: 28px; }\n");
        html.append("        h2 { color: #34495e; border-left: 4px solid #3498db; padding-left: 15px; font-size: 24px; }\n");
        html.append("        h3 { color: #7f8c8d; font-size: 20px; }\n");
        html.append("        .highlight { background-color: #e8f4fd; padding: 15px; border-left: 4px solid #3498db; margin: 20px 0; border-radius: 0 8px 8px 0; }\n");
        html.append("        ul, ol { margin: 15px 0; padding-left: 25px; }\n");
        html.append("        li { margin: 8px 0; }\n");
        html.append("        .section { margin-bottom: 40px; padding: 20px; background: #f8f9fa; border-radius: 8px; border-left: 4px solid #3498db; }\n");
        html.append("        .subsection { margin: 20px 0; padding: 15px; background: white; border-radius: 6px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <h1>ðŸ“š Understanding Rheumatoid Arthritis</h1>\n");
        html.append("\n");
        html.append("        <div class=\"highlight\">\n");
        html.append("            <strong>Comprehensive Guide to Rheumatoid Arthritis (RA)</strong>\n");
        html.append("            Learn everything you need to know about this chronic autoimmune condition.\n");
        html.append("        </div>\n");
        html.append("\n");
        html.append("        <div class=\"section\">\n");
        html.append("            <h2>ðŸ”¬ What is Rheumatoid Arthritis?</h2>\n");
        html.append("            <div class=\"subsection\">\n");
        html.append("                <p><strong>Rheumatoid Arthritis (RA)</strong> is a chronic autoimmune disease that primarily affects the joints. Unlike osteoarthritis, which is caused by wear and tear, RA occurs when your immune system mistakenly attacks the lining of your joints, causing inflammation, pain, and potential joint damage.</p>\n");
        html.append("\n");
        html.append("                <h3>Key Characteristics:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li><strong>Autoimmune Disease:</strong> The immune system attacks healthy joint tissue</li>\n");
        html.append("                    <li><strong>Symmetrical:</strong> Usually affects both sides of the body equally</li>\n");
        html.append("                    <li><strong>Progressive:</strong> Can lead to joint deformity if not managed properly</li>\n");
        html.append("                    <li><strong>Systemic:</strong> Can affect other organs beyond just joints</li>\n");
        html.append("                    <li><strong>Chronic:</strong> Requires ongoing management and treatment</li>\n");
        html.append("                </ul>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("\n");
        html.append("        <div class=\"section\">\n");
        html.append("            <h2>ðŸ¦´ Joint Involvement Patterns</h2>\n");
        html.append("            <div class=\"subsection\">\n");
        html.append("                <p>RA typically affects smaller joints first, particularly in the hands and feet. Common areas include:</p>\n");
        html.append("                <ul>\n");
        html.append("                    <li>Fingers and knuckles</li>\n");
        html.append("                    <li>Wrists</li>\n");
        html.append("                    <li>Toes and ankles</li>\n");
        html.append("                    <li>Knees</li>\n");
        html.append("                    <li>Shoulders</li>\n");
        html.append("                    <li>Elbows</li>\n");
        html.append("                </ul>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("\n");
        html.append("        <div class=\"section\">\n");
        html.append("            <h2>âš•ï¸ Disease Progression</h2>\n");
        html.append("            <div class=\"subsection\">\n");
        html.append("                <h3>Early Stage:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li>Mild joint pain and stiffness</li>\n");
        html.append("                    <li>Fatigue and general malaise</li>\n");
        html.append("                    <li>Morning stiffness lasting more than 30 minutes</li>\n");
        html.append("                </ul>\n");
        html.append("\n");
        html.append("                <h3>Moderate Stage:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li>Increased joint swelling and pain</li>\n");
        html.append("                    <li>Joint deformity begins</li>\n");
        html.append("                    <li>Reduced mobility and function</li>\n");
        html.append("                </ul>\n");
        html.append("\n");
        html.append("                <h3>Advanced Stage:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li>Severe joint damage and deformity</li>\n");
        html.append("                    <li>Significant functional limitations</li>\n");
        html.append("                    <li>Possible systemic complications</li>\n");
        html.append("                </ul>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("\n");
        html.append("        <div class=\"section\">\n");
        html.append("            <h2>ðŸ” Diagnosis Process</h2>\n");
        html.append("            <div class=\"subsection\">\n");
        html.append("                <h3>Clinical Evaluation:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li>Physical examination of joints</li>\n");
        html.append("                    <li>Assessment of symptoms and duration</li>\n");
        html.append("                    <li>Review of medical history</li>\n");
        html.append("                </ul>\n");
        html.append("\n");
        html.append("                <h3>Laboratory Tests:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li>Rheumatoid factor (RF) test</li>\n");
        html.append("                    <li>Anti-cyclic citrullinated peptide (anti-CCP) antibodies</li>\n");
        html.append("                    <li>Erythrocyte sedimentation rate (ESR)</li>\n");
        html.append("                    <li>C-reactive protein (CRP) levels</li>\n");
        html.append("                </ul>\n");
        html.append("\n");
        html.append("                <h3>Imaging Studies:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li>X-rays to assess joint damage</li>\n");
        html.append("                    <li>Ultrasound for soft tissue evaluation</li>\n");
        html.append("                    <li>MRI for detailed joint assessment</li>\n");
        html.append("                </ul>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("\n");
        html.append("        <div class=\"highlight\">\n");
        html.append("            <strong>ðŸ’¡ Key Point:</strong> Early diagnosis and treatment are crucial for managing RA effectively and preventing joint damage.\n");
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






