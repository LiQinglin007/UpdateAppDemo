package com.example.lql.updateappdemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.lql.updateappdemo.R;
import com.example.lql.updateappdemo.UpdateAppUtils;
import com.example.lql.updateappdemo.message.EventMessage;
import com.example.lql.updateappdemo.utils.T;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 作者：dell or Xiaomi Li
 * 时间： 2018/5/2
 * 内容：
 * 最后修改：
 */
public class HomeFragment extends Fragment {
    View mRootView = null;
    Button checkVersion;
    boolean IsDownLoad = false;

    public static HomeFragment getInstance() {
        return HomeFragmentHolder.homeFragment;
    }

    private final static class HomeFragmentHolder {
        final static HomeFragment homeFragment = new HomeFragment();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_home, container, false);
        initView();
        return mRootView;
    }

    private void initView() {
        //        //注册事件
        EventBus.getDefault().register(this);
        checkVersion = (Button) mRootView.findViewById(R.id.fragment_checkVersion_btn);
        checkVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //注意这里正常情况下，下载完成之后，安装了新的安装包，IsDownLoad就变成false了，
                //因为这里下载的不是这个apk,所以会有这个问题，以后会放一个apk在线上，
                // 改成下载当前项目的apk,就不会这样了
                if (IsDownLoad) {
                    T.shortToast(getActivity(), "正在更新，请稍后...");
                    return;
                }
                //升级app
                getData();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onExitappEvent(EventMessage messageEvent) {
        if (messageEvent.getMessageType() == EventMessage.Exitapp) {
            T.shortToast(getActivity(), "去处理退出app的方法");
        } else if (messageEvent.getMessageType() == EventMessage.CheckApp) {
            IsDownLoad = messageEvent.isDownLoading();
        }
    }


    private void getData() {
        String url = "https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk";
        String Version_name = "1.1";//版本名称
        String info = "模拟下载，使用QQApk";  //更新说明
        int Forced = 1;// 1：强制更新   0：不是
        int Version_no = 2;//版本号
        UpdateAppUtils.UpdateApp(getActivity(), HomeFragment.getInstance(), Version_no, Version_name, info,
                url, Forced == 1 ? true : false, true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //如果是从设置界面返回,就继续判断权限
        if (requestCode == UpdateAppUtils.REQUEST_PERMISSION_SDCARD_SETTING) {
            getData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        UpdateAppUtils.onActRequestPermissionsResult(requestCode, permissions, grantResults, getActivity(), HomeFragment.getInstance());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //取消注册事件
        EventBus.getDefault().unregister(this);
    }
}
