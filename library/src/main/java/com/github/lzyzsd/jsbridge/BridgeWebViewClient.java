package com.github.lzyzsd.jsbridge;

import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 如果要自定义WebViewClient必须要集成此类
 * Created by bruce on 10/28/15.
 */
public class BridgeWebViewClient extends WebViewClient {
    private static final String toLoadJs = "WebViewJavascriptBridge.js";

    private long uniqueId = 0;

    private Map<String, BridgeHandler> messageHandlers = new HashMap<>();
    private Map<String, CallBackFunction> responseCallbacks = new HashMap<>();
    private List<Message> startupMessage = new ArrayList<>();

    public List<Message> getStartupMessage() {
        return startupMessage;
    }

    public void setStartupMessage(List<Message> startupMessage) {
        this.startupMessage = startupMessage;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) { // 如果是返回数据
            handlerReturnData(url);
            return true;
        } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) { //
            flushMessageQueue(view);
            return true;
        } else {
            return this.onCustomShouldOverrideUrlLoading(url) || super.shouldOverrideUrlLoading(view, url);
        }
    }

    // 增加shouldOverrideUrlLoading在api》=24时
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String url = request.getUrl().toString();
            try {
                url = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
            if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) { // 如果是返回数据
                handlerReturnData(url);
                return true;
            } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) { //
                flushMessageQueue(view);
                return true;
            } else {
                return this.onCustomShouldOverrideUrlLoading(url) || super.shouldOverrideUrlLoading(view, request);
            }
        } else {
            return super.shouldOverrideUrlLoading(view, request);
        }
    }

    /**
     * 获取到CallBackFunction data执行调用并且从数据集移除
     *
     * @param url
     */
    private void handlerReturnData(String url) {
        String functionName = BridgeUtil.getFunctionFromReturnUrl(url);
        CallBackFunction f = responseCallbacks.get(functionName);
        String data = BridgeUtil.getDataFromReturnUrl(url);
        if (f != null) {
            f.onCallBack(data);
            responseCallbacks.remove(functionName);
        }
    }

    /**
     * 刷新消息队列
     */
    private void flushMessageQueue(final WebView view) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            loadUrl(view, BridgeUtil.JS_FETCH_QUEUE_FROM_JAVA, new CallBackFunction() {

                @Override
                public void onCallBack(String data) {
                    // deserializeMessage 反序列化消息
                    List<Message> list = null;
                    try {
                        list = Message.toArrayList(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    if (list == null || list.size() == 0) {
                        return;
                    }
                    for (int i = 0; i < list.size(); i++) {
                        Message m = list.get(i);
                        String responseId = m.getResponseId();
                        // 是否是response  CallBackFunction
                        if (!TextUtils.isEmpty(responseId)) {
                            CallBackFunction function = responseCallbacks.get(responseId);
                            String responseData = m.getResponseData();
                            function.onCallBack(responseData);
                            responseCallbacks.remove(responseId);
                        } else {
                            CallBackFunction responseFunction = null;
                            // if had callbackId 如果有回调Id
                            final String callbackId = m.getCallbackId();
                            if (!TextUtils.isEmpty(callbackId)) {
                                responseFunction = new CallBackFunction() {
                                    @Override
                                    public void onCallBack(String data) {
                                        Message responseMsg = new Message();
                                        responseMsg.setResponseId(callbackId);
                                        responseMsg.setResponseData(data);
                                        queueMessage(view, responseMsg);
                                    }
                                };
                            } else {
                                responseFunction = new CallBackFunction() {
                                    @Override
                                    public void onCallBack(String data) {
                                        // do nothing
                                    }
                                };
                            }
                            // BridgeHandler执行
                            BridgeHandler handler = null;
                            if (!TextUtils.isEmpty(m.getHandlerName())) {
                                handler = messageHandlers.get(m.getHandlerName());
                            }
                            if (handler != null) {
                                handler.handler(m.getData(), responseFunction);
                            }
                        }
                    }
                }
            });
        }
    }

    private void loadUrl(WebView view, String jsUrl, CallBackFunction returnCallback) {
        view.loadUrl(jsUrl);
        // 添加至 Map<String, CallBackFunction>
        responseCallbacks.put(BridgeUtil.parseFunctionName(jsUrl), returnCallback);
    }

    /**
     * list<message> != null 添加到消息集合否则分发消息
     *
     * @param m Message
     */
    private void queueMessage(WebView view, Message m) {
        if (startupMessage != null) {
            startupMessage.add(m);
        } else {
            dispatchMessage(view, m);
        }
    }

    /**
     * 分发message 必须在主线程才分发成功
     *
     * @param m Message
     */
    private void dispatchMessage(WebView view, Message m) {
        String messageJson = m.toJson();
        //escape special characters for json string  为json字符串转义特殊字符
        messageJson = messageJson.replaceAll("(\\\\)([^utrn])", "\\\\\\\\$1$2");
        messageJson = messageJson.replaceAll("(?<=[^\\\\])(\")", "\\\\\"");
        messageJson = messageJson.replaceAll("(?<=[^\\\\])(\')", "\\\\\'");
        messageJson = messageJson.replaceAll("%7B", URLEncoder.encode("%7B"));
        messageJson = messageJson.replaceAll("%7D", URLEncoder.encode("%7D"));
        messageJson = messageJson.replaceAll("%22", URLEncoder.encode("%22"));
        String javascriptCommand = String.format(BridgeUtil.JS_HANDLE_MESSAGE_FROM_JAVA, messageJson);
        // 必须要找主线程才会将数据传递出去 --- 划重点
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            view.loadUrl(javascriptCommand);
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        // 加载初始化所需的 Js
        BridgeUtil.webViewLoadLocalJs(view, toLoadJs);

        if (getStartupMessage() != null) {
            for (Message m : getStartupMessage()) {
                dispatchMessage(view, m);
            }
            setStartupMessage(null);
        }
    }


    protected boolean onCustomShouldOverrideUrlLoading(String url) {
        return false;
    }

    /**
     * call javascript registered handler
     * 调用javascript处理程序注册
     *
     * @param handlerName handlerName
     * @param data        data
     * @param callBack    CallBackFunction
     */
    public void callHandler(WebView view, String handlerName, String data, CallBackFunction callBack) {
        doSend(view, handlerName, data, callBack);
    }

    /**
     * 保存message到消息队列
     *
     * @param handlerName      handlerName
     * @param data             data
     * @param responseCallback CallBackFunction
     */
    private void doSend(WebView view, String handlerName, String data, CallBackFunction responseCallback) {
        Message m = new Message();
        if (!TextUtils.isEmpty(data)) {
            m.setData(data);
        }
        if (responseCallback != null) {
            String callbackStr = String.format(BridgeUtil.CALLBACK_ID_FORMAT, ++uniqueId + (BridgeUtil.UNDERLINE_STR + SystemClock.currentThreadTimeMillis()));
            responseCallbacks.put(callbackStr, responseCallback);
            m.setCallbackId(callbackStr);
        }
        if (!TextUtils.isEmpty(handlerName)) {
            m.setHandlerName(handlerName);
        }
        queueMessage(view, m);
    }

    /**
     * register handler,so that javascript can call it
     * 注册处理程序,以便javascript调用它
     *
     * @param handlerName handlerName
     * @param handler     BridgeHandler
     */
    public void registerHandler(String handlerName, BridgeHandler handler) {
        if (handler != null) {
            // 添加至 Map<String, BridgeHandler>
            messageHandlers.put(handlerName, handler);
        }
    }

    /**
     * unregister handler
     *
     * @param handlerName
     */
    public void unregisterHandler(String handlerName) {
        if (handlerName != null) {
            messageHandlers.remove(handlerName);
        }
    }
}