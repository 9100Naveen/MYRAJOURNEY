package com.example.myrajourney.doctor.education;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.example.myrajourney.R;

public class NutritionActivity extends AppCompatActivity {

    private WebView webView;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        webView = findViewById(R.id.webView);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Nutrition for RA Management");
        }

        // Setup WebView
        setupWebView();

        // Load nutrition content
        loadNutritionContent();
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

    private void loadNutritionContent() {
        String htmlContent = generateNutritionContent();
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
    }

    private String generateNutritionContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <style>\n");
        html.append("        body { font-family: 'Roboto', sans-serif; line-height: 1.6; margin: 0; padding: 20px; background-color: #f8f9fa; color: #333; }\n");
        html.append("        .container { max-width: 100%; margin: 0 auto; background: white; padding: 30px; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }\n");
        html.append("        h1, h2, h3 { color: #2c3e50; margin-top: 30px; margin-bottom: 15px; }\n");
        html.append("        h1 { border-bottom: 3px solid #27ae60; padding-bottom: 10px; text-align: center; font-size: 28px; }\n");
        html.append("        h2 { color: #34495e; border-left: 4px solid #27ae60; padding-left: 15px; font-size: 24px; }\n");
        html.append("        h3 { color: #7f8c8d; font-size: 20px; }\n");
        html.append("        .highlight { background-color: #d4edda; padding: 15px; border-left: 4px solid #28a745; margin: 20px 0; border-radius: 0 8px 8px 0; }\n");
        html.append("        .warning { background-color: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; margin: 20px 0; border-radius: 0 8px 8px 0; }\n");
        html.append("        ul, ol { margin: 15px 0; padding-left: 25px; }\n");
        html.append("        li { margin: 8px 0; }\n");
        html.append("        .section { margin-bottom: 40px; padding: 20px; background: #f8f9fa; border-radius: 8px; border-left: 4px solid #27ae60; }\n");
        html.append("        .subsection { margin: 20px 0; padding: 15px; background: white; border-radius: 6px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <h1>ðŸ¥— Nutrition for RA Management</h1>\n");
        html.append("\n");
        html.append("        <div class=\"highlight\">\n");
        html.append("            <strong>Food as Medicine:</strong> The right nutrition can help reduce inflammation, manage symptoms, and improve your overall quality of life with RA.\n");
        html.append("        </div>\n");
        html.append("\n");
        html.append("        <div class=\"section\">\n");
        html.append("            <h2>ðŸ¥‘ Anti-Inflammatory Foods</h2>\n");
        html.append("            <div class=\"subsection\">\n");
        html.append("                <h3>Omega-3 Rich Foods:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li><strong>Fatty Fish:</strong> Salmon, mackerel, sardines, tuna (2-3 servings/week)</li>\n");
        html.append("                    <li><strong>Flaxseeds:</strong> Ground flaxseed (1-2 tablespoons daily)</li>\n");
        html.append("                    <li><strong>Walnuts:</strong> Handful of walnuts as a snack</li>\n");
        html.append("                    <li><strong>Chia Seeds:</strong> Excellent source of plant-based omega-3s</li>\n");
        html.append("                </ul>\n");
        html.append("\n");
        html.append("                <h3>Antioxidant-Rich Foods:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li><strong>Berries:</strong> Blueberries, strawberries, raspberries, blackberries</li>\n");
        html.append("                    <li><strong>Cherries:</strong> Tart cherries (juice or whole fruit)</li>\n");
        html.append("                    <li><strong>Leafy Greens:</strong> Spinach, kale, broccoli, Brussels sprouts</li>\n");
        html.append("                    <li><strong>Colorful Vegetables:</strong> Bell peppers, tomatoes, carrots</li>\n");
        html.append("                </ul>\n");
        html.append("\n");
        html.append("                <h3>Spice Power:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li><strong>Turmeric:</strong> Contains curcumin with anti-inflammatory properties</li>\n");
        html.append("                    <li><strong>Ginger:</strong> Fresh ginger tea or in cooking</li>\n");
        html.append("                    <li><strong>Garlic:</strong> Raw or cooked garlic for immune support</li>\n");
        html.append("                    <li><strong>Cinnamon:</strong> Ceylon cinnamon for blood sugar control</li>\n");
        html.append("                </ul>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("\n");
        html.append("        <div class=\"section\">\n");
        html.append("            <h2>ðŸš« Foods to Limit or Avoid</h2>\n");
        html.append("            <div class=\"subsection\">\n");
        html.append("                <h3>Inflammatory Triggers:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li><strong>Processed Foods:</strong> High in trans fats and preservatives</li>\n");
        html.append("                    <li><strong>Sugary Foods:</strong> Soda, candy, baked goods</li>\n");
        html.append("                    <li><strong>Fried Foods:</strong> French fries, fried chicken, donuts</li>\n");
        html.append("                    <li><strong>Refined Carbohydrates:</strong> White bread, pasta, rice</li>\n");
        html.append("                </ul>\n");
        html.append("\n");
        html.append("                <h3>Potential Allergens:</h3>\n");
        html.append("                <ul>\n");
        html.append("                    <li><strong>Nightshade Vegetables:</strong> Tomatoes, potatoes, eggplant, peppers (if sensitive)</li>\n");
        html.append("                    <li><strong>Dairy Products:</strong> Milk, cheese, ice cream (if lactose intolerant)</li>\n");
        html.append("                    <li><strong>Gluten:</strong> Wheat, barley, rye (if gluten sensitive)</li>\n");
        html.append("                </ul>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("\n");
        html.append("        <div class=\"section\">\n");
        html.append("            <h2>ðŸ¥¤ Hydration and RA</h2>\n");
        html.append("            <div class=\"subsection\">\n");
        html.append("                <p>Proper hydration is crucial for managing RA symptoms:</p>\n");
        html.append("                <ul>\n");
        html.append("                    <li>Drink at least 8-10 glasses of water daily</li>\n");
        html.append("                    <li>Herbal teas can count toward hydration</li>\n");
        html.append("                    <li>Limit caffeine and alcohol intake</li>\n");
        html.append("                    <li>Consider bone broth for joint health</li>\n");
        html.append("                </ul>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("\n");
        html.append("        <div class=\"highlight\">\n");
        html.append("            <strong>ðŸ’¡ Pro Tip:</strong> Consider working with a registered dietitian who specializes in autoimmune conditions to create a personalized nutrition plan tailored to your specific needs and preferences.\n");
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






