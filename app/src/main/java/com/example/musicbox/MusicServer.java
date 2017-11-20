package com.example.musicbox;

/*
 * Created by qingming on 2017/11/18.
 */

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

public class MusicServer extends Service {

    private MediaPlayer mediaPlayer;
    private int message;
    private IBinder mBinder = new MyBinder();
    private boolean isStop = false;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return mBinder;
    }


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if(mediaPlayer==null){
            initMediaPlayer();
        }

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
                mediaPlayer.reset();//TODO 为什么不能用stop
                mediaPlayer = MediaPlayer.create(this, R.raw.melt);
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
    public class MyBinder extends Binder{
        @Override
        protected boolean onTransact(int code , Parcel data,Parcel reply,int flags) throws RemoteException{
            int [] Time = new int[2];
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
                    Time[0]=mediaPlayer.getCurrentPosition();
                    Time[1]=mediaPlayer.getDuration();
                    reply.writeIntArray(Time);
                    transact(code,data,reply,0);
                    break;
                case 105:
                    //拖动进度条服务处理函数
                    //scrollbar();
                    data.readIntArray(Time);
                    mediaPlayer.seekTo(Time[0]);
                    break;
            }
            return super.onTransact(code,data,reply,flags);
        }
        MusicServer getService(){
            return MusicServer.this;
        }

    }
    public void initMediaPlayer(){
        try{
            // R.raw.melt是资源文件，MP3格式的
            mediaPlayer = MediaPlayer.create(this, R.raw.melt);
            //mediaPlayer.setDataSource("/melt.mp3");
            //TODO 在这里不能加prepare，要不然不能循环
            mediaPlayer.setLooping(true);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public MediaPlayer getMediaPlayer(){
        return mediaPlayer;
    }
}
