package com.example.holyinfantschool;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;

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

        // 🔙 Back button
        ImageView back = findViewById(R.id.backButton);
        back.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        // ⚙ WebView settings
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

        // 📌 Swipe Down to Exit
        webView.setOnTouchListener(new View.OnTouchListener() {
            float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        startY = event.getY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float diff = event.getY() - startY;

                        if (diff > 150) {
                            finish();
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            return true;
                        }
                        break;
                }

                return false;
            }
        });

        FrameLayout videoFrame = findViewById(R.id.videoFrame);
        videoFrame.post(() -> {
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            int videoHeight = (int) (screenHeight * 0.50);
            videoFrame.getLayoutParams().height = videoHeight;
            videoFrame.requestLayout();
        });

        // 🌐 Load YouTube
        String url = "https://www.youtube-nocookie.com/embed/" + videoId
                + "?autoplay=1&controls=1&modestbranding=1";

        String html = "<html>" +
                "<head>" +
                "<style>" +
                "body { margin:0; padding:0; overflow:hidden; background:#000; }" +
                "iframe { width:100vw; height:100vh; border:none; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<iframe " +
                "src='" + url + "' " +
                "allow='accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture' " +
                "allowfullscreen>" +
                "</iframe>" +
                "</body>" +
                "</html>";

        webView.loadDataWithBaseURL(
                "https://www.youtube-nocookie.com",
                html,
                "text/html",
                "UTF-8",
                null
        );
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
