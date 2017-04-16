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

import com.example.ultim.radio5.Pojo.RadioStateEvent;
import com.example.ultim.radio5.Radio.RadioService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import eu.gsottbauer.equalizerview.EqualizerView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    View.OnClickListener onClickListener;
    ProgressBar progressBar;
    ImageButton imageButtonPlay;
    ImageButton imageButtonStop;
    EqualizerView equalizerView;
    RadioStateEvent.SateEnum state = RadioStateEvent.SateEnum.STOP;

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
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startOrStopService();
            }
        };
        imageButtonPlay.setOnClickListener(onClickListener);
        imageButtonStop.setOnClickListener(onClickListener);
        EventBus.getDefault().register(this);
    }

    public void startOrStopService(){
        if (state == RadioStateEvent.SateEnum.PAUSE){
            EventBus.getDefault().post("start");
        }
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

    private void changeState(RadioStateEvent.SateEnum inputState){
        state = inputState;
        if (state == RadioStateEvent.SateEnum.STOP) {
            progressBar.setVisibility(View.INVISIBLE);
            imageButtonPlay.setVisibility(View.VISIBLE);
            imageButtonStop.setVisibility(View.GONE);
            equalizerView.stopBars();
            equalizerView.setVisibility(View.INVISIBLE);
        }
        if (state == RadioStateEvent.SateEnum.BUFFERING) {
            progressBar.setVisibility(View.VISIBLE);
            imageButtonPlay.setVisibility(View.GONE);
            imageButtonStop.setVisibility(View.VISIBLE);
            equalizerView.stopBars();
            equalizerView.setVisibility(View.INVISIBLE);
        }
        if (state == RadioStateEvent.SateEnum.PLAY) {

            progressBar.setVisibility(View.INVISIBLE);
            imageButtonPlay.setVisibility(View.GONE);
            imageButtonStop.setVisibility(View.VISIBLE);
            equalizerView.animateBars();
            equalizerView.setVisibility(View.VISIBLE);
        }
        if (state == RadioStateEvent.SateEnum.PAUSE) {

            progressBar.setVisibility(View.INVISIBLE);
            imageButtonPlay.setVisibility(View.VISIBLE);
            imageButtonStop.setVisibility(View.GONE);
            equalizerView.animateBars();
            equalizerView.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onClick(View v) {

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(RadioStateEvent event){
        changeState( event.getSateEnum());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
