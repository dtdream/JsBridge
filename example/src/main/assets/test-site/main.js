var vConsole = new VConsole()

function invokeWhenReady(callback) {
  if (window.WebViewJavascriptBridge) {
    callback(WebViewJavascriptBridge)
  } else {
    console.log("WebViewJavascriptBridge not ready")
    document.addEventListener('WebViewJavascriptBridgeReady', function() {
          if (window.WebViewJavascriptBridge) {
            callback(WebViewJavascriptBridge)
          } else {
            console.error("WebViewJavascriptBridge not found")
          }
        },
        false
    );
  }
}

document.querySelector('#testRegisterHandler').onclick = function() {
  WebViewJavascriptBridge.registerHandler("tryRegister", function(data, responseCallback) {
      _bridgeLog(data)
      if (responseCallback) {
          responseCallback("success");
      }
  });
}

document.querySelector('#testCallHandler').onclick = function() {
  WebViewJavascriptBridge.callHandler('submitFromWeb', {
    say: "hello"
  }, function(result) {
      alert(result)
    }
  )
}

document.querySelector('#testSend').onclick = function() {
  WebViewJavascriptBridge.send({
    id: 1,
    content: "这是一个图片 <img src=\"a.png\"/> test\r\nhahaha"
  }, function(result) {
      alert(result)
    }
  )
}

function _bridgeLog(logContent) {
  document.querySelector("#android").textContent = "request from Android: " + logContent
}

invokeWhenReady(function(jsbridge) {
  jsbridge.init(function(message, responseCallback) {
      console.log("receive message from `send`: " + message);

      if (responseCallback) {
          responseCallback("default response");
      }
  });

  jsbridge.registerHandler("functionInJs", function(data, responseCallback) {
      _bridgeLog(data)
      if (responseCallback) {
          var responseData = "Javascript Says Right back aka!";
          responseCallback(responseData);
      }
  });
})
