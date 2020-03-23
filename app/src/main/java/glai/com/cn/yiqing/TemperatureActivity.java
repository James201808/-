package glai.com.cn.yiqing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.amap.api.services.help.Tip;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import glai.com.cn.yiqing.global.Global;

public class TemperatureActivity extends Activity implements View.OnClickListener {
    private Button btn_temperature_esc, btn_temperature_save, btn_search;
    private TextView tv_curlocation, tv_place;
    private String PersonId, PersonName, Address, Locator;
    private EditText et_Applydate, et_Applytime, et_temperature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);
        Intent intent = new Intent();
        Bundle bundle = this.getIntent().getExtras();
        PersonId = bundle.getString("PersonId");
        PersonName = bundle.getString("PersonName");
        Address = bundle.getString("Address");
        Locator = bundle.getString("Locator");
        btn_temperature_save = (Button) findViewById(R.id.btn_temperature_save);
        btn_temperature_esc = (Button) findViewById(R.id.btn_temperature_esc);
        btn_search = (Button) findViewById(R.id.btn_search);
        btn_temperature_esc.setOnClickListener(this);
        btn_temperature_save.setOnClickListener(this);
        btn_search.setOnClickListener(this);
        tv_curlocation = (TextView) findViewById(R.id.tv_curlocation);
        tv_curlocation.setText(Address);
        tv_place = (TextView) findViewById(R.id.tv_place);
        et_Applydate = (EditText) findViewById(R.id.et_Applydate);
        et_Applydate.setOnClickListener(this);
        et_Applytime = (EditText) findViewById(R.id.et_Applytime);
        et_Applytime.setOnClickListener(this);
        et_temperature = (EditText) findViewById(R.id.et_temperature);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        String sysdate, systime;
        if (month < 10) {
            sysdate = year + "-0" + month;
        } else {
            sysdate = year + "-" + month;
        }
        if (day < 10) {
            sysdate = sysdate + "-0" + day;
        } else {
            sysdate = sysdate + "-" + day;
        }
        et_Applydate.setText(sysdate);
        if (hour < 10) {
            systime = "0" + hour + ":";
        } else {
            systime = hour + ":";
        }
        if (minute < 10) {
            systime = systime + "0" + minute;
        } else {
            systime = systime + minute;
        }
        et_Applytime.setText(systime);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_temperature_esc:
                finish();
                break;
            case R.id.btn_temperature_save:
                if (CheckData()) {
                    if (et_temperature.getText().toString().trim().length() > 0) {
                        float temperature = Float.parseFloat(et_temperature.getText().toString().trim());
                        if (temperature > 37.3) {
                            AlertDialog alertDialog = new AlertDialog.Builder(TemperatureActivity.this)
                                    .setTitle("确认")
                                    .setMessage("体温超出37.3，请确认是否继续上报？")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            SaveRecord();
                                        }
                                    })
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            return;
                                        }
                                    }).create();
                            alertDialog.show();
                        } else {
                            SaveRecord();
                        }
                    } else {
                        SaveRecord();
                    }
                }
                break;
            case R.id.btn_search:
                Intent intent = new Intent(TemperatureActivity.this, InputTipsActivity.class);
                startActivityForResult(intent, 1000);
                break;
            case R.id.et_Applydate:
                BasisTimesUtils.showDatePickerDialog(TemperatureActivity.this, false, "请选择年月日", 2020, 2, 22, new BasisTimesUtils.OnDatePickerListener() {
                    @Override
                    public void onConfirm(int year, int month, int dayOfMonth) {
                        et_Applydate.setText(year + "-" + ((month < 10) ? "0" + month : month) + "-" + ((dayOfMonth < 10) ? "0" + dayOfMonth : dayOfMonth));
                    }

                    @Override
                    public void onCancel() {
                    }
                });
                break;
            case R.id.et_Applytime:
                BasisTimesUtils.showTimerPickerDialog(TemperatureActivity.this, "请选择时分", 10, 20, true, new BasisTimesUtils.OnTimerPickerListener() {
                    @Override
                    public void onConfirm(int hourOfDay, int minute) {
                        et_Applytime.setText(hourOfDay + ":" + (minute < 10 ? "0" + minute : minute + ""));
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                break;
        }
    }

    @Override
//重写了onAcitivityResult
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1000 && resultCode == 1001) {
            tv_curlocation.setText(data.getStringExtra("AddressName"));
            Locator = data.getStringExtra("Locator");
            Log.d("James", Locator);
//            MainActivity.this.setTitle("收到"+result);
//            Toast.makeText(getApplicationContext(), "取消", Toast.LENGTH_LONG).show();
        }
    }


    private boolean CheckData() {
        if (PersonId.trim().length() <= 0 || PersonName.trim().length() <= 0) {
            showErrMsgDialog("申请人员错误，请核对后再操作！");
            return false;
        }
        if (et_Applydate.getText().toString().trim().length() <= 0) {
            showErrMsgDialog("请选择日期(年月日)！");
            return false;
        }
        if (et_Applytime.getText().toString().trim().length() <= 0) {
            showErrMsgDialog("请选择日期(时分)！");
            return false;
        }
        return true;
    }

    public void showErrMsgDialog(String ErrorMsg) {
        new AlertDialog.Builder(TemperatureActivity.this).setTitle("错误信息")
                .setMessage(ErrorMsg).setPositiveButton("确定", null).show();
    }

    private void SaveRecord() {
        Map<String, Object> params = new HashMap<>();
        params.put("personnelId", PersonId);
        params.put("recordPlace", tv_curlocation.getText().toString().trim());
        params.put("recordPosition", Locator);
        params.put("recordType", 1);
        params.put("recordTemperature", et_temperature.getText().toString().trim());
        params.put("recordDate", et_Applydate.getText().toString().trim() + " " + et_Applytime.getText().toString().trim() + ":00");
//http://192.168.200.224:8088  https://hopetownportal.glai.com.cn
        HttpUtil.post(Global.WebUrl + "/glkg/record/saveRecord").setParams(params).res(new HttpUtil.Res() {
            @Override
            public void res(String resData) {
                Map<String, Object> data = JSON.parseObject(resData, Map.class);
                Log.d("James1",resData);
                if (((int) data.get("code")) != 500&&((int) data.get("code")) != -200) {
                    Log.e("提交成功", resData);
                    finish();
                } else {
                    showErrMsgDialog(data.get("msg").toString().trim());
                }

            }
        }).execPost();
    }
}
