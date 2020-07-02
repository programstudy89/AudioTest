package com.samsung.test.audioproejct.play;

import android.content.Context;
import android.media.MediaPlayer;
import android.widget.Toast;

import com.samsung.test.audioproejct.support.PlayState;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * view - url (x) - filepath - manager
 *
 *
 * CustomObject singletone (resource manage) - MediaPlayer/Recorder - Audio Engine
 */
public class AudioGlide {
    private final Context context;
    private MediaPlayer audioPlayer;
    private PlayState playState = PlayState.STOP; // 0  1 2 stop play pause
    private MediaPlayer dummyPlayer;
    PlayClickListener listener;

    public static AudioGlide with(Context context, PlayClickListener listener) {
        return new AudioGlide(context, listener);
    }

    private AudioGlide(Context context, PlayClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void onClicked(){
        switch(playState) {
            case STOP:
                play("");
                break;
            case PLAYING:
                stop();
                break;
            case PAUSE:
                resume();
                break;
            default:
                break;
        }
    }

    public int getPlayTime(File file) {
//        if (this.dummyPlayer == null) {
//            this.dummyPlayer = new MediaPlayer();
//        }
        MediaPlayer dummyPlayer = new MediaPlayer();
        try {
            dummyPlayer.setDataSource(file.getPath());
            dummyPlayer.prepare();
            int duration = dummyPlayer.getDuration();
            dummyPlayer.stop();
            dummyPlayer.release();
            return duration;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public MediaPlayer getDummyPlayer() {
//        if (this.dummyPlayer == null) {
//            this.dummyPlayer = new MediaPlayer();
//        }
//        return this.dummyPlayer;
        return new MediaPlayer();
    }

    public void play(String filePath) {
        if (!StringUtils.isBlank(filePath) && fileExist(filePath)) {
            playInit(filePath);
        }
    }

    private void playInit(String filePath) {
        remove();
        try {
            audioPlayer = new MediaPlayer();
            audioPlayer.setDataSource(filePath);
            audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stop();
                }
            });
            audioPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    stop();
                    return false;
                }
            });
            audioPlayer.prepare();
            audioPlayer.start();
            playState = PlayState.PLAYING;
        } catch (Exception e) {
            Toast.makeText(context, "비정상적인 파일입니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isPlay() {
        if (audioPlayer != null) {
            return audioPlayer.isPlaying();
        }
        return false;
    }

    public void pause() {
        if (audioPlayer != null) {
            audioPlayer.pause();
            // length remember
        }
    }

    public void resume() {
        if (audioPlayer != null) {
            audioPlayer.start();
        }
    }

    public void stop() {
        if (audioPlayer != null) {
            remove();
            Toast.makeText(context, "정지", Toast.LENGTH_SHORT).show();
            playState = PlayState.STOP;

            listener.onStop();
        }
    }

    /**
     * context에서 의무적으로 호출 해주어야 함
     */
    public void finish() {
        stop();
    }

    private void remove() {
        if (audioPlayer != null) {
            try {
                audioPlayer.stop();
                audioPlayer.release();
                audioPlayer = null;
            } catch (Exception e) {
            }
        }
    }

    private boolean fileExist(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile() && file.length() > 0) {
            return true;
        } else {
            return false;
        }
    }
}