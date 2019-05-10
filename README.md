[![](https://jitpack.io/v/dtdream/JsBridge.svg)](https://jitpack.io/#dtdream/JsBridge)

## Introduction

If your app minSdkVersion is higher than 17, it is recommended to use the [official solution](https://developer.android.google.cn/reference/android/webkit/WebView.html#addJavascriptInterface(java.lang.Object,%20java.lang.String)).

Otherwise you can try it.For more information see [the wiki](https://github.com/dtdream/JsBridge/wiki)

## Download

```groovy
repositories {
    // ...
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation 'com.github.dtdream:jsbridge:$latest_version'
}
```

## Notice
Listen for the WebViewJavascriptBridgeReady event, ensuring that the "earlier triggered" event takes effect

```javascript
document.addEventListener('WebViewJavascriptBridgeReady', function() {
               //do your work here
  },
  false
)
```

## License

This project is licensed under the terms of the MIT license.
