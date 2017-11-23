package com.example.musicbox;

/*
 * Created by qingming on 2017/11/18.
 */

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.provider.MediaStore;

import java.io.File;

public class MusicServer extends Service {

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private IBinder mBinder = new MyBinder();
    private boolean isStop = false;
    private Cursor cursor;
    private Context mContext;
    ContentResolver contentResolver;
    String[] projection = new String[]{MediaStore.Video.Media.TITLE};

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        initMediaPlayer();
        return mBinder;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        initMediaPlayer();
    }
    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        initMediaPlayer();
        return Service.START_STICKY;
    }
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mediaPlayer.release();
    }

    public void play(){
        if(mediaPlayer==null){
            initMediaPlayer();
        }
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }
        else{
            mediaPlayer.start();
            isStop=false;
        }
    }
    public void stop(){
        if(mediaPlayer!=null){
            try{
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.setDataSource(Environment.getExternalStorageDirectory().getPath()
                        +File.separator+"Music"+ File.separator+"melt.mp3");
                mediaPlayer.prepare();
                mediaPlayer.setLooping(true);
                mediaPlayer.seekTo(0);
                isStop=true;
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
    public void quit(){
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer=null;
        }
        else{
            mediaPlayer=null;
        }
    }
//    public void refresh(){
//        //TODO refresh
//    }
//    public void scrollbar(){
//        //TODO scrollbar
//    }

    //c.	在MusicServer类中定义自己的继承于Binder的MyBinder类，用于当前类与MainActivity之间的相互
    // 通信，其中当MainActivity中设置code值后调用Binder.transact函数的时候会根据transact参数中code
    // 的值做对应的操作
    public class MyBinder extends Binder{
        @Override
        protected boolean onTransact(int code , Parcel data,Parcel reply,int flags) throws RemoteException{
            int curTime;
            int maxTime;
            switch (code){
                case 101:
                    //播放按钮服务处理函数
                    play();
                    break;
                case 102:
                    //停止按钮服务处理函数
                    stop();
                    break;
                case 103:
                    //退出按钮服务处理函数
                    quit();
                    break;
                case 104:
                    //界面刷新，服务返回数据处理函数
                    int isplaying;
                    if(mediaPlayer.isPlaying()) {
                        isplaying=1;
                    }else{
                        isplaying=0;
                    }
                    curTime=mediaPlayer.getCurrentPosition();
                    maxTime=mediaPlayer.getDuration();
                    reply.writeInt(curTime);
                    reply.writeInt(maxTime);
                    reply.writeInt(isplaying);
                    break;
                case 105:
                    //拖动进度条服务处理函数
                    //scrollbar();
                    maxTime=data.readInt();
                    mediaPlayer.seekTo(maxTime);
                    break;
            }
            return super.onTransact(code,data,reply,flags);
        }
    }
    public void initMediaPlayer(){
        try{
            mediaPlayer.setDataSource(
                    Environment.getExternalStorageDirectory().getPath()+File.separator+"Music"+ File.separator+"melt.mp3");
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public MediaPlayer getMediaPlayer(){
        return mediaPlayer;
    }
}
