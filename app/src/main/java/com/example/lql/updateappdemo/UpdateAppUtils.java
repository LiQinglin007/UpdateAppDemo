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
import android.support.annotation.RequiresApi;
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
 * 类描述：版本更新工具类<br>
 * 这里使用DownLoadManager来做下载，在UpdateAppService类中<br>
 * 在UpdateAppUtils检测是否要下载<br>
 * 逻辑关系说明：<br>
 * 1、请求数据  拿到后台的apk的版本号、版本名、更新内容、下载地址（可能还会包括是否强制更新的标志位）<br>
 * 这里有一点需要注意，如果有强制更新的概念，这时候请求数据的时候要给服务器你当前的版本号，<br>
 * 如果你现在的版本号是1  ，用户很久没有操作，期间版本更新了2（强制更新）、3（不强制更新）。这时候用户打开版本号为1的版本，应该是要强制更新的。<br>
 * 2、和本地的apk版本号对比时候需要更新<br>
 * 3、这里用户可能会忽略此版本，这时候我要存储一个版本号，用户忽略的版本号，如果用户的是主动更新的，都要提示。如果不是主动更新的判断一下，判断一下。<br>
 * 4、在步骤2以后   如果需要更新，先判断步骤3里边存储的版本号，比较大小，<br>
 * 如果用户忽略更新这个版本，那就不更新了；如果用户没有忽略过此版本，继续往下<br>
 * 5、这时候给用户一个提示，展示内容是有新版本是否更新，更新内容<br>
 * 5.1  这里可能涉及到一个强制更新，如果是强制更新的，那就要求，用户没有更新，就退出程序，不是强制更新的，可以忽略此版本<br>
 * 6、启动下载服务<br>
 * 7、下载完成之后的自动安装<br>
 * 8、不要忘记兼容7.0  在res/xml文件中添加    file_paths.xml 文件，并且在AndroidManifest.xml文件中添加
 * 使用说明：<br>
 * 在外部直接调用UpdateApp()方法<br>
 * 作  者：李清林<br>
 * 时  间：2017.5.12<br>
 * 更新时间：2018.5.2<br>
 * 修改备注：增加申请权限功能<br>
 * 更新时间：2018.7.23<br>
 * 修改备注：兼容8.0<br>
 */
public class UpdateAppUtils {
    /**
     * 当前版本号
     */
    private static int mVersionCode = 0;
    private static Activity mActivity;
    private static Fragment mFragment;
    /**
     * 下载地址
     */
    private static String mDownloadUrl = "";
    /**
     * 写SD卡权限
     */
    public static int REQUEST_PERMISSION_SDCARD_6_0 = 0x56;
    /**
     * 允许安装未知来源权限
     */
    public static int REQUEST_PERMISSION_SDCARD_8_0 = 0x58;
    /**
     * 去设置页面
     */
    public static int REQUEST_PERMISSION_SDCARD_SETTING = 0x57;
    /**
     * 服务器的版本号码
     */
    private static int serviceVersionCode = 0;
    /**
     * 服务器的版本号名称
     */
    private static String serviceVersionName = "";
    /**
     * 是否强制更新
     */
    private static boolean IsUpdate = false;
    /**
     * 更新说明
     */
    private static String content = "";

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

        if (mVersionCode < serviceVersionCode) {
            //第2步骤
            if (PreferenceUtils.getInt(FinalData.VERSIONCODE, 0) < serviceVersionCode || IsToast) {
                //第3步骤
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
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                checkPermission();
                            } else {
                                downLoad();
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
     * 检查6.0权限
     */
    private static void checkPermission() {
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                checkInstallPermission();
            } else {
                //有权限去下载
                downLoad();
            }
        } else {
            // 该方法在用户上次拒绝后调用,因为已经拒绝了这次你还要申请授权你得给用户解释一波
            // 一般建议弹个对话框告诉用户 该方法在6.0之前的版本永远返回的是fasle
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                getPermissionDialog(0);
            } else {// 申请授权
                if (mFragment != null) {
                    mFragment.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_SDCARD_6_0);
                } else {
                    ActivityCompat.requestPermissions(mActivity,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_SDCARD_6_0);
                }
            }
        }
    }

    /**
     * 检查8.0权限
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void checkInstallPermission() {
        boolean b = mActivity.getPackageManager().canRequestPackageInstalls();
        if (b) {
            downLoad();
        } else {
            getPermissionDialog(1);
        }
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
     *
     * @param type 0:6.0   1:8.0
     */
    private static void getPermissionDialog(int type) {
        String message = "更新软件需要您允许我们获取您的数据读写权限,否则将无法更新";
        if (type == 1) {
            message = "为了正常升级APP，请点击设置-高级设置-允许安装未知来源应用，本功能只限用于APP版本升级";
        }
        AlertDialog dialog = new AlertDialog.Builder(mActivity).
                setCancelable(false).
                setTitle("权限提醒").
                setMessage(message).
                setPositiveButton("权限设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {//第6步骤，下载

                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", mActivity.getPackageName(), null);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            //注意这个是8.0新API,直接跳转到允许安装位置来源的页面
                            intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, uri);
                        } else {
                            intent.setData(uri);
                        }

                        if (mFragment != null) {
                            mFragment.startActivityForResult(intent, REQUEST_PERMISSION_SDCARD_SETTING);
                        } else {
                            mActivity.startActivityForResult(intent, REQUEST_PERMISSION_SDCARD_SETTING);
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
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    checkInstallPermission();
                } else {
                    //有权限去下载
                    downLoad();
                }
            } else {
                getPermissionDialog(0);
            }
        } else if (requestCode == REQUEST_PERMISSION_SDCARD_8_0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downLoad();
            } else {
                getPermissionDialog(1);
            }
        } else if (requestCode == REQUEST_PERMISSION_SDCARD_SETTING) {
            //设置页面
            checkPermission();
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
            exitApp();
        }
    }

    /**
     * 这里使用EventBus像Activity发送消息，当然你也可以使用广播
     */
    private static void exitApp() {
        EventBus.getDefault().post(new EventMessage(EventMessage.Exitapp));
    }
}
