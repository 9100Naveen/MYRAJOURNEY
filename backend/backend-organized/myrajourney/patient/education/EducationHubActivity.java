package com.example.myrajourney.patient.education;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.EducationArticle;
import com.google.android.material.appbar.MaterialToolbar;
import com.example.myrajourney.R;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EducationHubActivity extends AppCompatActivity {

    private View whatIsRABtn, nutritionBtn, lifestyleBtn, managementBtn;
    private MaterialToolbar toolbar;
    private List<EducationArticle> articles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_education_hub);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        whatIsRABtn = findViewById(R.id.whatIsRABtn);
        nutritionBtn = findViewById(R.id.nutritionBtn);
        lifestyleBtn = findViewById(R.id.lifestyleBtn);
        managementBtn = findViewById(R.id.managementBtn);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Education Hub");
        }

        // Load education articles from API
        loadEducationArticles();

        // Setup button click listeners for navigation
        setupButtonListeners();
    }

    private void loadEducationArticles() {
        ApiService apiService = com.example.myrajourney.core.network.ApiClient.getApiService(this);
        Call<ApiResponse<List<EducationArticle>>> call = apiService.getEducationArticles();
        
        call.enqueue(new Callback<ApiResponse<List<EducationArticle>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<EducationArticle>>> call, Response<ApiResponse<List<EducationArticle>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    articles = response.body().getData();
                    android.util.Log.d("EducationHub", "Loaded " + (articles != null ? articles.size() : 0) + " articles");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<EducationArticle>>> call, Throwable t) {
                // Handle error - app will work with static content as fallback
            }
        });
    }

    private void setupButtonListeners() {
        whatIsRABtn.setOnClickListener(v -> {
            navigateToArticle("what-is-ra", "What is Rheumatoid Arthritis?");
        });

        nutritionBtn.setOnClickListener(v -> {
            navigateToArticle("nutrition-tips", "Nutrition for RA Patients");
        });

        lifestyleBtn.setOnClickListener(v -> {
            navigateToArticle("lifestyle", "Lifestyle Management");
        });

        managementBtn.setOnClickListener(v -> {
            navigateToArticle("managing-symptoms", "Managing Your Symptoms");
        });
    }

    private void navigateToArticle(String slug, String title) {
        // Always use dynamic article viewer
        Intent intent = new Intent(this, ArticleViewerActivity.class);
        intent.putExtra("slug", slug);
        intent.putExtra("title", title);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}






