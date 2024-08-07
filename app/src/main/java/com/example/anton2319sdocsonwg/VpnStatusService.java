package com.example.anton2319sdocsonwg;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.wireguard.android.backend.GoBackend;
import com.wireguard.android.backend.Statistics;
import com.wireguard.android.backend.Tunnel;

// update by salman khan 03089222522
// donate me at whatsapp +923089222522 or fiverr https://www.fiverr.com/salmankhan150
public class VpnStatusService extends Service {
    private static final int NOTIFICATION_ID = 111;
    private static final String CHANNEL_ID = "status_vpn_channel";
    private static NotificationCompat.Builder notificationBuilder;
    private static NotificationManager notificationManager;
    private Handler handler = new Handler();
    private WgTunnel tunnel;
    private GoBackend backend;

    @Override
    public void onCreate() {
        super.onCreate();
        tunnel = PersistentConnectionProperties.getInstance(this).getTunnel();
        backend = PersistentConnectionProperties.getInstance(this).getBackend();

        // Initialize notificationManager
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create or update the notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "VPN Status", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Create the PendingIntent to launch MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create the notification builder
        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        // Start updating notification
        startForeground(NOTIFICATION_ID, notificationBuilder.build());
        handler.post(updateNotificationRunnable);
    }

    private Runnable updateNotificationRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (backend != null && tunnel != null && backend.getState(tunnel) == Tunnel.State.UP) {
                    Statistics statistics = backend.getStatistics(tunnel);
                    updateNotification(statistics.totalRx(), statistics.totalTx(), tunnel.getName(), "Connected");
                } else {
                    updateNotification(0, 0, tunnel.getName(), "Disconnected");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            handler.postDelayed(this, 1000);
        }
    };

    private void updateNotification(long currentRx, long currentTx, String name, String status) {
        if (status.equals("Connected")) {
            notificationBuilder.setContentText("Rx: " + formatBytes(currentRx) + " | Tx: " + formatBytes(currentTx));
            notificationBuilder.setContentTitle(name);
        } else if (status.equals("Disconnected")) {
            notificationBuilder.setContentText(name + " - Disconnected");
            notificationBuilder.setContentTitle(name);
        }

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateNotificationRunnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
