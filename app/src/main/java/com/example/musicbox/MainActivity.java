package com.example.musicbox;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    private Button play;
    private Button stop;
    private Button quit;
    private ImageView imageView;
    private TextView totalTime;
    private TextView currentTime;
    private TextView state;
    private AppCompatSeekBar seekBar;
    private Intent intent;
    private boolean isPlaying;
    private IBinder mBinder;
    private MusicServer ms;
    private ServiceConnection sc ;
    private SimpleDateFormat time;
    private boolean hasPermission;
    private boolean isFirstStart = true;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        play = (Button) findViewById(R.id.playButton);
        stop = (Button) findViewById(R.id.stopButton);
        quit =(Button) findViewById(R.id.quitButton);
        imageView = (ImageView)  findViewById(R.id.image);
        totalTime = (TextView) findViewById(R.id.totalTime);
        currentTime = (TextView) findViewById(R.id.currentTime);
        state = (TextView) findViewById(R.id.state);
        seekBar = (AppCompatSeekBar) findViewById(R.id.seekBar);
        //设置背景图旋转
        final ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360.0f);

        isPlaying = false;
        time = new SimpleDateFormat("mm:ss");
        hasPermission = true;

        sc = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d("service","connect");
                ms = ((MusicServer.MyBinder) service).getService();
                mBinder = service;
                totalTime.setText(time.format(ms.getMediaPlayer().getDuration()));
                if(ms.getMediaPlayer()!=null){//TODO 记住要加
                    seekBar.setProgress(ms.getMediaPlayer().getCurrentPosition());
                    seekBar.setMax(ms.getMediaPlayer().getDuration());
                }
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                sc = null;
            }
        };

        //开启背景音乐服务
        intent = new Intent(MainActivity.this, MusicServer.class);
        startService(intent);
        bindService(intent,sc, Context.BIND_AUTO_CREATE);


        animator.setDuration(10000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(-1);


        final Handler mHandler = new Handler(){
            @Override
            public void  handleMessage(Message msg){
                super.handleMessage(msg);
                switch (msg.what){
                    case 123:
                        update();
                        //mHandler.postDelayed(mThread,100);
                        break;
                }
            }
            public void update(){
//        try{
//            int code = 104;
//            int [] Time = new int [2];
//            Parcel data = Parcel.obtain();
//            Parcel reply = Parcel.obtain();
//            mBinder.transact(code,data,reply,0);
//            reply = Parcel.obtain();
//            reply.readIntArray(Time);
//            totalTime.setText(time.format(Time[1]));
//            currentTime.setText(time.format(Time[0]));
//            seekBar.setProgress(Time[0]);
//            seekBar.setMax(Time[1]);
//        }catch (RemoteException e){
//            e.printStackTrace();
//        }
                currentTime.setText(time.format(ms.getMediaPlayer().getCurrentPosition()));
                seekBar.setProgress(ms.getMediaPlayer().getCurrentPosition());
                if(ms.getMediaPlayer().isPlaying()){
                    play.setText(R.string.paused);
                    state.setText(R.string.Playing);
                    isPlaying=true;
                    if(isFirstStart){
                        animator.start();
                        isFirstStart=false;
                    }
                }
                else{
                    play.setText(R.string.play);
                    if(!isFirstStart)state.setText(R.string.Paused);
                    animator.pause();
                    isPlaying=false;
                }

            }
        };

        //定义一个新的线程
        final Thread mThread = new Thread(){
            @Override
            public void run(){
                while(true){
                    try{
                        Thread.sleep(200);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    if( sc != null && hasPermission){
                        mHandler.obtainMessage(123).sendToTarget();
                    }
                }
            }
        };

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(ms.getMediaPlayer()!=null){//TODO 记住要加
//                    seekBar.setProgress(ms.getMediaPlayer().getCurrentPosition());
//                    seekBar.setMax(ms.getMediaPlayer().getDuration());
//                }
                if(!isPlaying){
                    play.setText(R.string.paused);
                    state.setText(R.string.Playing);
                    if(!isFirstStart){
                        animator.resume();
                    }
                    else {
                        animator.start();
                        isFirstStart=false;
                    }
                    isPlaying=true;
                }
                else{
                    play.setText(R.string.play);
                    state.setText(R.string.Paused);
                    animator.pause();
                    isPlaying=false;
                    isFirstStart=false;
                }
                try{
                    int code = 101;
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    mBinder.transact(code,data,reply,0);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play.setText(R.string.play);
                state.setText(R.string.Stopped);
                currentTime.setText("00:00");
                animator.end();
                isPlaying=false;
                isFirstStart=true;
                try{
                    int code = 102;
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    mBinder.transact(code,data,reply,0);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        });

        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    int code = 103;
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    mBinder.transact(code,data,reply,0);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
                //关闭背景音乐服务
                unbindService(sc);
                sc = null;
                stopService(intent);
                mHandler.removeCallbacks(mThread);
                try{
                    MainActivity.this.finish();
                    System.exit(0);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser==true){//TODO 记住要加
                    try{
                        int code = 105;
                        int [] Time = new int [2];
                        currentTime.setText(time.format(seekBar.getProgress()));
                        //Toast.makeText(getApplicationContext(), String.valueOf(seekBar.getProgress()), Toast.LENGTH_SHORT).show();
                        Parcel data = Parcel.obtain();
                        Parcel reply = Parcel.obtain();
                        Time[0]=seekBar.getProgress();
                        data.writeIntArray(Time);
                        mBinder.transact(code,data,reply,0);
                    }catch (RemoteException e){
                        e.printStackTrace();
                    }
                }

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mThread.start();

    }
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
         super.onDestroy();
//        mHandler.removeCallbacks(mThread);
//        ms.getMediaPlayer().release();
    }
}
