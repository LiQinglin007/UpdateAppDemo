package com.example.lql.updateappdemo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;


import com.example.lql.updateappdemo.message.EventMessage;
import com.example.lql.updateappdemo.service.UpdateAppService;
import com.example.lql.updateappdemo.utils.FinalData;
import com.example.lql.updateappdemo.utils.PreferenceUtils;
import com.example.lql.updateappdemo.utils.T;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;


/**
 * 类描述：版本更新工具类
 * 这里使用DownLoadManager来做下载，在UpdateAppService类中
 * 在UpdateAppUtils检测是否要下载
 * 逻辑关系说明：
 * 1、请求数据  拿到后台的apk的版本号、版本名、更新内容、下载地址（可能还会包括是否强制更新的标志位）
 * 2、和本地的apk版本号对比时候需要更新
 * 3、这里用户可能会忽略此版本，这时候我要存储一个版本号，用户忽略的版本号，如果用户的是主动更新的，都要提示。如果不是主动更新的判断一下，判断一下。
 * 4、在步骤2以后   如果需要更新，先判断步骤3里边存储的版本号，比较大小，
 * 如果用户忽略更新这个版本，那就不更新了；如果用户没有忽略过此版本，继续往下
 * 5、这时候给用户一个提示，展示内容是有新版本是否更新，更新内容
 * 5.1  这里可能涉及到一个强制更新，如果是强制更新的，那就要求，用户没有更新，就退出程序，不是强制更新的，可以忽略此版本
 * 6、启动下载服务
 * 7、下载完成之后的自动安装
 * 使用说明：
 * 在外部直接调用UpdateApp()方法，并且需要重写onActivityResult()方法和onRequestPermissionsResult()方法
 * 作  者：李清林
 * 时  间：2018.5.2
 * 修改备注：增加申请权限功能
 */
public class UpdateAppUtils {
    private static int mVersionCode = 0;//当前版本号
    private static Activity mActivity;
    private static Fragment mFragment;
    private static String mDownloadUrl = "";//下载地址
    public static int REQUEST_PERMISSION_SDCARD_6_0 = 0x56;//写SD卡权限
    public static int REQUEST_PERMISSION_SDCARD_SETTING = 0x57;//去设置页面
    private static int serviceVersionCode = 0;//服务器的版本号码
    private static String serviceVersionName = "";//服务器的版本号名称
    private static boolean IsUpdate = false;//是否强制更新
    private static String content = "";//更新说明


    public UpdateAppUtils() {
    }

    /**
     * @param activity       上下文
     * @param newVersionCode 服务器的版本号
     * @param newVersionName 服务器的版本名称
     * @param content        更新了的内容
     * @param downUrl        下载地址
     * @param IsUpdate       是否强制更新
     * @param IsToast        是否提示用户当前已经是最新版本
     */
    public static void UpdateApp(Activity activity, Fragment fragment, int newVersionCode, String newVersionName,
                                 String content, String downUrl, boolean IsUpdate, boolean IsToast) {

        UpdateAppUtils.mActivity = activity;
        UpdateAppUtils.mFragment = fragment;
        UpdateAppUtils.mDownloadUrl = downUrl;
        UpdateAppUtils.serviceVersionCode = newVersionCode;
        UpdateAppUtils.serviceVersionName = newVersionName;
        UpdateAppUtils.IsUpdate = IsUpdate;
        UpdateAppUtils.content = content;
        //首先拿到当前的版本号和版本名
        try {
            UpdateAppUtils.mActivity = activity;
            PackageManager pm = mActivity.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mActivity.getPackageName(), 0);
            mVersionCode = pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (mVersionCode < serviceVersionCode) {//第2步骤
            if (PreferenceUtils.getInt(FinalData.VERSIONCODE, 0) < serviceVersionCode || IsToast) {//第3步骤
                //这时候要去更新，展示下载的对话框
                showDownLoadDialog();
            }
        } else {
            if (IsToast) {
                T.shortToast(mActivity, "当前已是最新版本");
            }
        }
    }


    /**
     * 下载对话框，并且请求权限
     */
    private static void showDownLoadDialog() {
        AlertDialog dialog = new AlertDialog.Builder(mActivity).
                setCancelable(false).
                setTitle("更新到 " + serviceVersionName).
                setMessage(content).
                setPositiveButton("下载", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {//第6步骤，下载
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                //有权限去下载
                                downLoad();
                            } else {
                                // 该方法在用户上次拒绝后调用,因为已经拒绝了这次你还要申请授权你得给用户解释一波
                                // 一般建议弹个对话框告诉用户 该方法在6.0之前的版本永远返回的是fasle
                                if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                    getPermissionDialog();
                                } else {
                                    // 申请授权
                                    ActivityCompat.requestPermissions(mActivity,
                                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_SDCARD_6_0);
                                }
                            }
                        } else {
                            downLoad();
                        }
                    }
                }).
                setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //这里涉及到下载的强制更新，是不是强制更新   强制更新，点取消按钮，退出程序
                        if (IsUpdate) {
                            T.shortToast(mActivity, "此版本需要更新，程序即将退出");
                            MyHandler myHandler = new MyHandler(new UpdateAppUtils());
                            myHandler.sendEmptyMessageDelayed(0, 1000 * 3);
                        } else {
                            PreferenceUtils.setInt(FinalData.VERSIONCODE, serviceVersionCode);
                            dialog.dismiss();
                        }
                    }
                }).
                create();
        dialog.show();
    }

    /**
     * 正式去下载
     */
    private static void downLoad() {
        T.shortToast(mActivity, "正在下载...");
        //如果要更新，并且是强制更新，这里发一个事件，不让其他的页面做操作了
        EventBus.getDefault().post(new EventMessage(EventMessage.CheckApp, true));
        new Thread(new Runnable() {
            @Override
            public void run() {
                //启动服务
                Intent service = new Intent(mActivity, UpdateAppService.class);
                service.putExtra("downLoadUrl", mDownloadUrl);
                mActivity.startService(service);
            }
        }).start();
    }


    /**
     * 申请权限的对话框
     */
    private static void getPermissionDialog() {
        AlertDialog dialog = new AlertDialog.Builder(mActivity).
                setCancelable(false).
                setTitle("权限提醒").
                setMessage("更新软件需要您允许我们获取您的数据读写权限,否则将无法更新").
                setPositiveButton("权限设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {//第6步骤，下载
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", mActivity.getPackageName(), null);
                        intent.setData(uri);
                        if (mFragment != null)
                            mFragment.startActivityForResult(intent, REQUEST_PERMISSION_SDCARD_SETTING);
                        else
                            mActivity.startActivityForResult(intent, REQUEST_PERMISSION_SDCARD_SETTING);
                    }
                }).
                setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //这里涉及到下载的强制更新，是不是强制更新   强制更新，点取消按钮，退出程序
                        if (IsUpdate) {
                            T.shortToast(mActivity, "此版本需要更新，程序即将退出");
                            MyHandler myHandler = new MyHandler(new UpdateAppUtils());
                            myHandler.sendEmptyMessageDelayed(0, 3000);
                        } else {
                            PreferenceUtils.setInt(FinalData.VERSIONCODE, serviceVersionCode);
                            dialog.dismiss();
                        }
                    }
                }).
                create();
        dialog.show();
    }


    /**
     * 申请权限返回结果时调用,用户是否同意
     *
     * @param requestCode  之前申请权限的请求码
     * @param permissions  申请的权限
     * @param grantResults 依次申请的结果
     */
    public static void onActRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                     @NonNull int[] grantResults, Activity activity, Fragment fragment) {
        mActivity = activity;
        mFragment = fragment;
        if (requestCode == REQUEST_PERMISSION_SDCARD_6_0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                downLoad();
            else
                getPermissionDialog();
        }
    }

    static class MyHandler extends Handler {
        WeakReference<UpdateAppUtils> mWeakReference;

        public MyHandler(UpdateAppUtils mUpdateAppUtils) {
            mWeakReference = new WeakReference<UpdateAppUtils>(mUpdateAppUtils);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mWeakReference.get().exitApp();
        }
    }

    /**
     * 这里使用EventBus像Activity发送消息，当然你也可以使用广播
     */
    private static void exitApp() {
        EventBus.getDefault().post(new EventMessage(EventMessage.Exitapp));
    }
}
