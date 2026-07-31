package com.example.myrajourney.core.network;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OfflineMockInterceptor implements Interceptor {
    private final Context context;

    public OfflineMockInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String method = request.method();
        String path = request.url().encodedPath();

        // Normalize to api/v1 relative path
        int idx = path.indexOf("/api/v1/");
        String relative = idx >= 0 ? path.substring(idx + 1) : path.replaceFirst("^/", "");

        String assetPath = mapToAsset(relative, method);
        String json = readAssetOrNull(assetPath);
        if (json == null) {
            // Default not found json
            json = "{\"success\":false,\"error\":{\"code\":\"OFFLINE_NOT_FOUND\",\"message\":\"No offline mock for " + relative + "\"}}";
            return new Response.Builder()
                    .code(404)
                    .message("Not Found")
                    .protocol(Protocol.HTTP_1_1)
                    .request(request)
                    .body(ResponseBody.create(json, MediaType.get("application/json")))
                    .build();
        }

        return new Response.Builder()
                .code(200)
                .message("OK")
                .protocol(Protocol.HTTP_1_1)
                .request(request)
                .body(ResponseBody.create(json, MediaType.get("application/json")))
                .build();
    }

    private String mapToAsset(String relative, String method) {
        // Example: api/v1/auth/login -> api/v1/auth/login.POST.json
        String normalized = relative.replaceAll("^/+", "");
        String suffix = "." + method + ".json";
        return "api/" + normalized + suffix;
    }

    private String readAssetOrNull(String assetPath) {
        try {
            AssetManager am = context.getAssets();
            InputStream is = am.open(assetPath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
}






