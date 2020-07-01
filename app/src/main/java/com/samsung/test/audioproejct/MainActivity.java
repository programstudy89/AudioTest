package com.samsung.test.audioproejct;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.test.audioproejct.support.RequestCode;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finishRecording();
        stopPlaying();
        overridePendingTransition(0, 0);
    }

    private MediaPlayer audioPlayer;
    private MediaRecorder audioRecorder;
    private String fileName;
    private String filePath;
    private String tempFullPath;
    private boolean recordState = false;
    private int playState = 0; // stop play pause
    private int lastPauseLength;
    private final int RECORD_MAX_DURATION = 60000;
    private final int N_RECORD_DURATION = 1;
    private String resultFiePath;
    private long recordStartTime;

    private enum PlayState {
        STOP
        , PLAYING
        , PAUSE
        ;
    }











    private boolean isMoreThanNTime(int minDuration) {
        long duration = (System.currentTimeMillis() - recordStartTime) / 1000;
        if (duration > minDuration) return true;
        return false;
    }

    /**
     * **** play
     */

    private void onPlayClicked() {
        if (!isPlayable()) {
            return ;
        }
        if (playState == 0) {
            try {
                refreshPlayer();
                startPlaying();
                Toast.makeText(this, "재생합니다.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                stopPlaying();
            }
        } else if (playState == 1) {
            pausePlaying();
        } else if (playState == 2) {
            resumePlaying();
        }
    }

    private MediaPlayer refreshPlayer() throws IOException {
        removePlayer();
        audioPlayer = new MediaPlayer();
        audioPlayer.setDataSource(resultFiePath);
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
        return audioPlayer;
    }

    private void startPlaying() throws IOException {
        playState = 1;
        changePlayBtn(PlayId.stop);
        audioPlayer.prepare();
        audioPlayer.start();

        initPlayTimer();
        startPlayTimer();
    }

    private void startRecordTimer() {
        String elapsed = format(0) + "/" + format(0);
        recordTimerText.setVisibility(View.VISIBLE);
        recordTimerText.setText(elapsed);
        recordStartTime = System.currentTimeMillis();
        Runnable timer = new Runnable() {
            @Override
            public void run() {
                if (recordState) {
                    int duration = (int)(System.currentTimeMillis() - recordStartTime);
                    String elapsed =  format(duration);
                    recordTimerText.setText(elapsed);
                    recordTimerText.postDelayed(this, 30);
                } else {
                    recordTimerText.removeCallbacks(this);
                    recordTimerText.setVisibility(View.INVISIBLE);
                }
            }
        };
        recordTimerText.post(timer);
    }

    private void startPlayTimer() {
        Runnable timer = new Runnable() {
            @Override
            public void run() {
                if (playState != 0) { // pause playing...
                    int duration = audioPlayer.getCurrentPosition();
                    String elapsed =  format(duration) + "/" + format(audioPlayer.getDuration());
//                    Toast.makeText(AudioEditActivity.this, elapsed, Toast.LENGTH_SHORT).show();
                    processDisplayText.setText(elapsed);
                    processDisplayText.postDelayed(this, 100);
                } else {
                    processDisplayText.removeCallbacks(this);
                }
            }
        };
        processDisplayText.post(timer);
    }

    private String format(int duration) {
        int time = duration / 1000;
//        time += ceil && (duration % 1000 > 0) ? 1 : 0;
        int min = time / 60;
        int seconds = time % 60;
        return String.format("%d:%02d", min, seconds);
    }

    private void pausePlaying() {
        playState = 2;
        changePlayStatus();
        audioPlayer.pause();
        this.lastPauseLength = audioPlayer.getCurrentPosition();
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

    /**
     * now playable?
     */
    private boolean isPlayable() {
        if (recordState || StringUtils.isBlank(resultFiePath)) {
            return false;
        }
        return true;
    }

    private class PlayId {
        public static final int stop = 0;
        public static final int play = 1;
        public static final int playReady = 2;
    }

    private void removePlayer() {
        if (audioPlayer != null) {
            try {
                audioPlayer.stop();
                audioPlayer.release();
                audioPlayer = null;
            } catch (RuntimeException e) {
            }
        }
    }

    private void onRecordClicked() {
        stopPlaying();
        if (!recordState) {
            refreshRecorder();
            audioRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder mediaRecorder, int i, int i1) {
                    finishRecording();
                    Toast.makeText(MainActivity.this, "녹음 에러 발생", Toast.LENGTH_SHORT).show();
                }
            });
            audioRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mediaRecorder, int i, int i1) {
                    resultFiePath = tempFullPath;
                    finishRecording();
                    Toast.makeText(MainActivity.this, "녹음을 종료합니다", Toast.LENGTH_SHORT).show();
                }
            });
            try {
                shootRecord();
                Toast.makeText(MainActivity.this, "녹음이 시작됩니다.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                finishRecording();
                MangoLog.e("녹음 시작에 실패하였습니다");
                Toast.makeText(this, "녹음 시작에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }

        } else {
            if (!isMoreThanNTime(N_RECORD_DURATION)) {
                Toast.makeText(this, N_RECORD_DURATION  + "초 이상 필요합니다", Toast.LENGTH_SHORT).show();
                finishRecording();
            } else {
                Toast.makeText(this, "녹음을 종료합니다", Toast.LENGTH_SHORT).show();
                resultFiePath = tempFullPath;
                finishRecording();
            }
        }
    }

    private void removeTempFile() {
        // temp file -> real path file...???
        //
    }

    // TODO : https://developer.android.com/reference/android/media/MediaRecorder 이 주소보고 리펙토링 필요!!
    private void refreshRecorder() {
        tempFullPath = FileUtils.getAudioPathString("voice", null);
        removeRecorder();
        audioRecorder = new MediaRecorder();
        audioRecorder.setAudioSamplingRate(44100); // sampling rate?? 128kB
        audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        audioRecorder.setAudioChannels(); // mono 로 세팅!!
        audioRecorder.setAudioEncodingBitRate(128000);
        audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // ???
        audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        audioRecorder.setOutputFile(tempFullPath);
        audioRecorder.setMaxDuration(RECORD_MAX_DURATION);
//                    audioRecorder.setMaxFileSize();
    }

    private void shootRecord() throws IOException {
        recordState = true;
        changeRecordBtn(true);
        changePlayStatus();
        audioRecorder.prepare();
        audioRecorder.start();
        startRecordTimer();
        save.setVisibility(View.INVISIBLE);
    }

    private void finishRecording() {
        changeRecordBtn(false);
        recordState = false;
        changePlayStatus();
        removeRecorder();
        save.setVisibility(View.VISIBLE);
    }

    private void removeRecorder() {
        if (audioRecorder != null) {
            try {
                audioRecorder.stop();
                audioRecorder.release();
                audioRecorder = null;
            } catch (RuntimeException e) {
            }
        }
    }

    private void onSave() {
        /**
         * 현재 녹음 중이지 않고
         * 재생 가능한 결과 파일이 존재하는 경우
         */
        if (recordState) {
            Toast.makeText(this, "음성 녹음 중입니다.", Toast.LENGTH_SHORT);
        } else if (StringUtils.isBlank(resultFiePath)) {
            Toast.makeText(this, "재생 가능한 파일이 없습니다.", Toast.LENGTH_SHORT);
        } else {
            finishRecording();
            stopPlaying();
            audioUpload(new File(resultFiePath));
        }
    }

    protected void audioUpload(File audio) {
        if (interfaceLife.isUsing()) {
            return ;
        }
        interfaceLife.willUse();
        MultipartBody.Part body = FileMultipartGenerator.generateAudio(this, audio);

        getRetrofitServiceFactory().getUploadService().uploadAudio(body)
                .enqueue(new Callback<UploadResponse>() {
                    @Override
                    public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                        try {
                            if (response.isSuccessful()) {
                                UploadResponse body = response.body();
                                if (StringUtils.equals(body.getResult(), "OK")) {
                                    Intent returnIntent = getIntent();
                                    returnIntent.putExtra("url", body.getUrl());
                                    returnIntent.putExtra("fullUrl", body.getFullUrl());
                                    setResult(RESULT_OK, returnIntent);
                                    finish();
                                    Toast.makeText(MainActivity.this, "음성이 저장 되었습니다.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(MainActivity.this, ToastMessage.SERVER_RESPONSE_FAILURE, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                        } finally {
                            interfaceLife.doneUsing();
                        }
                    }

                    @Override
                    public void onFailure(Call<UploadResponse> call, Throwable t) {
                        Toast.makeText(MainActivity.this, ToastMessage.INTERNET_FAILURE, Toast.LENGTH_SHORT).show();
                        interfaceLife.doneUsing();
                    }
                });
    }

    @OnClick({R.id.activity_voice_appeal_edit_record_btn, R.id.activity_voice_appeal_edit_save_btn, R.id.activity_voice_appeal_edit_play_btn})
    public void onClick(View view) {
        switch(view.getId()) {
            // audio 버튼
            case R.id.activity_voice_appeal_edit_record_btn :
                onRecordClicked();
                break;
            case R.id.activity_voice_appeal_edit_play_btn :
                onPlayClicked();
                break;
            case R.id.activity_voice_appeal_edit_save_btn :
                onSave();
                break;
//            case R.id.activity_voice_appeal_edit_cancel_btn :
//                finish();
//                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_edit);

        ButterKnife.bind(this);

        if (isAllNecessaryPermissionGranted()) {
            makeIntroduceAndRequestPermissionDialog();
        }

        fileName = getIntent().getExtras().getString("fileName");
        filePath = getIntent().getExtras().getString("filePath");

        init();
    }

    private void init() {
        finishRecording();
        stopPlaying();
    }

    private void initPlayTimer() {
        if (audioPlayer == null) {
            return ;
        }
        String elapsed = format(0) + "/" + format(audioPlayer.getDuration());
        processDisplayText.setText(elapsed);
    }

    private void changePlayStatus() {
        if (isPlayable()) {
            changePlayBtn(PlayId.playReady);
        } else {
            changePlayBtn(PlayId.play);
        }
    }

    /**
     * true recording 중으로
     * false recording 안하는 중
     */
    private void changeRecordBtn(boolean enable) {
        if (enable) {
            record.setImageResource(AUDIO_RECORD_RECORDING_RID);
        } else {
            record.setImageResource(AUDIO_RECORD_RECORD_RID);
        }
    }

    /**
     * 0 stop - 플레이 가능해 플레이 했을 경우
     * 1 play - 준비안된경우 플레이 아닐때
     * 2 play_ready - 준비된경우 플레이 아닐때
     */
    private void changePlayBtn(int i) {
        if (i == PlayId.stop){
            play.setImageResource(AUDIO_PAUSE_RID);
//            soundPlayAnim.show();
        } else if (i== PlayId.play) {
            play.setImageResource(AUDIO_PLAY_NOT_READY_RID);
//            soundPlayAnim.hide();
        } else {
            play.setImageResource(AUDIO_PLAY_READY_RID);
//            soundPlayAnim.hide();
        }
    }

    /**
     * permission
     */
    private boolean isAllNecessaryPermissionGranted() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
        ) {
            return true;
        }
        return false;
    }

    private void makeIntroduceAndRequestPermissionDialog() {
        DialogInterface.OnClickListener okay = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ActivityCompat.requestPermissions(MainActivity.this
                        , new String[] {
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                                , android.Manifest.permission.RECORD_AUDIO
                        }
                        , RequestCode.MainAudioPerm
                );
            }
        };

        new AlertDialog.Builder(this).setTitle("필수 권한 설정")
                .setCancelable(false)
                .setMessage("\n사진 권한 : 프로필을 저장하기 위해 필요합니다.\n녹음 권한 : 음성 녹음 기능에 필요합니다.\n그 외 어떠한 용도로도 사용되지 않습니다.")
                .setPositiveButton("확인", okay)
                .show()
        ;
    }


}