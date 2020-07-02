package com.samsung.test.audioproejct.record;

import android.content.Context;
import android.media.MediaRecorder;
import android.widget.Toast;

import com.samsung.test.audioproejct.file.FileManager;

import java.io.IOException;

public class RecordManager {
    private final int RECORD_MAX_DURATION = 60000; // 60초 최대 녹음
    private RecordState recordState = RecordState.NOT_RECORDING;
    private MediaRecorder audioRecorder;
    private Context context;

    public RecordManager(Context context) {
        this.context = context;
    }

    public void refreshRecorder() {
        removeRecorder();
        makeNewRecorder();
    }

    /**
     * 상세 설정
     * https://developer.android.com/reference/android/media/MediaRecorder
     */
    private void makeNewRecorder() {

        audioRecorder = new MediaRecorder();
        audioRecorder.setAudioSamplingRate(44100); // sampling rate?? 128kB
        audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        audioRecorder.setAudioChannels(); // mono 로 세팅!!
        audioRecorder.setAudioEncodingBitRate(128000);
        audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        audioRecorder.setOutputFile(getFilePath());
//        audioRecorder.setMaxDuration(RECORD_MAX_DURATION);
//                    audioRecorder.setMaxFileSize();
    }

    private String getFilePath() {
        return new FileManager().getFilePath();
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

    public void shootRecord() throws IOException {
        recordState = RecordState.RECORDING;
        audioRecorder.prepare();
        audioRecorder.start();

    }

    public void finishRecording() {
        recordState = RecordState.NOT_RECORDING;
        removeRecorder();

    }

    public boolean onRecordClick() {
        refreshRecorder();
        /**
         * 녹음 중 에러
         */
        audioRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mediaRecorder, int i, int i1) {
                finishRecording();
                Toast.makeText(context, "녹음 에러", Toast.LENGTH_SHORT).show();
            }
        });
        /**
         * Max시간 설정후 도달한 경우 완료 처리.
         */
        audioRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mediaRecorder, int i, int i1) {
                finishRecording();
                Toast.makeText(context, "녹음 종료", Toast.LENGTH_SHORT).show();
            }
        });
        try {
            shootRecord();
            Toast.makeText(context, "녹음 시작.", Toast.LENGTH_SHORT).show();
            return true;
        } catch (Exception e) {
            /**
             * 녹음 시작을 실패
             */
            e.printStackTrace();
            finishRecording();
            Toast.makeText(context, "녹음 시작 실패.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public RecordState getRecordState() {
        return this.recordState;
    }

    public boolean isRecording() {
        return this.recordState == RecordState.RECORDING;
    }

    private enum RecordState {
        RECORDING,
        NOT_RECORDING
        ;
    }
}
