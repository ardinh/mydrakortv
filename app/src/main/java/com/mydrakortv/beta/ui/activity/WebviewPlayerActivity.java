package com.mydrakortv.beta.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.mydrakortv.beta.R;
import com.mydrakortv.beta.model.Episode;
import com.mydrakortv.beta.model.Video;
import com.mydrakortv.beta.utils.ToastMsg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.RequiresApi;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.view.View.VISIBLE;

public class WebviewPlayerActivity extends Activity {
    private static final String TAG = "WebViewPlayerActivity";
    private static final String CLASS_NAME = "com.mydrakortv.beta.ui.activity.WebviewPlayerActivity";
    public WebView webView;
    public ProgressBar pb;
    private String url = "";
    private String url_lama = "";
    private String parser = "";
    private String category = "";
    private Video video = null;
    private Episode video1 = null;
    List<Video> videoList = new ArrayList<>();
    Headers header = null;

    @SuppressLint("SetJavaScriptEnabled")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webviewplayer);


        url = getIntent().getStringExtra("url");

        pb = findViewById(R.id.progress_bar_webview_player);
        pb.setVisibility(VISIBLE);

        webView = (WebView) findViewById(R.id.webView_player);
        webView.setVisibility(View.INVISIBLE);
//        webView.loadUrl(url);
        String summary = "<iframe width='560' height='315' src='"+url+"?fs=0"+"' frameborder='0' allowfullscreen></iframe>";
        webView.loadData(summary, "text/html", null);
        webView.setWebViewClient(new WebViewClient());
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webView.getSettings().setAppCacheEnabled(false);
        webView.getSettings().setJavaScriptEnabled(true);
        WebSettings ws = webView.getSettings();
        ws.setAllowContentAccess(true);
        webView.setWebViewClient(new WebViewClient(){

            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//                Log.e(TAG, "shouldOverrideUrlLoading: "+request);
                return super.shouldOverrideUrlLoading(view, request);
            }
            public void onPageFinished(WebView view, String weburl){
//                Log.e(TAG, "onPageFinished: "+weburl);

                view.loadUrl("javascript:(function() { document.getElementsByClassName('ytp-large-play-button ytp-button')[0].click(); })()");
//                long delta = 100;
//                long downTime = SystemClock.uptimeMillis();
//                float x = view.getLeft() + (view.getWidth()/2);
//                float y = view.getTop() + (view.getHeight()/2);
//
//                MotionEvent tapDownEvent = MotionEvent.obtain(downTime, downTime + delta, MotionEvent.ACTION_DOWN, x, y, 0);
//                tapDownEvent.setSource(InputDevice.SOURCE_CLASS_POINTER);
//                MotionEvent tapUpEvent = MotionEvent.obtain(downTime, downTime + delta + 2, MotionEvent.ACTION_UP, x, y, 0);
//                tapUpEvent.setSource(InputDevice.SOURCE_CLASS_POINTER);
//
//                view.dispatchTouchEvent(tapDownEvent);
//                view.dispatchTouchEvent(tapUpEvent);
                webView.setVisibility(VISIBLE);
                pb.setVisibility(View.GONE);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                webView.setVisibility(View.GONE);
                pb.setVisibility(VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
//                Log.e(TAG, "onReceivedError: "+error);
                super.onReceivedError(view, request, error);
            }
        });
    }

}




