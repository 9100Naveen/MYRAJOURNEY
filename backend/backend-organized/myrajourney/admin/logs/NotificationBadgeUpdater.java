package com.example.myrajourney.admin.logs;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.example.myrajourney.core.network.ApiClient;
import com.example.myrajourney.core.network.ApiService;
import com.example.myrajourney.data.model.ApiResponse;
import com.example.myrajourney.data.model.Notification;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationBadgeUpdater {

    public static void update(Context ctx, TextView badgeView) {

        if (badgeView == null || ctx == null) return;

        ApiService api = ApiClient.getApiService(ctx);

        // Unread = true → backend returns only unread notifications
        Call<ApiResponse<List<Notification>>> call =
                api.getNotifications(1, 100, true);

        call.enqueue(new Callback<ApiResponse<List<Notification>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Notification>>> call,
                                   Response<ApiResponse<List<Notification>>> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    badgeView.setVisibility(View.GONE);
                    return;
                }

                ApiResponse<List<Notification>> body = response.body();

                if (!body.isSuccess()) {
                    badgeView.setVisibility(View.GONE);
                    return;
                }

                List<Notification> items = body.getData();
                int count = (items == null) ? 0 : items.size();

                if (count > 0) {
                    badgeView.setText(String.valueOf(count));
                    badgeView.setVisibility(View.VISIBLE);
                } else {
                    badgeView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Notification>>> call, Throwable t) {
                badgeView.setVisibility(View.GONE);
            }
        });
    }
}
