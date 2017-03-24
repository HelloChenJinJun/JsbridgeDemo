package chen.jsbridgedemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import chen.jsbridgedemo.jsbridge.BridgeWebView;
import chen.jsbridgedemo.jsbridge.CallBackFunction;
import chen.jsbridgedemo.jsbridge.DefaultHandler;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, WrappedHandler, WrappedCallBack {
        private static final String TAG = MainActivity.class.getName();
        private BridgeWebView mBridgeWebView;
        private ValueCallback<Uri> mUploadMessage;
        private ProgressBar mProgressBar;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);
                mBridgeWebView = (BridgeWebView) findViewById(R.id.bwv_main_display);
                mProgressBar = (ProgressBar) findViewById(R.id.pb_main_progress);
                findViewById(R.id.btn_main_js_to_native).setOnClickListener(this);
                findViewById(R.id.btn_main_native_to_js).setOnClickListener(this);
                initJsBridgeWebView();
        }

        private void initJsBridgeWebView() {
                mBridgeWebView.setDefaultHandler(new DefaultHandler());
                WebSettings webSettings = mBridgeWebView.getSettings();
//                不允许缩放
                webSettings.setSupportZoom(false);
                // 自适应屏幕大小
                webSettings.setUseWideViewPort(true);
                webSettings.setLoadWithOverviewMode(true);
                mBridgeWebView.setWebChromeClient(new WebChromeClient() {
                        @SuppressWarnings("unused")
                        public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType, String capture) {
                                this.openFileChooser(uploadMsg);
                        }

                        @SuppressWarnings("unused")
                        public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType) {
                                this.openFileChooser(uploadMsg);
                        }


                        @Override
                        public void onProgressChanged(WebView view, int newProgress) {
                                if (newProgress == 100) {
                                        mProgressBar.setVisibility(View.GONE);
                                } else {
                                        if (mProgressBar.getVisibility() == View.GONE) {
                                                mProgressBar.setVisibility(View.VISIBLE);
                                        }
                                        mProgressBar.setProgress(newProgress);
                                }
                        }

                        void openFileChooser(ValueCallback<Uri> uploadMsg) {
                                mUploadMessage = uploadMsg;
                                pickFile();
                        }
                });
                mBridgeWebView.setOnKeyListener(new View.OnKeyListener() {
                        @Override
                        public boolean onKey(View v, int keyCode, KeyEvent event) {
                                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK
                                        && mBridgeWebView.canGoBack()) {
                                        mBridgeWebView.goBack();
                                        return true;
                                }
                                return false;
                        }
                });
                mBridgeWebView.loadUrl("file:///android_asset/demo.html");
                mBridgeWebView.registerHandler(Constant.NATIVE_GET_CONTENT_FROM_NATIVE, this);
                String data = "1这是我本地要传递到js的数据";
                mBridgeWebView.callHandler(Constant.JS_GET_CONTENT_FROM_JS, data, this);
                mBridgeWebView.send("发送一个空数据给js,应该有js注册的默认处理方法处理");
        }

        private void pickFile() {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, Constant.REQUEST_CODE_FILE);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                if (resultCode == RESULT_OK) {
                        switch (resultCode) {
                                case Constant.REQUEST_CODE_FILE:
                                        if (data != null && mUploadMessage != null) {
                                                mUploadMessage.onReceiveValue(data.getData());
                                                mUploadMessage = null;
                                        }
                                        break;

                        }
                }
        }

        @Override
        public void onClick(View v) {
                switch (v.getId()) {
                        case R.id.btn_main_native_to_js:
                                Log.e(TAG, "本地方法调用js方法");
                                String data = "onClick数据";
                                mBridgeWebView.callHandler(Constant.JS_GET_CONTENT_FROM_JS, data, new CallBackFunction() {
                                        @Override
                                        public void onCallBack(String data) {
                                                Log.e(TAG, "这是我从js得到的回应数据" + data);
                                        }
                                });
                                break;
                        case R.id.btn_main_js_to_native:
                                break;

                }
        }

        @Override
        public void onHandle(String methodName, String data, CallBackFunction callBackFunction) {
                switch (methodName) {
                        case Constant.NATIVE_GET_CONTENT_FROM_NATIVE:
                                Log.e(TAG, "这是js调用native方法传来的参数" + data);
                                callBackFunction.onCallBack("这是我们本地处理的结果");
                                break;
                }

        }

        @Override
        public void onCallBack(String methodName, String data) {
                switch (methodName) {
                        case Constant.JS_GET_CONTENT_FROM_JS:
                                Log.e(TAG, "这是我们从调用js方法得到的结果" + data);
                                break;
                }
        }
}
