package glai.com.cn.yiqing;


import android.os.Handler;
import android.os.Message;

public class HttpHandler extends Handler {

    @Override
    public void handleMessage(Message msg) {
        HttpUtil httpUtil = (HttpUtil)msg.obj;
        httpUtil.res.res(httpUtil.resData);
    }
}
