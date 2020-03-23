package glai.com.cn.yiqing.scan;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.king.zxing.CaptureActivity;
import com.king.zxing.DecodeFormatManager;
import com.king.zxing.Intents;
import com.king.zxing.util.CodeUtils;

import glai.com.cn.yiqing.Location_BackGround_Activity;
import glai.com.cn.yiqing.R;
import glai.com.cn.yiqing.utils.StatusBarUtils;
import glai.com.cn.yiqing.utils.UriUtils;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static glai.com.cn.yiqing.Location_BackGround_Activity.RC_READ_PHOTO;
import static glai.com.cn.yiqing.Location_BackGround_Activity.REQUEST_CODE_PHOTO;

/**
 * @author Jenly <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
public class EasyCaptureActivity extends CaptureActivity  {

    private ImageView iv_File;

    @Override
    public int getLayoutId() {
        return R.layout.easy_capture_activity;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        iv_File=(ImageView)findViewById(R.id.ivFile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        StatusBarUtils.immersiveStatusBar(this,toolbar,0.2f);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(getIntent().getStringExtra(Location_BackGround_Activity.KEY_TITLE));
        getCaptureHelper()
                .decodeFormats(DecodeFormatManager.QR_CODE_FORMATS)//设置只识别二维码会提升速度
                .playBeep(true)
                .vibrate(true);
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.ivLeft:
                onBackPressed();
                break;
            case R.id.ivFile:
                startPhotoCode();
                break;
        }
    }

    @AfterPermissionGranted(RC_READ_PHOTO)
    private void checkExternalStoragePermissions(){
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {//有权限
            startPhotoCode();
        }else{
            EasyPermissions.requestPermissions(this, getString(R.string.permission_external_storage),
                    RC_READ_PHOTO, perms);
        }
    }

    private void startPhotoCode(){
        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(pickIntent, REQUEST_CODE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data!=null){
            switch (requestCode){
                case REQUEST_CODE_PHOTO:
                    parsePhoto(data);
                    break;
            }

        }
    }

    private void parsePhoto(Intent data){
        final String path = UriUtils.getImagePath(this,data);
        Log.d("James","path:" + path);
        if(TextUtils.isEmpty(path)){
            return;
        }
        //异步解析
        asyncThread(new Runnable() {
            @Override
            public void run() {
                final String result = CodeUtils.parseCode(path);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(result!=null) {
                            Log.d("James", "result:" + result);
                            Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent();
                            intent.putExtra(Intents.Scan.RESULT, result);
                            EasyCaptureActivity.this.setResult(Activity.RESULT_OK, intent);
                            EasyCaptureActivity.this.finish();
                        }
                    }
                });

            }
        });

    }

    private Context getContext(){
        return this;
    }

    private void asyncThread(Runnable runnable){
        new Thread(runnable).start();
    }

    public boolean onResultCallback(String result) {
        Log.d("Test", result);
        return false;
    }
}