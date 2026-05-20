package com.example.smartscanner;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.smartscanner.activities.HistoryActivity;
import com.example.smartscanner.models.ScanModel;
import com.example.smartscanner.utils.HistoryManager;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SmartScanner";
    private static final int CAMERA_REQ_CODE = 101;
    private static final int STORAGE_REQ_CODE = 102;

    private PreviewView previewView;
    private EditText etResult;
    private ImageCapture imageCapture;
    private TextRecognizer textRecognizer;
    private Button btnCapture, btnScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI
        previewView = findViewById(R.id.previewView);
        etResult = findViewById(R.id.tvResult);
        btnCapture = findViewById(R.id.btnCapture);
        btnScan = findViewById(R.id.btnScan);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnHistory = findViewById(R.id.btnHistory);

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        // Permissions logic
        checkCameraPermissionOnStart();

        // 1. Capture Document ab photo lekar auto-scan karega
        btnCapture.setOnClickListener(v -> captureAndExtractText());

        // 2. Scan Button logic: Agar text nahi hai toh Toast dikhayega
        btnScan.setOnClickListener(v -> {
            String currentText = etResult.getText().toString().trim();
            if (currentText.isEmpty() || currentText.equals("Scanning document...")) {
                Toast.makeText(this, "Pehle 'Capture Document' dabayein!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Text pehle hi scan ho chuka hai.", Toast.LENGTH_SHORT).show();
            }
        });
        
        // 3. Save button par Custom Permission Popup
        btnSave.setOnClickListener(v -> showSavePermissionDialog());
        
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
    }

    private void showSavePermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Save Permission")
                .setMessage("SmartScanner document ko PDF mein save karne ki ijazat maang raha hai. Kya aap allow karte hain?")
                .setPositiveButton("Allow", (dialog, which) -> checkStorageAndSave())
                .setNegativeButton("Deny", (dialog, which) -> {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .show();
    }

    private void checkCameraPermissionOnStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQ_CODE);
        } else {
            startCamera();
        }
    }

    private void checkStorageAndSave() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQ_CODE);
            } else {
                saveData();
            }
        } else {
            saveData();
        }
    }

    private void saveData() {
        saveToHistory();
        createPdfInDocuments();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera Setup Error", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void captureAndExtractText() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera not ready!", Toast.LENGTH_SHORT).show();
            return;
        }

        setButtonsEnabled(false);
        etResult.setText("Scanning document...");

        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                processImageForOCR(imageProxy);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                setButtonsEnabled(true);
                Toast.makeText(MainActivity.this, "Error capturing image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void processImageForOCR(ImageProxy imageProxy) {
        if (imageProxy.getImage() != null) {
            InputImage inputImage = InputImage.fromMediaImage(
                    imageProxy.getImage(), 
                    imageProxy.getImageInfo().getRotationDegrees()
            );

            textRecognizer.process(inputImage)
                    .addOnSuccessListener(text -> {
                        setButtonsEnabled(true);
                        imageProxy.close();
                        if (text.getText().isEmpty()) {
                            etResult.setText("");
                            Toast.makeText(this, "Could not find any text. Try again.", Toast.LENGTH_LONG).show();
                        } else {
                            etResult.setText(text.getText());
                        }
                    })
                    .addOnFailureListener(e -> {
                        setButtonsEnabled(true);
                        imageProxy.close();
                        Toast.makeText(this, "OCR Error.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            setButtonsEnabled(true);
            imageProxy.close();
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        btnCapture.setEnabled(enabled);
        btnScan.setEnabled(enabled);
    }

    private void saveToHistory() {
        String resultText = etResult.getText().toString().trim();
        if (resultText.isEmpty() || resultText.equals("Scanning document...")) return;
        
        String date = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date());
        HistoryManager.saveScan(this, new ScanModel(resultText, date));
    }

    private void createPdfInDocuments() {
        String content = etResult.getText().toString().trim();
        if (content.isEmpty() || content.equals("Scanning document...")) return;

        String fileName = "SmartScan_" + System.currentTimeMillis() + ".pdf";

        try {
            OutputStream outputStream = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);

                Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
                if (uri != null) {
                    outputStream = getContentResolver().openOutputStream(uri);
                }
            } else {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), fileName);
                outputStream = new FileOutputStream(file);
            }

            if (outputStream != null) {
                PdfWriter writer = new PdfWriter(outputStream);
                PdfDocument pdf = new PdfDocument(writer);
                Document doc = new Document(pdf);
                doc.add(new Paragraph("Scanner AI Extracted Text\n\n" + content));
                doc.close();
                Toast.makeText(this, "PDF Saved to Documents!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to save PDF", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == CAMERA_REQ_CODE) startCamera();
            if (requestCode == STORAGE_REQ_CODE) saveData();
        }
    }
}