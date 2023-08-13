package com.bluelock.moj.db;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.bluelock.moj.models.FVideo;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/** @noinspection deprecation */
@SuppressLint("StaticFieldLeak")
public class Database {
    private static final String TAG = "Database";

    private static int listSize = 0;
    private static DbHelper dbHelper;
    private static Database instance;
    private LoadCallback callback;

    private Database() {
    }

    public static Database init(Context context1) {
        dbHelper = new DbHelper(context1);

        if (instance == null) {
            instance = new Database();
        }

        return instance;
    }

    public void setCallback(LoadCallback callback1) {
        callback = callback1;
    }

    public void addVideo(FVideo video) {
        new AddVideoAsync().execute(video);
    }

    public ArrayList<FVideo> getRecentVideos() {
        ArrayList<FVideo> result;
        try {
            result = new loadRecentVideosTask().execute().get();
            listSize = result.size();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /** @noinspection unchecked*/
    public void updateState(long downloadId, int state) {
        new UpdateStateAsync().execute(new Pair<>(downloadId, state));
    }
    /** @noinspection unchecked*/
    public void setUri(long downloadId, String uri) {
        new SetUriAsync().execute(new Pair<>(downloadId, uri));
    }

    public FVideo getVideo(long downloadId) {

        FVideo result;
        try {
            result = new loadVideoTask().execute(downloadId).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    public void deleteAVideo(Long videoId) {
        new DeleteAsync().execute(videoId);
    }

    public interface LoadCallback {
        void onUpdateDatabase();
    }

    static class loadRecentVideosTask extends AsyncTask<Void, Void, ArrayList<FVideo>> {

        @Override
        protected ArrayList<FVideo> doInBackground(Void... voids) {
            return dbHelper.getRecentVideos();
        }
    }

    static class loadVideoTask extends AsyncTask<Long, Void, FVideo> {

        @Override
        protected FVideo doInBackground(Long... longs) {
            Log.d(TAG, "doInBackground: getVideo");
            return dbHelper.getVideo(longs[0]);
        }
    }

    class AddVideoAsync extends AsyncTask<FVideo, Void, Void> {

        @Override
        protected Void doInBackground(FVideo... fVideos) {
            dbHelper.insertVideo(fVideos[0], listSize);
            Log.d(TAG, "addVideo: ");
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if (callback != null)
                callback.onUpdateDatabase();
        }
    }

    /** @noinspection unchecked*/
    class UpdateStateAsync extends AsyncTask<Pair<Long, Integer>, Void, Void> {

        @Override
        protected Void doInBackground(Pair<Long, Integer>... pairs) {
            Log.d(TAG, "doInBackground: update state");
            dbHelper.updateState(pairs[0].first, pairs[0].second);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if (callback != null)
                callback.onUpdateDatabase();
        }
    }

    class SetUriAsync extends AsyncTask<Pair<Long, String>, Void, Void> {

        /** @noinspection unchecked*/
        @Override
        protected Void doInBackground(Pair<Long, String>... pairs) {
            dbHelper.setUri(pairs[0].first, pairs[0].second);
            Log.d(TAG, "doInBackground: set uri");
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if (callback != null)
                callback.onUpdateDatabase();
        }
    }

    class DeleteAsync extends AsyncTask<Long, Void, Void> {

        @Override
        protected Void doInBackground(Long... longs) {
            dbHelper.deleteVideo(longs[0]);
            Log.d(TAG, "doInBackground: delete video");
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if (callback != null)
                callback.onUpdateDatabase();
        }
    }
}
