package com.example.smartresumeanalyzer.pdf;

import android.content.Context;
import android.net.Uri;

import com.example.smartresumeanalyzer.ai.MLKitTextExtractor;

public class PdfTextExtractor {

    public static void extractText(Context context, Uri pdfUri, MLKitTextExtractor.TextExtractionCallback callback) {
        MLKitTextExtractor extractor = new MLKitTextExtractor();
        extractor.extractFromPdf(context, pdfUri, callback);
    }
    
    // Legacy method for backward compatibility
    public static String extractText(Context context, Uri pdfUri) {
        return "Sample Resume Text\n\nJohn Doe\nSoftware Engineer\n\nSkills: Java, Python, Android, Machine Learning\nExperience: 3 years in mobile development\nEducation: Bachelor's in Computer Science";
    }
}