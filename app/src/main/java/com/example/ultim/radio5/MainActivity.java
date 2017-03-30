package com.example.ultim.radio5;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.example.ultim.radio5.Radio.RadioService;

import eu.gsottbauer.equalizerview.EqualizerView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    View.OnClickListener onClickListener;
    //Button button;
    ProgressBar progressBar;
    ImageButton imageButtonPlay;
    ImageButton imageButtonStop;
    EqualizerView equalizerView;

    int state = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("app", "RUN_APP");
        setContentView(R.layout.activity_main);
        equalizerView = (EqualizerView) findViewById(R.id.equalizerView);
        imageButtonPlay = (ImageButton) findViewById(R.id.imageButtonPlay);
        imageButtonStop = (ImageButton) findViewById(R.id.imageButtonStop);
        progressBar = (ProgressBar) findViewById(R.id.progress);


        equalizerView.setVisibility(View.INVISIBLE);

        progressBar.setVisibility(View.INVISIBLE);
        IntentFilter timerFilter = new IntentFilter("StateRadio");
        registerReceiver(myReceiver, timerFilter);
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startOrStopService();
            }
        };
        imageButtonPlay.setOnClickListener(onClickListener);
        imageButtonStop.setOnClickListener(onClickListener);

        if (RadioService.status.equals(AppConstant.StateRadio.Play)){
            imageButtonPlay.setVisibility(View.GONE);
            imageButtonStop.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            equalizerView.animateBars();
            equalizerView.setVisibility(View.VISIBLE);
        } else if (RadioService.status.equals(AppConstant.StateRadio.Stop)) {
            imageButtonPlay.setVisibility(View.VISIBLE);
            imageButtonStop.setVisibility(View.GONE);
            progressBar.setVisibility(View.INVISIBLE);
            equalizerView.stopBars();
            equalizerView.setVisibility(View.INVISIBLE);
        } else if (RadioService.status.equals(AppConstant.StateRadio.Buffering)) {
            progressBar.setVisibility(View.VISIBLE);
            imageButtonPlay.setVisibility(View.GONE);
            imageButtonStop.setVisibility(View.VISIBLE);
            equalizerView.stopBars();
            equalizerView.setVisibility(View.INVISIBLE);
        }
    }

    public void startOrStopService(){
        if( RadioService.isRunning){
            // Stop service
            Intent intent = new Intent(this, RadioService.class);
            stopService(intent);
            equalizerView.stopBars();
            equalizerView.setVisibility(View.INVISIBLE);
        }
        else {
            // Start service
            Intent intent = new Intent(this, RadioService.class);
            startService(intent);
        }
    }

    private void changeState(int state){
        if (state == 0) {
            progressBar.setVisibility(View.INVISIBLE);
            imageButtonPlay.setVisibility(View.VISIBLE);
            imageButtonStop.setVisibility(View.GONE);
            equalizerView.stopBars();
            equalizerView.setVisibility(View.INVISIBLE);
        }
        if (state == 1) {
            progressBar.setVisibility(View.VISIBLE);
            imageButtonPlay.setVisibility(View.GONE);
            imageButtonStop.setVisibility(View.VISIBLE);
            equalizerView.stopBars();
            equalizerView.setVisibility(View.INVISIBLE);
        }
        if (state == 2) {

            progressBar.setVisibility(View.INVISIBLE);
            imageButtonPlay.setVisibility(View.GONE);
            imageButtonStop.setVisibility(View.VISIBLE);
            equalizerView.animateBars();
            equalizerView.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onClick(View v) {

    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            int state = extras.getInt("state");
            changeState(state);
        }
    };
}
