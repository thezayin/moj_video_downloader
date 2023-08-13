package com.bluelock.moj.util;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Context.DOWNLOAD_SERVICE;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.bluelock.moj.R;
import com.bluelock.moj.db.Database;
import com.bluelock.moj.models.FVideo;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.net.URLConnection;


/** @noinspection RegExpRedundantEscape */
public class Utils {
    private static final String TAG = "Utils";
    public static Dialog customDialog;

    public static String RootDirectoryMoj = "/Smart_Downloader/moj/";

    public static File RootDirectoryMojShow = new File(Environment.getExternalStorageDirectory() + "/Download" + RootDirectoryMoj);

    public Utils() {
    }

    public static void setToast(Context mContext, String str) {
        Toast toast = Toast.makeText(mContext, str, Toast.LENGTH_SHORT);
        toast.show();
    }


    /** @noinspection ResultOfMethodCallIgnored*/
    public static void createMojFolder() {
        if (!RootDirectoryMojShow.exists()) {
            RootDirectoryMojShow.mkdirs();
        }
    }


    public static void showProgressDialog(Activity activity) {
        System.out.println("Show");
        if (customDialog != null) {
            customDialog.dismiss();
            customDialog = null;
        }
        customDialog = new BottomSheetDialog(activity, R.style.SheetDialog);
        LayoutInflater inflater = LayoutInflater.from(activity);
        @SuppressLint("InflateParams") View mView = inflater.inflate(R.layout.layout_progress_dialog, null);
        customDialog.setCancelable(false);
        customDialog.setContentView(mView);
        if (!customDialog.isShowing() && !activity.isFinishing()) {
            customDialog.show();
        }
    }

    public static void hideProgressDialog() {
        System.out.println("Hide");
        if (customDialog != null && customDialog.isShowing()) {
            customDialog.dismiss();
        }
    }

    public static FVideo startDownload(Context context, String videoUrl) {


        int result = ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT < 32 && result != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Don't have write permission to storage", Toast.LENGTH_SHORT).show();
            return null;
        }

        setToast(context, context.getResources().getString(R.string.download_started));
        String fileName = "snapchat_" + System.currentTimeMillis() + ".mp4";

        String downloadLocation = RootDirectoryMoj + fileName;

        Uri uri = Uri.parse(videoUrl); // Path where you want to download file.
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);  // Tell on which network you want to download file.
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);  // This will show notification on top when downloading the file.
        request.setTitle(fileName + ""); // Title for notification.
        request.setVisibleInDownloadsUi(true);

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, downloadLocation);// Storage directory path
        long downloadId = ((DownloadManager) context.getSystemService(DOWNLOAD_SERVICE)).enqueue(request); // This will start downloading

        //Creating a video object to track download is completed
        FVideo video = new FVideo(Environment.getExternalStorageDirectory() +
                downloadLocation, fileName, downloadId, false, System.currentTimeMillis());
        video.setState(FVideo.DOWNLOADING);
        video.setVideoSource(FVideo.SNAPCHAT);


        Database db = Database.init(context);
        db.addVideo(video);

        Log.d(TAG, "startDownload: " + Environment.getDataDirectory().getPath() + RootDirectoryMoj + fileName);

        try {
            MediaScannerConnection.scanFile(context, new String[]{new File(DIRECTORY_DOWNLOADS + "/" + fileName).getAbsolutePath()},
                    null, (path, uri1) -> Log.d("videoProcess", "onScanCompleted: " + path));


        } catch (Exception e) {
            e.printStackTrace();
        }

        FVideo fVideo = new FVideo(Environment.getExternalStorageDirectory() +
                downloadLocation + fileName,
                fileName, downloadId, false, System.currentTimeMillis());
        fVideo.setVideoSource(FVideo.FACEBOOK);
        return fVideo;
    }


    /** @noinspection DataFlowIssue*/
    public static void deleteVideoFromFile(Context context, FVideo video) {
        if (video.getState() == FVideo.COMPLETE) {

            File file = new File(video.getFileUri());
            Database db = Database.init(context);

            if (file.exists()) {
                new AlertDialog.Builder(context)
                        .setTitle("Want to delete this file?")
                        .setMessage("This will delete file from your Disk")
                        .setPositiveButton("Yes", (dialog, which) -> {

                            boolean isDeleted = file.delete();
                            if (isDeleted)
                                Toast.makeText(context, "Video deleted", Toast.LENGTH_SHORT).show();


                            db.deleteAVideo(video.getDownloadId());
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.cancel())
                        .show();
            } else {
                db.deleteAVideo((video.getDownloadId()));
                Toast.makeText(context, "video not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "File downloading...", Toast.LENGTH_SHORT).show();
        }
    }


    /** @noinspection DataFlowIssue*/
    public static boolean isVideoFile(Context context, String path) {
        if (path.startsWith("content")) {
            DocumentFile fromTreeUri = DocumentFile.fromSingleUri(context, Uri.parse(path));
            String mimeType = fromTreeUri.getType();
            return mimeType != null && mimeType.startsWith("video");
        } else {
            String mimeType = URLConnection.guessContentTypeFromName(path);
            return mimeType != null && mimeType.startsWith("video");
        }
    }
}
