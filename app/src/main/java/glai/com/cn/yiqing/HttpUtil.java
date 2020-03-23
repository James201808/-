package glai.com.cn.yiqing;

import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {

    private static HttpHandler httpHandler = new HttpHandler();
    public static final MediaType JSON = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    public String url;

    private Map<String,Object> params = new HashMap<>();

    public String resData;

    public Res res;

    public HttpUtil httpUtil;

    public static HttpUtil post(String url){
        HttpUtil httpUtil = new HttpUtil();
        httpUtil.url = url;
        httpUtil.httpUtil = httpUtil;
        return httpUtil;
    }

    public HttpUtil setParams(String name, Object value){
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(name,value);
        return this;
    }
    public HttpUtil setParams(Map<String,Object> params){

        this.params = params;
        return this;
    }

    public HttpUtil res(Res res) {
        this.res = res;
        return this;
    }

    public void execPost() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    url += "?";

                    for (String k: params.keySet()) {
                        url += k + "=" + params.get(k).toString() + "&";
                    }
                    url = url.substring(0,url.length() -1);
                    Log.d("James",url);
                    OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象
                    //RequestBody requestBody = RequestBody.create(JSON, com.alibaba.fastjson.JSON.toJSONString(params));
                            Request request = new Request.Builder()
                            .url(url)//请求接口。如果需要传参拼接到接口后面。

                            .build();//创建Request 对象

                    Response response  = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String res = response.body().string();
                        httpUtil.resData = res;
                        Message msg = Message.obtain();
                        msg.obj = httpUtil;
                        httpHandler.sendMessage(msg);
                    } else {
                        Message msg=Message.obtain();
                        httpUtil.resData ="{\"msg\":\"操作失败,请检查网络是否正常\",\"code\":-200,\"data\":{}}";
                        Log.d("James1",httpUtil.resData);
                        msg.obj = httpUtil;
                        httpHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    Message msg=Message.obtain();
                    httpUtil.resData ="{\"msg\":\"服务器请求失败,请检查网络是否正常\",\"code\":-200,\"data\":{}}";
                    Log.d("James1",httpUtil.resData);
                    msg.obj = httpUtil;
                    httpHandler.sendMessage(msg);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void execGet() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象
                    RequestBody requestBody = RequestBody.create(JSON, com.alibaba.fastjson.JSON.toJSONString(params));
                    Request request = new Request.Builder()
                            .url(url)//请求接口。如果需要传参拼接到接口后面。
                            .build();//创建Request 对象
                    Log.e("url",url);
                    Response response  = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String res = response.body().string();
                        httpUtil.resData = res;
                        Message msg = Message.obtain();
                        msg.obj = httpUtil;
                        Log.d("James1",res);
                        httpHandler.sendMessage(msg);
                    } else {
                        Message msg=Message.obtain();
                        httpUtil.resData ="{\"msg\":\"操作失败,请检查网络是否正常\",\"code\":-200,\"data\":{}}";
                        Log.d("James1",httpUtil.resData);
                        msg.obj = httpUtil;
                        httpHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    Message msg=Message.obtain();
                    httpUtil.resData ="{\"msg\":\"服务器请求失败,请检查网络是否正常\",\"code\":-200,\"data\":{}}";
                    Log.d("James1",httpUtil.resData);
                    msg.obj = httpUtil;
                    httpHandler.sendMessage(msg);
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public interface Res {
        public void res(String resData);
    }
}
