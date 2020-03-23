package glai.com.cn.yiqing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationClientOption.AMapLocationProtocol;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationQualityReport;
import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.google.gson.Gson;
//import com.google.zxing.integration.android.IntentIntegrator;
//import com.google.zxing.integration.android.IntentResult;
import com.king.zxing.Intents;

import org.json.JSONArray;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import glai.com.cn.yiqing.bean.JsonBean;
import glai.com.cn.yiqing.global.Global;
import glai.com.cn.yiqing.scan.EasyCaptureActivity;
import glai.com.cn.yiqing.scan.ScanActivity;
import glai.com.cn.yiqing.scan.ScanCodeActivity;
import glai.com.cn.yiqing.utils.ProperTies;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by hongming.wang on 2018/1/29.
 * 后台定位示例
 * <p>
 * 从Android 8.0开始，Android系统为了实现低功耗，Android 8.0系统对后台应用获取位置的频率进行了限制，只允许每小时几次位置更新。
 * 根据Android 8.0的开发指引，为了适配这一系统特性，
 * 高德定位SDK从8.0开始增加了两个新接口enableBackgroundLocation和disableBackgroundLocation用来控制是否开启后台定位。
 * 开启后sdk会生成一个前台服务通知，告知用户应用正在后台运行，使得开发者自己的应用退到后台的时候，仍有前台通知在，提高应用切入后台后位置更新的频率。
 * 如果您的应用在退到后台时本身就有前台服务通知，则无需按照本示例的介绍做适配。<br>
 * 示例中提供了两种方法启动和关闭后台定位功能,请根据业务场景进行相应的修改<br>
 * 1、通过按钮触发，点击按钮调用相应的接口开开启或者关闭后台定位，此种方法主要是更直观的展示后台定位的功能
 * 2、通过生命周期判断APP是否处于后台，当处于后台时才开启后台定位功能，恢复到前台后关闭后台定位功能
 * </p>
 */
public class Location_BackGround_Activity extends CheckPermissionsActivity
		implements
			OnClickListener,EasyPermissions.PermissionCallbacks{

	private ImageView image,iv_temperature,iv_qrcodescan;
	private Button btn_tj,btn_ht,btn_pass;
	private TextView tv_info,et_info_dz,tv_temperature,tv_inforemark;
	private EditText et_info_name,et_info_sfz,et_info_jkzk,et_info_jd,et_info_wd,et_info_fssj;
	private EditText et_name,et_sfz,et_gj,et_gs,et_bm,et_fsrq,et_cfdz,et_fhsq,et_fhzz,et_hbjc,et_sjh;
	private LinearLayout lout_data_name,lout_data_gj,lout_data_gs,lout_data_bm,lout_data_fsrq,lout_data_cfdz,lout_data_fhsq,lout_data_fhzz,lout_data_hbjc,lout_data_sjh,lout_data_tj;
	private LinearLayout lout_data,lout_info,lout_scan;

	public static String scanType,name,checkStarus,outDate,location,address,company,dept;
	private List<String> gsList,bmList;

	private  String[] gsinfos,bminfos,hbjcinfos;

	private String personnel,gsId,bmId;
    private int locationType,LocationCount;
    private long lastLocationDate=0;
	private AMapLocationClient locationClient = null;
	private AMapLocationClientOption locationOption = null;

	private Timer timer;
//	private Intent alarmIntent = null;
//	private PendingIntent alarmPi = null;
//	private AlarmManager alarm = null;

	private List<JsonBean> options1Items = new ArrayList<>();
	private ArrayList<ArrayList<String>> options2Items = new ArrayList<>();
	private ArrayList<ArrayList<ArrayList<String>>> options3Items = new ArrayList<>();
	private Thread thread;
	private static final int MSG_LOAD_DATA = 0x0001;
	private static final int MSG_LOAD_SUCCESS = 0x0002;
	private static final int MSG_LOAD_FAILED = 0x0003;

	private static boolean isLoaded = false;

	public static final String KEY_TITLE = "key_title";
	public static final String KEY_IS_QR_CODE = "key_code";
	public static final String KEY_IS_CONTINUOUS = "key_continuous_scan";
	public static final int REQUEST_CODE_SCAN = 0X01;
	public static final int REQUEST_CODE_PHOTO = 0X02;
	public static final int RC_CAMERA = 0X01;
	public static final int RC_READ_PHOTO = 0X02;
	private Class<?> cls;
	private boolean isContinuousScan;


	private void startTimer(){
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
//				Log.e("定时器",personnel);
				if(personnel == null || personnel.equals("") || personnel.equals("null")) {
//					tv_inforemark.setVisibility(View.VISIBLE);
				}else {
//					tv_inforemark.setVisibility(View.GONE);
					HttpUtil.post(Global.WebUrl + "/glkg/personnel/updateRecordDate?personnelId=" + personnel).res(new HttpUtil.Res() {
						@Override
						public void res(String resData) {
							Log.e("定时成功", resData);
						}
					}).execGet();
				}

			}
		};
		timer = new Timer();
		timer.schedule(task,0,60000);
	}

	private void requsetPermission(){
		if (Build.VERSION.SDK_INT>22){
			if (ContextCompat.checkSelfPermission(Location_BackGround_Activity.this,
					android.Manifest.permission.CAMERA)!=     PackageManager.PERMISSION_GRANTED){
				//先判断有没有权限 ，没有就在这里进行权限的申请
				ActivityCompat.requestPermissions(Location_BackGround_Activity.this,
						new String[]{android.Manifest.permission.CAMERA},1);

			}else {

			}
		}else {

		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		// Forward results to EasyPermissions
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
	}

	@Override
	public void onPermissionsGranted(int requestCode, List<String> list) {
		// Some permissions have been granted

	}

	@Override
	public void onPermissionsDenied(int requestCode, List<String> list) {
		// Some permissions have been denied
		// ...
	}
//	@Override
//	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//		switch (requestCode){
//			case 1:
//				if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
//					//这里已经获取到了摄像头的权限，想干嘛干嘛了可以
//
//				}else {
//					//这里是拒绝给APP摄像头权限，给个提示什么的说明一下都可以。
//					Toast.makeText(Location_BackGround_Activity.this,"请手动打开相机权限",Toast.LENGTH_SHORT).show();
//				}
//				break;
//			default:
//				break;
//		}
//
//	}

	@Override
	public void onBackPressed() {//重写的Activity返回
		new AlertDialog.Builder(Location_BackGround_Activity.this).setTitle("提示")
				.setMessage("请不要结束程序后台运行").setPositiveButton("确定", null).show();
		Intent intent = new Intent();
		intent.setAction("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.HOME");
		startActivity(intent);

	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			new AlertDialog.Builder(Location_BackGround_Activity.this).setTitle("提示")
					.setMessage("程序将转入后台运行，请不要强制结束").setPositiveButton("确定", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					moveTaskToBack(true);
				}

			}).show();

			return true;
		}
		return super.onKeyDown(keyCode, event);


	}

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_DATA:
                    if (thread == null) {//如果已创建就不再重新创建子线程了
                        Toast.makeText(Location_BackGround_Activity.this, "Begin Parse Data", Toast.LENGTH_SHORT).show();
                        thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // 子线程中解析省市区数据
                                initJsonData();
                            }
                        });
                        thread.start();
                    }
                    break;
                case MSG_LOAD_SUCCESS:
                    Toast.makeText(Location_BackGround_Activity.this, "Parse Succeed", Toast.LENGTH_SHORT).show();
                    isLoaded = true;
                    break;
                case MSG_LOAD_FAILED:
                    Toast.makeText(Location_BackGround_Activity.this, "Parse Failed", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void showPickerView(final int ntype) {// 弹出选择器

        OptionsPickerView pvOptions = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                //返回的分别是三个级别的选中位置
                String opt1tx = options1Items.size() > 0 ?
                        options1Items.get(options1).getPickerViewText() : "";

                String opt2tx = options2Items.size() > 0
                        && options2Items.get(options1).size() > 0 ?
                        options2Items.get(options1).get(options2) : "";

                String opt3tx = options2Items.size() > 0
                        && options3Items.get(options1).size() > 0
                        && options3Items.get(options1).get(options2).size() > 0 ?
                        options3Items.get(options1).get(options2).get(options3) : "";

                String tx = opt1tx + opt2tx + opt3tx;
                if(ntype==0) {
					et_gj.setText(tx);
				}
                if(ntype==1){
                	et_cfdz.setText(tx);
				}
            }
        })

                .setTitleText("城市选择")
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK) //设置选中项文字颜色
                .setContentTextSize(20)
                .build();

        /*pvOptions.setPicker(options1Items);//一级选择器
        pvOptions.setPicker(options1Items, options2Items);//二级选择器*/
        pvOptions.setPicker(options1Items, options2Items, options3Items);//三级选择器
        pvOptions.show();
    }

    private void initJsonData() {//解析数据

        /**
         * 注意：assets 目录下的Json文件仅供参考，实际使用可自行替换文件
         * 关键逻辑在于循环体
         *
         * */
        String JsonData = new GetJsonDataUtil().getJson(this, "province.json");//获取assets目录下的json文件数据

        ArrayList<JsonBean> jsonBean = parseData(JsonData);//用Gson 转成实体

        /**
         * 添加省份数据
         *
         * 注意：如果是添加的JavaBean实体，则实体类需要实现 IPickerViewData 接口，
         * PickerView会通过getPickerViewText方法获取字符串显示出来。
         */
        options1Items = jsonBean;

        for (int i = 0; i < jsonBean.size(); i++) {//遍历省份
            ArrayList<String> cityList = new ArrayList<>();//该省的城市列表（第二级）
            ArrayList<ArrayList<String>> province_AreaList = new ArrayList<>();//该省的所有地区列表（第三极）

            for (int c = 0; c < jsonBean.get(i).getCityList().size(); c++) {//遍历该省份的所有城市
                String cityName = jsonBean.get(i).getCityList().get(c).getName();
                cityList.add(cityName);//添加城市
                ArrayList<String> city_AreaList = new ArrayList<>();//该城市的所有地区列表

                //如果无地区数据，建议添加空字符串，防止数据为null 导致三个选项长度不匹配造成崩溃
                /*if (jsonBean.get(i).getCityList().get(c).getArea() == null
                        || jsonBean.get(i).getCityList().get(c).getArea().size() == 0) {
                    city_AreaList.add("");
                } else {
                    city_AreaList.addAll(jsonBean.get(i).getCityList().get(c).getArea());
                }*/
                city_AreaList.addAll(jsonBean.get(i).getCityList().get(c).getArea());
                province_AreaList.add(city_AreaList);//添加该省所有地区数据
            }

            /**
             * 添加城市数据
             */
            options2Items.add(cityList);

            /**
             * 添加地区数据
             */
            options3Items.add(province_AreaList);
        }

        mHandler.sendEmptyMessage(MSG_LOAD_SUCCESS);

    }

    public ArrayList<JsonBean> parseData(String result) {//Gson 解析
        ArrayList<JsonBean> detail = new ArrayList<>();
        try {
            JSONArray data = new JSONArray(result);
			Gson gson = new Gson();
            for (int i = 0; i < data.length(); i++) {
                JsonBean entity = gson.fromJson(data.optJSONObject(i).toString(), JsonBean.class);
                detail.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mHandler.sendEmptyMessage(MSG_LOAD_FAILED);
        }
        return detail;
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.KITKAT) {
			//透明状态栏
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			//透明导航栏
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
		setContentView(R.layout.activity_location_background);
		setTitle(R.string.title_locationBackground);
		initView();

		initLocation();
		startTimer();
//		Intent start=new Intent (this,KeepLiveService.class);
//		if(Build.VERSION.SDK_INT>=26){
//			startForegroundService (start);
//		}else{
//			startService (start);
//		}
//		startService(new Intent(Location_BackGround_Activity.this, MusicService.class));
	}

	@Override
	protected void onResume() {
		super.onResume();
		//切入前台后关闭后台定位功能
		if(null != locationClient) {
			locationClient.disableBackgroundLocation(true);
		}
	}


	@Override
	protected void onStop() {
		super.onStop();
		Log.e("后台01","");
		boolean isBackground = ((MyApplication)getApplication()).isBackground();
		//如果app已经切入到后台，启动后台定位功能
		if(isBackground){
			Log.e("后台1","");
			if(null != locationClient) {
				Log.e("后台2","");
				locationClient.enableBackgroundLocation(2001, buildNotification());
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	//初始化控件
	private void initView(){
		tv_info = (TextView)findViewById(R.id.tv_info);
		tv_temperature=(TextView)findViewById(R.id.tv_temperature);
		tv_inforemark=(TextView)findViewById(R.id.tv_inforemark);
		image = (ImageView)findViewById(R.id.image);
		iv_temperature=(ImageView)findViewById(R.id.iv_temperature);
		iv_qrcodescan=(ImageView)findViewById(R.id.iv_qrcodescan);
		btn_tj = (Button) findViewById(R.id.btn_tj);
		btn_ht = (Button) findViewById(R.id.btn_ht);
		btn_pass=(Button)findViewById(R.id.btn_pass);
		et_name = (EditText) findViewById(R.id.et_name);
		et_sfz = (EditText) findViewById(R.id.et_sfz);
		et_gj = (EditText) findViewById(R.id.et_gj);
		et_gs = (EditText) findViewById(R.id.et_gs);
		et_bm = (EditText) findViewById(R.id.et_bm);
		et_fsrq = (EditText) findViewById(R.id.et_fsrq);
		et_cfdz = (EditText) findViewById(R.id.et_cfdz);
		et_fhsq = (EditText) findViewById(R.id.et_fhsq);
		et_fhzz = (EditText) findViewById(R.id.et_fhdz);
		et_hbjc = (EditText) findViewById(R.id.et_hbjc);
		et_sjh = (EditText) findViewById(R.id.et_sjh);

		et_info_jd = (EditText) findViewById(R.id.et_info_jd);
		et_info_name = (EditText) findViewById(R.id.et_info_name);
		et_info_wd = (EditText) findViewById(R.id.et_info_wd);
		et_info_sfz = (EditText) findViewById(R.id.et_info_sfz);
		et_info_jkzk = (EditText) findViewById(R.id.et_info_jkzk);
		et_info_dz = (TextView) findViewById(R.id.et_info_dz);
		et_info_fssj = (EditText) findViewById(R.id.et_info_fssj);

		lout_data_name = (LinearLayout) findViewById(R.id.lout_data_name);
		lout_data_gj = (LinearLayout) findViewById(R.id.lout_data_gj);
		lout_data_gs = (LinearLayout) findViewById(R.id.lout_data_gs);
		lout_data_bm = (LinearLayout) findViewById(R.id.lout_data_bm);
		lout_data_fsrq = (LinearLayout) findViewById(R.id.lout_data_fsrq);
		lout_data_cfdz = (LinearLayout) findViewById(R.id.lout_data_cfdz);
		lout_data_fhsq = (LinearLayout) findViewById(R.id.lout_data_fhsq);
		lout_data_fhzz = (LinearLayout) findViewById(R.id.lout_data_fhdz);
		lout_data_hbjc = (LinearLayout) findViewById(R.id.lout_data_hbjc);
		lout_data_sjh = (LinearLayout) findViewById(R.id.lout_data_sjh);
		lout_data_tj = (LinearLayout) findViewById(R.id.lout_data_tj);
		lout_data = (LinearLayout) findViewById(R.id.lout_data);
		lout_info = (LinearLayout) findViewById(R.id.lout_info);
		lout_scan = (LinearLayout) findViewById(R.id.lout_scan);

		btn_tj.setOnClickListener(this);
		btn_ht.setOnClickListener(this);
		btn_pass.setOnClickListener(this);
		tv_info.setOnClickListener(this);
		tv_temperature.setOnClickListener(this);
//		lout_scan.setOnClickListener(this);
		iv_qrcodescan.setOnClickListener(this);
		iv_temperature.setOnClickListener(this);

		et_gs.setOnClickListener(this);
		et_bm.setOnClickListener(this);
		et_hbjc.setOnClickListener(this);
		et_fhsq.setOnClickListener(this);
		et_gj.setOnClickListener(this);
		et_cfdz.setOnClickListener(this);
//		et_gs.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//			@Override
//			public void onFocusChange(View view, boolean b) {
//				if(b){
//					loadGs();
//				}
//			}
//		});
//		et_bm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//			@Override
//			public void onFocusChange(View view, boolean b) {
//				if(b){
//					if(gsId == null){
//						showErrMsgDialog("请先选择公司");
//						return ;
//					}
//					loadBm();
//				}
//			}
//		});

//		et_fsrq.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//			@Override
//			public void onFocusChange(View view, boolean b) {
//				if(b){
//					BasisTimesUtils.showDatePickerDialog(Location_BackGround_Activity.this, false, "请选择年月日", 2020, 2, 10, new BasisTimesUtils.OnDatePickerListener() {
//						@Override
//						public void onConfirm(int year, int month, int dayOfMonth) {
//							et_fsrq.setText(year + "-" + ((month < 10 ) ? "0" + month : month) + "-" + ((dayOfMonth < 10 ) ? "0" + dayOfMonth : dayOfMonth));
//						}
//
//						@Override
//						public void onCancel() {
//
//						}
//					});
//				}
//			}
//		});
		et_fsrq.setOnClickListener(this);


	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
//        if (mHandler != null) {
//            mHandler.removeCallbacksAndMessages(null);
//        }
//		if (null != locationClient) {
//			/**
//			 * 如果AMapLocationClient是在当前Activity实例化的，
//			 * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
//			 */
//			locationClient.onDestroy();
//			locationClient = null;
//			locationOption = null;
//		}
//
//		if(null != alarmReceiver){
//			unregisterReceiver(alarmReceiver);
//			alarmReceiver = null;
//		}
		destroyLocation();
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_tj) {

			   if(et_name.getText().toString().equals("")){
				   if(et_sfz.getText().toString().trim().equals("")){
					   showErrMsgDialog("请输入身份证");
					   return;
				   }else if(et_sfz.getText().toString().trim().length() != 18){
					   showErrMsgDialog("请输入正确的身份证");
					   return;
				   }
				   showInfo(et_sfz.getText().toString().trim());
			   }else {
				   if(checkData()) {
					   Map<String, Object> params = new HashMap<>();
					   params.put("personnelId", et_sfz.getText().toString());
					   params.put("personnelName", et_name.getText().toString());
					   params.put("personnelNativeplace", et_gj.getText().toString());
					   params.put("personnelBackDate", et_fsrq.getText().toString());
					   params.put("personnelAddress", et_fhzz.getText().toString());
					   params.put("personnelPhone", et_sjh.getText().toString());
					   params.put("personnelTemperature", 36);
					   params.put("personnelBackAddress", et_cfdz.getText().toString());
					   params.put("personnelCompany", gsId);
					   params.put("personnelContact", et_sjh.getText().toString().equals("是") ? 1 : 0);
					   params.put("personnelArea", et_fhsq.getText().toString());
					   params.put("personnelDept", bmId);
					   params.put("personnelToken", "空");
					   Log.d("James", bmId);
//http://192.168.200.224:8088   https://hopetownportal.glai.com.cn
					   HttpUtil.post(Global.WebUrl + "/glkg/personnel/register").setParams(params).res(new HttpUtil.Res() {
						   @Override
						   public void res(String resData) {
							   Log.e("数据2", resData);
							   Map<String, Object> data = JSON.parseObject(resData, Map.class);
							   if (((int) data.get("code")) == 0) {
								   lout_data.setVisibility(View.GONE);
								   lout_info.setVisibility(View.VISIBLE);
								   Map map = (Map) data.get("data");
								   et_info_name.setText(map.get("personnelName").toString());
								   name = map.get("personnelName").toString();
								   personnel = map.get("personnelId").toString();
								   et_info_sfz.setText(personnel.substring(0, personnel.length() - 5) + "****");
								   image.setImageBitmap(QRCodeUtil.createQRCode(map.get("personnelId").toString(), 600));
								   String tsr;

								   String fssj = map.get("personnelBackDate").toString().split(" ")[0];
								   if (map.get("personnelStatus").toString().equals("2")) {
									   tsr = "异常";
									   et_info_jkzk.setTextColor(Color.parseColor("#ff3b30"));
								   } else if (map.get("personnelStatus").toString().equals("0")) {
									   tsr = "正常";
									   et_info_jkzk.setTextColor(Color.parseColor("#ffe317"));
								   } else {
									   tsr = "正常";
									   et_info_jkzk.setTextColor(Color.parseColor("#30ff52"));
								   }

								   et_info_fssj.setText("返深时间:" + fssj);
								   et_info_jkzk.setText(tsr);
								   SPUtils.put(Location_BackGround_Activity.this, "personnel", personnel);

								   start_location();
//								   startLocation();
							   }
							   else{
								   showErrMsgDialog(data.get("msg").toString());
							   }

						   }
					   }).execPost();

				   }
			   }

				//image.setImageBitmap(QRCodeUtil.createQRCode("我爱你",100));

//				btLocation.setText(getResources().getString(
//						R.string.stopLocation));
//				tvResult.setText("正在定位...");
//				startLocation();


//				btLocation.setText(getResources().getString(
//						R.string.startLocation));
//				stopLocation();
//				tvResult.setText("定位停止");

		}
		if(v.getId()==R.id.et_gs){
			loadGs();
		}
		if(v.getId()==R.id.et_bm){
			if(gsId == null){
				showErrMsgDialog("请先选择所属公司");
				return ;
			}
			loadBm();
		}
		if(v.getId()==R.id.et_gj||v.getId()==R.id.et_cfdz) {
			if (isLoaded) {
				showPickerView(v.getId()==R.id.et_gj?0:1);
			} else {
				Toast.makeText(Location_BackGround_Activity.this, "Please waiting until the data is parsed", Toast.LENGTH_SHORT).show();
			}
		}
		if(v.getId()==R.id.et_hbjc){
			hbjcinfos =  new String[2];
			hbjcinfos[0]="是";
			hbjcinfos[1]="否";
			AlertDialog.Builder builder = new AlertDialog.Builder(Location_BackGround_Activity.this);

			builder.setTitle("选择湖北接触史");
			builder.setItems(hbjcinfos, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{

					et_hbjc.setText(hbjcinfos[which]);
				}
			});
			builder.show();
		}
		if(v.getId() == R.id.btn_ht){
			moveTaskToBack(true);
		}
		if(v.getId()==R.id.btn_pass){
			Intent intent = new Intent();
			intent.setClass(Location_BackGround_Activity.this, PassActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("PersonId" , personnel);
			String personname=et_info_name.getText().toString().trim();
			Log.d("James",personname);
			bundle.putString("PersonName" , personname);
			intent.putExtras(bundle);
			startActivity(intent);
		}
		if(v.getId()==R.id.et_fsrq){
			BasisTimesUtils.showDatePickerDialog(Location_BackGround_Activity.this, false, "请选择年月日", 2020, 2, 10, new BasisTimesUtils.OnDatePickerListener() {
				@Override
				public void onConfirm(int year, int month, int dayOfMonth) {
					et_fsrq.setText(year + "-" + ((month < 10 ) ? "0" + month : month) + "-" + ((dayOfMonth < 10 ) ? "0" + dayOfMonth : dayOfMonth));
				}
				@Override
				public void onCancel() {

				}
			});
		}
		if(v.getId()==R.id.et_fhsq){
			BasisTimesUtils.showDatePickerDialog(Location_BackGround_Activity.this, false, "请选择年月日", 2020, 2, 10, new BasisTimesUtils.OnDatePickerListener() {
				@Override
				public void onConfirm(int year, int month, int dayOfMonth) {
					et_fhsq.setText(year + "-" + ((month < 10 ) ? "0" + month : month) + "-" + ((dayOfMonth < 10 ) ? "0" + dayOfMonth : dayOfMonth));
				}

				@Override
				public void onCancel() {

				}
			});
		}
		if(v.getId() == R.id.tv_info || v.getId() == R.id.iv_qrcodescan){
			if(personnel == null || personnel.equals("") || personnel.equals("null")){
				showErrMsgDialog("请登记信息后重试");
				return ;
			}
			this.cls = EasyCaptureActivity.class;
			checkCameraPermissions();
//			/*以下是启动我们自定义的扫描活动*/
//			IntentIntegrator intentIntegrator = new IntentIntegrator(Location_BackGround_Activity.this);
//			intentIntegrator.setBeepEnabled(true);
//			/*设置启动我们自定义的扫描活动，若不设置，将启动默认活动*/
//			intentIntegrator.setCaptureActivity(ScanActivity.class);
//			intentIntegrator.initiateScan();
		}
		if(v.getId() == R.id.tv_temperature || v.getId() == R.id.iv_temperature){
			if(personnel == null || personnel.equals("") || personnel.equals("null")){
				showErrMsgDialog("请登记信息后重试");
				return ;
			}
			if(et_info_jd.getText().toString().trim().length()==0||et_info_wd.getText().toString().trim().length()==0){
				showErrMsgDialog("位置获取中，请稍后再试");
				return ;
			}
			Intent intent = new Intent();
			intent.setClass(Location_BackGround_Activity.this, TemperatureActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("PersonId" , personnel);
			String personname=et_info_name.getText().toString().trim();
			Log.d("James",personname);
			bundle.putString("PersonName" , personname);
			bundle.putString("Address",et_info_dz.getText().toString().trim());
			bundle.putString("Locator",et_info_jd.getText().toString().trim()+","+et_info_wd.getText().toString().trim());
			Log.d("James",et_info_jd.getText().toString().trim()+","+et_info_wd.getText().toString().trim());
			intent.putExtras(bundle);
			startActivity(intent);
		}
	}

	/**
	 * 检测拍摄权限
	 */
	@AfterPermissionGranted(RC_CAMERA)
	private void checkCameraPermissions(){
		String[] perms = {Manifest.permission.CAMERA};
		if (EasyPermissions.hasPermissions(this, perms)) {//有权限
			startScan(cls,"二维码扫描");
		} else {
			// Do not have permissions, request them now
			EasyPermissions.requestPermissions(this, getString(R.string.permission_camera),
					RC_CAMERA, perms);
		}
	}
	/**
	 * 扫码
	 * @param cls
	 * @param title
	 */
	private void startScan(Class<?> cls,String title){
		ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeCustomAnimation(this,R.anim.in,R.anim.out);
		Intent intent = new Intent(this, cls);
		intent.putExtra(KEY_TITLE,"二维码扫描");
		intent.putExtra(KEY_IS_CONTINUOUS,isContinuousScan);
		ActivityCompat.startActivityForResult(this,intent,REQUEST_CODE_SCAN,optionsCompat.toBundle());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK && data!=null){
			switch (requestCode){
				case REQUEST_CODE_SCAN:
					String result = data.getStringExtra(Intents.Scan.RESULT);
					Toast.makeText(this,result,Toast.LENGTH_SHORT).show();
					String[] temp=result.split("\\?");
					Log.d("James",temp[0].toString()+" " +temp.length+" "+temp[0].indexOf(Global.WebUrl+"/accessqrcode/"));
					if(temp.length!=2||temp[0].indexOf(Global.WebUrl+"/accessqrcode/")<0) {
						showErrMsgDialog("二维码解析异常，请扫描正确的二维码");
						return;
					}
					String[] params=temp[1].trim().split("\\&");
					if(params.length!=3){
						showErrMsgDialog("二维码解析异常，请扫描正确的二维码");
						return;
					}
					scanType=params[0].substring(5).toString().trim();
					location=params[1].substring(9).toString().trim();
					address=params[2].substring(5).toString().trim();
					Log.d("James",scanType+" "+location+" "+address);
					if(!scanType.equals("in")&&!scanType.equals("out")){
						showErrMsgDialog("二维码解析异常，请扫描正确的二维码");
						return;
					}
					checkSq();
					break;
//				case REQUEST_CODE_PHOTO:
//					parsePhoto(data);
//					break;
			}

		}
//		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//		if(result != null) {
//			if(result.getContents() == null) {
//				Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
//			} else {
//				Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_SHORT).show();
//
//				String code = result.getContents();
//				String[] temp=code.split("\\?");
//				Log.d("James",temp[0].toString()+" " +temp.length+" "+temp[0].indexOf(Global.WebUrl+"/accessqrcode/"));
//				if(temp.length!=2||temp[0].indexOf(Global.WebUrl+"/accessqrcode/")<0) {
//					showErrMsgDialog("二维码解析异常，请扫描正确的二维码");
//					return;
//				}
//				String[] params=temp[1].trim().split("\\&");
//				if(params.length!=3){
//					showErrMsgDialog("二维码解析异常，请扫描正确的二维码");
//					return;
//				}
//				scanType=params[0].substring(5).toString().trim();
//				location=params[1].substring(9).toString().trim();
//				address=params[2].substring(5).toString().trim();
//				Log.d("James",scanType+" "+location+" "+address);
//				if(!scanType.equals("in")&&!scanType.equals("out")){
//					showErrMsgDialog("二维码解析异常，请扫描正确的二维码");
//					return;
//				}
//				checkSq();
//
//			}
//		} else {
//			super.onActivityResult(requestCode, resultCode, data);
//		}
	}

	private void checkSq() {
		HttpUtil.post(Global.WebUrl+"/glkg/application/listByPer?rows=5&personnelId=" + personnel).res(new HttpUtil.Res() {
			@Override
			public void res(String resData) {
				Log.d("James", resData);
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                String CurDate="";
                if(month<10) {
                    CurDate = year + "-0" + month;
                }
                else {
                    CurDate = year + "-" + month;
                }
                if(day<10) {
                    CurDate = CurDate + "-0" + day;
                }
                else {
                    CurDate = CurDate + "-" + day;
                }
				Map<String, Object> data = JSON.parseObject(resData, Map.class);
				if (((int) data.get("code")) == 0) {
					List<Map> list = (List<Map>) data.get("data");
					boolean perssionflag = false;//授权标识
					if (list.size() > 0) {
						String returnReasult = "";
						for (int i = 0; i < list.size(); i++) {
							Map map = list.get(i);
							returnReasult = map.get("applicationResult").toString().trim();
							checkStarus = map.get("applicationResult").toString();
							outDate = map.get("applicationStart").toString().split(" ")[0];
							company=map.get("companyName").toString().trim();
							dept=map.get("deptName").toString().trim();
							Log.d("James",company);
							if ((returnReasult.equals("1")&&CurDate.trim().equals(outDate.trim()))||returnReasult.equals("3")) {//审批通过和外出状态
								perssionflag = true;
								break;
							}
						}
						if (perssionflag) {
							if (returnReasult.equals("1")) {//审批状态
								if(scanType.equals("out")) {//扫出园码
									login();
								}
								else{
									showErrMsgDialog("二维码解析异常，请扫码正确的出园二维码!");
								}
							}
							if(returnReasult.equals("3")){//外出状态
								if(scanType.equals("in")){//扫入园码
									login();
								}
								else if(scanType.equals("out")) {
									if (CurDate.trim().equals(outDate.trim())) {
										Intent intent = new Intent();//外出状态再次扫描出园码
										intent.setClass(Location_BackGround_Activity.this, ScanCodeActivity.class);
										startActivity(intent);
									} else {
										showErrMsgDialog("您的外出申请日期跟当前日期不符，请先扫入园码再申请当日出园申请!");
									}
								}
							}
						}
					} else {
						perssionflag = false;
					}
					if (!perssionflag) {
						showErrMsgDialog("抱歉，您没有授权外出申请，如要外出，请跟公司领导申请！");
					}
				}
				else{
					showErrMsgDialog(data.get("msg").toString());
				}

			}
		}).execGet();
	}

	private void login(){
		HttpUtil.post(Global.WebUrl+"/glkg/personnel/updatePersonnelLogin?" +
				"personnelId=" + personnel + "" +
//				"&loginStatus=" +(scanType.equals("in") ? 0 : 1) +
		        "&recordoutPosition=" +location +
		        "&recordoutAddress=" +address +
		        "&recordoutType="  +(scanType.equals("in") ? 1 : 0)).res(new HttpUtil.Res() {
			@Override
			public void res(String resData) {
				Log.e("数据2",resData);
				Map<String,Object> data = JSON.parseObject(resData,Map.class);
				if(((int)data.get("code")) == 0){
					Intent intent = new Intent();
					intent.setClass(Location_BackGround_Activity.this, ScanCodeActivity.class);
					startActivity(intent);
				}

			}
		}).execGet();
	}

	private void start_location(){

		if(null == locationClient){
			locationClient = new AMapLocationClient(this);
		}
		startLocation();
		//启动后台定位
		locationClient.enableBackgroundLocation(2001, buildNotification());
	}

	private void loadGs(){
		HttpUtil.post(Global.WebUrl+"/glkg/company/companyList").res(new HttpUtil.Res() {
			@Override
			public void res(String resData) {
				Log.e("数据2",resData);
				Map<String,Object> data = JSON.parseObject(resData,Map.class);
				if(((int)data.get("code")) == 0){
					List<Map> list = (List<Map>) data.get("data");
					gsList = new ArrayList<String>();
					gsinfos =  new String[list.size()];

					for (int i = 0; i < list.size(); i++) {
						Map map = list.get(i);
						gsList.add(map.get("companyId").toString());
						gsinfos[i] = map.get("companyName").toString();
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(Location_BackGround_Activity.this);

					builder.setTitle("选择公司");
					builder.setItems(gsinfos, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{

							gsId=gsList.get(which);
							et_gs.setText(gsinfos[which]);
							et_bm.setText("");
						}
					});
					builder.show();
				}

			}
		}).execGet();
	}

	private void loadBm(){
		HttpUtil.post(Global.WebUrl+"/glkg/dept/deptListByCompany?companyId=" + gsId).res(new HttpUtil.Res() {
			@Override
			public void res(String resData) {
				Log.e("数据2",resData);
				Map<String,Object> data = JSON.parseObject(resData,Map.class);
				if(((int)data.get("code")) == 0){
					List<Map> list = (List<Map>) data.get("data");
					bmList = new ArrayList<String>();
					bminfos =  new String[list.size()];

					for (int i = 0; i < list.size(); i++) {
						Map map = list.get(i);
						bmList.add(map.get("DeptId").toString());
						bminfos[i] = map.get("DeptName").toString();
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(Location_BackGround_Activity.this);

					builder.setTitle("选择部门");
					builder.setItems(bminfos, new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{

							bmId=bmList.get(which);
							et_bm.setText(bminfos[which]);
						}
					});
					builder.show();
				}

			}
		}).execGet();
	}

	private void showInfo(String personnelId) {
		if(personnelId == null || personnelId.equals("") || personnelId.equals("null")) {
			tv_inforemark.setVisibility(View.VISIBLE);
			return;
		}
		tv_inforemark.setVisibility(View.GONE);
		HttpUtil.post(Global.WebUrl+"/glkg/personnel/getByPersonnelId?personnelId="+ personnelId).res(new HttpUtil.Res() {
			@Override
			public void res(String resData) {
				Log.e("数据2",resData);
				Map<String,Object> data = JSON.parseObject(resData,Map.class);
				if(((int)data.get("code")) == 500){
					Toast.makeText(Location_BackGround_Activity.this, "请填写信息后上报", Toast.LENGTH_LONG).show();
					showData();
				}else{
					lout_data.setVisibility(View.GONE);
					lout_info.setVisibility(View.VISIBLE);
					Map map = (Map)data.get("data");
					et_info_name.setText(map.get("personnelName").toString());
					name = map.get("personnelName").toString();
					personnel = map.get("personnelId").toString();
					et_info_sfz.setText(personnel.substring(0,personnel.length() - 4) + "****");
					image.setImageBitmap(QRCodeUtil.createQRCode(map.get("personnelId").toString(),600));
					String tsr;

					String fssj = map.get("personnelBackDate").toString().split(" ")[0];
					if(map.get("personnelStatus").toString().equals("2") ){
						tsr = "异常";
						et_info_jkzk.setTextColor(Color.parseColor("#ff3b30"));
					}else if(map.get("personnelStatus").toString().equals("0") ){
						tsr = "正常";
						et_info_jkzk.setTextColor(Color.parseColor("#ffe317"));
					}else {
						tsr = "正常";
						et_info_jkzk.setTextColor(Color.parseColor("#30ff52"));
					}
					et_info_jkzk.setText(tsr);
					et_info_fssj.setText("返深时间:" + fssj);
					String loc = SPUtils.get(Location_BackGround_Activity.this,"personnel","") +"";
					SPUtils.put(Location_BackGround_Activity.this,"personnel",personnel);
					Log.e("loc1",loc);
					Log.e("personnel",personnel);
					loc = SPUtils.get(Location_BackGround_Activity.this,"personnel","") +"";
					Log.e("loc2",loc);
//					startLocation();
					start_location();

				}

			}
		}).execGet();
	}
	public  Long dateDiff(String startTime,  String format) {
		// 按照传入的格式生成一个simpledateformate对象
		SimpleDateFormat sd = new SimpleDateFormat(format);
		long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数
		long nh = 1000 * 60 * 60;// 一小时的毫秒数
		long nm = 1000 * 60;// 一分钟的毫秒数
		long ns = 1000;// 一秒钟的毫秒数
		long diff;
		long day = 0;
		long hour = 0;
		long min = 0;
		long sec = 0;
		// 获得两个时间的毫秒时间差异
		try {
			diff = new Date().getTime() - sd.parse(startTime).getTime();
			day = diff / nd;// 计算差多少天
			hour = diff % nd / nh + day * 24;// 计算差多少小时
			min = diff % nd % nh / nm + day * 24 * 60;// 计算差多少分钟
			sec = diff % nd % nh % nm / ns;// 计算差多少秒
			// 输出结果

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return day;
	}
	private boolean checkData() {
		if(et_name.getText().toString().trim().equals("")){
			showErrMsgDialog("请输入姓名");
			return false;
		}
		if(et_sfz.getText().toString().trim().equals("")){
			showErrMsgDialog("请输入身份证");
			return false;
		}else if(et_sfz.getText().toString().trim().length() != 18){
			showErrMsgDialog("请输入正确的身份证");
			return false;
		}
		if(et_gj.getText().toString().trim().equals("")){
			showErrMsgDialog("请输入籍贯");
			return false;
		}
		if(et_gs.getText().toString().trim().equals("")){
			showErrMsgDialog("选择公司");
			return false;
		}
		if(et_bm.getText().toString().trim().equals("")){
			showErrMsgDialog("选择部门");
			return false;
		}
		if(et_fsrq.getText().toString().trim().equals("")){
			showErrMsgDialog("请选择返深日期");
			return false;
		}
		if(et_cfdz.getText().toString().trim().equals("")){
			showErrMsgDialog("请输入出发地址");
			return false;
		}
		if(et_fhsq.getText().toString().trim().equals("")){
			showErrMsgDialog("请选择入园日期");
			return false;
		}

		if(et_fhzz.getText().toString().trim().equals("")){
			showErrMsgDialog("请输入园外居住住址");
			return false;
		}
		if(et_hbjc.getText().toString().trim().equals("")){
			showErrMsgDialog("请输入湖北接触情况");
			return false;
		}else if(!et_hbjc.getText().toString().trim().equals("是") && !et_hbjc.getText().toString().trim().equals("否")){
			showErrMsgDialog("请填写正确的接触情况，是或否");
			return false;
		}
		if(et_sjh.getText().toString().trim().equals("")){
			showErrMsgDialog("请输入手机号码");
			return false;
		}else if(et_sjh.getText().toString().trim().length() != 11){
			showErrMsgDialog("请输入正确手机号码");
			return false;
		}
		return true;
	}

	public void showErrMsgDialog(String ErrorMsg){
		new AlertDialog.Builder(Location_BackGround_Activity.this).setTitle("错误信息")
				.setMessage(ErrorMsg).setPositiveButton("确定", null).show();
	}
	private void showData() {
		lout_data_name.setVisibility(View.VISIBLE);
		lout_data_gj.setVisibility(View.VISIBLE);
		lout_data_gs.setVisibility(View.VISIBLE);
		lout_data_bm.setVisibility(View.VISIBLE);
		lout_data_fsrq.setVisibility(View.VISIBLE);
		lout_data_cfdz.setVisibility(View.VISIBLE);
		lout_data_fhsq.setVisibility(View.VISIBLE);
		lout_data_fhzz.setVisibility(View.VISIBLE);
		lout_data_hbjc.setVisibility(View.VISIBLE);
		lout_data_sjh.setVisibility(View.VISIBLE);
		lout_data_tj.setVisibility(View.GONE);
		tv_inforemark.setVisibility(View.GONE);
		mHandler.sendEmptyMessage(MSG_LOAD_DATA);
	}

//	// 根据控件的选择，重新设置定位参数
//	private void initOption() {
//		// 设置是否需要显示地址信息
//		locationOption.setNeedAddress(true);
//		/**
//		 * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
//		 * 注意：只有在高精度模式下的单次定位有效，其他方式无效
//		 */
//		locationOption.setGpsFirst(true);
//		locationOption.setInterval(10000);
//		locationOption.setLocationCacheEnable(false);
//
//	}
//	private void initLocation(){
//		locationClient = new AMapLocationClient(this.getApplicationContext());
//		locationOption = new AMapLocationClientOption();
//		// 设置定位模式为高精度模式
//		locationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
//		// 设置定位监听
//		locationClient.setLocationListener(Location_BackGround_Activity.this);
//
//		// 创建Intent对象，action为LOCATION
//		alarmIntent = new Intent();
//		alarmIntent.setAction("LOCATION");
//		IntentFilter ift = new IntentFilter();
//
//		// 定义一个PendingIntent对象，PendingIntent.getBroadcast包含了sendBroadcast的动作。
//		// 也就是发送了action 为"LOCATION"的intent
//		alarmPi = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
//		// AlarmManager对象,注意这里并不是new一个对象，Alarmmanager为系统级服务
//		alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
//
//		//动态注册一个广播
//		IntentFilter filter = new IntentFilter();
//		filter.addAction("LOCATION");
//		registerReceiver(alarmReceiver, filter);
//	}
//	private BroadcastReceiver alarmReceiver = new BroadcastReceiver(){
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			if(intent.getAction().equals("LOCATION")){
//				if(null != locationClient){
//					locationClient.startLocation();
//				}
//			}
//		}
//	};

//	private void startLocation(){
//		initOption();
//		int alarmInterval = 20;//定位周期為20秒
//		// 设置定位参数
//		locationClient.setLocationOption(locationOption);
//		// 启动定位
//		locationClient.startLocation();
//		mHandler.sendEmptyMessage(Utils.MSG_LOCATION_START);
//
//		if(null != alarm){
//			//设置一个闹钟，60秒之后每隔一段时间执行启动一次定位程序
//			alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 60*1000,
//					alarmInterval * 1000, alarmPi);
//		}
//	}

//	Handler mLocationHandler = new Handler() {
//		public void dispatchMessage(android.os.Message msg) {
//			switch (msg.what) {
//				//开始定位
//				case Utils.MSG_LOCATION_START:
//					break;
//				// 定位完成
//				case Utils.MSG_LOCATION_FINISH:
//					AMapLocation loc = (AMapLocation) msg.obj;
//					String result = Utils.getLocationStr(loc);
//					long curtime=loc.getTime();
//					locationType=loc.getLocationType();
//					if(lastLocationDate>curtime+2000||lastLocationDate<curtime-2000) {//定位时间和当前时间相差超过2s
//						lastLocationDate=curtime;
//						if (locationType == 1 || locationType == 2 || locationType == 5) {//GPS定位、上次定位、WIFI定位
//							et_info_jd.setText(loc.getLongitude() + "");
//							et_info_wd.setText(loc.getLatitude() + "");
//							String dz = loc.getAddress() + "";
//							if (dz.length() > 20) {
//								dz = dz.substring(0, 20) + "\n" + dz.substring(20, dz.length());
//							}
//							if (dz.length() > 0) {
//								et_info_dz.setText(dz);
//							}
//							Map<String, Object> params = new HashMap<>();
//							params.put("personnelId", personnel);
//							params.put("recordPlace", loc.getAddress());
//							params.put("recordPosition", loc.getLongitude() + "," + loc.getLatitude());
//							params.put("recordType", 0);
//							params.put("recordTemperature", "");
//							params.put("recordDate", "null");
//							HttpUtil.post(Global.WebUrl + "/glkg/record/saveRecord").setParams(params).res(new HttpUtil.Res() {
//								@Override
//								public void res(String resData) {
//									Map<String, Object> data = JSON.parseObject(resData, Map.class);
//									if (((int) data.get("code")) == 0) {
//										Log.e("提交成功", resData);
//									}
//								}
//							}).execPost();
//						}
//					}
//					Log.d("James",result);
//					break;
//				//停止定位
//				case Utils.MSG_LOCATION_STOP:
//					break;
//				default:
//					break;
//			}
//		};
//	};

//	// 定位监听
//	@Override
//	public void onLocationChanged(AMapLocation loc) {
//		if (null != loc) {
//			Message msg = mLocationHandler.obtainMessage();
//			msg.obj = loc;
//			msg.what = Utils.MSG_LOCATION_FINISH;
//			mLocationHandler.sendMessage(msg);
//		}
//	}

	/**
	 * 初始化定位
	 *
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private void initLocation(){
		//初始化client
		locationClient = new AMapLocationClient(this.getApplicationContext());
		locationOption = getDefaultOption();
		//设置定位参数
		locationClient.setLocationOption(locationOption);
		// 设置定位监听
		locationClient.setLocationListener(locationListener);

		personnel = SPUtils.get(Location_BackGround_Activity.this,"personnel","")+"";
		showInfo(personnel);
	}

	/**
	 * 默认的定位参数
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private AMapLocationClientOption getDefaultOption(){
		AMapLocationClientOption mOption = new AMapLocationClientOption();
		mOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
		mOption.setGpsFirst(true);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
		mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
		mOption.setInterval(30000);//可选，设置定位间隔。默认为2秒
		mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
		mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
		mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
		AMapLocationClientOption.setLocationProtocol(AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
		mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
		mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
		mOption.setLocationCacheEnable(false); //可选，设置是否使用缓存定位，默认为true
		return mOption;
	}

	/**
	 * 定位监听
	 */
	AMapLocationListener locationListener = new AMapLocationListener() {
		@Override
		public void onLocationChanged(AMapLocation location) {
			if (null != location) {

				StringBuffer sb = new StringBuffer();
				//errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
				if(location.getErrorCode() == 0){
					sb.append("定位成功" + "\n");
					sb.append("定位类型: " + location.getLocationType() + "\n");
					sb.append("经    度    : " + location.getLongitude() + "\n");
					sb.append("纬    度    : " + location.getLatitude() + "\n");
					sb.append("精    度    : " + location.getAccuracy() + "米" + "\n");
					sb.append("提供者    : " + location.getProvider() + "\n");

					sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
					sb.append("角    度    : " + location.getBearing() + "\n");
					// 获取当前提供定位服务的卫星个数
					sb.append("星    数    : " + location.getSatellites() + "\n");
					sb.append("国    家    : " + location.getCountry() + "\n");
					sb.append("省            : " + location.getProvince() + "\n");
					sb.append("市            : " + location.getCity() + "\n");
					sb.append("城市编码 : " + location.getCityCode() + "\n");
					sb.append("区            : " + location.getDistrict() + "\n");
					sb.append("区域 码   : " + location.getAdCode() + "\n");
					sb.append("地    址    : " + location.getAddress() + "\n");
					sb.append("地    址    : " + location.getDescription() + "\n");
					sb.append("兴趣点    : " + location.getPoiName() + "\n");
					//定位完成的时间
					sb.append("定位时间: " + Utils.formatUTC(location.getTime(), "yyyy-MM-dd HH:mm:ss") + "\n");
					locationType=location.getLocationType();
					Log.d("James",sb.toString());
					et_info_jd.setText(location.getLongitude()+"");
					et_info_wd.setText(location.getLatitude()+"");

					String dz = location.getAddress()+"";
					if(dz.length() > 20) {
						dz = dz.substring(0,20) + "\n" + dz.substring(20,dz.length() );
					}
                    if(dz.length() >0){
						et_info_dz.setText(dz);
					}
                    if(location.getAddress().length()>0) {
						long curtime = location.getTime();
						locationType = location.getLocationType();
						Log.d("James2",location.getLongitude()+"");
						String[] Temp=(location.getLongitude()+"").split("\\.");
						Log.d("James2",Temp[1].length()+"");
						if (Temp[1].length()>7 &&(lastLocationDate > curtime + 2000 || lastLocationDate < curtime - 2000)) {//定位时间和当前时间相差超过2s
							lastLocationDate = curtime;
							if (locationType == 1 || locationType == 2 || locationType == 5) {//GPS定位、上次定位、WIFI定位
								Map<String, Object> params = new HashMap<>();
								params.put("personnelId", personnel);
								params.put("recordPlace", location.getAddress());
								params.put("recordPosition", location.getLongitude() + "," + location.getLatitude());
								params.put("recordType", 0);
								params.put("recordTemperature", "");
								params.put("recordDate", "null");
								HttpUtil.post(Global.WebUrl + "/glkg/record/saveRecord").setParams(params).res(new HttpUtil.Res() {
									@Override
									public void res(String resData) {

										Map<String, Object> data = JSON.parseObject(resData, Map.class);
										if (((int) data.get("code")) == 0) {
											Log.e("提交成功", resData);
										}

									}
								}).execPost();
							}
						}
					}
				} else {
					//定位失败
					sb.append("定位失败" + "\n");
					sb.append("错误码:" + location.getErrorCode() + "\n");
					sb.append("错误信息:" + location.getErrorInfo() + "\n");
					sb.append("错误描述:" + location.getLocationDetail() + "\n");
				}
				sb.append("***定位质量报告***").append("\n");
				sb.append("* WIFI开关：").append(location.getLocationQualityReport().isWifiAble() ? "开启":"关闭").append("\n");
				sb.append("* GPS状态：").append(getGPSStatusString(location.getLocationQualityReport().getGPSStatus())).append("\n");
				sb.append("* GPS星数：").append(location.getLocationQualityReport().getGPSSatellites()).append("\n");
				sb.append("****************").append("\n");
				//定位之后的回调时间
				sb.append("回调时间: " + Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss") + "\n");
				Log.e("tom",sb.toString());
				Toast.makeText(Location_BackGround_Activity.this, "定位失败,请确认打开手机GPS和网络！", Toast.LENGTH_SHORT);

			} else {
				Toast.makeText(Location_BackGround_Activity.this, "定位失败,请确认打开手机GPS和网络！", Toast.LENGTH_SHORT);
			}
		}
	};


	/**
	 * 获取GPS状态的字符串
	 * @param statusCode GPS状态码
	 * @return
	 */
	private String getGPSStatusString(int statusCode){
		String str = "";
		switch (statusCode){
			case AMapLocationQualityReport.GPS_STATUS_OK:
				str = "GPS状态正常";
				break;
			case AMapLocationQualityReport.GPS_STATUS_NOGPSPROVIDER:
				str = "手机中没有GPS Provider，无法进行GPS定位";
				break;
			case AMapLocationQualityReport.GPS_STATUS_OFF:
				str = "GPS关闭，建议开启GPS，提高定位质量";
				break;
			case AMapLocationQualityReport.GPS_STATUS_MODE_SAVING:
				str = "选择的定位模式中不包含GPS定位，建议选择包含GPS定位的模式，提高定位质量";
				break;
			case AMapLocationQualityReport.GPS_STATUS_NOGPSPERMISSION:
				str = "没有GPS定位权限，建议开启gps定位权限";
				break;
		}
		return str;
	}

	/**
	 * 开始定位
	 *
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private void startLocation(){
		// 设置定位参数
		locationClient.setLocationOption(locationOption);
		// 启动定位
		locationClient.startLocation();
	}

	/**
	 * 停止定位
	 *
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private void stopLocation(){
		// 停止定位
		locationClient.stopLocation();
	}



	/**
	 * 销毁定位
	 *
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private void destroyLocation(){
		if (null != locationClient) {
			/**
			 * 如果AMapLocationClient是在当前Activity实例化的，
			 * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
			 */
			locationClient.disableBackgroundLocation(true);
			locationClient.onDestroy();
			locationClient = null;
			locationOption = null;
		}
	}

	private void createNotificationChannel(){

	}



	private static final String NOTIFICATION_CHANNEL_NAME = "BackgroundLocation";
	private NotificationManager notificationManager = null;
	boolean isCreateChannel = false;
	@SuppressLint("NewApi")
	private Notification buildNotification() {

		Notification.Builder builder = null;
		Notification notification = null;
		if(android.os.Build.VERSION.SDK_INT >= 26) {
			//Android O上对Notification进行了修改，如果设置的targetSDKVersion>=26建议使用此种方式创建通知栏
			if (null == notificationManager) {
				notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			}
			String channelId = getPackageName();
			if(!isCreateChannel) {
				NotificationChannel notificationChannel = new NotificationChannel(channelId,
						NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
				notificationChannel.enableLights(true);//是否在桌面icon右上角展示小圆点
				notificationChannel.setLightColor(Color.BLUE); //小圆点颜色
				notificationChannel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
				notificationManager.createNotificationChannel(notificationChannel);
				isCreateChannel = true;
			}
			builder = new Notification.Builder(getApplicationContext(), channelId);
		} else {
			builder = new Notification.Builder(getApplicationContext());
		}
		builder.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(Utils.getAppName(this))
				.setContentText("正在后台运行")
				.setWhen(System.currentTimeMillis());

		if (android.os.Build.VERSION.SDK_INT >= 16) {
			notification = builder.build();
		} else {
			return builder.getNotification();
		}
		return notification;
	}

}
