package com.mydrakortv.beta.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mydrakortv.beta.BuildConfig;
import com.mydrakortv.beta.R;
import com.mydrakortv.beta.model.Episode;
import com.mydrakortv.beta.model.Subtitle;
import com.mydrakortv.beta.model.Video;
import com.mydrakortv.beta.utils.ToastMsg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.RequiresApi;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.view.View.VISIBLE;

public class WebviewActivity extends Activity {
    private static final String TAG = "WebViewActivity";
    private static final String CLASS_NAME = "com.mydrakortv.beta.ui.activity.WebviewActivity";
    public WebView webView;
    public ProgressBar pb;
    public TextView tv;
    private String url = "";
    private String url_lama = "";
    private String parser = "";
    private String category = "";
    private Video video = null;
    private Episode video1 = null;
    List<Video> videoList = new ArrayList<>();


    @SuppressLint("SetJavaScriptEnabled")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);


        parser = getIntent().getStringExtra("parser");
        url = getIntent().getStringExtra("streamUrl");
        category = getIntent().getStringExtra("category");

        pb = findViewById(R.id.progress_bar_webview);
        tv = (TextView) findViewById(R.id.fetching);

        pb.setVisibility(VISIBLE);
        tv.setVisibility(VISIBLE);

        webView = (WebView) findViewById(R.id.webView);
        if(category.equalsIgnoreCase("tvseries")){
            video1 = (Episode) getIntent().getSerializableExtra("video");
        }else{
            video = (Video) getIntent().getSerializableExtra("video");
        }

        webView.addJavascriptInterface(new JavaScriptInterface(this), "httpClient");
        webView.addJavascriptInterface(new JavaScriptInterfaces(this), "setVideoInfo");
        webView.addJavascriptInterface(new JavaScriptInterface(this), "htmlRequest");
        webView.addJavascriptInterface(new JavaScriptInterface(this), "getUrl");

        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);

        webView.loadUrl("mydrakortv/assets/fetch.js");
        webView.loadUrl("javascript:(function() {" +
                "var parent = document.getElementsByTagName('head').item(0);" +
                "var script = document.createElement('script');" +
                "script.type = 'text/javascript';" +
                "script.innerHTML = window.atob('" + parser + "');" +
                "parent.appendChild(script)" +
                "})()");

        webView.evaluateJavascript("onGetUrlResponse('"+url+"','"+ BuildConfig.VERSION_NAME +"')",null);
        webView.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }
            public void onPageFinished(WebView view, String weburl){
                webView.setVisibility(View.INVISIBLE);
            }
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }
        });
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                if (consoleMessage.messageLevel().toString().equalsIgnoreCase("ERROR")){
                    new ToastMsg(WebviewActivity.this).toastIconError("Can't Fetching Video URL");
                    finish();
                }
                return super.onConsoleMessage(consoleMessage);
            }
        });

    }

    class JavaScriptInterface {
        Context mContext;

        JavaScriptInterface(Context c) {
            mContext = c;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @JavascriptInterface
        public void postMessage(String toast) {
            JSONObject jsonObject;
            Headers header = null;
            String id = "";
            String body = "";
            String cookie = "";
            String method = "";
            Boolean redirect = true;
            String content_type = "";

            if (!toast.isEmpty()){
                try {
                    jsonObject = new JSONObject(toast);

                    if(jsonObject.has("id")) {
                        id = (String) jsonObject.get("id");
                    }
                    if(jsonObject.has("body")){
                        body = (String) jsonObject.get("body");
                    }
                    if(jsonObject.has("method")){
                        method = (String) jsonObject.get("method");
                    }
                    if(jsonObject.has("isRedirect")){
                        redirect = (Boolean) jsonObject.get("isRedirect");
                    }
                    if (jsonObject.has("header")){
                        JSONObject j = (JSONObject) jsonObject.get("header");
                        if(j.has("Content-Type")){
                            content_type = (String) j.get("Content-Type");

                        }
                        Iterator<String> iter = j.keys();
                        Map<String,String> f  = new HashMap<>();
                        while (iter.hasNext()) {
                            String key = iter.next();
                            String value = (String) j.get(key);
                            f.put(key, value);
                        }
                        header = Headers.of(f);
                    }

                    url = (String) jsonObject.get("url");



                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if(!url.contains("http") || !url.contains("https")){
                url = "https://www.youtube.com/watch?v="+url;
            }

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(220, TimeUnit.SECONDS)
                    .writeTimeout(210, TimeUnit.SECONDS)
                    .readTimeout(230, TimeUnit.SECONDS)
                    .followRedirects(redirect)
                    .build();

            Request request;
            if (method.equals("POST")){
                MediaType mediaType = MediaType.parse(content_type);
                RequestBody requestBody = RequestBody.create(mediaType,body);

                request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .addHeader("Content-Type",content_type)
                        .build();
            }else if (url_lama.isEmpty() || header == null){
                request = new Request.Builder()
                        .url(url)
                        .build();
            }else {
                request = new Request.Builder()
                        .headers(header)
                        .url(url)
                        .build();
            }
            url_lama = url;
            try (Response response = client.newCall(request).execute()) {
                byte[] bytes1 = response.headers().toString().getBytes(StandardCharsets.UTF_8);
                String headers = Base64.encodeToString(bytes1,Base64.DEFAULT);

                byte[] bytes = response.body() != null ? response.body().string().getBytes(StandardCharsets.UTF_8) : new byte[0];
                String data = Base64.encodeToString(bytes,Base64.DEFAULT);
                String finalId = id;

                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        webView.evaluateJavascript("onHttpClientResponse('"+ finalId +"',`"+data+"`, `"+headers+"`)",null);
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class JavaScriptInterfaces {
        Context mContext;

        JavaScriptInterfaces(Context c) {
            mContext = c;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @JavascriptInterface
        public void postMessage(String toast) {
            JSONArray jsonArray;
            JSONArray subArray;
            String id = "";
            String oneObjectsItem = null;
            List<Subtitle> subtitleArrayList = new ArrayList<>();
            ArrayList<Subtitle> sub = new ArrayList<>(subtitleArrayList);

            Subtitle s = new Subtitle();
            if (!toast.isEmpty()){
                try {
                    JSONObject object = new JSONObject(toast);
                    jsonArray  = object.getJSONArray("videos");
                    subArray  = object.getJSONArray("subtitles");
                        try {
                            JSONObject oneObject = jsonArray.getJSONObject(0);
                            for (int i = 0; i < subArray.length(); i++) {
                                JSONObject subObject = subArray.getJSONObject(i);

                                if(subObject.has("url") || subObject.has("name")){
                                    s.setUrl(subObject.getString("url"));
                                    s.setLanguage(subObject.getString("name"));
                                    sub.add(s);
                                }
                            }
                            if(oneObject.has("url")) {
                                oneObjectsItem = oneObject.getString("url");
                                if(oneObjectsItem.contains("m3u8")){
                                    if(category.equalsIgnoreCase("tvseries")){
                                        video1.setFileType("hls");
                                    }else{
                                        video.setFileType("hls");
                                    }
                                }
                            }
                            if(category.equalsIgnoreCase("youtube")){
                                video.setFileUrl(oneObjectsItem);
                                Intent playerIntent = new Intent(WebviewActivity.this, PlayerActivity.class);
                                playerIntent.putExtra("id", id);
                                playerIntent.putExtra("videoType", "trailer");
                                playerIntent.putExtra("streamUrl", video.getFileUrl());
                                ArrayList<Video> videoListForIntent = new ArrayList<>(videoList);
                                playerIntent.putExtra("videos", videoListForIntent);
                                playerIntent.putExtra("video", video); //including subtitle lis't
                                playerIntent.putExtra("category", "movie");
                                startActivity(playerIntent);
                                finish();
                            } else if (category.equalsIgnoreCase("tvseries")){
                                video1.setFileUrl(oneObjectsItem);
                                video1.setSubtitle(sub);

                                Intent playerIntent = new Intent(WebviewActivity.this, PlayerActivity.class);
                                playerIntent.putExtra("id", id);
                                playerIntent.putExtra("videoType", video1.getFileType());
                                playerIntent.putExtra("streamUrl", video1.getFileUrl());
                                ArrayList<Video> videoListForIntent = new ArrayList<>(videoList);
                                playerIntent.putExtra("videos", videoListForIntent);
                                Video videoModel = new Video();
                                videoModel.setSubtitle(video1.getSubtitle());
                                playerIntent.putExtra("video", videoModel); //including subtitle lis't
                                playerIntent.putExtra("category", category);
                                startActivity(playerIntent);
                                finish();
                            }else{
                                video.setFileUrl(oneObjectsItem);
                                video.setSubtitle(sub);

                                Intent playerIntent = new Intent(WebviewActivity.this, PlayerActivity.class);
                                playerIntent.putExtra("id", id);
                                playerIntent.putExtra("videoType", video.getFileType());
                                playerIntent.putExtra("streamUrl", video.getFileUrl());
                                ArrayList<Video> videoListForIntent = new ArrayList<>(videoList);
                                playerIntent.putExtra("videos", videoListForIntent);
                                playerIntent.putExtra("video", video); //including subtitle lis't
                                playerIntent.putExtra("category", "movie");
                                startActivity(playerIntent);
                                finish();
                            }

                        } catch (JSONException e) {
                            // Oops
                        }
//                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}




