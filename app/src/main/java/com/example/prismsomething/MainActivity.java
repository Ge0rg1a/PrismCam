package com.example.prismsomething;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.prismsomething.gl.CameraRenderer;
import com.example.prismsomething.gl.CameraSurfaceView;
import com.example.prismsomething.gl.FilterType;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CAMERA = 1001;

    private CameraSurfaceView cameraView;
    private LinearLayout filterBar;
    private SeekBar paramBar;
    private ImageButton shutter;
    private Button flipBtn;
    private ImageView thumbnail;

    private ProcessCameraProvider cameraProvider;
    private CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
    private FilterType currentFilter = FilterType.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = findViewById(R.id.cameraView);
        filterBar = findViewById(R.id.filterBar);
        paramBar = findViewById(R.id.paramBar);
        shutter = findViewById(R.id.shutter);
        flipBtn = findViewById(R.id.flip);
        thumbnail = findViewById(R.id.thumbnail);

        cameraView.setReadyListener(this::onGLReady);

        buildFilterChips();

        paramBar.setMax(100);
        paramBar.setProgress(50);
        paramBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cameraView.setParam(progress / 100f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        shutter.setOnClickListener(v -> takePhoto());
        flipBtn.setOnClickListener(v -> flipCamera());

        selectFilter(FilterType.NONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.onResume();
        if (checkCameraPermission()) {
            ensureCamera();
        } else {
            requestCameraPermission();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraView.releaseRenderer();
    }

    private void onGLReady() {
        if (checkCameraPermission()) {
            ensureCamera();
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQ_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ensureCamera();
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.camera_permission_required)
                        .setMessage(R.string.camera_permission_required)
                        .setPositiveButton(R.string.go_to_settings, (d, w) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.fromParts("package", getPackageName(), null));
                            startActivity(intent);
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        }
    }

    private void ensureCamera() {
        if (cameraProvider != null) {
            bindPreview();
            return;
        }
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                cameraProvider = future.get();
                bindPreview();
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview() {
        if (cameraProvider == null) return;
        cameraProvider.unbindAll();
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(cameraView.getRenderer());
        try {
            cameraProvider.bindToLifecycle(this, cameraSelector, preview);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to bind camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void flipCamera() {
        if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
        } else {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        }
        bindPreview();
    }

    private void buildFilterChips() {
        float density = getResources().getDisplayMetrics().density;
        int padH = (int) (16 * density);
        int padV = (int) (6 * density);
        int margin = (int) (8 * density);
        filterBar.removeAllViews();
        for (FilterType type : FilterType.values()) {
            TextView tv = new TextView(this);
            tv.setText(getString(type.nameRes));
            tv.setBackgroundResource(R.drawable.filter_chip_bg);
            tv.setTextColor(getResources().getColor(R.color.white));
            tv.setPadding(padH, padV, padH, padV);
            tv.setOnClickListener(v -> selectFilter(type));
            tv.setTag(type);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(margin);
            tv.setLayoutParams(lp);
            filterBar.addView(tv);
        }
    }

    private void selectFilter(FilterType type) {
        currentFilter = type;
        for (int i = 0; i < filterBar.getChildCount(); i++) {
            View child = filterBar.getChildAt(i);
            child.setSelected(child.getTag() == type);
        }
        if (type.hasParam) {
            paramBar.setVisibility(View.VISIBLE);
            paramBar.setProgress(50);
        } else {
            paramBar.setVisibility(View.GONE);
        }
        cameraView.setFilter(type);
        cameraView.setParam(paramBar.getProgress() / 100f);
    }

    private void takePhoto() {
        shutter.setEnabled(false);
        cameraView.takePhoto(new CameraRenderer.PhotoCallback() {
            @Override
            public void onSaved(Uri uri) {
                runOnUiThread(() -> {
                    shutter.setEnabled(true);
                    updateThumbnail(uri);
                    Toast.makeText(MainActivity.this, R.string.photo_saved, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailed() {
                runOnUiThread(() -> {
                    shutter.setEnabled(true);
                    Toast.makeText(MainActivity.this, R.string.photo_save_failed, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateThumbnail(Uri uri) {
        new Thread(() -> {
            try (InputStream is = getContentResolver().openInputStream(uri)) {
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                if (bitmap == null) return;
                int size = (int) (56 * getResources().getDisplayMetrics().density);
                Bitmap cropped = cropToCircle(bitmap, size);
                runOnUiThread(() -> {
                    if (cropped != null) thumbnail.setImageBitmap(cropped);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private Bitmap cropToCircle(Bitmap bitmap, int size) {
        if (bitmap == null) return null;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int side = Math.min(w, h);
        int x = (w - side) / 2;
        int y = (h - side) / 2;
        Bitmap square = Bitmap.createBitmap(bitmap, x, y, side, side);
        Bitmap scaled = Bitmap.createScaledBitmap(square, size, size, true);
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(scaled, 0, 0, paint);
        return output;
    }
}
