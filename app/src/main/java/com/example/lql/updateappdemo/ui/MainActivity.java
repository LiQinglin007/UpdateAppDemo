package com.example.lql.updateappdemo.ui;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.lql.updateappdemo.R;
import com.example.lql.updateappdemo.UpdateAppUtils;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    final int WRITE_EXTERNAL_STORAGE=0x1;

    Button checkVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        checkVersion= (Button) findViewById(R.id.checkVersion_btn);
        checkVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chackPermission();
            }
        });
    }



    /**
     * 定义所需要的权限
     */
    String[] perms = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    /**
     * 检查权限
     */
    private void chackPermission(){
        if (EasyPermissions.hasPermissions(this, perms)) {
            getData();
        } else {
            EasyPermissions.requestPermissions(this, "需要访问sd卡权限", WRITE_EXTERNAL_STORAGE, perms);
        }
    }


    private void getData(){
        String url="https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk";
        String Version_name = "1.1";//版本名称
        String info = "模拟下载，使用QQApk";  //更新说明
        int Forced = 0;// 1：强制更新   0：不是
        int Version_no = 2;//版本号
        UpdateAppUtils.UpdateApp( MainActivity.this , Version_no , Version_name , info ,
                url , Forced == 1 ? true : false ,true );
    }


    @Override
    public void onPermissionsGranted(int requestCode, List perms) {//权限同意        Toast.makeText(this, "你允许了本权限", Toast.LENGTH_SHORT).show();
        getData();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List perms) {//权限拒绝
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this, "为了您能使用，请开启SD卡读写权限！").setTitle("提示").
                    setPositiveButton("去设置").setNegativeButton("取消", null).setRequestCode(WRITE_EXTERNAL_STORAGE).build().show();
        }else{
            Toast.makeText(this, "你拒绝了本权限，将无法使用部分功能", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}
