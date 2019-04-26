package cn.org.bjca.cipherproxy;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.Log;
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

import cn.org.bjca.cipherproxy.bean.Action;
import cn.org.bjca.cipherproxy.updownload.DownLoadObserver;
import cn.org.bjca.cipherproxy.updownload.DownloadInfo;
import cn.org.bjca.cipherproxy.updownload.DownloadManager;
import cn.org.bjca.cipherproxy.websocket.Spanny;
import cn.org.bjca.cipherproxy.websocket.WsManager;
import cn.org.bjca.cipherproxy.websocket.WsStatusListener;
import okhttp3.OkHttpClient;

import okhttp3.Response;
import okio.ByteString;

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
            int command = 0;
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(text);
                command = (int) jsonObject.get("command");

                if (command == 10001) {//注册结果

                    int resultCode = (int) jsonObject.get("resultCode");
                    if (resultCode == 1) {
                        Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    } else if (resultCode == 0)
                        Toast.makeText(MainActivity.this, "Fail", Toast.LENGTH_SHORT).show();
                    else Log.e("result", "命令错误,或json串错误");
                }
                if (command == 10002) {//绑定命令
                    String taskId = (String) jsonObject.get("taskId");

                }
                if (command == 10004) {//解绑命令
                    String taskId = (String) jsonObject.get("taskId");
                }
                if (command == 10005) {//安装apk
                    //    String url = (String) jsonObject.get("path");
                    String url = "https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=3803697947,2311042153&fm=11&gp=0.jpg";

                    downloadFile(url);
                    //TODO 下载apk并安装
                }
                if (command == 10007) {//测试命令
                    String content = (String) jsonObject.get("content");
                    String str = content.substring(21);//去掉base64的头部 data:text/xml;base64,
                    byte[] bytes = Base64.decode(str, Base64.NO_WRAP);// 将字符串转换为byte数组
                    ByteArrayInputStream in = new ByteArrayInputStream(bytes);

                    Action.ActionBean actionBean = getActionBean(in);
                    System.out.println(actionBean);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

    }

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

    public void sendMsg(View view) {
        wsManager.sendMessage("Hello, I am clint");
    }

    public void register(View view) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("command", 10000);
            jsonObject.put("type", 1);
            jsonObject.put("mac", "device_tag_1234567890");
            jsonObject.put("name", "00001");
            jsonObject.put("taskId", "");
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
            Action.ActionBean actionBean = getActionBean(in);
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

    @Override
    protected void onDestroy() {
        if (wsManager != null) {
            wsManager.stopConnect();
            wsManager = null;
        }

        super.onDestroy();
    }

}
