package glai.com.cn.yiqing.activies;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import glai.com.cn.yiqing.HttpUtil;
import glai.com.cn.yiqing.Location_BackGround_Activity;
import glai.com.cn.yiqing.R;
import glai.com.cn.yiqing.global.Global;
import glai.com.cn.yiqing.utils.OkHttpGet;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SplashActivity extends Activity {

    private TextView tv_splash_version;
    private TextView tv_update_info;
    private PackageManager packageManager;
    private PackageInfo packageInfo;
    private Context content;
    private Message message;
    private final int FAILURE=0,ENTER_HOME=1,SHOW_UPDATE_DIALOG=2;
    private String apkUrl;
    private String apkPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/app.apk";
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d("James",msg.what+"");
            switch (msg.what){
                case FAILURE://失败
                    Toast.makeText(SplashActivity.this,"升级出错，请检查网络是否正常",Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case ENTER_HOME://进入主页
                    enterHome();
                    break;
                case SHOW_UPDATE_DIALOG://显示对话框
                    Toast.makeText(SplashActivity.this,"显示升级的对话框",Toast.LENGTH_SHORT).show();
                    apkUrl=(String)msg.obj;
                    showUpdateDialog();
                    break;
            }
        }
    };
    private String description;
    private String apkurl;
    private boolean installAllowed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        init();
        //checkUpdate();
        AutoUpdate();
    }


    //-------------------------------------初始化-----------------------------------------
    private void init() {
        content= SplashActivity.this;
        tv_splash_version=(TextView)findViewById(R.id.tv_splash_version);//版本号
        tv_splash_version.setText("版本为："+getVersion());
        tv_update_info=(TextView)findViewById(R.id.tv_update_info);//更新进度条
    }

    //-------------------------------------得到版本号------------------------------------------
    public String getVersion() {
        packageManager =getPackageManager();//得到包管理对象
        try {
            packageInfo=packageManager.getPackageInfo(getPackageName(),0);//得到getPackageName()包的信息
            return packageInfo.versionName;//返回版本名
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }
    //-------------------------------------检测是否需要更新-----------------------------------------
    private void checkUpdate() {
        message=Message.obtain();
        //OkHttp的使用，首先在build.gradle中添加： implementation 'com.squareup.okhttp3:okhttp:3.6.0'
        /*
         *这里的url修改为自己的服务器ip:端口号/文件
         */
        OkHttpGet.sendRequestGetOkHttp("http://192.168.200.224:8088/glkg/version/getVersion", content, new Callback() {
            //请求失败
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                message.what=FAILURE;
                handler.sendMessage(message);
            }
            //请求成功
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code()==200){//
                    String result=response.body().string();//将结果转化为字符串
                    try {
                        JSONObject jsonObject=new JSONObject(result);//将字符转化为ujson对象
                        String version= jsonObject.getString("version");//版本号
                        description =jsonObject.getString("description");//描述信息
                        apkurl =  jsonObject.getString("apkurl");//更新URL
                        if(version==getVersion()){
                            message.what= ENTER_HOME;//不需要更新，进入主页面
                        }
                        else {
                            message.what= SHOW_UPDATE_DIALOG;//显示更新对话框
                            message.obj=apkurl;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finally {
                        handler.sendMessage(message);
                    }
                }
                else {
                    message.what=FAILURE;
                    handler.sendMessage(message);
                }
            }
        });
    }

    private void AutoUpdate() {
        message = Message.obtain();
        HttpUtil.post(Global.WebUrl + "/glkg/version/getVersion").res(new HttpUtil.Res() {
            @Override
            public void res(String resData) {
                Log.e("数据2", resData);
                Map<String, Object> data = JSON.parseObject(resData, Map.class);
                if (((int) data.get("code")) != 0) {
                    message.what = FAILURE;
                    handler.sendMessage(message);
                } else {
                    Map map = (Map) data.get("data");
                    String version = map.get("versionId").toString();//版本号
                    description = map.get("versionRemark").toString();//描述信息
                    apkurl = map.get("versionUrl").toString();//更新URL
                    Log.d("James", "服务器版本号：" + version + "当前版本号：" + getVersion());
                    if (version.trim().equals(getVersion().trim())) {
                        message.what = ENTER_HOME;//不需要更新，进入主页面
                    } else {
                        message.what = SHOW_UPDATE_DIALOG;//显示更新对话框
                        message.obj = apkurl;
                    }
                    handler.sendMessage(message);
                }
            }
        }).execGet();
    }
    //-------------------------------------进入主页面---------------------------------------
    private void enterHome(){
        startActivity(new Intent(SplashActivity.this, Location_BackGround_Activity.class));
        finish();
    }
    //-------------------------------------跟新对话框---------------------------------------
    private void showUpdateDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(content);
        builder.setTitle("提示更新");
        builder.setMessage(description);
        //设置点击空白位置监听器
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                enterHome();//进入主页面
                dialog.dismiss();
            }
        });
//        builder.setNegativeButton("下次再说",new DialogInterface.OnClickListener(){
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                enterHome();//进入主页面
//                dialog.dismiss();
//            }
//        });
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //下载服务器中的apk到手机sdk中,并替换安装
                //判断是否装有sdk
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    //有sdk,然后监测是否有向sdk中读取的权限
                    dialog.dismiss();
                    sdkPermission();
                }
                else {
                    Toast.makeText(content,"没有sdcard，请安装上在试",0).show();
                }
            }
        });
        builder.show();
    }
    //-------------------------------------动态申请sdk存储权限---------------------------------------
    private void sdkPermission() {
        //如果版本高于6.0,动态申请权限
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.M){
            //不等于，表示没有同意权限
            if(ContextCompat.checkSelfPermission(content,"android.permission.WRITE_EXTERNAL_STORAGE")!=PackageManager.PERMISSION_GRANTED){
                //申请权限
                ActivityCompat.requestPermissions(this,new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"},6);
            }
            else {
                downloadApk();//有权限，直接下载apk
            }
        }
        else {
            downloadApk();//低于6.0版本，直接下载apk
        }
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 6:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    //权限已经都通过了，可以下载apk到SDk中了
                    downloadApk();
                }
                else {
                    // 没有申请权限
                    Toast.makeText(content,"没有权限去下载内容",0).show();
                }
                break;
        }
    }
    //-------------------------------------下载内容到SDK中---------------------------------------
    private void downloadApk() {
        FinalHttp finalHttp=new FinalHttp();//首先在libs下导入afinal_0.5_bin.jar包
        finalHttp.download(apkUrl, apkPath, new AjaxCallBack<File>() {//apkUrl：下载地址  ， apkPath：保存位置 ， 回调
            @SuppressLint("WrongConstant")
            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {//下载失败
                super.onFailure(t, errorNo, strMsg);
                t.printStackTrace();
                Toast.makeText(content,"下载失败",0).show();
            }
            @Override
            public void onLoading(long count, long current) {//下载站中
                super.onLoading(count, current);
                int progress=(int)(current*100/count);//count：下载文件的总共长度   current：当前下载长度
                tv_update_info.setVisibility(View.VISIBLE);
                tv_update_info.setText("下载进度："+progress+"%");
            }

            @Override
            public void onSuccess(File file) {//下载成功
                super.onSuccess(file);
                //版本高于8.0,查看是否允许安装未知来源软件
                if(Build.VERSION.SDK_INT>Build.VERSION_CODES.O){
                    installAllowed=content.getPackageManager().canRequestPackageInstalls();//是否允许安装包
                    if(installAllowed){
                        installApk(file);//允许，安装
                    }else {
                        //跳转到设置页面，设置成允许安装
                        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + content.getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        content.startActivity(intent);
                        installApk(file);
                        return;
                    }

                }
                //版本低于8.0
                else {
                    installApk(file);
                }
            }
        });

    }
    //-------------------------------------安装apk-------------------------------------------
    private void installApk(File file) {
        Uri uri=null;
        try {
            Intent intent=new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//为intent 设置特殊的标志，会覆盖 intent 已经设置的所有标志。
            if(Build.VERSION.SDK_INT>=24){//7.0 以上版本利用FileProvider进行访问私有文件
                uri= FileProvider.getUriForFile(content,content.getPackageName() + ".android7.fileprovider",file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//为intent 添加特殊的标志，不会覆盖，只会追加。
            }
            else {
                //直接访问文件
                uri=Uri.fromFile(file);
                intent.setAction(Intent.ACTION_VIEW);
            }
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
