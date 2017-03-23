package chen.jsbridgedemo;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import chen.jsbridgedemo.jsbridge.BridgeHandler;
import chen.jsbridgedemo.jsbridge.BridgeWebView;
import chen.jsbridgedemo.jsbridge.CallBackFunction;
import chen.jsbridgedemo.jsbridge.DefaultHandler;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
        private static final String TAG = MainActivity.class.getName();
        private BridgeWebView mBridgeWebView;
        private ValueCallback<Uri> mUploadMessage;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);
                findViewById(R.id.btn_main_js_to_native).setOnClickListener(this);
                findViewById(R.id.btn_main_native_to_js).setOnClickListener(this);
                mBridgeWebView = (BridgeWebView) findViewById(R.id.bwv_main_display);
                mBridgeWebView.setDefaultHandler(new DefaultHandler());
                mBridgeWebView.loadUrl("file:///android_asset/demo.html");
                mBridgeWebView.setWebChromeClient(new WebChromeClient() {
                        @SuppressWarnings("unused")
                        public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType, String capture) {
                                this.openFileChooser(uploadMsg);
                        }

                        @SuppressWarnings("unused")
                        public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType) {
                                this.openFileChooser(uploadMsg);
                        }

                        void openFileChooser(ValueCallback<Uri> uploadMsg) {
                                mUploadMessage = uploadMsg;
                                pickFile();
                        }
                });
                mBridgeWebView.registerHandler("getContentFromNative", new BridgeHandler() {
                        @Override
                        public void handler(String data, CallBackFunction function) {
                                Log.e(TAG, "这里处理来自js的数据:" + data);
                                function.onCallBack("这是来自native的结果");
                        }
                });
                String data = "1这是我本地要传递到js的数据";
                mBridgeWebView.callHandler("getContentFromJs", data, new CallBackFunction() {
                        @Override
                        public void onCallBack(String data) {
                                Log.e(TAG, "这是我从js得到的回应数据" + data);

                        }
                });
                mBridgeWebView.send("发送一个空数据给js,应该有js注册的默认处理方法处理");
                Log.e(TAG, "修改啦啦啦");

        }

        private void pickFile() {
                Log.e(TAG, "这里传递过来文件拉");
                mUploadMessage.onReceiveValue(null);
                mUploadMessage = null;
        }

        @Override
        public void onClick(View v) {
                switch (v.getId()) {
                        case R.id.btn_main_native_to_js:
                                Log.e(TAG, "本地方法调用js方法");
                                String data = "onClick数据";
                                mBridgeWebView.callHandler("getContentFromJs", data, new CallBackFunction() {
                                        @Override
                                        public void onCallBack(String data) {
                                                Log.e(TAG, "这是我从js得到的回应数据" + data);
                                        }
                                });
                                break;
                        case R.id.btn_main_js_to_native:
                                Log.e(TAG, "js方法调用本地方法,暂时没有");
                                break;

                }
        }

        public String getContentFromAsset() {
                try {
                        InputStream in = getAssets().open("demo.html");
                        BufferedInputStream buffer = new BufferedInputStream(in);
                        byte[] a = new byte[1024];
                        int count = 0;
                        int len;
                        StringBuilder string = new StringBuilder();
                        while ((len = buffer.read(a)) != -1) {
                                count += len;
                                string.append(new String(a));
                        }
                        return string.toString();
                } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "解析出现异常拉拉");
                }
                return null;
        }
}
