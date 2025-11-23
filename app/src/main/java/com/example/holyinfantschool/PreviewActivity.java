package com.example.holyinfantschool;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import android.view.View;


public class PreviewActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_DISPLAY_NAME = "extra_display_name";

    FrameLayout container;
    ProgressBar progress;
    PhotoView imageView;
    VideoView videoView;
    ImageButton closeBtn;
    TextView titleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        container = findViewById(R.id.previewContainer);
        progress = findViewById(R.id.previewProgress);
        imageView = findViewById(R.id.previewImage);
        videoView = findViewById(R.id.previewVideo);
        closeBtn = findViewById(R.id.closePreview);
        titleView = findViewById(R.id.previewTitle);

        closeBtn.setOnClickListener(v -> finish());

        String url = getIntent().getStringExtra(EXTRA_URL);
        String name = getIntent().getStringExtra(EXTRA_DISPLAY_NAME);

        titleView.setText(name);

        if (url == null) {
            Toast.makeText(this, "Invalid file", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        previewFile(url, name);
    }

    private void previewFile(String url, String name) {
        progress.setVisibility(View.VISIBLE);

        String ext = getExtension(name);
        if (ext.isEmpty()) ext = getExtension(url);

        ext = ext.toLowerCase();

        if (isImage(ext)) {
            loadImage(url);
        } else if (isVideo(ext)) {
            loadVideo(url);
        } else if (ext.equals("pdf")) {
            loadPdf(url, name);
        } else {
            downloadAndOpenExternally(url, name);
        }
    }

    // ------------------- IMAGE (universal) -------------------
    private void loadImage(String url) {
        hideAll();
        imageView.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                // 1. Download file completely into cache
                File out = new File(getCacheDir(), "preview_img_" + System.currentTimeMillis() + ".jpg");

                InputStream in = new URL(url).openStream();
                FileOutputStream fo = new FileOutputStream(out);

                byte[] buf = new byte[4096];
                int n;
                while ((n = in.read(buf)) != -1) {
                    fo.write(buf, 0, n);
                }

                fo.close();
                in.close();

                // 2. Decode from the FILE (MTK safe)
                Bitmap bitmap = BitmapFactory.decodeFile(out.getAbsolutePath());

                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);

                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    } else {
                        Toast.makeText(this, "Image decode failed (MTK)", Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // ------------------- VIDEO (universal) -------------------
    private void loadVideo(String url) {
        hideAll();
        progress.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);

        try {
            videoView.setVideoURI(Uri.parse(url));
            MediaController mc = new MediaController(this);
            videoView.setMediaController(mc);
            videoView.start();
        } catch (Exception e) {
            Toast.makeText(this, "Cannot play video", Toast.LENGTH_SHORT).show();
        }
    }

    // ------------------- PDF (MTK SAFE) -------------------
    private void loadPdf(String url, String name) {
        hideAll();

        new Thread(() -> {
            try {
                // Download PDF into cache
                File pdfFile = downloadTemp(url, name);

                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    openPdfNative(pdfFile);
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "PDF load failed", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void openPdfNative(File file) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No PDF viewer installed", Toast.LENGTH_LONG).show();
        }
    }

    // ------------------- DOC / PPT / XLS fallback -------------------
    private void downloadAndOpenExternally(String url, String name) {
        hideAll();
        new Thread(() -> {
            try {
                File file = downloadTemp(url, name);

                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    openExternal(file);
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Cannot open file", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void openExternal(File file) {
        try {
            String mime = getMime(file.getName());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), mime);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No app installed to open this file", Toast.LENGTH_LONG).show();
        }
    }

    // ------------------- HELPERS -------------------
    private File downloadTemp(String url, String name) throws Exception {
        InputStream in = new URL(url).openStream();
        File outFile = new File(getCacheDir(), name);
        FileOutputStream out = new FileOutputStream(outFile);

        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);

        out.close();
        in.close();
        return outFile;
    }

    private void hideAll() {
        imageView.setVisibility(View.GONE);
        videoView.setVisibility(View.GONE);
    }

    private String getExtension(String name) {
        int i = name.lastIndexOf('.');
        if (i == -1) return "";
        return name.substring(i + 1);
    }

    private boolean isImage(String ext) {
        return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png")
                || ext.equals("gif") || ext.equals("webp");
    }

    private boolean isVideo(String ext) {
        return ext.equals("mp4") || ext.equals("mov") || ext.equals("mkv")
                || ext.equals("3gp") || ext.equals("webm");
    }

    private String getMime(String filename) {
        String ext = getExtension(filename);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
    }
}
