window.app = {
  debugMode: false,
  ready: _invokeWhenReady,
  call: _invokeApi,
  unsafeCall: _sendRequest
}

const APIS = [
  "setOptionsMenu",
  "setSubTitle"
]

function _log(message, optionalParams) {
  if (window.app.debugMode) {
    if (optionalParams) {
      console.log(message, optionalParams)
    } else {
      console.log(message)
    }
  }
}

function _invokeWhenReady(callback) {
  if (window.WebViewJavascriptBridge) {
    callback()
  } else {
    _log("jssdk not ready")
    document.addEventListener('WebViewJavascriptBridgeReady', function() {
          if (window.WebViewJavascriptBridge) {
            callback()
          } else {
            _log("#WARNING#", "jssdk not found")
          }
        },
        false
    );
  }
}

// check the api exits, then send the request
function _invokeApi(apiName, params, success, fail) {
  if (APIS.includes(apiName)) {
    _sendRequest(apiName, params, success, fail)
  } else {
    _log("#WARNING#", apiName + " not support yet")
  }
}

function _sendRequest(apiName, params, success, fail) {
  WebViewJavascriptBridge.callHandler("CallApp", {
    apiName: apiName,
    params: params
  }, function(result) {
      success(result)
    }
  )
}

_invokeWhenReady(function() {
  WebViewJavascriptBridge.registerHandler("jsMenuCallback", function(data, responseCallback) {
    _log("jsMenuCallback", data)
    var event = new CustomEvent('appMenuSelected', { detail: JSON.parse(data) });
    document.dispatchEvent(event);
  })
})
