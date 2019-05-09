package com.github.lzyzsd.jsbridge.example;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.github.lzyzsd.jsbridge.DefaultHandler;
import com.google.gson.Gson;

public class MainActivity extends Activity {

    private Toolbar toolbar;

    private BridgeWebView webView;

    static class Location {
        String address;
    }

    static class User {
        String name;
        Location location;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        Button button = (Button) findViewById(R.id.button);
        webView = (BridgeWebView) findViewById(R.id.webView);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.callHandler("tryRegister", "Button Clicked", new CallBackFunction() {

                    @Override
                    public void onCallBack(String data) {
                        Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
                    }

                });
            }
        });

        webView.setDefaultHandler(new DefaultHandler());
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                toolbar.setTitle(title);
            }
        });
        webView.registerHandler("submitFromWeb", new BridgeHandler() {

            @Override
            public void handler(String data, CallBackFunction function) {
                function.onCallBack("Android: receive params " + data);
            }

        });

        webView.loadUrl("file:///android_asset/test-site/index.html");
        sendMessageToWeb();
    }

    private void sendMessageToWeb() {
        User user = new User();
        Location location = new Location();
        location.address = "SDU";
        user.location = location;
        user.name = "大头鬼";

        // js 端需注册一个 "functionInJs"
        webView.callHandler("functionInJs", new Gson().toJson(user), new CallBackFunction() {
            @Override
            public void onCallBack(String data) {
                Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
            }
        });

        // js 端先调用 init
        webView.send("hello");
    }
}
