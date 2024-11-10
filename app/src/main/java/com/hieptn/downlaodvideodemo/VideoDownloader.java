package com.hieptn.downlaodvideodemo;

import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.File;

public class VideoDownloader {

    private Context context;
    private long downloadID;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private static final String CHANNEL_ID = "download_channel";

    public VideoDownloader(Context context) {
        this.context = context;
        createNotificationChannel();
    }


    public void downloadVideo(String videoUrl, String fileName) {
        try {
            // Initialize Download Manager
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

            // Define custom directory path
            File customDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "folder_of_you");

            // Create the directory if it doesn't exist
            if (!customDirectory.exists()) {
                if (customDirectory.mkdirs()) {
                    Toast.makeText(context, "Directory created: " + customDirectory.getPath(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to create directory: " + customDirectory.getPath(), Toast.LENGTH_LONG).show();
                    return; // Stop if directory creation failed
                }
            }

            // Define the destination file within the custom directory
            File destinationFile = new File(customDirectory, fileName + ".mp4");

            Uri uri = Uri.parse(videoUrl);

            // Set up download request with custom URI
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle("Downloading " + fileName);
            request.setDescription("Downloading video file...");
            request.setDestinationUri(Uri.fromFile(destinationFile)); // Use setDestinationUri for custom path
//            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN); // Hide default notification

            // Start download and save download ID
            downloadID = downloadManager.enqueue(request);

            // Set up progress notification
            showProgressNotification();

            // Register BroadcastReceiver to track download completion
            context.registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

            // Track download progress
            new Thread(() -> trackDownloadProgress(downloadManager, downloadID)).start();

            Toast.makeText(context, "Download Started", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Download Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showProgressNotification() {
        // Initialize notification manager and builder
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle("Downloading Video")
                .setContentText("Download in progress")
                .setOngoing(true)
                .setProgress(100, 0, true);
        notificationManager.notify((int) downloadID, notificationBuilder.build());
    }

    private void updateProgressNotification(int progress) {
        notificationBuilder.setProgress(100, progress, false)
                .setContentText("Downloaded " + progress + "%");
        notificationManager.notify((int) downloadID, notificationBuilder.build());
    }

    private void trackDownloadProgress(DownloadManager downloadManager, long downloadID) {
        boolean downloading = true;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = 1; // Unique ID for your download notification

        while (downloading) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadID);
            Cursor cursor = downloadManager.query(query);

            if (cursor != null && cursor.moveToFirst()) {
                int bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                int totalBytesIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);

                if (bytesDownloadedIndex != -1 && totalBytesIndex != -1) {
                    int bytesDownloaded = cursor.getInt(bytesDownloadedIndex);
                    int totalBytes = cursor.getInt(totalBytesIndex);

                    if (totalBytes > 0) {
                        int progress = (int) ((bytesDownloaded * 100L) / totalBytes);
                        updateProgressNotification(progress);

                        // Cancel the notification when download reaches 100%
                        if (progress == 100) {
                            notificationManager.cancel(notificationId);
                            downloading = false;
                        }
                    }
                }

                int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (statusIndex != -1) {
                    int status = cursor.getInt(statusIndex);
                    if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED) {
                        downloading = false;
                        // Cancel the notification upon download completion or failure
                        notificationManager.cancel(notificationId);
                    }
                }
                cursor.close();
            }
        }
    }


    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (id == downloadID) {
                notificationBuilder.setContentText("Download complete")
                        .setProgress(0, 0, false)
                        .setOngoing(false)
                        .setSmallIcon(android.R.drawable.stat_sys_download_done);
                notificationManager.notify((int) downloadID, notificationBuilder.build());
                context.unregisterReceiver(onDownloadComplete);
            }
        }
    };

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Download Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Notification channel for download progress");
            notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
