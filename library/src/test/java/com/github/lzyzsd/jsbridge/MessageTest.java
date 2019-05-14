package com.github.lzyzsd.jsbridge;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class MessageTest {

    @Test
    public void empty_toJson() {
        Message m = new Message();
        assertEquals("{}", m.toJson());
    }

    @Test
    public void noResponseData_toJson() {
        Message m = new Message();
        m.setData("empty");
        m.setCallbackId("callbackId11");
        m.setHandlerName("testToJson");
        m.setResponseId("responseIdAA");
        String excepted = "{\"data\":\"empty\",\"callbackId\":\"callbackId11\",\"handlerName\":\"testToJson\",\"responseId\":\"responseIdAA\"}";
        assertEquals(excepted, m.toJson());
    }

    @Test
    public void hasSimpleResponseData_toJson() {
        Message m = new Message();
        m.setResponseData("simple Str");
        String excepted = "{\"responseData\":\"simple Str\"}";
        assertEquals(excepted, m.toJson());
    }

    @Test
    public void toObject() {
        // TODO
    }

    @Test
    public void toArrayList() {
        // TODO
    }
}