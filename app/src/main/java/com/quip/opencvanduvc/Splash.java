package com.quip.opencvanduvc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

public class Splash extends AppCompatActivity {
    private USBMonitor mUSBMonitor;
    private final Object mSync = new Object();

    boolean isUSBAttached = false;
    ConstraintLayout connect_usb_lay, connected_lay;
    ImageView usb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_splash);
           // connect_usb_lay = findViewById(R.id.connect_usb_lay);
           // connected_lay = findViewById(R.id.connected_lay);
           // usb = findViewById(R.id.usb);

            setFullscreen();

            if (checkPermission()) {
                mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);

            } else {
                requestPermission();
            }
           // showConnectUSB();

        new Thread(new Runnable() {
    @Override
    public void run() {
        try {
            Thread.sleep(3000);
            Intent intent = new Intent(Splash.this, DeviceCamera.class);
            startActivity(intent);
            finish();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}).start();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void showConnectUSB() {

        connect_usb_lay.animate().translationYBy(-400).setDuration(2000).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    usbanimate();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    void showConnectedDevice() {

        connected_lay.animate().translationYBy(-400).setDuration(2000).start();
    }

    void usbanimate() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.float_usb);
        usb.startAnimation(animation);
    }

    public void setFullscreen() {

        int flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN;
        flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;


        this.getWindow().getDecorView().setSystemUiVisibility(flags);

    }

    public void exitFullscreen() {

        this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

    }

    @Override
    protected void onStart() {
        super.onStart();
        try {

            synchronized (mSync) {
                if (mUSBMonitor != null) {
                    //mUSBMonitor.register();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {

        synchronized (mSync) {

            if (mUSBMonitor != null) {
                mUSBMonitor.unregister();
                mUSBMonitor.destroy();
                mUSBMonitor = null;
            }
        }
        super.onDestroy();
    }

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener
            = new USBMonitor.OnDeviceConnectListener() {

        @Override
        public void onAttach(final UsbDevice device) {

            isUSBAttached = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connect_usb_lay.animate().translationYBy(400).setDuration(2000).start();
                    showConnectedDevice();
                }
            });

            mUSBMonitor.requestPermission(device);
        }

        @Override
        public void onConnect(final UsbDevice device,
                              final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            mUSBMonitor.unregister();
            Intent intent = new Intent(Splash.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {


        }

        @Override
        public void onDettach(final UsbDevice device) {


        }

        @Override
        public void onCancel(final UsbDevice device) {
            //  setCameraButton(false);
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
                    if(mUSBMonitor==null) {
                    mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
                    }
                    if(!mUSBMonitor.isRegistered()){
                       // mUSBMonitor.register();
                    }
                    // main logic
                } else {

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        showMessageOKCancel("You need to allow access permissions",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermission();
                                    }
                                });
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(Splash.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}