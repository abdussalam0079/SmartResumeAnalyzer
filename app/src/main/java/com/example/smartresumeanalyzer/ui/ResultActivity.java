package com.example.smartresumeanalyzer.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartresumeanalyzer.R;
import com.example.smartresumeanalyzer.ai.JobMatcher;
import com.example.smartresumeanalyzer.ai.LanguageDetector;
import com.example.smartresumeanalyzer.ai.ResumeMatcher;
import com.example.smartresumeanalyzer.ai.ResumeRater;
import com.example.smartresumeanalyzer.ai.SkillsExtractor;
import com.example.smartresumeanalyzer.utils.PdfExporter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class ResultActivity extends AppCompatActivity {
    
    private String resumeText;
    private List<String> extractedSkills;
    private int resumeScore;
    private int jobMatchScore = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        
        resumeText = getIntent().getStringExtra("resume_text");
        
        initViews();
        analyzeResume();
        setupClickListeners();
    }
    
    private void initViews() {
        TextView resultText = findViewById(R.id.resultText);
        resultText.setText(resumeText);
    }
    
    private void analyzeResume() {
        // Extract skills
        extractedSkills = SkillsExtractor.extractSkills(resumeText);
        
        // Calculate resume score
        resumeScore = ResumeRater.calculateScore(resumeText, extractedSkills);
        
        // Update UI
        TextView tvScore = findViewById(R.id.tvScore);
        tvScore.setText(String.valueOf(resumeScore));
        
        // Setup skills RecyclerView
        RecyclerView rvSkills = findViewById(R.id.rvSkills);
        rvSkills.setLayoutManager(new FlexibleGridLayoutManager(this, 3));
        rvSkills.setAdapter(new SkillsAdapter(extractedSkills));
    }
    
    private void setupClickListeners() {
        MaterialButton btnAnalyzeJob = findViewById(R.id.btnAnalyzeJob);
        MaterialButton btnExportPdf = findViewById(R.id.btnExportPdf);
        MaterialButton btnNewAnalysis = findViewById(R.id.btnNewAnalysis);
        
        btnAnalyzeJob.setOnClickListener(v -> analyzeJobMatch());
        btnExportPdf.setOnClickListener(v -> exportToTxt());
        btnNewAnalysis.setOnClickListener(v -> startNewAnalysis());
    }
    
    private void analyzeJobMatch() {
        TextInputEditText etJobDescription = findViewById(R.id.etJobDescription);
        String jobDescription = etJobDescription.getText().toString().trim();
        
        if (jobDescription.isEmpty()) {
            Toast.makeText(this, "Please enter job description", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Use AI-powered matching
        ResumeMatcher matcher = new ResumeMatcher(this);
        float aiScore = matcher.matchResume(resumeText, jobDescription);
        jobMatchScore = (int)(aiScore * 100);
        
        TextView tvJobMatch = findViewById(R.id.tvJobMatch);
        tvJobMatch.setText(jobMatchScore + "%");
        
        Toast.makeText(this, "AI-powered job match analyzed!", Toast.LENGTH_SHORT).show();
    }
    
    private void exportToTxt() {
        String filePath = PdfExporter.exportAnalysis(this, resumeText, extractedSkills, resumeScore, jobMatchScore);
        
        if (filePath != null) {
            Toast.makeText(this, "Report exported to Downloads", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void startNewAnalysis() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}