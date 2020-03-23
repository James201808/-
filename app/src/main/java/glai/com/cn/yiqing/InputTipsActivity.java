package glai.com.cn.yiqing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import glai.com.cn.yiqing.scan.AddressClass;

public class InputTipsActivity extends Activity implements TextWatcher,Inputtips.InputtipsListener, AdapterView.OnItemClickListener {

    private AutoCompleteTextView search_edittext;
    private ListView search_list;
    private SearchAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_input_tips);
        initView();
    }
    private void initView(){
        search_edittext = (AutoCompleteTextView) findViewById(R.id.search_edit);
        search_list = (ListView) findViewById(R.id.search_list);
        search_edittext.addTextChangedListener(this);
        search_list.setOnItemClickListener(this);
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {

    }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        String content=s.toString().trim();//获取自动提示输入框的内容
        InputtipsQuery inputtipsQuery=new InputtipsQuery(content,"");//初始化一个输入提示搜索对象，并传入参数
        inputtipsQuery.setCityLimit(false);//将获取到的结果进行城市限制筛选
        Inputtips inputtips=new Inputtips(this,inputtipsQuery);//定义一个输入提示对象，传入当前上下文和搜索对象
        inputtips.setInputtipsListener(this);//设置输入提示查询的监听，实现输入提示的监听方法onGetInputtips()
        inputtips.requestInputtipsAsyn();//输入查询提示的异步接口实现
    }
    @Override
    public void afterTextChanged(Editable s) {

    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
//        Toast.makeText(this,parent.getAdapter().getItem(0).toString().trim()+","+parent.getAdapter().getItem(1).toString().trim(),Toast.LENGTH_LONG).show();
//        //Item点击事件处理
//        AddressClass.setAddressName(parent.getAdapter().getItem(0).toString().trim());
//        AddressClass.setLocator(parent.getAdapter().getItem(1).toString().trim());
        String coordinate=((HashMap)(parent.getAdapter().getItem(position))).get("address").toString().trim();
        String[] Temp=coordinate.split(",");
        coordinate=Temp[1].trim()+","+Temp[0];
        Intent intent=new Intent(InputTipsActivity.this,TemperatureActivity.class);
        intent.putExtra("AddressName",((HashMap)(parent.getAdapter().getItem(position))).get("name").toString().trim());
        intent.putExtra("Locator",coordinate);
        Log.d("James","选择"+parent.getAdapter().getItem(position).toString().trim());
        setResult(1001, intent);
        finish();
    }

    @Override
    /*
     输入提示的回调方法
     参数1：提示列表
     参数2：返回码
     */
    public void onGetInputtips(List<Tip> list, int returnCode) {
        if(returnCode== AMapException.CODE_AMAP_SUCCESS){//如果输入提示搜索成功
            List<HashMap<String,String>> searchList=new ArrayList<HashMap<String, String>>() ;
            for (int i=0;i<list.size();i++){
                HashMap<String,String> hashMap=new HashMap<String, String>();
                hashMap.put("name",list.get(i).getDistrict()+list.get(i).getName());
                hashMap.put("address",list.get(i).getPoint().toString());//将地址信息取出放入HashMap中
                searchList.add(hashMap);//将HashMap放入表中

            }
            mAdapter=new SearchAdapter(this);//新建一个适配器
            search_list.setAdapter(mAdapter);//为listview适配
            SimpleAdapter aAdapter = new SimpleAdapter(getApplicationContext(), searchList, R.layout.adapter_inputtips,
                    new String[] {"name","address"}, new int[] {R.id.item_title, R.id.item_text});

            search_list.setAdapter(aAdapter);
            aAdapter.notifyDataSetChanged();//动态更新listview


        }else{
            //ToastUtil.show(this,returnCode);

        }

    }
}
