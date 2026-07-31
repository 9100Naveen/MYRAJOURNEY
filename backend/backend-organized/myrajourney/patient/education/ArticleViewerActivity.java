package com.example.myrajourney.patient.education;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

// ✅ FIX: Import R
import com.example.myrajourney.R;

import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.EducationArticle;
import com.google.android.material.appbar.MaterialToolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArticleViewerActivity extends AppCompatActivity {

    private WebView webView;
    private MaterialToolbar toolbar;
    private String articleSlug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_viewer);

        // Get article slug from intent
        articleSlug = getIntent().getStringExtra("slug");
        String title = getIntent().getStringExtra("title");

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        webView = findViewById(R.id.webView);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(title != null ? title : "Article");
        }

        // Setup WebView
        setupWebView();

        // Load article from API
        if (articleSlug != null) {
            loadArticleFromAPI(articleSlug);
        } else {
            // Fallback static content
            loadFallbackContent();
        }
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

    private void loadArticleFromAPI(String slug) {
        ApiService apiService = com.example.myrajourney.core.network.ApiClient.getApiService(this);
        Call<ApiResponse<EducationArticle>> call = apiService.getEducationArticle(slug);

        call.enqueue(new Callback<ApiResponse<EducationArticle>>() {
            @Override
            public void onResponse(Call<ApiResponse<EducationArticle>> call, Response<ApiResponse<EducationArticle>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    EducationArticle article = response.body().getData();
                    if (article != null) {
                        displayArticle(article);
                        return;
                    }
                }
                // Fallback if API fails
                loadFallbackContent();
            }

            @Override
            public void onFailure(Call<ApiResponse<EducationArticle>> call, Throwable t) {
                // Fallback if network fails
                loadFallbackContent();
            }
        });
    }

    private void displayArticle(EducationArticle article) {
        // Use the helper method or direct field
        String htmlContent = article.getContentHtml();

        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            // Generate HTML from article data
            htmlContent = generateHTMLFromArticle(article);
        }

        // Wrap in proper HTML structure if needed
        if (!htmlContent.contains("<html")) {
            htmlContent = wrapInHTML(htmlContent, article.getTitle());
        }

        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);

        // Update toolbar title
        if (getSupportActionBar() != null && article.getTitle() != null) {
            getSupportActionBar().setTitle(article.getTitle());
        }
    }

    private String generateHTMLFromArticle(EducationArticle article) {
        StringBuilder html = new StringBuilder();
        html.append("<div style='padding: 20px; font-family: sans-serif;'>");
        html.append("<h1>").append(article.getTitle() != null ? article.getTitle() : "Article").append("</h1>");
        html.append("<p><strong>Category:</strong> ").append(article.getCategory() != null ? article.getCategory() : "General").append("</p>");
        if (article.getContentHtml() != null && !article.getContentHtml().trim().isEmpty()) {
            html.append(article.getContentHtml());
        } else {
            html.append("<p>Content coming soon...</p>");
        }
        html.append("</div>");
        return html.toString();
    }

    private String wrapInHTML(String content, String title) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; padding: 20px; } " +
                "h1, h2, h3 { color: #2c3e50; } img { max-width: 100%; height: auto; } " +
                ".highlight { background: #f0f8ff; padding: 15px; border-left: 4px solid #2196F3; margin: 20px 0; }</style></head><body>" +
                (title != null ? "<h1>" + title + "</h1>" : "") + content + "</body></html>";
    }

    private void loadFallbackContent() {
        // Generate basic fallback content based on slug
        String content = generateFallbackContent(articleSlug);
        String html = wrapInHTML(content, "Education Article");
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }

    private String generateFallbackContent(String slug) {
        if (slug == null) return "<p>Content not available.</p>";

        switch (slug.toLowerCase()) {
            case "what-is-ra":
                return "<div><h2>What is Rheumatoid Arthritis?</h2>" +
                        "<p>Rheumatoid Arthritis (RA) is a chronic autoimmune disease that causes inflammation in the joints and can affect other parts of the body.</p>" +
                        "<h3>Key Points:</h3><ul><li>Autoimmune disorder</li><li>Affects joints symmetrically</li><li>Can cause permanent joint damage</li><li>Early treatment is crucial</li></ul></div>";
            case "nutrition-tips":
                return "<div><h2>Nutrition for RA Patients</h2>" +
                        "<p>Proper nutrition plays a vital role in managing RA symptoms and overall health.</p>" +
                        "<h3>Recommended Foods:</h3><ul><li>Omega-3 rich fish</li><li>Fresh fruits and vegetables</li><li>Whole grains</li><li>Lean proteins</li></ul></div>";
            case "lifestyle":
                return "<div><h2>Lifestyle Management</h2>" +
                        "<p>Maintaining a healthy lifestyle can significantly improve RA symptoms and quality of life.</p>" +
                        "<h3>Tips:</h3><ul><li>Regular gentle exercise</li><li>Adequate rest</li><li>Stress management</li><li>Smoking cessation</li></ul></div>";
            case "managing-symptoms":
                return "<div><h2>Managing Your Symptoms</h2>" +
                        "<p>Effective symptom management is key to living well with RA.</p>" +
                        "<h3>Strategies:</h3><ul><li>Medication adherence</li><li>Physical therapy</li><li>Heat and cold therapy</li><li>Joint protection techniques</li></ul></div>";
            default:
                return "<p>Content for this article is being prepared. Please check back soon.</p>";
        }
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