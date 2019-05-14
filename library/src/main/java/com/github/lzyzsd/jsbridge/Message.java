package com.github.lzyzsd.jsbridge;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * data of bridge
 *
 * @author haoqing
 */
class Message {

    private String callbackId; //callbackId
    private String responseId; //responseId
    private String responseData; //responseData
    private String data; //data of message
    private String handlerName; //name of handler

    private final static String CALLBACK_ID_STR = "callbackId";
    private final static String RESPONSE_ID_STR = "responseId";
    private final static String RESPONSE_DATA_STR = "responseData";
    private final static String DATA_STR = "data";
    private final static String HANDLER_NAME_STR = "handlerName";

    String getResponseId() {
        return responseId;
    }

    void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    String getResponseData() {
        return responseData;
    }

    void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    String getCallbackId() {
        return callbackId;
    }

    void setCallbackId(String callbackId) {
        this.callbackId = callbackId;
    }

    String getData() {
        return data;
    }

    void setData(String data) {
        this.data = data;
    }

    String getHandlerName() {
        return handlerName;
    }

    void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(CALLBACK_ID_STR, callbackId);
            jsonObject.put(DATA_STR, data);
            jsonObject.put(HANDLER_NAME_STR, handlerName);
            jsonObject.put(RESPONSE_DATA_STR, responseData);
            jsonObject.put(RESPONSE_ID_STR, responseId);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    static Message formJson(String jsonStr) {
        Message m = new Message();
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            m.setHandlerName(jsonObject.optString(HANDLER_NAME_STR, null));
            m.setCallbackId(jsonObject.optString(CALLBACK_ID_STR, null));
            m.setResponseData(jsonObject.optString(RESPONSE_DATA_STR, null));
            m.setResponseId(jsonObject.optString(RESPONSE_ID_STR, null));
            m.setData(jsonObject.optString(DATA_STR, null));
            return m;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return m;
    }

    static List<Message> toArrayList(String jsonStr) {
        List<Message> list = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                list.add(Message.formJson(jsonObject.toString()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}
