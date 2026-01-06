package com.example.smartresumeanalyzer.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartresumeanalyzer.R;
import com.example.smartresumeanalyzer.ai.MLKitTextExtractor;
import com.example.smartresumeanalyzer.pdf.PdfTextExtractor;

public class MainActivity extends AppCompatActivity {

    private static final int PDF_PICK_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnUpload = findViewById(R.id.btnUploadPdf);
        btnUpload.setOnClickListener(v -> pickPdf());
    }

    private void pickPdf() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        startActivityForResult(intent, PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PDF_PICK_CODE && resultCode == RESULT_OK && data != null) {
            Uri pdfUri = data.getData();

            Toast.makeText(this, "PDF Selected - Processing with ML Kit...", Toast.LENGTH_SHORT).show();

            // Use ML Kit for text extraction
            PdfTextExtractor.extractText(this, pdfUri, new MLKitTextExtractor.TextExtractionCallback() {
                @Override
                public void onSuccess(String extractedText) {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                        intent.putExtra("resume_text", extractedText);
                        startActivity(intent);
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Using fallback extraction", Toast.LENGTH_SHORT).show();
                        String fallbackText = PdfTextExtractor.extractText(MainActivity.this, pdfUri);
                        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                        intent.putExtra("resume_text", fallbackText);
                        startActivity(intent);
                    });
                }
            });
        }
    }
}
