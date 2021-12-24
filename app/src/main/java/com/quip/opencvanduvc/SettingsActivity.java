package com.quip.opencvanduvc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
int w,h;
    LinearLayout settings_main,resolution_layer;
    ImageButton setting_close_btn;
    Button resolution_btn,resolution_back_btn,fhd_btn,hd_btn,settings_main_close_btn,presets_btn;
    Switch fpsswitch;
    ConstraintLayout settings_layer;
    private int MAX_FPS = 30;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setting_close_btn = findViewById(R.id.setting_close_btn);
        settings_layer = findViewById(R.id.settings_layer);
        resolution_btn = findViewById(R.id.resolution_btn);
        settings_main = findViewById(R.id.settings_main);
        resolution_layer = findViewById(R.id.resolution_layer);
        resolution_back_btn = findViewById(R.id.resolution_back_btn);
        presets_btn = findViewById(R.id.presets_btn);
        fhd_btn = findViewById(R.id.fhd_btn);
        hd_btn = findViewById(R.id.hd_btn);
        fpsswitch = findViewById(R.id.fpsswitch);
        settings_main_close_btn = findViewById(R.id.settings_main_close_btn);
        SharedPreferences sharedPrefer = getSharedPreferences("QUIP", Context.MODE_PRIVATE);
        int fps = sharedPrefer.getInt("fps", 0);
        if(fps!=0){
            if(fps==60) {
                fpsswitch.setChecked(true);
            }else{
                fpsswitch.setChecked(false);
            }
           MAX_FPS=fps;
        }else {
            fpsswitch.setChecked(true);
            MAX_FPS=60;
        }
        try{

            setting_close_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);

                    startActivity(intent);
                    finish();
                }
            });
            presets_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences sharedPrefer = getSharedPreferences("QUIP", Context.MODE_PRIVATE);
                    sharedPrefer.edit()
                            .putInt("brightness", 50)
                            .putInt("contrast", 50)
                            .putInt("saturation", 50)
                            .putInt("tint", 2)
                            .putInt("temp", 5)
                            .apply();
                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);

                    startActivity(intent);
                    finish();
                }
            });
            resolution_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resolution_layer.setVisibility(View.VISIBLE);
                    settings_main.setVisibility(View.GONE);
                }
            });
            resolution_back_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resolution_layer.setVisibility(View.GONE);
                    settings_main.setVisibility(View.VISIBLE);
                }
            });
            fhd_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences sharedPrefer = getSharedPreferences("QUIP", Context.MODE_PRIVATE);
                    sharedPrefer.edit()
                            .putInt("width", 1920)
                            .putInt("height", 1080)
                            .apply();
                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    intent.putExtra("res", new int[] {1920, 1080,MAX_FPS});
                    startActivity(intent);
                    finish();
                }
            });
            hd_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences sharedPrefer = getSharedPreferences("QUIP", Context.MODE_PRIVATE);
                    sharedPrefer.edit()
                            .putInt("width", 1280)
                            .putInt("height", 720)
                            .apply();
                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    intent.putExtra("res", new int[] {1280, 720, MAX_FPS});
                    startActivity(intent);
                    finish();
                }
            });
            fpsswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    if(b){
                        MAX_FPS=60;
                    }else{
                        MAX_FPS=30 ;
                    }
                    SharedPreferences sharedPrefer1 = getSharedPreferences("QUIP", Context.MODE_PRIVATE);
                    sharedPrefer1.edit()
                            .putInt("fps", MAX_FPS)
                            .apply();

                }
            });

//            1920x1080@30fps
//            1920x1080@60fps
//            1280720@30fps
//            1280720@60fps

        }catch (Exception e){
            e.printStackTrace();
        }
        settings_main_close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);

        startActivity(intent);
        finish();
    }
}