package com.nightonke.githubwidget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Weiping on 2016/4/26.
 */

public class ListViewContentTask extends AsyncTask<String, Void, String> {

    private RemoteViews remoteViews;
    private Context context;
    private ComponentName componentName;
    private int appWidgetId;

    public ListViewContentTask(
            RemoteViews remoteViews,
            Context context,
            ComponentName componentName,
            int appWidgetId) {
        this.remoteViews = remoteViews;
        this.context = context;
        this.componentName = componentName;
        this.appWidgetId = appWidgetId;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        if (BuildConfig.DEBUG) Log.d("GithubWidget", "Execute ListViewContentTask");
        // check whether the user id is got
        String userName = SettingsManager.getUserName();
        String result = null;
        if (userName == null) {
            // user didn't set the user name
            // but we can still get the trending
            result = getTrending();
        } else {
            int userId = SettingsManager.getUserId();
            URL url = null;
            HttpURLConnection httpURLConnection = null;
            if (userId == -1) {
                // we haven't got the user id
                try {
                    url = new URL("https://api.github.com/users/" + userName);
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setConnectTimeout(5000);
                    httpURLConnection.connect();
                    if(httpURLConnection.getResponseCode() == 200){
                        InputStream in = httpURLConnection.getInputStream();
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len = 0;
                        while((len = in.read(buffer)) != -1) {
                            byteArrayOutputStream.write(buffer, 0, len);
                        }
                        if (BuildConfig.DEBUG) Log.d("GithubWidget", "Write user basic data: "
                                + byteArrayOutputStream.toString());
                        Util.writeUserBasicData(context, byteArrayOutputStream.toString());
                        return byteArrayOutputStream.toString();
                    } else {
                        return null;
                    }
                } catch (IOException i) {
                    i.printStackTrace();
                } finally{
                    if (httpURLConnection != null) httpURLConnection.disconnect();
                }
            }
            if (SettingsManager.getListViewContent().equals(ListViewContent.TRENDING_DAILY)
                    || SettingsManager.getListViewContent().equals(ListViewContent.TRENDING_WEEKLY))
                result = getTrending();
            if (SettingsManager.getListViewContent().equals(ListViewContent.EVENT)) {
                result = getEvent();
            }

        }
        if (result != null) SettingsManager.setListViewContents(result);
        return result;
    }

    private String getTrending() {
        if (SettingsManager.getListViewContent().equals(ListViewContent.EVENT)) return null;
        URL url = null;
        HttpURLConnection httpURLConnection = null;
        try {
            String urlString = "";
            if (SettingsManager.getListViewContent().equals(ListViewContent.TRENDING_DAILY))
                urlString = "https://github.com/trending/"
                        + SettingsManager.getLanguage() + "?since=daily";
            if (SettingsManager.getListViewContent().equals(ListViewContent.TRENDING_WEEKLY))
                urlString = "https://github.com/trending/"
                        + SettingsManager.getLanguage() + "?since=weekly";
            if (BuildConfig.DEBUG)
                Log.d("GithubWidget", "Get trending: " + urlString);
            url = new URL(urlString);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.connect();
            if(httpURLConnection.getResponseCode() == 200){
                InputStream in = httpURLConnection.getInputStream();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while((len = in.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, len);
                }
                return byteArrayOutputStream.toString();
            } else {
                return null;
            }
        } catch (IOException i) {
            i.printStackTrace();
        } finally{
            if (httpURLConnection != null) httpURLConnection.disconnect();
        }
        return null;
    }

    private String getEvent() {
        if (SettingsManager.getUserId() == -1) return null;
        URL url = null;
        HttpURLConnection httpURLConnection = null;
        try {
            String urlString = "https://api.github.com/users/" + SettingsManager.getUserName()
                    + "/received_events?per_page=" + SettingsManager.getReceivedEventPerPage();
            if (BuildConfig.DEBUG)
                Log.d("GithubWidget", "Get user stars: " + urlString);
            url = new URL(urlString);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.connect();
            if(httpURLConnection.getResponseCode() == 200){
                InputStream in = httpURLConnection.getInputStream();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while((len = in.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, len);
                }
                return byteArrayOutputStream.toString();
            } else {
                return null;
            }
        } catch (IOException i) {
            i.printStackTrace();
        } finally{
            if (httpURLConnection != null) httpURLConnection.disconnect();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (result == null) {
            // do nothing
        } else {
            Intent intent = new Intent(context, WidgetListViewService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            remoteViews.setRemoteAdapter(R.id.list_view, intent);

            AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, remoteViews);
            AppWidgetManager.getInstance(context)
                    .notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_view);
        }
    }

}