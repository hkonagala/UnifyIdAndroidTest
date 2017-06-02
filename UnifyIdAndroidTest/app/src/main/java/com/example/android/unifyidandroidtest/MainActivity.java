package com.example.android.unifyidandroidtest;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback {

    Button btTakePictures;

    Camera.PictureCallback jpegPictureCallback;
    Camera camera;
    int index = 0;
    List<String> bitmaps;
    private Handler mHandler;
    int interval = 500; // 0.5s = 500 milliseconds
    SurfaceView surfaceView;
    private boolean safeToTakePicture = true;
    DBHelper mDbHelper;
    private SQLiteDatabase db;
    private String currentTimestamp;
    private CameraManager cManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDbHelper = new DBHelper(getApplicationContext());
        db = mDbHelper.getWritableDatabase();
        bitmaps = new ArrayList<>();
        btTakePictures = (Button) findViewById(R.id.bt_takePictures);
        mHandler = new Handler();
        //surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceView = new SurfaceView(this);
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA},
                100);
        cManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for(final String cameraId : cManager.getCameraIdList()){
                CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if(cOrientation == CameraCharacteristics.LENS_FACING_FRONT){
                    camera = Camera.open(Integer.parseInt(cameraId));
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        try {
            surfaceView.getHolder().addCallback(this);
            camera.setPreviewDisplay(surfaceView.getHolder());
            Camera.Parameters parameters = camera.getParameters();
            camera.setParameters(parameters);
            SurfaceTexture st = new SurfaceTexture(MODE_PRIVATE);
            try {
                camera.setPreviewTexture(st);
            } catch (IOException e) {
                e.printStackTrace();
            }
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
        jpegPictureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                SurfaceTexture st = new SurfaceTexture(MODE_PRIVATE);
                try {
                    camera.setPreviewTexture(st);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                camera.startPreview();
                if (data == null) {
                    safeToTakePicture = true;
                    return;
                }
                String img = Base64.encodeToString(data, Base64.NO_WRAP);
                bitmaps.add(index, img);
                ContentValues values = new ContentValues();
                values.put("timestamp", currentTimestamp);
                values.put("image", img);
                db.insert("PICTURES", null, values);
                safeToTakePicture = true;
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        btTakePictures.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        btTakePictures.setOnClickListener(null);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.bt_takePictures:
                startTakingPictures();
                break;
        }
    }

    Runnable task = new Runnable() {
        @Override
        public void run() {
            try {
                if (camera != null) {
                    if (safeToTakePicture) {
                        camera.takePicture(null, null, jpegPictureCallback);
                        safeToTakePicture = false;
                    }
                }
            } finally {
                int numberOfImages = 10;
                if (index < numberOfImages) {
                    mHandler.postDelayed(task, interval);
                    index = index + 1;
                } else{
                    index = 0;
                    stopTakingPictures();
                }
            }
        }
    };

    void startTakingPictures() {
        Long tsLong = System.currentTimeMillis()/1000;
        currentTimestamp = tsLong.toString();
        if (camera != null) camera.release();
        try {
            for(final String cameraId : cManager.getCameraIdList()){
                CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if(cOrientation == CameraCharacteristics.LENS_FACING_FRONT){
                    camera = Camera.open(Integer.parseInt(cameraId));
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        task.run();
    }

    void stopTakingPictures() {
        mHandler.removeCallbacks(task);
        camera.stopPreview();
        if (camera != null) camera.release();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (camera != null) camera.release();
        try {
            for(final String cameraId : cManager.getCameraIdList()){
                CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if(cOrientation == CameraCharacteristics.LENS_FACING_FRONT){
                    camera = Camera.open(Integer.parseInt(cameraId));
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        SurfaceTexture st = new SurfaceTexture(MODE_PRIVATE);
        try {
            camera.setPreviewTexture(st);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
        safeToTakePicture = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        SurfaceTexture st = new SurfaceTexture(MODE_PRIVATE);
        try {
            camera.setPreviewTexture(st);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
        safeToTakePicture = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            // Call stopPreview() to stop updating the preview surface.
            camera.stopPreview();
        }

    }


}
