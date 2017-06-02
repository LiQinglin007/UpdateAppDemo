package com.example.lql.updateappdemo.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.example.lql.updateappdemo.utils.PublicStaticData;


/**
 * 类描述：初始化数据
 * 作  者：李清林
 * 时  间：2017.6.2
 * 修改备注：
 */
public class MyApplication extends Application {


    private static MyApplication mContext;
    public static Context context;

    public synchronized static MyApplication getInstance() {
        if (mContext == null) {
            mContext = new MyApplication();
        }
        return mContext;
    }

    @Override
    public void onCreate() {
        getInstance();
        PublicStaticData.mySharedPreferences = getSharedPreferences("UpdateDemo_App", Activity.MODE_PRIVATE);//初始化PreferencesUtils
        context=getApplicationContext();
        super.onCreate();
    }
}
