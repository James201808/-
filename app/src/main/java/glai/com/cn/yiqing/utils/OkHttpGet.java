package glai.com.cn.yiqing.utils;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class OkHttpGet {
    public static void sendRequestGetOkHttp(String url, Context context, final  okhttp3.Callback callback){

        OkHttpClient client=new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)//设置超时时间
                .readTimeout(10, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(10, TimeUnit.SECONDS)//设置写入超时时间
                .build();

        Request request=new Request.Builder()
                .url(url)
                .get()//Get请求
                .build();
        client.newCall(request).enqueue(callback);
        //enqueue()已经为我们创建好子线程，然后执行Http，最终的请求结果回调到okhttp3.Callback
    }
}
