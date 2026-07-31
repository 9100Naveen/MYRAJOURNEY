package com.example.myrajourney.core.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkUtils {
    private static final String TAG = "NetworkUtils";
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public enum NetworkType {
        WIFI, CELLULAR, ETHERNET, VPN, OTHER, NONE
    }

    public static boolean isNetworkAvailable(Context context) {
        if (context == null) return false;

        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            // For Android M (API 23) and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = connectivityManager.getActiveNetwork();
                if (network == null) return false;

                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                return capabilities != null &&
                        (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
            } else {
                // For older Android versions
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        }
        return false;
    }

    public static NetworkType getNetworkType(Context context) {
        if (context == null) return NetworkType.NONE;

        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = connectivityManager.getActiveNetwork();
                if (network == null) return NetworkType.NONE;

                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return NetworkType.WIFI;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return NetworkType.CELLULAR;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        return NetworkType.ETHERNET;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        return NetworkType.VPN;
                    } else {
                        return NetworkType.OTHER;
                    }
                }
            } else {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                    int type = activeNetworkInfo.getType();
                    switch (type) {
                        case ConnectivityManager.TYPE_WIFI:
                            return NetworkType.WIFI;
                        case ConnectivityManager.TYPE_MOBILE:
                            return NetworkType.CELLULAR;
                        case ConnectivityManager.TYPE_ETHERNET:
                            return NetworkType.ETHERNET;
                        default:
                            return NetworkType.OTHER;
                    }
                }
            }
        }
        return NetworkType.NONE;
    }

    public static CompletableFuture<Boolean> isIpReachable(String ipAddress) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                InetAddress inet = InetAddress.getByName(ipAddress);
                return inet.isReachable(5000); // 5 second timeout
            } catch (Exception e) {
                Log.w(TAG, "IP reachability test failed for " + ipAddress, e);
                return false;
            }
        }, executor);
    }

    public static CompletableFuture<Boolean> isPortOpen(String host, int port) {
        return CompletableFuture.supplyAsync(() -> {
            Socket socket = null;
            try {
                socket = new Socket();
                socket.connect(new java.net.InetSocketAddress(host, port), 5000);
                return true;
            } catch (Exception e) {
                Log.w(TAG, "Port connectivity test failed for " + host + ":" + port, e);
                return false;
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        Log.w(TAG, "Failed to close test socket", e);
                    }
                }
            }
        }, executor);
    }

    public static CompletableFuture<Boolean> canResolveHostname(String hostname) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                InetAddress.getByName(hostname);
                return true;
            } catch (UnknownHostException e) {
                Log.w(TAG, "Hostname resolution failed for " + hostname, e);
                return false;
            }
        }, executor);
    }

    public static String getNetworkInfo(Context context) {
        StringBuilder info = new StringBuilder();
        
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return "ConnectivityManager not available";
        }

        info.append("Network Type: ").append(getNetworkType(context)).append("\n");
        info.append("Network Available: ").append(isNetworkAvailable(context)).append("\n");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                if (capabilities != null) {
                    info.append("Download Bandwidth: ");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        info.append(capabilities.getLinkDownstreamBandwidthKbps()).append(" Kbps\n");
                    } else {
                        info.append("Unknown\n");
                    }
                    
                    info.append("Upload Bandwidth: ");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        info.append(capabilities.getLinkUpstreamBandwidthKbps()).append(" Kbps\n");
                    } else {
                        info.append("Unknown\n");
                    }
                    
                    info.append("Metered: ").append(!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)).append("\n");
                }
            }
        } else {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                info.append("Network State: ").append(activeNetworkInfo.getState()).append("\n");
                info.append("Network Subtype: ").append(activeNetworkInfo.getSubtypeName()).append("\n");
            }
        }

        return info.toString();
    }

    public static boolean isOnWiFi(Context context) {
        return getNetworkType(context) == NetworkType.WIFI;
    }

    public static boolean isOnCellular(Context context) {
        return getNetworkType(context) == NetworkType.CELLULAR;
    }

    public static boolean isMeteredConnection(Context context) {
        if (context == null) return false;

        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = connectivityManager.getActiveNetwork();
                if (network != null) {
                    NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                    if (capabilities != null) {
                        return !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
                    }
                }
            } else {
                return connectivityManager.isActiveNetworkMetered();
            }
        }
        return false;
    }

    /**
     * Get a human-readable description of the current network status
     */
    public static String getNetworkStatusDescription(Context context) {
        if (!isNetworkAvailable(context)) {
            return "No internet connection available";
        }

        NetworkType type = getNetworkType(context);
        boolean isMetered = isMeteredConnection(context);
        
        StringBuilder description = new StringBuilder();
        description.append("Connected via ");
        
        switch (type) {
            case WIFI:
                description.append("WiFi");
                break;
            case CELLULAR:
                description.append("mobile data");
                break;
            case ETHERNET:
                description.append("Ethernet");
                break;
            case VPN:
                description.append("VPN");
                break;
            default:
                description.append("unknown network");
                break;
        }
        
        if (isMetered) {
            description.append(" (metered connection)");
        }
        
        return description.toString();
    }
}






