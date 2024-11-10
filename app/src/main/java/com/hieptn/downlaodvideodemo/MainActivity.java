package com.hieptn.downlaodvideodemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest; // correct

import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    String urlString = "https://file-examples.com/storage/fe504ae8c8672e49a9e2d51/2017/04/file_example_MP4_480_1_5MG.mp4";
    Button btnDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        VideoDownloader videoDownloader = new VideoDownloader(this);
        String videoUrl = urlString;  // Replace with your video URL
        String fileName = "video_" + System.currentTimeMillis();  // Desired file name
//        videoDownloader.downloadVideo(videoUrl, fileName);


        btnDownload = findViewById(R.id.btnDownload);
        btnDownload.setOnClickListener(v -> {


            videoDownloader.downloadVideo(videoUrl, fileName);

//            downloadVideo(urlString, Long.toString(System.currentTimeMillis() / 1000));
        });
    }


//    public void downloadVideo(String videoUrl, String fileName) {
//        try {
//            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
//
//            Uri uri = Uri.parse(videoUrl);
//
//            DownloadManager.Request request = new DownloadManager.Request(uri);
//            request.setTitle("Downloading Video");
//            request.setDescription("Downloading " + fileName);
//
//            // Set the destination to the Downloads folder with the desired file name
//            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName + ".mp4");
//
//            // Make the download visible and notify when it is complete
//            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//
//            // Enqueue the request
//            downloadManager.enqueue(request);
//
//            Toast.makeText(this, "Download Started", Toast.LENGTH_SHORT).show();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Download Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
//        }
//    }
}