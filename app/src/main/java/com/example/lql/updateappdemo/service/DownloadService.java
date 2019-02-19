package com.example.lql.updateappdemo.service;


import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import com.example.lql.updateappdemo.message.EventMessage;
import com.example.lql.updateappdemo.utils.FinalData;

import org.greenrobot.eventbus.EventBus;

import java.io.File;


/**
 * @describe：下载服务<br>
 * @author：Xiaomi<br>
 * @createTime：2017/10/10<br>
 * @remarks：统一修改变量命名规则,添加开始下载和下载失败监听，分别发送DOWNLOAD_START、DOWNLOAD_FAIL消息、添加取消下载方法<br>
 * @changeTime:2019/2/11<br>
 */
public class DownloadService extends Service {
    /**
     * 安卓系统下载类
     **/
    static DownloadManager mManager;

    /**
     * 接收下载完的广播
     **/
    DownloadCompleteReceiver mReceiver;
    /**
     * 下载地址
     */
    String mDownLoadUrl;

    /**
     * 初始化下载器
     */
    private void initDownManager(String downLoadUrl, String downLoadTitle) {
        mDownLoadUrl = downLoadUrl;
        mManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        mReceiver = new DownloadCompleteReceiver();
        //设置下载地址
        Uri parse = Uri.parse(downLoadUrl);
        DownloadManager.Request down = new DownloadManager.Request(parse);
        // 设置允许使用的网络类型，这里是移动网络和wifi都可以
        down.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        // 下载时，通知栏显示途中
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            down.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        }
        down.setTitle(downLoadTitle);
        // 显示下载界面
        down.setVisibleInDownloadsUi(true);
        // 设置下载后文件存放的位置
        String apkName = parse.getLastPathSegment();
        down.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, apkName);
        // 将下载请求放入队列
        long downLoadId = mManager.enqueue(down);
        EventMessage miEventMessage = new EventMessage(EventMessage.DOWNLOAD_START);
        miEventMessage.setMessageLong(downLoadId);
        miEventMessage.setMessageString(downLoadUrl);
        EventBus.getDefault().post(miEventMessage);
        //注册下载广播
        registerReceiver(mReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    /**
     * 取消下载
     *
     * @param downLoadId
     */
    public static void cancleDownload(long downLoadId) {
        if (mManager != null) {
            mManager.remove(downLoadId);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String downloadUrl = intent.getStringExtra(FinalData.DOWNLOAD_URL);
        String downloadTitle = intent.getStringExtra(FinalData.DOWNLOAD_TITLE);
        // 调用下载
        initDownManager(downloadUrl, downloadTitle);
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        // 注销下载广播
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        super.onDestroy();
    }

    /**
     * 接受下载完成后的intent
     */
    class DownloadCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
                DownloadManager.Query query = new DownloadManager.Query();
                // 在广播中取出下载任务的id
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                query.setFilterById(id);
                Cursor cursor = manager.query(query);
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    int status = cursor.getInt(columnIndex);
                    switch (status) {
                        case DownloadManager.STATUS_SUCCESSFUL:
                            int fileUriIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                            String fileUri = cursor.getString(fileUriIdx);
                            String filePath = null;
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                                if (fileUri != null) {
                                    filePath = Uri.parse(fileUri).getPath();
                                }
                            } else {
                                //Android 7.0以上的方式：请求获取写入权限，这一步报错
                                //过时的方式：DownloadManager.COLUMN_LOCAL_FILENAME
                                int fileNameIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                                filePath = cursor.getString(fileNameIdx);
                            }
                            if (null != filePath && !TextUtils.isEmpty(filePath)) {
                                if (filePath.contains(".apk")) {
                                    install1(context, filePath);
                                } else {
                                    File file = new File(filePath);
                                    EventMessage miEventMessage = new EventMessage(EventMessage.DOWNLOAD_FINISH);
                                    miEventMessage.setDownLoadFile(file);
                                    miEventMessage.setMessageString(mDownLoadUrl);
                                    EventBus.getDefault().post(miEventMessage);
                                }
                            }
                            //停止服务并关闭广播
                            DownloadService.this.stopSelf();
                            break;
                        default:
                            EventMessage miEventMessage = new EventMessage(EventMessage.DOWNLOAD_FAIL);
                            miEventMessage.setMessageString(mDownLoadUrl);
                            EventBus.getDefault().post(miEventMessage);
                            break;
                    }
                }
            }
        }

        private void install1(Context context, String filePath) {
            final Intent mIntent = new Intent();
            File file = new File(filePath);
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
            } else {
                uri = Uri.fromFile(file);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            mIntent.setDataAndType(uri, "application/vnd.android.package-archive");
            context.startActivity(mIntent);
        }
    }
}