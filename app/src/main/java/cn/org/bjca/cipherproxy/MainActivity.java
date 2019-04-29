package cn.org.bjca.cipherproxy;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import cn.org.bjca.ciphermodule.CipherAidlInterface;
import cn.org.bjca.cipherproxy.bean.ActionBean;
import cn.org.bjca.cipherproxy.updownload.DownLoadObserver;
import cn.org.bjca.cipherproxy.updownload.DownloadInfo;
import cn.org.bjca.cipherproxy.updownload.DownloadManager;
import cn.org.bjca.cipherproxy.websocket.Spanny;
import cn.org.bjca.cipherproxy.websocket.WsManager;
import cn.org.bjca.cipherproxy.websocket.WsStatusListener;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okio.ByteString;

import static cn.org.bjca.cipherproxy.utils.Base64Util.base64ConvertStr;
import static cn.org.bjca.cipherproxy.utils.Base64Util.strConvertBase64;
import static cn.org.bjca.cipherproxy.utils.Obj2Json.toJson;
import static cn.org.bjca.cipherproxy.utils.ParseXML.getActionBean;

public class
MainActivity extends AppCompatActivity {
    private EditText et_url;
    private Button btn_connect;
    private Button btn_disconnect;
    private Button btn_download;
    private Button btn_install;
    private ProgressBar progress;
    private final static String TAG = "MainActivity";
    private Button button;
    private WsManager wsManager;
    private TextView btn_send, btn_clear, tv_content;
    private Gson gson = new Gson();

    private WsStatusListener wsStatusListener = new WsStatusListener() {
        @Override
        public void onOpen(Response response) {
            Log.d(TAG, "WsManager-----onOpen");
            tv_content.append(Spanny.spanText("服务器连接成功\n\n", new ForegroundColorSpan(
                    ContextCompat.getColor(getBaseContext(), R.color.colorPrimary))));
        }

        @Override
        public void onMessage(String text) {
            Log.d(TAG, "WsManager-----onMessage");
            try {
                JSONObject jsonObject = new JSONObject(text);//外部json
                int command = Integer.parseInt(jsonObject.optString("command", "0"));
                String data = jsonObject.optString("data");

                String jsonString = base64ConvertStr(data);
                JSONObject innerJsonObject = new JSONObject(jsonString);//内部json

                if (command == 10001) {//注册结果

                    int resultCode = Integer.parseInt(innerJsonObject.optString("resultCode"));
                    String resultMsg = innerJsonObject.optString("resultMsg");

                    if (resultCode == 1) {
                        Toast.makeText(MainActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                    } else if (resultCode == 0)
                        Toast.makeText(MainActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                    else Log.e("result", "命令错误,或json串错误");
                } else if (command == 10002) {//绑定命令
                    String taskId = innerJsonObject.optString("taskId");
                    bindResult(taskId);
                } else if (command == 10004) {//解绑命令
                    String taskId = innerJsonObject.optString("taskId");
                    SharedPreferences share = getSharedPreferences("CIPHER_PROXY", MODE_PRIVATE);
                    SharedPreferences.Editor editor = share.edit();
                    String oldTaskId = share.getString("taskId", "");
                    if (oldTaskId.equals(taskId)) {
                        editor.remove("taskId");
                        editor.apply();
                    }
                } else if (command == 10005) {//安装apk
                    //    String url = (String) innerJsonObject.get("path");
                    String url = "https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=3803697947,2311042153&fm=11&gp=0.jpg";

                    downloadFile(url);
                    //TODO 下载apk并安装
                } else if (command == 10007) {//测试命令
                    String action = innerJsonObject.optString("action");
                    // String str = action.substring(21);//去掉base64的头部 data:text/xml;base64,
                    byte[] bytes = Base64.decode(action, Base64.NO_WRAP);// 将字符串转换为byte数组
                    ByteArrayInputStream in = new ByteArrayInputStream(bytes);

                    ActionBean actionBean = getActionBean(in);

                    String json = toJson(actionBean);
                    Log.e("msg", json);
                    sendJson2TestApp(json, actionBean.getId());
                    Log.e("msg", actionBean.getId() + "" + actionBean.getParam());
                } else {
                    Log.e("msg", "命令错误");
                }


                tv_content.append(Spanny.spanText("服务器 " + DateUtils.formatDateTime(getBaseContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME) + "\n", new ForegroundColorSpan(ContextCompat.getColor(getBaseContext(), R.color.colorPrimary))));
                //   tv_content.append(text + "\n\n");
                // tv_content.append(fromHtmlText(text) + "\n\n");

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }


        @Override
        public void onMessage(ByteString bytes) {
            Log.d(TAG, "WsManager-----onMessage");
        }

        @Override
        public void onReconnect() {
            Log.d(TAG, "WsManager-----onReconnect");
            tv_content.append(Spanny.spanText("服务器重连接中...\n", new ForegroundColorSpan(
                    ContextCompat.getColor(getBaseContext(), android.R.color.holo_red_light))));
        }

        @Override
        public void onClosing(int code, String reason) {
            Log.d(TAG, "WsManager-----onClosing");
            tv_content.append(Spanny.spanText("服务器连接关闭中...\n", new ForegroundColorSpan(
                    ContextCompat.getColor(getBaseContext(), android.R.color.holo_red_light))));
        }

        @Override
        public void onClosed(int code, String reason) {
            Log.d(TAG, "WsManager-----onClosed");
            tv_content.append(Spanny.spanText("服务器连接已关闭\n", new ForegroundColorSpan(ContextCompat.getColor(getBaseContext(), android.R.color.holo_red_light))));
        }

        @Override
        public void onFailure(Throwable t, Response response) {
            Log.d(TAG, "WsManager-----onFailure");
            tv_content.append(Spanny.spanText("服务器连接失败\n", new ForegroundColorSpan(
                    ContextCompat.getColor(getBaseContext(), android.R.color.holo_red_light))));
        }
    };
    private CipherAidlInterface cipherInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        Intent intent = new Intent();
        intent.setAction("cn.org.bjca.ciphermodule.CipherService");
        intent.setPackage("cn.org.bjca.ciphermodule");
        bindService(intent, connection, Service.BIND_AUTO_CREATE);

    }

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            cipherInterface = CipherAidlInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void initView() {
        et_url = findViewById(R.id.et_url);
        btn_connect = findViewById(R.id.btn_connect);
        btn_disconnect = findViewById(R.id.btn_disconnect);
        btn_download = findViewById(R.id.btn_download);
        btn_install = findViewById(R.id.btn_install);
        tv_content = findViewById(R.id.tv_content);
        progress = findViewById(R.id.progress);

    }

    public void connect(View view) {
        String url = et_url.getText().toString();
        if (!TextUtils.isEmpty(url) && url.contains("ws")) {
            if (wsManager != null) {
                wsManager.stopConnect();
                wsManager = null;
            }
            wsManager = new WsManager.Builder(getBaseContext()).client(
                    new OkHttpClient().newBuilder()
                            .pingInterval(15, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(true)
                            .build())
                    .needReconnect(true)
                    .wsUrl(url)
                    .build();
            wsManager.setWsStatusListener(wsStatusListener);
            wsManager.startConnect();
        } else {
            Toast.makeText(getBaseContext(), "请填写需要连接的地址", Toast.LENGTH_SHORT).show();
        }
    }

    private void bindResult(String taskId) {
        SharedPreferences share = getSharedPreferences("CIPHER_PROXY", MODE_PRIVATE);
        SharedPreferences.Editor editor = share.edit();
        String oldTaskId = share.getString("taskId", "");

        if (oldTaskId.equals("")) {
            editor.putString("taskId", taskId);
            editor.apply();
            wsManager.sendMessage(sendResultJson(1, "绑定成功"));
            Log.e("bind", "绑定成功");
        } else {
            if (oldTaskId.equals(taskId))
                wsManager.sendMessage(sendResultJson(1, "重连成功"));
            Log.e("bind", "重连成功");
            if (!oldTaskId.equals(taskId))
                wsManager.sendMessage(sendResultJson(1, "不重连"));
            Log.e("bind", "不重连");
        }
    }

    private String sendResultJson(int resultCode, String resultMsg) {
        String resultJson = "";
        try {
            JSONObject innerJsonObject = new JSONObject();//内部json
            innerJsonObject.put("resultCode", resultCode);
            innerJsonObject.put("resultMsg", resultMsg);
            String innerJsonBase64 = strConvertBase64(innerJsonObject.toString());

            JSONObject jsonObject = new JSONObject();//外部json
            jsonObject.put("command", 10003);
            jsonObject.put("data", innerJsonBase64);
            resultJson = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultJson;
    }


    private void downloadFile(String url) {
        DownloadManager.getInstance().download(url, new DownLoadObserver() {
            @Override
            public void onNext(DownloadInfo value) {
                super.onNext(value);
                progress.setMax((int) value.getTotal());
                progress.setProgress((int) value.getProgress());
            }

            @Override
            public void onComplete() {
                if (downloadInfo != null) {
                    Toast.makeText(MainActivity.this,
                            downloadInfo.getFileName() + "下载完成",
                            Toast.LENGTH_SHORT).show();
                    String filePath = Environment.getExternalStorageDirectory() + "/CipherTest/apk/" + downloadInfo.getFileName();
                    install(filePath);
                }
            }
        });


    }

    private void install(String filePath) {
        File apkFile = new File(filePath);
        Intent install;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //判读版本是否在7.0以上
            Uri apkUri = FileProvider.getUriForFile(this, "cn.org.bjca.cipherproxy.fileprovider", apkFile);//在AndroidManifest中的android:authorities值
            install = new Intent(Intent.ACTION_VIEW);
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            install.setDataAndType(apkUri, "application/vnd.android.package-archive");

        } else {  //以前的启动方法

            install = new Intent(Intent.ACTION_VIEW);
            install.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        }
        startActivityForResult(install, 0);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == 0)
                Log.e("install", "安装成功");
            if (resultCode == 1)
                Log.e("install", "安装失败");
        }
    }


    public void sendMsg(View view) {
        wsManager.sendMessage("Hello, I am clint");
    }

    public void sendJson2TestApp(String json, String actionid) {
        SharedPreferences share = getSharedPreferences("CIPHER_PROXY", MODE_PRIVATE);
        String taskId = share.getString("taskId", "");
        String result = null;
        try {
            result = cipherInterface.invokingInterface(json);


            JSONObject innerJsonObject = new JSONObject();//内部json
            innerJsonObject.put("resultCode", 1);
            innerJsonObject.put("resultMsg", "测试成功");
            innerJsonObject.put("taskId", taskId);
            innerJsonObject.put("actionId", actionid);
            innerJsonObject.put("result", result);
            String innerJsonBase64 = strConvertBase64(innerJsonObject.toString());

            JSONObject jsonObject = new JSONObject();//外部json
            jsonObject.put("command", 10008);
            jsonObject.put("data", innerJsonBase64);
            String resultJson = jsonObject.toString();

            wsManager.sendMessage(resultJson);
            Log.e("result", result);
        } catch (RemoteException e) {
            e.printStackTrace();
            JSONObject innerJsonObject = new JSONObject();//内部json
            try {
                innerJsonObject.put("resultCode", 0);
                innerJsonObject.put("resultMsg", "测试失败");
                innerJsonObject.put("taskId", taskId);
                innerJsonObject.put("actionId", actionid);
                innerJsonObject.put("result", result);
                String innerJsonBase64 = strConvertBase64(innerJsonObject.toString());

                JSONObject jsonObject = new JSONObject();//外部json
                jsonObject.put("command", 10008);
                jsonObject.put("data", innerJsonBase64);
                String resultJson = jsonObject.toString();
                wsManager.sendMessage(resultJson);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void register(View view) {
        JSONObject jsonObject = new JSONObject();
        JSONObject innerJsonObject = new JSONObject();
        try {
            innerJsonObject.put("type", 1);
            innerJsonObject.put("mac", "device_tag_1234567890");
            innerJsonObject.put("name", "xiaomi 5");
            innerJsonObject.put("taskId", "");//任务ID绑定时才有，用于断线重连
            String base64 = strConvertBase64(innerJsonObject.toString());

            jsonObject.put("command", 10000);
            jsonObject.put("data", base64);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        wsManager.sendMessage(jsonObject.toString());
    }

    private Spanned fromHtmlText(String s) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(s, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(s);
        }
        return result;
    }

    public void parseXML(View view) {
        //得到资源中的数据流
        String fileName = "testcaseEnDecrypt.xml"; //文件名字
        try {
            InputStream in = getResources().getAssets().open(fileName);
            ActionBean actionBean = getActionBean(in);
            System.out.println(
                    actionBean.getCount() + "--" +
                            actionBean.getId() + "--" +
                            actionBean.getType() + "--" +
                            actionBean.getFlow() + "--" +
                            actionBean.getNext() + "--" +
                            actionBean.getFuncname() + "--" +
                            actionBean.getFuncid() + "--" +
                            actionBean.getCount() + "--" +

                            actionBean.getParam().get(0).getContent() + "--" +
                            actionBean.getParam().get(0).getName() + "--" +
                            actionBean.getParam().get(1).getContent() + "--" +
                            actionBean.getParam().get(1).getName()


            );

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }


    public void disconnect(View view) {
        if (wsManager != null) {
            wsManager.stopConnect();
            wsManager = null;
        }
    }

    //点击返回键时不销毁activity
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (wsManager != null) {
            wsManager.stopConnect();
            wsManager = null;
        }

        super.onDestroy();
    }

}
