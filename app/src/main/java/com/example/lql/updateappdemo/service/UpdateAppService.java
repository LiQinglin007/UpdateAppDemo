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
import android.text.TextUtils;

import com.example.lql.updateappdemo.utils.PublicStaticData;
import com.example.lql.updateappdemo.utils.T;

import java.io.File;

/**
 * 类描述：下载服务
 * 作  者：李清林
 * 时  间：
 * 修改备注：
 */
public class UpdateAppService extends Service {

    public UpdateAppService() {

    }

    /** 安卓系统下载类 **/
    DownloadManager manager;

    /** 接收下载完的广播 **/
    DownloadCompleteReceiver receiver;

    /** 初始化下载器 **/
    private void initDownManager() {

        manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        receiver = new DownloadCompleteReceiver();

        if(TextUtils.isEmpty(PublicStaticData.downLoadUrl)){
            T.shortToast(UpdateAppService.this,"下载地址为空");
            return;
        }

        //设置下载地址
        String urlPath = PublicStaticData.downLoadUrl;
        Uri parse = Uri.parse(urlPath);
        DownloadManager.Request down = new DownloadManager.Request(parse);
        // 设置允许使用的网络类型，这里是移动网络和wifi都可以
        down.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        // 下载时，通知栏显示途中
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            down.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        }
        // 显示下载界面
        down.setVisibleInDownloadsUi(true);
        // 设置下载后文件存放的位置
        String apkName = parse.getLastPathSegment();
        down.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, apkName);
        // 将下载请求放入队列
        manager.enqueue(down);
        //注册下载广播
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 调用下载
        initDownManager();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

        // 注销下载广播
        if (receiver != null)
            unregisterReceiver(receiver);
        super.onDestroy();
    }

    // 接受下载完成后的intent
    class DownloadCompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
                DownloadManager.Query query = new DownloadManager.Query();
                // 在广播中取出下载任务的id
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                query.setFilterById(id);
                Cursor c = manager.query(query);
                if (c.moveToFirst()) {
                    String filename = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                    if (null!=filename&&!TextUtils.isEmpty(filename)) {

                        install1(context, filename);
                    }
                    //停止服务并关闭广播
                    UpdateAppService.this.stopSelf();
                }
            }
        }

        private  boolean install1(Context context, String filePath) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            File file = new File(filePath);
            if (file != null && file.length() > 0 && file.exists() && file.isFile()) {
                i.setDataAndType(Uri.parse("file://" + filePath), "application/vnd.android.package-archive");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
                return true;
            }
            return false;
        }
    }

}