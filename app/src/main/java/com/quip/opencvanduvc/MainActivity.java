package com.quip.opencvanduvc;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.usb.UsbDevice;

import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;


import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.utils.CpuMonitor;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public final class MainActivity extends BaseActivity
        implements CameraDialog.CameraDialogParent {

    private static final boolean DEBUG = true;    // TODO set false on release
    private static final String TAG = "MainActivity";
  //  private int PREVIEW_WIDTH = 1280;
   // private int PREVIEW_HEIGHT = 720;

  private int PREVIEW_WIDTH = 640;
 private int PREVIEW_HEIGHT = 480;
    private int MAX_FPS = 30;
    private USBMonitor mUSBMonitor;
    private USBMonitor mUSBMonitor1;
    protected SurfaceView mResultView;
    private Surface mPreviewSurface;
    private UVCCamera mUVCCamera;
    private boolean isActive, isPreview;
    private final Object mSync = new Object();
    ImageView histogramimgView, edgeimageview, zebraimageview, zebrabakimageview, focusbackimageview, wbtempimageview, wbtintimageview;
    SeekBar focusscaleseekbar, histogramseekbar;


    SeekBar brightnessseekBar, contrastseekBar, saturationseekBar, whitebalancetempseekBar, whitebalancetintseekBar;
    TextView brightnestext, contrasttext, whitebalancetemptext, whitebalancetinttext, saturationtext;

    ImageProcess imageProcess;

    private Matrix matrix = new Matrix();
    Switch focusswitch, focuscolorswitch, zebraswitch;
    ImageButton settings_btn;
    Button focuspeakingbtn, colourbtn, histogrambtn, zebrastripesbtn, zebrastage2_btn, zebrastage1_btn;

    private final CpuMonitor cpuMonitor = new CpuMonitor();
    ConstraintLayout colorsetting_lay, focuspeakinglayout, histogramlayout, zebrastripeslayout, menu_lay, bottom_lay, focusassist_color_change_lay, focusassist_size_change_lay, constraintLayout;


    boolean histypecolor = false;
    boolean settings_call = true;
    boolean ismenu_show = false;
    int screen_height, screen_width;
    int temp=2, tint =5;
    int sharpness = 1;
    int brightness=50,contrast=50,saturation=50;

    float aspect_width;
    float  aspect_height;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.v(TAG, "onCreate:");
        setContentView(R.layout.activity_main);
        OpenCVLoader.initDebug();
        System.loadLibrary("NativeImageProcessor");
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screen_height = displayMetrics.heightPixels;
        screen_width = displayMetrics.widthPixels;
        setFullscreen();
imageProcess=new ImageProcess();

        try {
            if (getIntent().getExtras() != null) {
                int[] res = getIntent().getExtras().getIntArray("res");
                PREVIEW_WIDTH = res[0];
                PREVIEW_HEIGHT = res[1];
                MAX_FPS = res[2];

            } else {
                try {
                    SharedPreferences sharedPrefer = getSharedPreferences("QUIP", Context.MODE_PRIVATE);
                    int fps = sharedPrefer.getInt("fps", 0);
                    int width = sharedPrefer.getInt("width", 0);
                    int height = sharedPrefer.getInt("height", 0);
                    brightness = sharedPrefer.getInt("brightness", 50);
                    contrast = sharedPrefer.getInt("contrast", 50);
                    saturation = sharedPrefer.getInt("saturation", 50);
                    temp = sharedPrefer.getInt("temp", 2);
                    tint = sharedPrefer.getInt("tint", 0);
                    if (fps != 0) {
                        MAX_FPS = fps;
                    } else {
                        sharedPrefer.edit()
                                .putInt("fps", 30)
                                .apply();
                    }
                    if (width != 0) {
                        PREVIEW_WIDTH = width;
                        PREVIEW_HEIGHT = height;
                    } else {
                        sharedPrefer.edit()
                                .putInt("width", PREVIEW_WIDTH)
                                .putInt("height", PREVIEW_HEIGHT)
                                .apply();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //Settings
        settings_btn = findViewById(R.id.settings_btn);
        menu_lay = findViewById(R.id.menu_lay);
        bottom_lay = findViewById(R.id.bottom_lay);

        //COLOUR
        brightnessseekBar = findViewById(R.id.brightnessseekBar);
        brightnestext = findViewById(R.id.brightnesstext);
        contrastseekBar = findViewById(R.id.contrastseekBar);
        contrasttext = findViewById(R.id.contrasttext);
        saturationseekBar = findViewById(R.id.saturationseekBar);
        saturationtext = findViewById(R.id.saturationtext);
        whitebalancetempseekBar = findViewById(R.id.whitebalancetempseekBar);
        whitebalancetintseekBar = findViewById(R.id.whitebalancetintseekBarr);
        whitebalancetemptext = findViewById(R.id.whitebalancetemptext);
        whitebalancetinttext = findViewById(R.id.whitebalancetinttext);
        colorsetting_lay = findViewById(R.id.colorsetting_lay);
        wbtempimageview = findViewById(R.id.wbtempimageview);
        wbtintimageview = findViewById(R.id.wbtintimageview);


        //focus
        edgeimageview = findViewById(R.id.edgeimageviewer);
        focusbackimageview = findViewById(R.id.focusback);
        focusscaleseekbar = findViewById(R.id.focusshapeningseekbar);
        focusswitch = findViewById(R.id.focusswitch);
        focuscolorswitch = findViewById(R.id.focuscolorswitch);
        focusassist_color_change_lay = findViewById(R.id.focusassist_color_change_lay);
        focusassist_size_change_lay = findViewById(R.id.focusassist_size_change_lay);
        focusscaleseekbar.setEnabled(false);
        focuspeakinglayout = findViewById(R.id.focus_peaking_layout);

        //histogram
        histogramimgView = findViewById(R.id.histogram_img);
        histogramseekbar = findViewById(R.id.histogramseekbar);
        constraintLayout = findViewById(R.id.constraintLayout);
        histogramlayout = findViewById(R.id.histogram_layout);


        //zebrastrips
        zebraimageview = findViewById(R.id.zebraimageviewer);
        zebrabakimageview = findViewById(R.id.zebraback);
        zebraswitch = findViewById(R.id.zebraswitch);
        zebrastripeslayout = findViewById(R.id.zebra_stripes_layout);
        zebrastage2_btn = findViewById(R.id.zebrastage2_btn);
        zebrastage1_btn = findViewById(R.id.zebrastage1_btn);
        zebrabtnclose();

        focuspeakingbtn = findViewById(R.id.focus_peaking_btn);
        colourbtn = findViewById(R.id.lut_btn);
        histogrambtn = findViewById(R.id.histogram_btn);
        zebrastripesbtn = findViewById(R.id.zebra_stripes_btn);


        closealllayouts();

        if (checkPermission()) {
            mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
            mResultView = findViewById(R.id.camera_surface_view);
            mResultView.getHolder().addCallback(mSurfaceViewCallback);

        } else {
            requestPermission();
        }
        setAspet(16, 9);

        try {
            //SETTINGS
            settings_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    settings_call = false;
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    finish();
                }
            });


            //FOUCUS
            focuspeakingbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // focus.start();

                    int visibility = focuspeakinglayout.getVisibility();
                    if (visibility == 8) {
                        closealllayouts();
                        focuspeakinglayout.setVisibility(View.VISIBLE);
                        focuspeakingbtn.setBackgroundColor(Color.argb(70, 0, 0, 0));
                        focuspeakingbtn.setTextColor(Color.rgb(255, 255, 255));
                    } else {
                        closealllayouts();

                    }
                }
            });
            focusscaleseekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int progress = seekBar.getProgress();


                    switch (progress) {
                        case 0:

                            imageProcess.scale = 3;

                            break;
                        case 1:
                            imageProcess.scale = 5;

                            break;
                        case 2:
                            imageProcess.scale = 7;

                            break;

                    }
                }
            });
            focuscolorswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    imageProcess.focuscolor = isChecked;

                }
            });
            focusswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        setFocuspeaking();
                        focusassist_color_change_lay.setAlpha(1f);
                        focusassist_size_change_lay.setAlpha(1f);
                        focuscolorswitch.setEnabled(true);
                        focusscaleseekbar.setEnabled(true);

                        //fassitch.setVisibility(View.VISIBLE);
                        // edgechangelay.setVisibility(View.VISIBLE);
                    } else {
                        removeFocuspeaking();
                        focusassist_color_change_lay.setAlpha(0.3f);
                        focusassist_size_change_lay.setAlpha(0.3f);
                        focuscolorswitch.setEnabled(false);
                        focusscaleseekbar.setEnabled(false);
                        // fssetting_lay.setVisibility(View.GONE);
                        //fssetting1_lay.setVisibility(View.GONE);
                        //fassitch.setVisibility(View.GONE);
                        // fassitch.setChecked(false);
                        // edgechangelay.setVisibility(View.GONE);
                    }
                }
            });

            //Histogram
            histogrambtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int visibility = histogramlayout.getVisibility();
                    if (visibility == 8) {
                        closealllayouts();
                        histogramlayout.setVisibility(View.VISIBLE);
                        histogrambtn.setBackgroundColor(Color.argb(70, 0, 0, 0));
                        histogrambtn.setTextColor(Color.rgb(255, 255, 255));

                    } else {
                        closealllayouts();
                    }
                }
            });
            histogramseekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int progress = seekBar.getProgress();
                    removeHistogram();

                    switch (progress) {
                        case 0:
                            removeHistogram();
                            histogramisActive = false;
                            histogramimgView.setVisibility(View.GONE);
                            break;
                        case 1:
                            histypecolor = false;
                            setHistogramking();
                            histogramisActive = true;
                            histogramimgView.setVisibility(View.VISIBLE);
                            break;
                        case 2:
                            histypecolor = true;
                            setHistogramking();
                            histogramisActive = true;
                            histogramimgView.setVisibility(View.VISIBLE);
                            break;

                    }
                }
            });

            //ZEBRA
            zebrastripesbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int visibility = zebrastripeslayout.getVisibility();
                    if (visibility == 8) {
                        closealllayouts();
                        zebrastripeslayout.setVisibility(View.VISIBLE);
                        zebrastripesbtn.setBackgroundColor(Color.argb(70, 0, 0, 0));
                        zebrastripesbtn.setTextColor(Color.rgb(255, 255, 255));
                    } else {
                        closealllayouts();

                    }
                }


            });
            zebraswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        setzebra();
                        zebraisActive = true;
                        imageProcess.zebraprecentage = 175;
                        zebrastage2_btn.setAlpha(1f);
                        zebrastage1_btn.setAlpha(1f);
                    } else {
                        setzebra();
                        zebraisActive = false;
                        removezebra();
                        zebrabtnclose();
                        zebrastage2_btn.setAlpha(0.5f);
                        zebrastage1_btn.setAlpha(0.5f);
                    }
                }
            });
            zebrastage2_btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageProcess.zebraprecentage = 175;
                    zebrabtnclose();
                    zebrastage2_btn.setBackgroundColor(Color.argb(70, 0, 0, 0));
                    zebrastage2_btn.setTextColor(Color.rgb(255, 255, 255));
                }
            });
            zebrastage1_btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageProcess.zebraprecentage = 220;
                    zebrabtnclose();
                    zebrastage1_btn.setBackgroundColor(Color.argb(70, 0, 0, 0));
                    zebrastage1_btn.setTextColor(Color.rgb(255, 255, 255));
                }
            });

            //colour
            colourbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int visibility = colorsetting_lay.getVisibility();
                    if (visibility == 8) {
                        closealllayouts();

                        colourbtn.setBackgroundColor(Color.argb(70, 0, 0, 0));
                        colourbtn.setTextColor(Color.rgb(255, 255, 255));
                        colorsetting_lay.setVisibility(View.VISIBLE);
                        wbtempimageview.setVisibility(View.VISIBLE);
                        wbtintimageview.setVisibility(View.VISIBLE);


                    } else {
                        closealllayouts();
                        colorsetting_lay.setVisibility(View.GONE);
                        //  wbtempimageview.setVisibility(View.GONE);
                        // wbtintimageview.setVisibility(View.GONE);
                    }
                    if (whitebalancetempisActive) {
                        removeWbtemp();
                        removeWbtint();
                    }
                }
            });
            brightnessseekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                    brightness(i);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {


                }
            });
            contrastseekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                    contrast(i);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            saturationseekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                    saturation(i);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            whitebalancetempseekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    temp = i / 10;
                    whitebalancetemptext.setText((i) + "");
                    setWbtemp();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {


                }
            });
            whitebalancetintseekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    tint = i;
                    whitebalancetinttext.setText((tint) + "");
                    setWbtint();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {


                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    void zebrabtnclose() {
        zebrastage1_btn.setBackgroundColor(Color.argb(50, 255, 255, 255));
        zebrastage1_btn.setTextColor(Color.argb(100, 0, 0, 0));
        zebrastage2_btn.setBackgroundColor(Color.argb(50, 255, 255, 255));
        zebrastage2_btn.setTextColor(Color.argb(100, 0, 0, 0));
    }

    float y1, y2;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            y1 = event.getY();
            return false;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {

            y2 = event.getY();

            if (y2 > y1 && (!ismenu_show)) {
                //down
                ismenu_show = true;

                menu_lay.animate().translationY(140).setDuration(500).start();

                if (bottom_lay.getY() == screen_height) {

                    bottom_lay.animate().translationYBy(-280).setDuration(500).start();
                    colorsetting_lay.animate().translationYBy(-750).setDuration(500).start();
                }
            } else if (y2 < y1 && (ismenu_show)) {
//up
                ismenu_show = false;
                menu_lay.animate().translationY(0).setDuration(500).start();
                if (bottom_lay.getY() == screen_height - 280) {
                    bottom_lay.animate().translationYBy(280).setDuration(500).start();
                    colorsetting_lay.animate().translationYBy(750).setDuration(500).start();
                }

            }

            return false;
        }

        return super.onTouchEvent(event);
    }

    void setAspet(float x, float y) {


        if(screen_width <= PREVIEW_WIDTH){
            aspect_width = PREVIEW_WIDTH;
        }else{
            aspect_width = screen_width;
        }
        aspect_height = aspect_width * (9 / 16f);

        if (aspect_height > screen_height) {
            aspect_height = screen_height;
            aspect_width = aspect_height * (16 / 9f);
        }

        System.out.println("screen width: " + screen_width);
        System.out.println("screen height: " + screen_height);

        System.out.println("aspect width: " + aspect_width);
        System.out.println("aspect height: " + aspect_height);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Math.round(aspect_width), Math.round(aspect_height));
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        mResultView.setLayoutParams(layoutParams);
        zebrabakimageview.setLayoutParams(layoutParams);

//        float top , bottom , left , right ;
//        float balancey = height - (aspect_height);
//            System.out.println("balancey=" + balancey);
//
//                top = balancey / 2;
//                bottom = balancey / 2;
//
//
//            float balancex = screen_width - (aspect_width);
//            System.out.println("balancex=" + balancex);
//
//                left = balancex / 2;
//                right = balancex / 2;
//
//
//            Toast.makeText(this, aspect_width + " , " + aspect_height, Toast.LENGTH_SHORT).show();
//
//        System.out.println("screen_width"+screen_width+"screen_height"+height);
//        System.out.println("aspect_width"+aspect_width+"aspect_height"+aspect_height);
//            System.out.println("top="+top+" bottom="+bottom+" left="+left+" right="+right);
//            top_aspect.setMinHeight(Math.round(top));
//            bottom_aspect.setMinHeight(Math.round(bottom));
//            left_aspect.setMinWidth(Math.round(left));
//            right_aspect.setMinWidth(Math.round(right));

//        try {
//            float screen_height = height;
//            float screen_width = width;
//            float top = 0.0f, bottom = 0.0f, left = 0.0f, right = 0.0f;
//            //
//            //logic
//            float aspect_h = y * 10, aspect_w = x * 10;
//
//            System.out.println("aspect_w=" + aspect_w + " aspect_h=" + aspect_h);
//            while (true) {
//                if (((aspect_h * 1.0001) <= screen_height) && (aspect_w * 1.0001) <= screen_width) {
//                    aspect_w = (float) (aspect_w * 1.0001);
//                    aspect_h = (float) (aspect_h * 1.0001);
//                } else {
//                    break;
//                }
//            }
//
//
//            System.out.println("sh=" + screen_height + " aspect_h=" + aspect_h);
//            System.out.println("sw=" + screen_width + " aspect_w=" + aspect_w);
//            float balancey = screen_height - (aspect_h);
//            System.out.println("balancey=" + balancey);
//            if (balancey < screen_height) {
//                top = balancey / 2;
//                bottom = balancey / 2;
//            }
//
//            float balancex = screen_width - (aspect_w);
//            System.out.println("balancex=" + balancex);
//            if (balancex < screen_width) {
//                left = balancex / 2;
//                right = balancex / 2;
//            }
//
//            Toast.makeText(this, aspect_w + " , " + aspect_h, Toast.LENGTH_SHORT).show();
//
//////////////
//            Toast.makeText(this, "top=" + top + " bottom=" + bottom + " left=" + left + " right=" + right, Toast.LENGTH_SHORT).show();
//            //      System.out.println("top="+top+" bottom="+bottom+" left="+left+" right="+right);
//            top_aspect.setMinHeight(Math.round(top));
//            bottom_aspect.setMinHeight(Math.round(bottom));
//            left_aspect.setMinWidth(Math.round(left));
//            right_aspect.setMinWidth(Math.round(right));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

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

    Thread  focus, zebra, his, wbtemp, wbtint;
    private void setzebra() {
        if (!zebraisActive) {
            zebrathread();
            zebraisActive = true;
            zebra.start();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    zebraimageview.setVisibility(View.VISIBLE);
                    zebrabakimageview.setVisibility(View.VISIBLE);
                }
            });


        }
    }
    private void removezebra() {
        zebraisActive = false;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                zebraimageview.setVisibility(View.GONE);

                zebrabakimageview.setVisibility(View.GONE);
            }
        });


    }

    private void setFocuspeaking() {
        if (!focusisActive) {

            focuspeakingthread();
            focusisActive = true;
            edgeimageview.setVisibility(View.VISIBLE);
            focusbackimageview.setVisibility(View.VISIBLE);
            focus.start();

        }
    }
    private void removeFocuspeaking() {
        focusisActive = false;
        focusbackimageview.setVisibility(View.GONE);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                edgeimageview.setVisibility(View.GONE);
                focusbackimageview.setVisibility(View.GONE);
            }
        });

    }

    private void setHistogramking() {
        if (!HistogramisActive) {
            histogramthread();
            HistogramisActive = true;
            histogramimgView.setVisibility(View.VISIBLE);
            his.start();

        }
    }
    private void removeHistogram() {
        HistogramisActive = false;
        histogramimgView.setVisibility(View.GONE);
    }

    private void setWbtemp() {
        if (!whitebalancetempisActive) {
            wbtempthread();
            whitebalancetempisActive = true;
            wbtempimageview.setVisibility(View.VISIBLE);
            wbtemp.start();

        }
    }
    private void removeWbtemp() {
        whitebalancetempisActive = false;
    }

    private void setWbtint() {
        if (!whitebalancetintisActive) {
            whitebalancetintisActive = true;
            wbtinthread();
            wbtintimageview.setVisibility(View.VISIBLE);
            wbtint.start();

        }
    }
    private void removeWbtint() {
        whitebalancetintisActive = false;
    }

    boolean histogramisActive = false;
    boolean focusisActive = false;
    boolean zebraisActive = false;
    boolean HistogramisActive = false;
    boolean whitebalancetempisActive = false;
    boolean whitebalancetintisActive = false;

    public Bitmap drawBitmap() {

        Bitmap surfaceBitmap = Bitmap.createBitmap(mResultView.getWidth(), mResultView.getHeight(), Bitmap.Config.ARGB_8888);
        PixelCopy.OnPixelCopyFinishedListener listener = copyResult -> {
        };
        PixelCopy.request(mResultView, surfaceBitmap, listener, mResultView.getHandler());

//        Mat imageMat = new Mat();
//        Mat imageDest = new Mat();
//
//        Utils.bitmapToMat(surfaceBitmap, imageMat);
//
//        Imgproc.resize(imageMat, imageDest, new Size(480, 270), .5, .5, Imgproc.INTER_AREA);
//        surfaceBitmap = Bitmap.createBitmap(imageDest.cols(), imageDest.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(imageDest, surfaceBitmap);
//
//        imageMat.release();
//        imageDest.release();
//
//        imageMat = new Mat();
//        imageDest = new Mat();
//
//        Utils.bitmapToMat(surfaceBitmap,imageMat);
//
//        Imgproc.resize(imageMat, imageDest, new Size(Math.round(aspect_width), Math.round(aspect_height)), 1, 1, Imgproc.INTER_AREA);
//        surfaceBitmap = Bitmap.createBitmap(imageDest.cols(), imageDest.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(imageDest, surfaceBitmap);

        return surfaceBitmap;
    }
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
    private void zebrathread() {
        zebra = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                try {
                    while (zebraisActive) {


                        Bitmap zebrabitmap = imageProcess.zebra(drawBitmap());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                zebraimageview.setImageBitmap(zebrabitmap);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }
    private void focuspeakingthread() {
        focus = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                try {
                    while (focusisActive) {

                        ArrayList<Bitmap> edgesimg = imageProcess.edgesimg(drawBitmap());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                focusbackimageview.setImageBitmap(edgesimg.get(0));
                                edgeimageview.setImageBitmap(edgesimg.get(2));
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }
    private void histogramthread() {
        his = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                try {
                    while (histogramisActive) {
                        Bitmap histBitmap;
                        histW = constraintLayout.getWidth() * 4;
                        if (histypecolor) {
                            histBitmap = imageProcess.Histogram(drawBitmap());
                        } else {
                            histBitmap = imageProcess.StanderdHistogram(drawBitmap());
                        }


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                histogramimgView.setImageBitmap(histBitmap);


                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }
    private void wbtempthread() {
        wbtemp = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                try {
                    while (whitebalancetempisActive) {
                        Bitmap sharpBitmap = whitebalanceTemp(drawBitmap());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                wbtempimageview.setImageBitmap(sharpBitmap);


                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }
    private void wbtinthread() {
        wbtint = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                try {
                    while (whitebalancetintisActive) {
                        Bitmap sharpBitmap = whitebalanceTint(drawBitmap());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                wbtintimageview.setImageBitmap(sharpBitmap);


                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }


    Mat reduceColors(Mat img, int numRed, int numGreen, int numBlue) {
        Mat redLUT = createLUT(numRed);
        Mat greenLUT = createLUT(numGreen);
        Mat blueLUT = createLUT(numBlue);


        List<Mat> BGR = new ArrayList<>(4);
        Core.split(img, BGR); // splits the image into its channels in the List of Mat arrays.

        Core.LUT(BGR.get(0), blueLUT, BGR.get(0));
        Core.LUT(BGR.get(1), greenLUT, BGR.get(1));
        Core.LUT(BGR.get(2), redLUT, BGR.get(2));
        Core.merge(BGR, img);

        return img;
    }

    Mat reduceColors(Mat img, int numRed, int numGreen, int numBlue, int numalpha) {

        Mat alpha = new Mat(img.height(), img.width(), img.type());
        Mat dst = new Mat();


        Scalar scalar = new Scalar(numRed, numGreen, numBlue, numalpha);
        Mat mat = alpha.setTo(scalar);

//Core.multiply(mat,img,dst);
        return mat;
    }

    Mat createLUT(int numColors) {
        // When numColors=1 the LUT will only have 1 color which is black.
        if (numColors < 0 || numColors > 256) {
            System.out.println("Invalid Number of Colors. It must be between 0 and 256 inclusive.");
            return null;
        }

        Mat lookupTable = Mat.zeros(new Size(1, 256), CvType.CV_8UC1);

        int startIdx = 0;
        for (int x = 0; x < 256; x += 256.0 / numColors) {
            lookupTable.put(x, 0, x);

            for (int y = startIdx; y < x; y++) {
                if (lookupTable.get(y, 0)[0] == 0) {
                    lookupTable.put(y, 0, lookupTable.get(x, 0));
                }
            }
            startIdx = x;
        }
        return lookupTable;
    }

    private void closealllayouts() {
        removeWbtemp();
        removeWbtint();
        colorsetting_lay.setVisibility(View.GONE);
        zebrastripeslayout.setVisibility(View.GONE);
        histogramlayout.setVisibility(View.GONE);

        focuspeakinglayout.setVisibility(View.GONE);
        focuspeakingbtn.setBackgroundColor(Color.argb(50, 255, 255, 255));
        colourbtn.setBackgroundColor(Color.argb(50, 255, 255, 255));
        histogrambtn.setBackgroundColor(Color.argb(50, 255, 255, 255));
        zebrastripesbtn.setBackgroundColor(Color.argb(50, 255, 255, 255));
        settings_btn.setBackgroundColor(Color.argb(50, 255, 255, 255));
        focuspeakingbtn.setTextColor(Color.argb(100, 0, 0, 0));
        colourbtn.setTextColor(Color.argb(100, 0, 0, 0));
        histogrambtn.setTextColor(Color.argb(100, 0, 0, 0));
        zebrastripesbtn.setTextColor(Color.argb(100, 0, 0, 0));


    }

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
    public void onDestroy() {
        if (DEBUG) Log.v(TAG, "onDestroy:");

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
                if (mUSBMonitor1 != null) {
                    mUSBMonitor1.destroy();
                    mUSBMonitor1 = null;
                }
            }
            mResultView = null;
            super.onDestroy();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPrefer = getSharedPreferences("QUIP", Context.MODE_PRIVATE);
        sharedPrefer.edit()
                .putInt("brightness", brightness)
                .putInt("contrast", contrast)
                .putInt("saturation", saturation)
                .putInt("tint", tint)
                .putInt("temp", temp)
                .apply();
    }

    public void setFullscreen() {

        int flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN;
        flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;


        this.getWindow().getDecorView().setSystemUiVisibility(flags);

    }


    private final OnDeviceConnectListener mOnDeviceConnectListener
            = new OnDeviceConnectListener() {

        @Override
        public void onAttach(final UsbDevice device) {

            mUSBMonitor.requestPermission(device);
        }

        @Override
        public void onConnect(final UsbDevice device,
                              final UsbControlBlock ctrlBlock, final boolean createNew) {
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

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                  brightness(brightness);
                                  contrast(contrast);
                            saturation(saturation);
                            setWbtemp();
                            setWbtint();
                            whitebalancetempseekBar.setProgress(temp*10);
                            whitebalancetintseekBar.setProgress(tint);
                            whitebalancetemptext.setText((temp*10) + "");
                            whitebalancetinttext.setText((tint) + "");

                                }
                            });
                        }
                    }
                }
            }, 0);



        }

        @Override
        public void onDisconnect(final UsbDevice device,
                                 final UsbControlBlock ctrlBlock) {
            if (settings_call) {

                Intent intent = new Intent(MainActivity.this, Splash.class);
                startActivity(intent);
                finish();
            }
        }

        @Override
        public void onDettach(final UsbDevice device) {
            if (settings_call) {
                Intent intent = new Intent(MainActivity.this, Splash.class);
                startActivity(intent);
                finish();
            }
        }

        @Override
        public void onCancel(final UsbDevice device) {
            //  setCameraButton(false);
        }
    };

    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (DEBUG) Log.v(TAG, "onDialogResult:canceled=" + canceled);
    }

    //FOCUS
    public ArrayList<Bitmap> edgesimg(Bitmap bitmap) {
        Bitmap original = bitmap;
        bitmap = getResizedBitmap(bitmap, 700);
        ArrayList<Bitmap> ba = new ArrayList<>();
        try {
            Mat src = new Mat();
            Mat src_gray = new Mat();
            Mat dst = new Mat();
            Mat abs_dst = new Mat();
            Mat result = new Mat();
            Utils.bitmapToMat(bitmap, src);
            src.convertTo(src_gray, CvType.CV_64F);
//            Size GaussianBlur_kernel_size = new Size(3, 3);
//            Imgproc.GaussianBlur(src, src, GaussianBlur_kernel_size, 3, 3, Core.BORDER_DEFAULT);
            Imgproc.cvtColor(src, src_gray, Imgproc.COLOR_RGB2GRAY);

            Imgproc.Laplacian(src_gray, dst, CvType.CV_8U, 1, imageProcess.scale, -200, Core.BORDER_DEFAULT);

            //Imgproc.Canny(src_gray,dst,255,255,3);
            Core.convertScaleAbs(dst, abs_dst);
            Bitmap resultBitmap = Bitmap.createBitmap(abs_dst.cols(), abs_dst.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(abs_dst, resultBitmap);
            Bitmap overlay = makeBlackTransparent(resultBitmap);
            Utils.bitmapToMat(overlay, result);
            Mat mat;
            if (imageProcess.focuscolor) {
                mat = reduceColors(result, 0, 0, 50);
            } else {
                mat = reduceColors(result, 0, 50, 0);
            }
            Utils.matToBitmap(mat, overlay);
            ba.add(original);
            ba.add(resultBitmap);
            ba.add(overlay);
        } catch (Exception e) {

            e.printStackTrace();
        }
        return ba;
    }
    private Bitmap makeBlackTransparent(Bitmap image) {
        // convert image to matrix
        Mat src = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC4);
        Utils.bitmapToMat(image, src);

        // init new matrices
        Mat dst = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC4);
        Mat tmp = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC4);
        Mat alpha = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC4);

        // convert image to grayscale
        Imgproc.cvtColor(src, tmp, Imgproc.COLOR_BGR2GRAY);

        // threshold the image to create alpha channel with complete transparency in black background region and zero transparency in foreground object region.
        Imgproc.threshold(tmp, alpha, 100, 255, Imgproc.THRESH_BINARY);

        // split the original image into three single channel.
        List<Mat> rgb = new ArrayList<Mat>(3);
        Core.split(src, rgb);

        // Create the final result by merging three single channel and alpha(BGRA order)
        List<Mat> rgba = new ArrayList<Mat>(4);
        rgba.add(rgb.get(0));
        rgba.add(rgb.get(1));
        rgba.add(rgb.get(2));
        rgba.add(alpha);
        Core.merge(rgba, dst);

        // convert matrix to output bitmap
        Bitmap output = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, output);
        return output;
    }

    //ZEBRA
    private Bitmap makeWhiteTransparent(Bitmap image) {
        // convert image to matrix
        Mat src = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC4);
        Utils.bitmapToMat(image, src);
        // init new matrices
        Mat dst = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC4);
        Mat tmp = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC4);
        Mat alpha = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC4);
        // convert image to grayscale
        Imgproc.cvtColor(src, tmp, Imgproc.COLOR_BGR2GRAY);
        // threshold the image to create alpha channel with complete transparency in white background region and zero transparency in foreground object region.
        Imgproc.threshold(tmp, alpha, imageProcess.zebraprecentage, 255, Imgproc.THRESH_BINARY_INV);
        // split the original image into three single channel.
        List<Mat> rgb = new ArrayList<Mat>(3);
        Core.split(src, rgb);
        // Create the final result by merging three single channel and alpha(BGRA order)

        List<Mat> rgba = new ArrayList<Mat>(4);
        rgba.add(rgb.get(0));
        rgba.add(rgb.get(1));
        rgba.add(rgb.get(2));
        rgba.add(alpha);
        Core.merge(rgba, dst);
        Imgproc.cvtColor(dst, dst, Imgproc.COLOR_RGBA2mRGBA);
        // convert matrix to output bitmap

        Utils.matToBitmap(dst, image);
        //Bitmap bitmap = removeBackground(output);
        return image;
    }
    public Bitmap zebra(Bitmap original) {
        Bitmap zebraBitmap = makeWhiteTransparent(original);
        return zebraBitmap;
    }

    //HISTOGRAM
    int histW = 2048, histH = 550;
    public Bitmap Histogram(Bitmap bitmap) {
        Mat src = new Mat();
        Utils.bitmapToMat(bitmap, src);
        List<Mat> bgrPlanes = new ArrayList<>();
        Core.split(src, bgrPlanes);
        int histSize = 256;
        float[] range = {0, 256}; //the upper boundary is exclusive
        MatOfFloat histRange = new MatOfFloat(range);
        boolean accumulate = false;
        Mat bHist = new Mat(), gHist = new Mat(), rHist = new Mat();
        Imgproc.calcHist(bgrPlanes, new MatOfInt(0), new Mat(), bHist, new MatOfInt(histSize), histRange, accumulate);
        Imgproc.calcHist(bgrPlanes, new MatOfInt(1), new Mat(), gHist, new MatOfInt(histSize), histRange, accumulate);
        Imgproc.calcHist(bgrPlanes, new MatOfInt(2), new Mat(), rHist, new MatOfInt(histSize), histRange, accumulate);

        int binW = (int) Math.round((double) histW / histSize);
        Mat histImage = new Mat(histH, histW, CvType.CV_8UC3, new Scalar(0, 0, 0));
        Core.normalize(bHist, bHist, 0, histImage.rows(), Core.NORM_MINMAX);
        Core.normalize(gHist, gHist, 0, histImage.rows(), Core.NORM_MINMAX);
        Core.normalize(rHist, rHist, 0, histImage.rows(), Core.NORM_MINMAX);
        float[] bHistData = new float[(int) (bHist.total() * bHist.channels())];
        bHist.get(0, 0, bHistData);
        float[] gHistData = new float[(int) (gHist.total() * gHist.channels())];
        gHist.get(0, 0, gHistData);
        float[] rHistData = new float[(int) (rHist.total() * rHist.channels())];
        rHist.get(0, 0, rHistData);
        for (int i = 1; i < histSize; i++) {
            Imgproc.line(histImage, new Point(binW * (i - 1), histH - Math.round(bHistData[i - 1])),
                    new Point(binW * (i), histH - Math.round(bHistData[i])), new Scalar(255, 0, 0), 2);
            Imgproc.line(histImage, new Point(binW * (i - 1), histH - Math.round(gHistData[i - 1])),
                    new Point(binW * (i), histH - Math.round(gHistData[i])), new Scalar(0, 255, 0), 2);
            Imgproc.line(histImage, new Point(binW * (i - 1), histH - Math.round(rHistData[i - 1])),
                    new Point(binW * (i), histH - Math.round(rHistData[i])), new Scalar(0, 0, 255), 2);
        }


        Bitmap resultBitmap = Bitmap.createBitmap(histImage.cols(), histImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(histImage, resultBitmap);

        return resultBitmap;
    }
    public Bitmap StanderdHistogram(Bitmap bitmap) {

        Mat src = new Mat();
        Utils.bitmapToMat(bitmap, src);
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);
        List<Mat> bgrPlanes = new ArrayList<>();
        Core.split(src, bgrPlanes);
        int histSize = 256;
        float[] range = {0, 256}; //the upper boundary is exclusive
        MatOfFloat histRange = new MatOfFloat(range);
        boolean accumulate = false;
        Mat bHist = new Mat();
        Imgproc.calcHist(bgrPlanes, new MatOfInt(0), new Mat(), bHist, new MatOfInt(histSize), histRange, accumulate);


        int binW = (int) Math.round((double) histW / histSize);
        Mat histImage = new Mat(histH, histW, CvType.CV_8UC3, new Scalar(0, 0, 0));
        Core.normalize(bHist, bHist, 0, histImage.rows(), Core.NORM_MINMAX);
        float[] bHistData = new float[(int) (bHist.total() * bHist.channels())];
        bHist.get(0, 0, bHistData);
        for (int i = 1; i < histSize; i++) {
            Imgproc.line(histImage, new Point(binW * (i - 1), histH - Math.round(bHistData[i - 1])),
                    new Point(binW * (i), histH - Math.round(bHistData[i])), new Scalar(153, 153, 153), 2);
        }

        Bitmap resultBitmap = Bitmap.createBitmap(histImage.cols(), histImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(histImage, resultBitmap);

        return resultBitmap;
    }

//    //SHARPNESS
//    public void sharpness(int value) {
//        synchronized (mSync) {
//            if (mUVCCamera != null) {
//                System.out.println("before=" + mUVCCamera.getSharpness());
//                mUVCCamera.resetSharpness();
//                mUVCCamera.updateCameraParams();
//                mUVCCamera.setSharpness(value);
//                mUVCCamera.updateCameraParams();
//                int mSharpnessMax = mUVCCamera.mSharpnessMax;
//                System.out.println("after=" + mUVCCamera.getSharpness());
//                System.out.println("max=" + mSharpnessMax);
//            }
//        }
//    }
//    public Bitmap sharpness(Bitmap original) {
//
//        //  original = BitmapFactory.decodeResource(getResources(),R.drawable.cybertruck);
//        Mat src = new Mat();
//        Mat kernel = new Mat();
//        Utils.bitmapToMat(original, src);
//        Mat dest = new Mat(src.rows(), src.cols(), src.type());
////        Imgproc.GaussianBlur(src, dest, new Size(0,0), 10);
////        Core.addWeighted(src, 1.5, src, -0.5, sharpness, dest);
//        Bitmap resultBitmap = Bitmap.createBitmap(dest.cols(), dest.rows(), Bitmap.Config.ARGB_8888);
////        Utils.matToBitmap(dest,resultBitmap);
//        int kernel_size = 3 + 2 * (sharpness % 5);
//        Mat ones = Mat.ones(kernel_size, kernel_size, CvType.CV_32F);
//        Core.multiply(ones, new Scalar(1 / (double) (kernel_size * kernel_size)), kernel);
//
//        Imgproc.filter2D(src, dest, -1, kernel, new Point(-1, -1), 0.0, Core.BORDER_DEFAULT);
//        Utils.matToBitmap(dest, resultBitmap);
//        return resultBitmap;
//    }

    //TINT
    public Bitmap whitebalanceTint(Bitmap original) {
        Mat src = new Mat();
        Mat mat = new Mat();
        Utils.bitmapToMat(original, src);

        if (tint == 0) {

            mat = reduceColors(src, 0, 0, 0, 0);
        } else if (tint > 0) {
            int f = (tint);
            System.out.println("temp" + tint);
            System.out.println("f=" + f);
            System.out.println("" + tint + "," + 0 + "," + tint);
            mat = reduceColors(src, tint, 0, tint, f);
        } else {
            int f = (tint * -1);
            System.out.println("temp =" + tint);
            System.out.println("f=" + f);
            System.out.println("" + 0 + "," + tint * -1 + "," + 0);
            mat = reduceColors(src, 0, (tint * -1), 0, f);
        }

        Bitmap resultBitmap = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, resultBitmap);

        return resultBitmap;

    }

    //TEMP
    public Bitmap whitebalanceTemp(Bitmap original) {
        Mat src = new Mat();
        Mat mat = new Mat();
        Utils.bitmapToMat(original, src);

        if (temp == 0) {

            mat = reduceColors(src, 0, 0, 0, 0);
        } else if (temp > 0) {
            int f = (temp);
            System.out.println("temp" + temp);
            System.out.println("f=" + f);
            System.out.println("" + temp + "," + 0 + "," + temp);
            mat = reduceColors(src, temp, temp, 0, f);
        } else {
            int f = (temp * -1);
            System.out.println("temp =" + temp);
            System.out.println("f=" + f);
            System.out.println("" + 0 + "," + temp * -1 + "," + 0);
            mat = reduceColors(src, 0, 0, (temp * -1), f);
        }

        Bitmap resultBitmap = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, resultBitmap);

        return resultBitmap;
    }

    //BRIGHTNESS
    public void brightness(int value) {
        synchronized (mSync) {
            if (mUVCCamera != null) {
                mUVCCamera.resetBrightness();
                mUVCCamera.updateCameraParams();
                mUVCCamera.setBrightness(value);
                mUVCCamera.updateCameraParams();
                brightnestext.setText(value + "%");
                brightnessseekBar.setProgress(value);
                brightness=value;
            }
        }
    }

    //CONTRAST
    public void contrast(int value) {
        synchronized (mSync) {
            if (mUVCCamera != null) {
                mUVCCamera.resetContrast();
                mUVCCamera.updateCameraParams();
                mUVCCamera.setContrast(value);
                mUVCCamera.updateCameraParams();
                contrasttext.setText(value + "%");
                contrastseekBar.setProgress(value);
                contrast=value;
            }
        }
    }

    //SATURATION
    public void saturation(int value) {
        synchronized (mSync) {
            if (mUVCCamera != null) {
                System.out.println("before=" + mUVCCamera.getSaturation());
                mUVCCamera.resetSaturation();
                mUVCCamera.updateCameraParams();
                mUVCCamera.setSaturation(value);
                mUVCCamera.updateCameraParams();
                saturationtext.setText(value+ "%");
                saturationseekBar.setProgress(value);
                saturation=value;
            }
        }
    }

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
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}
