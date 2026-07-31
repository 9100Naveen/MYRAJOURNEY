package com.example.myrajourney.doctor.dashboard;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.example.myrajourney.R;

public class ManagementActivity extends AppCompatActivity {

    private WebView webView;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        webView = findViewById(R.id.webView);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Managing Your RA");
        }

        // Setup WebView
        setupWebView();

        // Load management content
        loadManagementContent();
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

    private void loadManagementContent() {
        String htmlContent = generateManagementContent();
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
    }

    private String generateManagementContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <style>\n");
        html.append("        body { font-family: 'Roboto', sans-serif; line-height: 1.6; margin: 0; padding: 20px; background-color: #f8f9fa; color: #333; }\n");
        html.append("        .container { max-width: 100%; margin: 0 auto; background: white; padding: 30px; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }\n");
        html.append("        h1, h2, h3 { color: #2c3e50; margin-top: 30px; margin-bottom: 15px; }\n");
        html.append("        h1 { border-bottom: 3px solid #e74c3c; padding-bottom: 10px; text-align: center; font-size: 28px; }\n");
        html.append("        h2 { color: #34495e; border-left: 4px solid #e74c3c; padding-left: 15px; font-size: 24px; }\n");
        html.append("        h3 { color: #7f8c8d; font-size: 20px; }\n");
        html.append("        .highlight { background-color: #e8f4fd; padding: 15px; border-left: 4px solid #3498db; margin: 20px 0; border-radius: 0 8px 8px 0; }\n");
        html.append("        .warning { background-color: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; margin: 20px 0; border-radius: 0 8px 8px 0; }\n");
        html.append("        ul, ol { margin: 15px 0; padding-left: 25px; }\n");
        html.append("        li { margin: 8px 0; }\n");
        html.append("        .section { margin-bottom: 40px; padding: 20px; background: #f8f9fa; border-radius: 8px; border-left: 4px solid #e74c3c; }\n");
        html.append("        .subsection { margin: 20px 0; padding: 15px; background: white; border-radius: 6px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <h1>ðŸ©º Managing Your RA Condition</h1>\n");
        html.append("\n");
        html.append("        <div class=\"highlight\">\n");
        html.append("            <strong>Active Management:</strong> Taking control of your RA requires regular monitoring, communication with your healthcare team, and proactive self-care.\n");
        html.append("        </div>\n");
        html.append("\n");
        html.append("        <div class=\"section\">\n");
        html.append("            <h2>ðŸ“Š Regular Monitoring</h2>\n");
        html.append("            <div class=\"subsection\">\n");
        html.append("                <h3>Laboratory Tests:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li><strong>Inflammation Markers:</strong> ESR and CRP levels</li>\n");
        html.append("                    <li><strong>Liver Function:</strong> Monitor medication side effects</li>\n");
        html.append("                    <li><strong>Kidney Function:</strong> Ensure organs are functioning properly</li>\n");
        html.append("                    <li><strong>Complete Blood Count:</strong> Check for anemia or other issues</li>\n");
        html.append("                </ul>\n");
        html.append("\n");
        html.append("                <h3>Clinical Assessments:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li><strong>Joint Examinations:</strong> Range of motion and tenderness</li>\n");
        html.append("                    <li><strong>Disease Activity Scores:</strong> DAS28 or CDAI scoring</li>\n");
        html.append("                    <li><strong>Functional Assessments:</strong> HAQ or similar questionnaires</li>\n");
        html.append("                    <li><strong>Imaging Studies:</strong> X-rays, ultrasound, or MRI as needed</li>\n");
        html.append("                </ul>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("\n");
        html.append("        <div class=\"section\">\n");
        html.append("            <h2>ðŸ“ Self-Monitoring Strategies</h2>\n");
        html.append("            <div class=\"subsection\">\n");
        html.append("                <h3>Daily Tracking:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li><strong>Symptom Journal:</strong> Pain levels, stiffness, fatigue</li>\n");
        html.append("                    <li><strong>Medication Log:</strong> Track doses, timing, and effectiveness</li>\n");
        html.append("                    <li><strong>Activity Diary:</strong> Note activities that help or worsen symptoms</li>\n");
        html.append("                    <li><strong>Mood Tracking:</strong> Monitor emotional well-being</li>\n");
        html.append("                </ul>\n");
        html.append("\n");
        html.append("                <h3>Pattern Recognition:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li><strong>Trigger Identification:</strong> What makes symptoms worse?</li>\n");
        html.append("                    <li><strong>Effective Strategies:</strong> What helps manage symptoms?</li>\n");
        html.append("                    <li><strong>Medication Response:</strong> Which treatments work best?</li>\n");
        html.append("                    <li><strong>Flare Prediction:</strong> Early warning signs</li>\n");
        html.append("                </ul>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("\n");
        html.append("        <div class=\"section\">\n");
        html.append("            <h2>ðŸš¨ Flare Management</h2>\n");
        html.append("            <div class=\"subsection\">\n");
        html.append("                <h3>Early Recognition:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li><strong>Increased Pain:</strong> Joint pain that doesn't respond to usual measures</li>\n");
        html.append("                    <li><strong>Swelling:</strong> Visible joint swelling or warmth</li>\n");
        html.append("                    <li><strong>Stiffness:</strong> Prolonged morning stiffness</li>\n");
        html.append("                    <li><strong>Fatigue:</strong> Unusual tiredness or malaise</li>\n");
        html.append("                </ul>\n");
        html.append("\n");
        html.append("                <h3>Management Strategies:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li><strong>Rest:</strong> Affected joints need time to recover</li>\n");
        html.append("                    <li><strong>Ice/Heat:</strong> Cold packs for acute inflammation, heat for stiffness</li>\n");
        html.append("                    <li><strong>Medication Adjustment:</strong> Contact your doctor about dose changes</li>\n");
        html.append("                    <li><strong>Gentle Movement:</strong> Avoid complete immobilization</li>\n");
        html.append("                </ul>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("\n");
        html.append("        <div class=\"warning\">\n");
        html.append("            <strong>ðŸš¨ Emergency Signs:</strong> Seek immediate medical attention for:\n");
        html.append("            <ul>\n");
        html.append("                <li>Severe chest pain or shortness of breath</li>\n");
        html.append("                <li>Sudden vision changes or neurological symptoms</li>\n");
        html.append("                <li>High fever with severe joint pain</li>\n");
        html.append("                <li>Signs of infection at injection sites</li>\n");
        html.append("            </ul>\n");
        html.append("        </div>\n");
        html.append("\n");
        html.append("        <div class=\"highlight\">\n");
        html.append("            <strong>ðŸ’¡ Key Message:</strong> You're the most important member of your healthcare team. Regular monitoring, open communication with your providers, and proactive self-management are essential for optimal RA control.\n");
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






