package glai.com.cn.yiqing.scan;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

//import com.journeyapps.barcodescanner.CaptureManager;
//import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import glai.com.cn.yiqing.BasisTimesUtils;
import glai.com.cn.yiqing.HttpUtil;
import glai.com.cn.yiqing.Location_BackGround_Activity;
import glai.com.cn.yiqing.R;

public class ScanCodeActivity extends Activity implements View.OnClickListener {

    private LinearLayout lout_back,lout_bcg_2,lout_bcg_3,lout_bcg;
    private EditText et_info_skat,et_info_name,et_info_company,et_info_dept;
    private TextView tv_info,et_info_date,et_info_time,lout_bcg_1,et_info_yy;
    private Button btn_ht;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_code);

        lout_back = (LinearLayout)findViewById(R.id.lout_back);
        lout_bcg_1 = (TextView)findViewById(R.id.lout_bcg_1);
        lout_bcg_2 = (LinearLayout)findViewById(R.id.lout_bcg_2);
        lout_bcg_3 = (LinearLayout)findViewById(R.id.lout_bcg_3);
        lout_bcg = (LinearLayout)findViewById(R.id.lout_bcg);
        tv_info = (TextView)findViewById(R.id.tv_info);
        et_info_time = (TextView)findViewById(R.id.et_info_time);
        et_info_date = (TextView)findViewById(R.id.et_info_date);
        et_info_yy = (TextView)findViewById(R.id.et_info_yy);
        btn_ht = (Button)findViewById(R.id.btn_ht);
        et_info_skat = (EditText)findViewById(R.id.et_info_skzt);
        et_info_name = (EditText)findViewById(R.id.et_info_name);
        et_info_company=(EditText)findViewById(R.id.et_info_company);
        et_info_dept=(EditText)findViewById(R.id.et_info_dept);

        lout_back.setOnClickListener(this);
        tv_info.setOnClickListener(this);
        btn_ht.setOnClickListener(this);
        if(Location_BackGround_Activity.checkStarus.equals("1")){
            et_info_skat.setText("允许出园");
            et_info_skat.setTextColor(Color.parseColor("#FF0000"));
        }
        else {
            if(Location_BackGround_Activity.scanType.equals("out")){
                et_info_skat.setText("允许出园");
                et_info_skat.setTextColor(Color.parseColor("#FF0000"));
            }
            else {
                et_info_skat.setText("允许入园");
                et_info_skat.setTextColor(Color.parseColor("#30ff52"));
            }
        }

//        if(Location_BackGround_Activity.checkStarus.equals("0")){
//            et_info_skat.setText("未审核");
//            et_info_skat.setTextColor(Color.parseColor("#FF0000"));
//        }else if(Location_BackGround_Activity.checkStarus.equals("1")) {
//            et_info_skat.setText("审核通过");
//            et_info_skat.setTextColor(Color.parseColor("#30ff52"));
//        }else if(Location_BackGround_Activity.checkStarus.equals("2")) {
//            et_info_skat.setText("审核不通过");
//            et_info_skat.setTextColor(Color.parseColor("#FF0000"));
//        }else if(Location_BackGround_Activity.checkStarus.equals("3")) {
//            et_info_skat.setText("已外出");
//            et_info_skat.setTextColor(Color.parseColor("#FFFF00"));
//        }
//        String dateTime = BasisTimesUtils.getStringTimeOfSSS(new Date().getTime());
//        String date = Location_BackGround_Activity.outDate;
//        String time = dateTime.split(" ")[1].substring(0,5);

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int min=calendar.get(Calendar.MINUTE);
        int hour=calendar.get(Calendar.HOUR_OF_DAY);
        et_info_date.setText(month+"-"+day);
        et_info_yy.setText(year+"");
        et_info_time.setText(hour+":"+min);
        et_info_name.setText(Location_BackGround_Activity.name);
        et_info_company.setText(Location_BackGround_Activity.company);
        et_info_dept.setText(Location_BackGround_Activity.dept);

        int dd = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

       if(dd == 1){
            update(this.getResources().getDrawable(R.drawable.btnb7),this.getResources().getDrawable(R.drawable.lot7));
        }
        else if(dd == 2){
            update(this.getResources().getDrawable(R.drawable.btnb),this.getResources().getDrawable(R.drawable.lot));
        }
        else if(dd  == 3){
            update(this.getResources().getDrawable(R.drawable.btnb2),this.getResources().getDrawable(R.drawable.lot2));
        }
        else if(dd  == 4){
            update(this.getResources().getDrawable(R.drawable.btnb3),this.getResources().getDrawable(R.drawable.lot3));
        }else if(dd  == 5){
           update(this.getResources().getDrawable(R.drawable.btnb4),this.getResources().getDrawable(R.drawable.lot4));
        }else if(dd  == 6){
           update(this.getResources().getDrawable(R.drawable.btnb5),this.getResources().getDrawable(R.drawable.lot5));
       }else if(dd  ==7){
           update(this.getResources().getDrawable(R.drawable.btnb6),this.getResources().getDrawable(R.drawable.lot6));
       }
        startTimer();

    }
    private int index = 0;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            String color = "#FF0000";
            switch (index){
                case 1:
                    color = "#FF0000";
                    break;
                case 2:
                    color = "#FF7F00";
                    break;
                case 3:
                    color = "#FFFF00";
                    break;
                case 4:
                    color = "#00FF00";
                    break;
                case 5:
                    color = "#00FFFF";
                    break;
                case 6:
                    color = "#0000FF";
                    break;
                case 7:
                    color = "#8B00FF";
                    break;
            }
            et_info_date.setTextColor(Color.parseColor(color));
        }
    };

    private void startTimer(){
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                index++;
                if(index == 8) index =0;

                handler.sendMessage(Message.obtain());
            }
        };
        timer = new Timer();
        timer.schedule(task,0,800);
    }

    private void update(Drawable btn,Drawable lou){

        btn_ht.setBackground(btn);

        lout_bcg_1.setBackground(lou);
        lout_bcg_2.setBackground(lou);
        lout_bcg_3.setBackground(lou);
        lout_bcg.setBackground(lou);
    }



    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.lout_back || v.getId() == R.id.tv_info || v.getId() == R.id.btn_ht){
           this.finish();
        }
    }
}