package com.example.myrajourney.core.network;

import android.content.Context;
import android.util.Log;

import com.example.myrajourney.R;

/**
 * Helper class to centralize network configuration
 * All API URLs are generated from the IP and port in network_config.xml
 */
public class NetworkConfigHelper {
    private static final String TAG = "NetworkConfigHelper";

    /**
     * Get the base API URL from centralized configuration
     */
    public static String getApiBaseUrl(Context context) {
        try {
            String ip = context.getString(R.string.api_base_ip);
            String port = context.getString(R.string.api_port);

            String url = String.format(
                    context.getString(R.string.api_base_url_formatted),
                    ip, port);

            Log.d(TAG, "Generated API base URL: " + url);
            return url;

        } catch (Exception e) {
            Log.e(TAG, "Error generating API base URL", e);
            // Fallback to hardcoded URL
            return "http://192.168.29.162/backend/public/index.php/api/v1/";
        }
    }

    /**
     * Get the admin API URL from centralized configuration
     */
    public static String getAdminApiUrl(Context context) {
        try {
            String ip = context.getString(R.string.api_base_ip);
            String port = context.getString(R.string.api_port);

            String url = String.format(
                    context.getString(R.string.admin_api_url_formatted),
                    ip, port);

            Log.d(TAG, "Generated admin API URL: " + url);
            return url;

        } catch (Exception e) {
            Log.e(TAG, "Error generating admin API URL", e);
            // Fallback to hardcoded URL
            return "http://192.168.29.162/backend/public/myra-admin.php";
        }
    }

    /**
     * Get the base IP address
     */
    public static String getBaseIp(Context context) {
        return context.getString(R.string.api_base_ip);
    }

    /**
     * Get the API port
     */
    public static String getApiPort(Context context) {
        return context.getString(R.string.api_port);
    }

    /**
     * Check if network configuration is valid
     */
    public static boolean isConfigurationValid(Context context) {
        try {
            String ip = getBaseIp(context);
            String port = getApiPort(context);

            return ip != null && !ip.isEmpty() &&
                    port != null && !port.isEmpty() &&
                    !ip.equals("your-ip-here") &&
                    !port.equals("your-port-here");

        } catch (Exception e) {
            Log.e(TAG, "Error validating network configuration", e);
            return false;
        }
    }

    /**
     * Get full server URL (without API path)
     */
    public static String getServerUrl(Context context) {
        try {
            String ip = context.getString(R.string.api_base_ip);
            String port = context.getString(R.string.api_port);

            return String.format("http://%s:%s", ip, port);

        } catch (Exception e) {
            Log.e(TAG, "Error generating server URL", e);
            return "http://10.0.2.2";
        }
    }
}