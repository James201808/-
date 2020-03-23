package glai.com.cn.yiqing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import java.text.SimpleDateFormat;

import android.graphics.Color;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import glai.com.cn.yiqing.global.Global;


public class PassActivity extends Activity implements View.OnClickListener {
    private TextView tv_lastpass;
    private EditText et_pass_name,et_pass_outDate,et_pass_inDate,et_pass_remark;
    private Button btn_pass_save,btn_pass_esc;
    private String PersonId,PersonName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass);
        Intent intent = new Intent();
        Bundle bundle = this.getIntent().getExtras();
        PersonId=bundle.getString("PersonId");
        PersonName=bundle.getString("PersonName");
        Log.d("James",PersonId+" "+PersonName);
        tv_lastpass=(TextView)findViewById(R.id.tv_lastpass);
        et_pass_name=(EditText)findViewById(R.id.et_pass_name);
        et_pass_inDate=(EditText)findViewById(R.id.et_pass_inDate);
        et_pass_outDate=(EditText)findViewById(R.id.et_pass_outDate);
        et_pass_remark=(EditText)findViewById(R.id.et_pass_remark);
        btn_pass_save=(Button)findViewById(R.id.btn_pass_save);
        btn_pass_esc=(Button)findViewById(R.id.btn_pass_esc);
        btn_pass_save.setOnClickListener(this);
        btn_pass_esc.setOnClickListener(this);
        et_pass_inDate.setOnClickListener(this);
        et_pass_outDate.setOnClickListener(this);
        et_pass_name.setText(PersonName);
        GetPassRecord();
    }

    @Override
    public void onClick(View view) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        switch (view.getId()) {
            case R.id.et_pass_inDate:
                BasisTimesUtils.showDatePickerDialog(PassActivity.this, false, "请选择年月日", year, month, day, new BasisTimesUtils.OnDatePickerListener() {
                    @Override
                    public void onConfirm(int year, int month, int dayOfMonth) {
                        et_pass_inDate.setText(year + "-" + ((month < 10 ) ? "0" + month : month) + "-" + ((dayOfMonth < 10 ) ? "0" + dayOfMonth : dayOfMonth));
                    }
                    @Override
                    public void onCancel() {
                    }
                });
                break;
            case R.id.et_pass_outDate:
                BasisTimesUtils.showDatePickerDialog(PassActivity.this, false, "请选择年月日", year, month, day, new BasisTimesUtils.OnDatePickerListener() {
                    @Override
                    public void onConfirm(int year, int month, int dayOfMonth) {
                        et_pass_outDate.setText(year + "-" + ((month < 10 ) ? "0" + month : month) + "-" + ((dayOfMonth < 10 ) ? "0" + dayOfMonth : dayOfMonth));
                    }
                    @Override
                    public void onCancel() {
                    }
                });
                break;
            case R.id.btn_pass_save:
                if(CheckData()){
                    SaveApply();
                }
                break;
            case R.id.btn_pass_esc:
                finish();
                break;
        }
    }

    private boolean CheckData(){
        if(PersonId.trim().length()<=0||PersonName.trim().length()<=0) {
            showErrMsgDialog("申请人员错误，请核对后再操作！");
            return false;
        }
        if(et_pass_outDate.getText().toString().trim().length()<=0){
            showErrMsgDialog("请选择出园日期！");
            return false;
        }
        else{
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date dateStart = dateFormat.parse(et_pass_outDate.getText().toString());
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                String CurDateStr = year + "-" + month + "-" + day;
                Date curDate = dateFormat.parse(CurDateStr);
                if (curDate.getTime() > dateStart.getTime()) {
                    showErrMsgDialog("出园日期不能早于当前日期！");
                    return false;
                }
            }catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(PassActivity.this, "数据格式有误！", Toast.LENGTH_SHORT)
                        .show();
                return false;
            }
        }
        if(et_pass_inDate.getText().toString().trim().length()<=0){
            showErrMsgDialog("请选择返回日期！");
            return false;
        }
        else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date dateStart = dateFormat.parse(et_pass_outDate.getText().toString());
                Date dateEnd = dateFormat.parse(et_pass_inDate.getText().toString());
                if(dateEnd.getTime()<dateStart.getTime()) {
                    showErrMsgDialog("返回日期不能早于出园日期！");
                    return  false;
                }
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(PassActivity.this, "数据格式有误！", Toast.LENGTH_SHORT)
                        .show();
                return false;
            }
        }
        return true;
    }

    public void showErrMsgDialog(String ErrorMsg){
        new AlertDialog.Builder(PassActivity.this).setTitle("错误信息")
                .setMessage(ErrorMsg).setPositiveButton("确定", null).show();
    }

    private void GetPassRecord() {
        HttpUtil.post(Global.WebUrl + "/glkg/application/listByPer?rows=5&personnelId=" + PersonId).res(new HttpUtil.Res() {
            @Override
            public void res(String resData) {
                Log.d("James", resData);
                Map<String, Object> data = JSON.parseObject(resData, Map.class);
                if (((int) data.get("code")) == 0) {
                    List<Map> list = (List<Map>) data.get("data");
                    if (list.size() > 0) {
                        String returnReasult = list.get(0).get("applicationResult").toString().trim();
                        String applayDate = list.get(0).get("applicationStart").toString().replace("00:00:00", "").trim();
                        switch (returnReasult) {
                            case "0":
                                tv_lastpass.setText("您最近的 " + applayDate + " 出园申请正在审批中，请耐心等候！");
                                tv_lastpass.setTextColor(Color.DKGRAY);
                                break;
                            case "1":
                                tv_lastpass.setText("您最近的 " + applayDate + " 出园申请已经审批通过，请注意安全出行！");
                                tv_lastpass.setTextColor(Color.GREEN);
                                break;
                            case "2":
                                tv_lastpass.setText("您最近的 " + applayDate + " 出园申请已经审批不予通过，请询问相关领导！");
                                tv_lastpass.setTextColor(Color.BLACK);
                                break;
                            case "3":
                                tv_lastpass.setText("您最近的 " + applayDate + " 出园申请已经审批通过，您已经离开园区！");
                                tv_lastpass.setTextColor(android.graphics.Color.BLUE);
                                break;
                            case "4":
                                tv_lastpass.setText("您最近的 " + applayDate + " 出园申请已经结束！");
                                tv_lastpass.setTextColor(Color.LTGRAY);
                                break;
                            case "5":
                                tv_lastpass.setText("您最近的 " + applayDate + " 出园申请已经作废！");
                                tv_lastpass.setTextColor(Color.RED);
                                break;
                        }
                    }
                } else {
                    tv_lastpass.setText("您还未进行出园申请！");
                }
            }
        }).execGet();
    }

    private void SaveApply(){
        HttpUtil.post(Global.WebUrl+"/glkg/application/outGoing?" +
                "personnelId=" + PersonId + "" +
                "&applicationReason=" + et_pass_remark.getText().toString().trim() + "" +
                "&applicationStart=" + et_pass_outDate.getText().toString().trim() + " 00:00:00" +
                "&applicationEnd="  +et_pass_inDate.getText().toString().trim()+" 23:59:59").res(new HttpUtil.Res() {
            @Override
            public void res(String resData) {
                Log.e("数据2",resData);
                Map<String,Object> data = JSON.parseObject(resData,Map.class);
                if(((int)data.get("code")) != 0){
                    showErrMsgDialog(data.get("msg").toString().trim());
                }
                else{
                    finish();
                }
            }
        }).execGet();
    }
}
