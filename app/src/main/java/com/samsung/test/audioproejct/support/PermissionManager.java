package com.samsung.test.audioproejct.support;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionManager {

    private Context context;

    public PermissionManager(Context context) {
        this.context = context;
    }

    /**
     * permission
     */
    public boolean isAllNecessaryPermissionGranted() {
        if (
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED

        ) {
            return true;
        }
        return false;
    }

    public void makeIntroduceAndRequestPermissionDialog() {
        DialogInterface.OnClickListener okay = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ActivityCompat.requestPermissions((Activity) context
                        , new String[] {
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                                , android.Manifest.permission.RECORD_AUDIO
                        }
                        , RequestCode.MainAudioStoragePerm
                );
            }
        };

        new AlertDialog.Builder(context).setTitle("필수 권한 설정")
                .setCancelable(false)
                .setMessage("\n저장소 권한.\n녹음 권한. 허가 해주세요")
                .setPositiveButton("확인", okay)
                .show()
        ;
    }
}
