package com.samsung.test.audioproejct;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.samsung.test.audioproejct.file.FileManager;
import com.samsung.test.audioproejct.play.AudioGlide;
import com.samsung.test.audioproejct.play.PlayClickListener;
import com.samsung.test.audioproejct.play.PlayListAdapter;
import com.samsung.test.audioproejct.record.RecordManager;
import com.samsung.test.audioproejct.support.PermissionManager;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private RecordManager recordManager;
    @BindView(R.id.play_idx)
    TextView playIdxView;
    @BindView(R.id.record_timer)
    TextView recordTimer;
    @BindView(R.id.btn_stop)
    TextView btnStop;
    @BindView(R.id.btn_record)
    TextView btnRecord;
    @BindView(R.id.play_list)
    RecyclerView playListView;
    private PlayListAdapter playListAdapter;
    private PermissionManager permissionManager;
    private Timer timer;
    private AudioGlide audioGlide;

    private PlayClickListener playClickListener = new PlayClickListener() {
        @Override
        public void onClick(int idx) {
            playIdxView.setText(String.format("%d 실행중", idx));
        }

        @Override
        public void onStop() {
            playIdxView.setText("정지");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        permissionManager = new PermissionManager(this);

        if (permissionManager.isAllNecessaryPermissionGranted()) {
            permissionManager.makeIntroduceAndRequestPermissionDialog();
        } else {
            recordManager = new RecordManager(this);
            initView();
            drawFileList();
        }
    }

    /**
     * 화면 밖을 나가면 record를 중지.
     */
    @Override
    protected void onPause() {
        super.onPause();

        if (recordManager != null) {
            recordManager.finishRecording();

            btnRecord.setVisibility(View.VISIBLE);
            btnStop.setVisibility(View.GONE);
            recordTimer.setVisibility(View.GONE);
//            Toast.makeText(this, "녹음 종료", Toast.LENGTH_SHORT).show();
        }

        if (timer != null) {
            timer.cancel();
        }

        stopPlaying();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean allPermissionOkay = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allPermissionOkay = false;
                break;
            }
        }

        if (allPermissionOkay) {
            recordManager = new RecordManager(this);
            initView();
            drawFileList();
        } else {
            permissionManager.makeIntroduceAndRequestPermissionDialog();
        }
    }

    @OnClick(
        {R.id.btn_record, R.id.btn_pause, R.id.btn_play, R.id.btn_stop})
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btn_record :
                if (onRecordClicked()) {
                    btnRecord.setVisibility(View.GONE);
                    btnStop.setVisibility(View.VISIBLE);
                    recordTimer.setVisibility(View.VISIBLE);
                    startRecordTimer();
                }

                break;
            case R.id.btn_stop:
                onRecordStop();
                btnRecord.setVisibility(View.VISIBLE);
                btnStop.setVisibility(View.GONE);
                recordTimer.setVisibility(View.GONE);

                break;

            case R.id.btn_play :
                onPlayClicked();
                break;

            default:
                break;
        }
    }

    private boolean onRecordClicked() {
        stopPlaying();
        if (!recordManager.isRecording()) {
            return recordManager.onRecordClick();
        }
        return false;
    }

    private void onRecordStop() {
        recordManager.finishRecording();
        drawFileList();
        timer.cancel();
        Toast.makeText(this, "녹음 종료", Toast.LENGTH_SHORT).show();
    }

    private void drawFileList() {
        playListAdapter.refresh(new FileManager().getFilePathList());
    }

    private void initView() {
        audioGlide = AudioGlide.with(this, playClickListener);

        playListAdapter = new PlayListAdapter(this, new ArrayList<>(), audioGlide, playClickListener);
        playListView.setAdapter(playListAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        playListView.setLayoutManager(layoutManager);
    }

    private void startRecordTimer() {
        String elapsed = format(0);
        recordTimer.setText(elapsed);

        timer = new Timer();
        TimerTask task = new TimerTask() {
            long currentTime = 0; // millisec
            @Override
            public void run() {
                recordTimer.post(new Runnable() {
                    @Override
                    public void run() {
                        String elapsed =  format(currentTime);
                        recordTimer.setText(elapsed);
                    }
                });
                currentTime += 100;
            }
        };
        timer.schedule(task, 100 ,100);
    }

    private String format(long duration) {
        long sec = duration / 1000;
//        time += ceil && (duration % 1000 > 0) ? 1 : 0;
        long min = sec / 60;
        long seconds = sec % 60;
        return String.format("%d:%02d", min, seconds);
    }




    /**
     * **** play
     */
    private void stopPlaying() {
        audioGlide.stop();
    }

    private void onPlayClicked() {
    }

}