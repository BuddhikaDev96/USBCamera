package com.quip.opencvanduvc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.utils.CpuMonitor;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DeviceCamera extends BaseActivity
        implements CameraDialog.CameraDialogParent , CameraBridgeViewBase.CvCameraViewListener2 {

    private static final boolean DEBUG = true;    // TODO set false on release
    private static final String TAG = "Devicecamera Activity";
    ImageProcess imageProcess;
    LottieAnimationView gallerybtn,videobtn,takephotobtn;
    ImageButton tousb,camera_switch_btn;
    ImageView device_camview,zebraback2,zebraimageviewer2,focusback2,histrogramview;
    ConstraintLayout usbcameralayer,devicecameralayer,histrogramlayer;
//
private int PREVIEW_WIDTH = 640;
    private int PREVIEW_HEIGHT = 480;
    private int MAX_FPS = 30;
    private USBMonitor mUSBMonitor;
    protected SurfaceView mResultView;
    private Surface mPreviewSurface;
    private UVCCamera mUVCCamera;
    private boolean isActive, isPreview;
    private final Object mSync = new Object();
    boolean settings_call = true;
    private final CpuMonitor cpuMonitor = new CpuMonitor();
    //


    //
    Mat mRgba,mRgbaF,mRgbaT;
    private CameraBridgeViewBase openCvView;
    private int mCameraId = 0;
    //
    private BaseLoaderCallback mLoaderCallback=new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:{
                    openCvView.enableView();

                }break;
                default:{
                    super.onManagerConnected(status);
                }break;
            }
        }
    };
    private int screen_height,screen_width;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        try{
            setContentView(R.layout.activity_device_camera);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            //OpenCVLoader.initDebug();
            System.loadLibrary("NativeImageProcessor");
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            screen_height = displayMetrics.heightPixels;
            screen_width = displayMetrics.widthPixels;
            imageProcess=new ImageProcess();
            gallerybtn = findViewById(R.id.gallery);
            videobtn = findViewById(R.id.video);
            takephotobtn = findViewById(R.id.click);
            tousb = findViewById(R.id.tousbbtn);
            usbcameralayer=findViewById(R.id.usb_cam_layer);
            devicecameralayer=findViewById(R.id.device_cam_layer);
            device_camview=findViewById(R.id.devicecamview);
            camera_switch_btn=findViewById(R.id.camera_switch_btn);
            zebraimageviewer2=findViewById(R.id.zebraimageviewer2);
            zebraback2=findViewById(R.id.zebraback2);
            focusback2=findViewById(R.id.focusback2);
            histrogramlayer=findViewById(R.id.histrogram_layer);
            histrogramview=findViewById(R.id.histrogram_view);


            openCvView=(JavaCameraView)findViewById(R.id.opencvView);
            openCvView.setCvCameraViewListener(this);
            setFullscreen();
            if (checkPermission()) {
                mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
                mResultView = findViewById(R.id.camera_surface_view1);
                mResultView.getHolder().addCallback(mSurfaceViewCallback);

            } else {
                requestPermission();
            }
        }catch (Exception e){
            Toast.makeText(this, ""+e, Toast.LENGTH_SHORT).show();
        }



        gallerybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gallerybtn.setProgress(0);
                gallerybtn.pauseAnimation();
                gallerybtn.playAnimation();
                Toast.makeText(DeviceCamera.this, "Cheers!!", Toast.LENGTH_SHORT).show();
                //---- Your code here------
            }
        });
        videobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videobtn.setProgress(0);
                videobtn.pauseAnimation();
                videobtn.playAnimation();
                Toast.makeText(DeviceCamera.this, "Cheers!!", Toast.LENGTH_SHORT).show();
                //---- Your code here------
            }
        });
        takephotobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takephotobtn.setProgress(0);
                takephotobtn.pauseAnimation();
                takephotobtn.playAnimation();
                Toast.makeText(DeviceCamera.this, "Cheers!!", Toast.LENGTH_SHORT).show();
                //---- Your code here------
            }
        });
        tousb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(DeviceCamera.this, MainActivity.class);
//                startActivity(intent);
//                finish();

                devicecameralayer.setVisibility(View.GONE);
                usbcameralayer.setVisibility(View.VISIBLE);

            }
        });
        camera_switch_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraId = mCameraId^1; //bitwise not operation to flip 1 to 0 and vice versa
                openCvView.disableView();
                openCvView.setCameraIndex(mCameraId);
                openCvView.enableView();
            }
        });
    }

    public void setFullscreen() {

        int flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN;
        flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;


        this.getWindow().getDecorView().setSystemUiVisibility(flags);

    }

    private final Runnable mCPUMonitorTask = new Runnable() {
        @Override
        public void run() {
            if (cpuMonitor.sampleCpuUtilization()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
            queueEvent(this, 1000);
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        synchronized (mSync) {
            if (mUSBMonitor != null) {
                mUSBMonitor.register();
                queueEvent(mCPUMonitorTask, 1000);

            }
        }
    }

    @Override
    protected void onStop() {
        if (DEBUG) Log.v(TAG, "onStop:");
//        synchronized (mSync) {
//            if (mUSBMonitor != null) {
//                mUSBMonitor.unregister();
//            }
//        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0,this,mLoaderCallback);

        }else{
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(openCvView!=null){
            openCvView.disableView();
        }
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.v(TAG, "onDestroy:");
        if(openCvView!=null){
            openCvView.disableView();
        }
        try {
            synchronized (mSync) {
                isActive = isPreview = false;
                if (mUVCCamera != null) {
                    mUVCCamera.destroy();
                    mUVCCamera = null;
                }
                if (mUSBMonitor != null) {
                    mUSBMonitor.destroy();
                    mUSBMonitor = null;
                }

            }
            mResultView = null;
            super.onDestroy();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener
            = new USBMonitor.OnDeviceConnectListener() {

        @Override
        public void onAttach(final UsbDevice device) {

            mUSBMonitor.requestPermission(device);
            Toast.makeText(DeviceCamera.this, "onAttach", Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onConnect(final UsbDevice device,
                              final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            synchronized (mSync) {
                if (mUVCCamera != null) {
                    mUVCCamera.destroy();
                }
                isActive = isPreview = false;
            }
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    synchronized (mSync) {
                        final UVCCamera camera = new UVCCamera();

                        camera.open(ctrlBlock);
                        try {
                            camera.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT, UVCCamera.FRAME_FORMAT_MJPEG, MAX_FPS);
                        } catch (final IllegalArgumentException e) {
                            try {
                                camera.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT, UVCCamera.FRAME_FORMAT_YUYV, MAX_FPS);
                                com.serenegiant.usb.Size previewSize = camera.getPreviewSize();


                            } catch (final IllegalArgumentException e1) {
                                e1.printStackTrace();
                                camera.destroy();
                                return;
                            }
                        }
                        mPreviewSurface = mResultView.getHolder().getSurface();


                        if (mPreviewSurface != null) {

                            isActive = true;
                            camera.setPreviewDisplay(mPreviewSurface);
                            camera.startPreview();
                            isPreview = true;

                        }
                        synchronized (mSync) {
                            mUVCCamera = camera;


                        }
                    }
                }
            }, 0);



        }
        @Override
        public void onDisconnect(final UsbDevice device,
                                 final USBMonitor.UsbControlBlock ctrlBlock) {
            if (settings_call) {

//                Intent intent = new Intent(DeviceCamera.this, Splash.class);
//                startActivity(intent);
//                finish();
            }
        }

        @Override
        public void onDettach(final UsbDevice device) {
            if (settings_call) {
                Intent intent = new Intent(DeviceCamera.this, Splash.class);
                startActivity(intent);
                finish();
            }
        }

        @Override
        public void onCancel(final UsbDevice device) {
            //  setCameraButton(false);
        }
    };
    private final SurfaceHolder.Callback mSurfaceViewCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(final SurfaceHolder holder) {
            if (DEBUG) Log.v(TAG, "surfaceCreated:");
        }

        @Override
        public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
            if ((width == 0) || (height == 0)) return;

            if (DEBUG) Log.v(TAG, "surfaceChanged:");
            mPreviewSurface = holder.getSurface();
            synchronized (mSync) {
                if (isActive && !isPreview && (mUVCCamera != null)) {
                    mUVCCamera.setPreviewDisplay(mPreviewSurface);
                    mUVCCamera.startPreview();
                    isPreview = true;
                }
            }
        }

        @Override
        public void surfaceDestroyed(final SurfaceHolder holder) {
            if (DEBUG) Log.v(TAG, "surfaceDestroyed:");
            synchronized (mSync) {
                if (mUVCCamera != null) {
                    mUVCCamera.stopPreview();
                }
                isPreview = false;
            }
            mPreviewSurface = null;
        }
    };

    //PERMISSION
    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            return false;
        }
        return true;
    }
    private void requestPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                200);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 200:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // main logic
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel("You need to allow access permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermission();
                                            }
                                        }
                                    });
                        }
                    }
                }
                break;
        }
    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(DeviceCamera.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (DEBUG) Log.v(TAG, "onDialogResult:canceled=" + canceled);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
mRgba=new Mat(height,width, CvType.CV_8UC4);
mRgbaF=new Mat(height,width, CvType.CV_8UC4);
mRgbaT=new Mat(height,width, CvType.CV_8UC4);

    }

    @Override
    public void onCameraViewStopped() {
mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba=inputFrame.rgba();
       if(mCameraId==1) {

       Core.transpose(mRgba,mRgbaT);

        Core.transpose(mRgbaT,mRgba);
        Imgproc.resize(mRgba,mRgbaF,mRgbaF.size(),0,0,0);
        Core.flip(mRgbaF,mRgba,1);
       }
        Bitmap output=Bitmap.createBitmap(mRgba.width(),mRgba.height(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mRgba,output);

        Bitmap histogram = imageProcess.Histogram(output);
      //  Bitmap sthistogram = imageProcess.StanderdHistogram(output);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
             //   Toast.makeText(DeviceCamera.this, histrogramlayer.getWidth()+" "+histrogramlayer.getHeight(), Toast.LENGTH_SHORT).show();
                histrogramlayer.setVisibility(View.VISIBLE);
                device_camview.setImageBitmap(output);
                histrogramview.setImageBitmap(histogram);
            }
        });
        //FOCUS
//        ArrayList<Bitmap> edgesimg = imageProcess.edgesimg(output);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                focusback2.setVisibility(View.VISIBLE);
//                device_camview.setImageBitmap(output);
//                focusback2.setImageBitmap(edgesimg.get(1));
//            }
//        });

        //ZEBRA
//        Bitmap zebra = imageProcess.zebra(output);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                zebraimageviewer2.setVisibility(View.VISIBLE);
//                zebraback2.setVisibility(View.VISIBLE);
//                device_camview.setImageBitmap(output);
//                zebraimageviewer2.setImageBitmap(zebra);
//            }
//        });
        return mRgba;
    }
}