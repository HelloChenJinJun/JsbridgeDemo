package chen.jsbridgedemo;

import chen.jsbridgedemo.jsbridge.CallBackFunction;

/**
 * 项目名称:    JsbridgeDemo
 * 创建人:        陈锦军
 * 创建时间:    2017/3/24      14:00
 * QQ:             1981367757
 */

public interface WrappedHandler {
        void onHandle(String methodName, String data, CallBackFunction callBackFunction);
}
