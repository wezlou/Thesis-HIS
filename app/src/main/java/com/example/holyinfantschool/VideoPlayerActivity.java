package com.example.holyinfantschool;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class VideoPlayerActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_player);

        String videoId = getIntent().getStringExtra("videoId");

        webView = findViewById(R.id.youtubeWebView);

        webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);

        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setMediaPlaybackRequiresUserGesture(false);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());

        String url = "https://www.youtube-nocookie.com/embed/" + videoId
                + "?autoplay=1&controls=1&modestbranding=1";

        String html = "<html><body style='margin:0;padding:0;background:black;'>"
                + "<iframe width='100%' height='100%' "
                + "src='" + url + "' "
                + "frameborder='0' allow='accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture' "
                + "allowfullscreen>"
                + "</iframe>"
                + "</body></html>";

        webView.loadDataWithBaseURL("https://www.youtube-nocookie.com", html, "text/html", "UTF-8", null);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
