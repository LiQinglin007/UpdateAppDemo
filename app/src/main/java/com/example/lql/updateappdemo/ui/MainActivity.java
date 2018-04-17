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
import com.example.lql.updateappdemo.message.EventMessage;
import com.example.lql.updateappdemo.utils.T;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    final int WRITE_EXTERNAL_STORAGE = 0x1;

    Button checkVersion;

    boolean IsDownLoad = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        //        //注册事件
        EventBus.getDefault().register(this);
        checkVersion = (Button) findViewById(R.id.checkVersion_btn);
        checkVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //注意这里正常情况下，下载完成之后，安装了新的安装包，IsDownLoad就变成false了，
                //因为这里下载的不是这个apk,所以会有这个问题，以后会放一个apk在线上，
                // 改成下载当前项目的apk,就不会这样了
                if (IsDownLoad) {
                    T.shortToast(MainActivity.this, "正在更新，请稍后");
                    return;
                }
                chackPermission();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onExitappEvent(EventMessage messageEvent) {
        if (messageEvent.getMessageType() == EventMessage.Exitapp) {
            T.shortToast(MainActivity.this, "去处理退出app的方法");
        } else if (messageEvent.getMessageType() == EventMessage.CheckApp) {
            IsDownLoad = messageEvent.isDownLoading();
        }
    }

    /**
     * 定义所需要的权限
     */
    String[] perms = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    /**
     * 检查权限
     */
    private void chackPermission() {
        if (EasyPermissions.hasPermissions(this, perms)) {
            getData();
        } else {
            EasyPermissions.requestPermissions(this, "需要访问sd卡权限", WRITE_EXTERNAL_STORAGE, perms);
        }
    }


    private void getData() {
        String url = "https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk";
        String Version_name = "1.1";//版本名称
        String info = "模拟下载，使用QQApk";  //更新说明
        int Forced = 1;// 1：强制更新   0：不是
        int Version_no = 2;//版本号
        UpdateAppUtils.UpdateApp(MainActivity.this, Version_no, Version_name, info,
                url, Forced == 1 ? true : false, true);
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
        } else {
            Toast.makeText(this, "你拒绝了本权限，将无法使用部分功能", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消注册事件
        EventBus.getDefault().unregister(this);
    }
}
