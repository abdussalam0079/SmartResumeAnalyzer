package com.example.smartresumeanalyzer.ai;

import android.content.Context;

import org.tensorflow.lite.Interpreter;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.io.FileInputStream;

public class ResumeMatcher {

    private Interpreter interpreter;
    private static final int INPUT_SIZE = 512;

    public ResumeMatcher(Context context) {
        try {
            interpreter = new Interpreter(loadModel(context), null);
        } catch (Exception e) {
            e.printStackTrace();
            interpreter = null;
        }
    }

    private MappedByteBuffer loadModel(Context context) {
        try {
            FileInputStream fis = new FileInputStream(
                context.getAssets().openFd("resume_matcher.tflite").getFileDescriptor());
            FileChannel channel = fis.getChannel();
            long startOffset = context.getAssets().openFd("resume_matcher.tflite").getStartOffset();
            long declaredLength = context.getAssets().openFd("resume_matcher.tflite").getDeclaredLength();
            return channel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        } catch (Exception e) {
            throw new RuntimeException("Model load failed: " + e.getMessage());
        }
    }

    public float matchResume(String resumeText, String jobDescription) {
        if (interpreter == null) {
            // Fallback to simple text matching
            return simpleTextMatch(resumeText, jobDescription);
        }
        
        try {
            // Generate embeddings
            float[] resumeEmbedding = TextEmbedding.generateEmbedding(resumeText);
            float[] jobEmbedding = TextEmbedding.generateEmbedding(jobDescription);
            
            // Combine embeddings (concatenate or difference)
            float[] combinedInput = combineEmbeddings(resumeEmbedding, jobEmbedding);
            
            // Run inference
            float[][] output = new float[1][1];
            interpreter.run(new float[][]{combinedInput}, output);
            
            return output[0][0];
        } catch (Exception e) {
            e.printStackTrace();
            return simpleTextMatch(resumeText, jobDescription);
        }
    }
    
    private float[] combineEmbeddings(float[] resume, float[] job) {
        float[] combined = new float[INPUT_SIZE];
        
        // Use first 256 for resume, next 256 for job
        System.arraycopy(resume, 0, combined, 0, 256);
        System.arraycopy(job, 0, combined, 256, 256);
        
        return combined;
    }
    
    private float simpleTextMatch(String resume, String job) {
        float[] resumeEmb = TextEmbedding.generateEmbedding(resume);
        float[] jobEmb = TextEmbedding.generateEmbedding(job);
        return TextEmbedding.cosineSimilarity(resumeEmb, jobEmb);
    }
}