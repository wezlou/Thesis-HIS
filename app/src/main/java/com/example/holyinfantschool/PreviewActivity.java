package com.example.holyinfantschool;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.github.chrisbanes.photoview.PhotoView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.InputStream;
import java.util.Locale;

public class PreviewActivity extends AppCompatActivity {
    public static final String EXTRA_STORED_NAME = "extra_stored_name";
    public static final String EXTRA_DISPLAY_NAME = "extra_display_name";
    public static final String EXTRA_URL = "extra_url";

    private static final String TAG = "PreviewActivity";

    private FrameLayout container;
    private ProgressBar progressBar;
    private TextView titleView;
    private PhotoView imageView;
    private VideoView videoView;
    private WebView webView;
    private ImageButton closeBtn;

    private final OkHttpClient ok = new OkHttpClient();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        container = findViewById(R.id.previewContainer);
        progressBar = findViewById(R.id.previewProgress);
        titleView = findViewById(R.id.previewTitle);
        imageView = findViewById(R.id.previewImage);
        videoView = findViewById(R.id.previewVideo);
        webView = findViewById(R.id.previewWebView);
        closeBtn = findViewById(R.id.closePreview);

        closeBtn.setOnClickListener(v -> finish());

        String url = getIntent().getStringExtra(EXTRA_URL);
        String stored = getIntent().getStringExtra(EXTRA_STORED_NAME);
        String display = getIntent().getStringExtra(EXTRA_DISPLAY_NAME);

        titleView.setText(display != null ? display : (stored != null ? stored : "Preview"));

        if (url != null && !url.isEmpty()) {
            startPreview(url, display);
        } else if (stored != null && !stored.isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
            // generate download url on background thread
            new Thread(() -> {
                try {
                    String generated = BackblazeUploader.generateDownloadUrl(stored);
                    runOnUiThread(() -> startPreview(generated, display));
                } catch (Exception e) {
                    Log.e(TAG, "Failed to get download URL", e);
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(PreviewActivity.this, "Cannot preview file: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    });
                }
            }).start();
        } else {
            Toast.makeText(this, "No file to preview", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void startPreview(String url, String display) {
        progressBar.setVisibility(View.VISIBLE);
        // determine type from extension
        String ext = getExtension(display != null ? display : url);
        if (ext.isEmpty()) ext = getExtension(url);

        if (isImage(ext)) {
            showImage(url);
        } else if (isVideo(ext)) {
            showVideo(url);
        } else if ("pdf".equals(ext)) {
            showPdf(url);
        } else if (isOfficeDoc(ext)) {
            showDoc(url);
        } else {
            // fallback to doc/webview
            showDoc(url);
        }
    }

    private void showImage(String url) {
        hideAll();
        imageView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                Request req = new Request.Builder().url(url).build();
                Response res = ok.newCall(req).execute();
                InputStream in = res.body() != null ? res.body().byteStream() : null;
                final Bitmap bm = in != null ? BitmapFactory.decodeStream(in) : null;
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (bm != null) imageView.setImageBitmap(bm);
                    else Toast.makeText(PreviewActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e(TAG, "image load", e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PreviewActivity.this, "Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void showVideo(String url) {
        hideAll();
        videoView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        try {
            videoView.setVideoURI(Uri.parse(url));
            MediaController mc = new MediaController(this);
            mc.setAnchorView(videoView);
            videoView.setMediaController(mc);
            videoView.requestFocus();
            videoView.setOnPreparedListener(MediaPlayer::start);
            videoView.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(this, "Cannot play video", Toast.LENGTH_SHORT).show();
                return true;
            });
        } catch (Exception e) {
            Toast.makeText(this, "Cannot play video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showPdf(String url) {
        hideAll();
        webView.setVisibility(View.VISIBLE);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);

        try {
            // use android.net.Uri.encode to avoid API lint for URLEncoder
            String encoded = android.net.Uri.encode(url);
            String viewer = "https://docs.google.com/gview?embedded=true&url=" + encoded;
            webView.loadUrl(viewer);
            progressBar.setVisibility(View.GONE);
        } catch (Exception e) {
            webView.loadUrl(url);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showDoc(String url) {
        hideAll();
        webView.setVisibility(View.VISIBLE);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        try {
            String encoded = android.net.Uri.encode(url);
            String viewer = "https://docs.google.com/gview?embedded=true&url=" + encoded;
            webView.loadUrl(viewer);
        } catch (Exception e) {
            webView.loadUrl(url);
        } finally {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void hideAll() {
        imageView.setVisibility(View.GONE);
        videoView.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
    }

    private String getExtension(String s) {
        if (s == null) return "";
        int idx = s.lastIndexOf('.');
        if (idx == -1) return "";
        return s.substring(idx + 1).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private boolean isImage(String ext) {
        return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || ext.equals("gif") || ext.equals("webp");
    }

    private boolean isVideo(String ext) {
        return ext.equals("mp4") || ext.equals("mov") || ext.equals("mkv") || ext.equals("webm") || ext.equals("3gp");
    }

    private boolean isOfficeDoc(String ext) {
        return ext.equals("doc") || ext.equals("docx") || ext.equals("ppt") || ext.equals("pptx") || ext.equals("xls") || ext.equals("xlsx") || ext.equals("txt") || ext.equals("odt") || ext.equals("pdf");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            try { videoView.stopPlayback(); } catch (Exception ignored) {}
        }
    }
}
