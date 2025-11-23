package com.example.holyinfantschool;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;

public class PreviewActivity extends AppCompatActivity {

    public static final String EXTRA_STORED_NAME = "storedName";
    public static final String EXTRA_DISPLAY_NAME = "displayName";

    private View imageContainer, pdfContainer, videoContainer, docContainer;
    private PhotoView photoView;
    private ImageView pdfImage;
    private VideoView videoView;
    private ProgressBar loader;
    private TextView fileName;

    private ExoPlayer exoPlayer;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        imageContainer = findViewById(R.id.imageContainer);
        pdfContainer = findViewById(R.id.pdfContainer);
        videoContainer = findViewById(R.id.videoContainer);
        docContainer = findViewById(R.id.docContainer);

        photoView = findViewById(R.id.photoView);
        pdfImage = findViewById(R.id.pdfImage);
        videoView = findViewById(R.id.videoView);
        loader = findViewById(R.id.previewLoader);
        fileName = findViewById(R.id.fileName);

        String storedName = getIntent().getStringExtra(EXTRA_STORED_NAME);
        String displayName = getIntent().getStringExtra(EXTRA_DISPLAY_NAME);

        fileName.setText(displayName);

        loadFile(storedName);

        ImageButton closeBtn = findViewById(R.id.closePreview);
        closeBtn.setOnClickListener(v -> finish());

        gestureDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        finish();
                        return true;
                    }
                });
    }

    private void loadFile(String stored) {
        new Thread(() -> {
            try {
                String url = BackblazeUploader.generateDownloadUrl(stored);

                URLConnection conn = new URL(url).openConnection();
                File temp = new File(getCacheDir(), stored);
                FileOutputStream out = new FileOutputStream(temp);
                byte[] buf = new byte[4096];
                int n;

                while ((n = conn.getInputStream().read(buf)) > 0) {
                    out.write(buf, 0, n);
                }

                out.close();

                String ext = stored.toLowerCase();

                runOnUiThread(() -> {
                    loader.setVisibility(View.GONE);

                    if (ext.endsWith(".jpg") || ext.endsWith(".jpeg") || ext.endsWith(".png") || ext.endsWith(".webp")) {
                        previewImage(temp);
                    } else if (ext.endsWith(".mp4") || ext.endsWith(".webm") || ext.endsWith(".mkv")) {
                        previewVideo(temp);
                    } else if (ext.endsWith(".pdf")) {
                        previewPdf(temp);
                    } else {
                        previewDoc(temp);
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void previewImage(File temp) {
        imageContainer.setVisibility(View.VISIBLE);
        photoView.setImageURI(Uri.fromFile(temp));
    }

    private void previewVideo(File temp) {
        videoContainer.setVisibility(View.VISIBLE);
        exoPlayer = new ExoPlayer.Builder(this).build();
        videoView.setVideoURI(Uri.fromFile(temp));
        videoView.start();
    }

    private void previewPdf(File temp) {
        pdfContainer.setVisibility(View.VISIBLE);
        try {
            ParcelFileDescriptor fd = ParcelFileDescriptor.open(temp, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer renderer = new PdfRenderer(fd);
            PdfRenderer.Page page = renderer.openPage(0);

            Bitmap bmp = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            pdfImage.setImageBitmap(bmp);

            page.close();
            renderer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void previewDoc(File temp) {
        docContainer.setVisibility(View.VISIBLE);
        TextView t = findViewById(R.id.docText);
        t.setText("Cannot preview file.\nTap to download.");
        t.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.fromFile(temp), "*/*");
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(i);
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}
