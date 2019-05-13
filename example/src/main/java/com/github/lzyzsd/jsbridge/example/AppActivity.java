package com.github.lzyzsd.jsbridge.example;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebViewClient;
import com.github.lzyzsd.jsbridge.CallBackFunction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("SetJavaScriptEnabled")
public class AppActivity extends AppCompatActivity {

    private static final String ANDROID_INTERFACE = "CallApp";
    private static final String JS_MENU_INTERFACE = "jsMenuCallback";

    private List<JsMenuItem> optionsForJS = new ArrayList<>();

    private WebView webView;

    private BridgeWebViewClient bridgeClient;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);
        webView = (WebView) findViewById(R.id.webView);
        bridgeClient = new BridgeWebViewClient();

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(bridgeClient);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                getSupportActionBar().setTitle(title);
            }
        });

        bridgeClient.registerHandler(ANDROID_INTERFACE, new BridgeHandler() {
            @Override
            public void handler(String s, CallBackFunction callBackFunction) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String apiName = jsonObject.getString("apiName");
                    JSONObject params = jsonObject.getJSONObject("params");
                    handleRequest(apiName, params, callBackFunction);
                } catch (JSONException e) {
                    e.printStackTrace();
                    callBackFunction.onCallBack("fail");
                }
            }
        });

        webView.loadUrl("file:///android_asset/small-app/app.html");
    }

    private void handleRequest(String apiName, JSONObject params, CallBackFunction callback) throws JSONException {
        switch (apiName) {
            case "setSubTitle":
                getSupportActionBar().setSubtitle(params.optString("text", ""));
                callback.onCallBack("success");
                break;
            case "setOptionsMenu":
                final JSONArray items = params.getJSONArray("items");
                // 使用临时变量避免传参错误地更新
                List<JsMenuItem> temp = new ArrayList<>();
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    temp.add(new JsMenuItem(item.getInt("id"), item.getString("text")));
                }
                optionsForJS.clear();
                optionsForJS.addAll(temp);
                callback.onCallBack("setMenu success");
                invalidateOptionsMenu();
                break;
            default:
                callback.onCallBack("fail, not found this api");
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        for (JsMenuItem item : optionsForJS) {
            menu.add(Menu.NONE, item.id, Menu.NONE, item.text);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            bridgeClient.callHandler(webView, JS_MENU_INTERFACE, new JSONObject().put("selectedId", item.getItemId()).toString(), new CallBackFunction() {
                @Override
                public void onCallBack(String s) {
                    Toast.makeText(AppActivity.this, "receive js callback" + s, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    static class JsMenuItem {
        int id;
        String text;
        Drawable icon;

        JsMenuItem(int id, String text) {
            this.id = id;
            this.text = text;
        }

        public JsMenuItem(int id, String text, Drawable icon) {
            this.id = id;
            this.text = text;
            this.icon = icon;
        }
    }
}
