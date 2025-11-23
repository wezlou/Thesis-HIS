package com.example.holyinfantschool;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PdfPreviewActivity extends AppCompatActivity {
    public static final String EXTRA_URL = "extra_pdf_url";
    private ImageView pageView;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private ParcelFileDescriptor parcelFileDescriptor;
    private ImageButton nextBtn, prevBtn;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_preview);
        pageView = findViewById(R.id.pdfPageImage);
        nextBtn = findViewById(R.id.pdfNext);
        prevBtn = findViewById(R.id.pdfPrev);

        String url = getIntent().getStringExtra(EXTRA_URL);
        if (url == null) { finish(); return; }

        new Thread(() -> {
            try {
                // download temp file
                File tmp = File.createTempFile("preview", ".pdf", getCacheDir());
                URL u = new URL(url);
                HttpURLConnection c = (HttpURLConnection) u.openConnection();
                c.connect();
                InputStream in = c.getInputStream();
                FileOutputStream fos = new FileOutputStream(tmp);
                byte[] buf = new byte[8192];
                int r;
                while ((r = in.read(buf)) != -1) fos.write(buf, 0, r);
                fos.close();
                in.close();

                parcelFileDescriptor = ParcelFileDescriptor.open(tmp, ParcelFileDescriptor.MODE_READ_ONLY);
                pdfRenderer = new PdfRenderer(parcelFileDescriptor);
                runOnUiThread(() -> showPage(0));

                runOnUiThread(() -> {
                    nextBtn.setOnClickListener(v -> showPage(currentIndex + 1));
                    prevBtn.setOnClickListener(v -> showPage(currentIndex - 1));
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(this::finish);
            }
        }).start();
    }

    private void showPage(int index) {
        if (pdfRenderer == null) return;
        if (index < 0 || index >= pdfRenderer.getPageCount()) return;
        if (currentPage != null) currentPage.close();
        currentPage = pdfRenderer.openPage(index);
        Bitmap bmp = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(), Bitmap.Config.ARGB_8888);
        currentPage.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        pageView.setImageBitmap(bmp);
        currentIndex = index;
    }

    @Override
    protected void onDestroy() {
        try { if (currentPage != null) currentPage.close(); } catch (Exception ignored){}
        try { if (pdfRenderer != null) pdfRenderer.close(); } catch (Exception ignored){}
        try { if (parcelFileDescriptor != null) parcelFileDescriptor.close(); } catch (Exception ignored){}
        super.onDestroy();
    }
}
