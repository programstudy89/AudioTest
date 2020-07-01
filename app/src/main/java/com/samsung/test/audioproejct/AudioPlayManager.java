package com.samsung.test.audioproejct;

import android.app.Activity;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

// TODO os자체에서 다른 음성 짜르기
// TODO : audioFilePath를 다르게 처리할 방법은 없나? 참조 말고 객체 자체로 들고 있는 방법?
// fragment activity에 한 상태로 설치한다.
// view와 resourceId는 미리 넣어 둔다.
// 한개의 오디오에 대해 하나씩 설치해준다.
public class AudioPlayManager {
    private final int AUDIO_PLAY_NOT_READY_RID = R.drawable.audio_play;
    private final int AUDIO_PLAY_READY_RID = R.drawable.audio_play_ready;
    private final int AUDIO_PAUSE_RID = R.drawable.audio_pause;

    private Activity context;
    private int playState;
    private MediaPlayer audioPlayer;
    private ImageView playBtnView;
    private TextView playTimerView;
    private int lastPauseLength;

    private String audioFilePath;

    // TODO : 가능한 모든 view관련 요소를 바깥으로 뺸다.
    // TODO : callback이 가능한가?
    // view 변환 resourceId를 미리 받아야 하나? 다른 형식이라면?
    public static AudioPlayManager newInstance(Activity context, ImageView playBtnView
            , TextView playTimerView) {
        AudioPlayManager manager = new AudioPlayManager();
        manager.context = context;
        manager.playBtnView = playBtnView;
        manager.playTimerView = playTimerView;
//        manager.audioFilePath = audioFilePath;
        return manager;
    }

    /**
     * View의 OnClick에 걸어준다.
     */
    public void playOrPause(String audioFilePath) { // audioFilePath는 play에서만 사용 됨.
        if (!isPlayable()) {
            return ;
        }
        if (playState == 0) {
            try {
                refreshPlayer();
                startPlaying();
                Toast.makeText(context, "목소리를 재생합니다.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                stopPlaying();
            }
        } else if (playState == 1) {
            pausePlaying();
        } else if (playState == 2) {
            resumePlaying();
        }

//        if (!playState) {
//            if (TextUtils.isEmpty(audioFilePath)) return ; // 멈추는건 언제나!
//            removePlayer();
//            Toast.makeText(context, "목소리를 재생합니다.", Toast.LENGTH_SHORT).show();
//            try {
//                audioPlayer = new MediaPlayer();
//                audioPlayer.setDataSource(audioFilePath);
//                audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer mediaPlayer) {
//                        stopPlaying();
//                    }
//                });
//                audioPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
//                    @Override
//                    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
//                        stopPlaying();
//                        return false;
//                    }
//                });
//                startPlaying();
//            } catch (Exception e) {
//                stopPlaying();
//            }
//        } else {
//            stopPlaying();
//        }
    }

    // 한 UI에 - url이 매핑되어 context에서 설치된 후 재사용하는 형태로 사용.
    // crash날 요소가 많고 내용이 쉴드되어 있음로 NPE체크를 많이하는 식으로 개발

    public boolean isPlay() {
        if (audioPlayer != null) {
            return audioPlayer.isPlaying();
        }
        return false;
    }

    public void pause() {
        if (audioPlayer != null && prepared) {
            audioPlayer.pause();
        }
    }

    public void resume() {
        if (audioPlayer != null && prepared) {
            audioPlayer.start();
        }
    }

    public void stop() {
        if (audioPlayer != null && prepared) {
            audioPlayer.pause();
            audioPlayer.seekTo(0);
            Toast.makeText(context, "정지합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void finish() {
        if (audioPlayer != null) {
            audioPlayer.stop();
            audioPlayer.release();
        }
    }

    // once is initialized??
    // do we really need a cache????TT... just ... button synchronously 동작...

    // 스트리밍은 아니것으로 보이니, 이게 테스트상 안되면 그냥 캐싱으로 만들어야겠음
    // 그럴경우 업로드창에서는 player를 filepath로 그냥 사용해도 됨 다만 플레이가능을 url로 판단해야 함.
    private boolean prepared = false;
    private boolean isInitializing = false;
    public void play(String url) {
        // not initialized
        if (!isInitializing && !prepared) {
            try {
                isInitializing = true;
                audioPlayer = new MediaPlayer();
                audioPlayer.setDataSource(context, Uri.parse(url));
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

                if (Build.VERSION.SDK_INT >= 21) {
                    AudioAttributes attributes = new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA).build();
                    audioPlayer.setAudioAttributes(attributes);
                }

//                context.runOnUiThread(async);
//                new ThreadTest().start();
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(async);
//                playBtnView.post(async);
                // s3 특성상 streaming이 아닌 미리 다운받고 동작하는 형식으로 개발
            } catch (IOException e) {
                isInitializing = false;
            } catch (SecurityException e) {
                isInitializing = false;
            } catch (IllegalStateException e) {
                isInitializing = false;
            } catch (IllegalArgumentException e) {
                isInitializing = false;
            } catch (Exception e) {
                isInitializing = false;
            }

        } else if (prepared) { // stop -> start
            audioPlayer.seekTo(0);
            audioPlayer.start();
            Toast.makeText(context, "재시작합니다.", Toast.LENGTH_SHORT).show();
        } else { // 어떤 동작도 안함
            Toast.makeText(context, "초기화 중입니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void runTo() {
        try {
            audioPlayer.prepare();
            // 이곳을 지나면 prepared된 것으로 간주. 실제 stop pause play를 사용할 수 있다.
            // 현재 prepare async수행중일 수 있다.
            audioPlayer.start();
            Toast.makeText(context, "시작합니다.", Toast.LENGTH_SHORT).show();
            prepared = true;
            isInitializing = false;
        } catch (IOException e) {
            isInitializing = false;
        } catch (IllegalStateException e) {
            isInitializing = false;
        } catch (Exception e) {
            isInitializing = false;
        }
    }

    private class ThreadTest extends Thread {
        @Override
        public void run() {
            runTo();
        }
    }

    Runnable async = new Runnable() {
        @Override
        public void run() {
            try {
                audioPlayer.prepare();
                // 이곳을 지나면 prepared된 것으로 간주. 실제 stop pause play를 사용할 수 있다.
                // 현재 prepare async수행중일 수 있다.
                audioPlayer.start();
                Toast.makeText(context, "시작합니다.", Toast.LENGTH_SHORT).show();
                prepared = true;
                isInitializing = false;
            } catch (IOException e) {
                isInitializing = false;
            } catch (IllegalStateException e) {
                isInitializing = false;
            } catch (Exception e) {
                isInitializing = false;
            }
        }
    };
    // player는 사실상 프로필에서만 붙여주어도 무관하다.





    private MediaPlayer refreshPlayer() throws IOException {
        removePlayer();
        audioPlayer = new MediaPlayer();
        audioPlayer.setDataSource(audioFilePath);
        audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopPlaying();
            }
        });
        audioPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                stopPlaying();
                return false;
            }
        });
        audioPlayer.setOnTimedTextListener(new MediaPlayer.OnTimedTextListener() {
            @Override
            public void onTimedText(MediaPlayer mp, TimedText text) {

            }
        });
        return audioPlayer;
    }

    private void pausePlaying() {
        playState = 2;
        changePlayStatus();
        audioPlayer.pause();
        lastPauseLength = audioPlayer.getCurrentPosition();
    }

    private void resumePlaying() {
        playState = 1;
        changePlayBtn(PlayId.stop);
        audioPlayer.seekTo(this.lastPauseLength);
        audioPlayer.start();
    }

    private void stopPlaying() {
        playState = 0;
        changePlayStatus();
        initPlayTimer();
        removePlayer();
    }

    private void startPlaying() throws IOException {
        playState = 1;
        changePlayBtn(PlayId.stop);
        audioPlayer.prepare();
        audioPlayer.start();

        initPlayTimer();
        startPlayTimer();
    }

    public void removePlayer() {
        if (audioPlayer != null) {
            try {
                audioPlayer.stop();
                audioPlayer.release();
                audioPlayer = null;
            } catch (RuntimeException e) {

            }
        }
    }

    private void changePlayStatus() {
        if (isPlayable()) {
            changePlayBtn(PlayId.playReady);
        } else {
            changePlayBtn(PlayId.play);
        }
    }

    private boolean isPlayable() {
        if (StringUtils.isBlank(audioFilePath)) {
            return false;
        }
        return true;
    }

    private void changePlayBtn(int i) {
        if (i == PlayId.stop){
            playBtnView.setImageResource(AUDIO_PAUSE_RID);
        } else if (i== PlayId.play) {
            playBtnView.setImageResource(AUDIO_PLAY_NOT_READY_RID);
        } else {
            playBtnView.setImageResource(AUDIO_PLAY_READY_RID);
        }
    }

    private void initPlayTimer() {
        if (audioPlayer == null) {
            return ;
        }
        String elapsed = format(0) + "/" + format(audioPlayer.getDuration());
        playTimerView.setText(elapsed);
    }

    private String format(int duration) {
        int time = duration / 1000;
//        time += ceil && (duration % 1000 > 0) ? 1 : 0;
        int min = time / 60;
        int seconds = time % 60;
        return String.format("%d:%02d", min, seconds);
    }

    private void startPlayTimer() {
        Runnable timer = new Runnable() {
            @Override
            public void run() {
                if (playState != 0) { // pause playing...
                    int duration = audioPlayer.getCurrentPosition();
                    String elapsed =  format(duration) + "/" + format(audioPlayer.getDuration());
//                    Toast.makeText(AudioEditActivity.this, elapsed, Toast.LENGTH_SHORT).show();
                    playTimerView.setText(elapsed);
                    playTimerView.postDelayed(this, 100);
                } else {
                    playTimerView.removeCallbacks(this);
                }
            }
        };
        playTimerView.post(timer);
    }

    private class PlayId {
        public static final int stop = 0;
        public static final int play = 1;
        public static final int playReady = 2;
    }

}

