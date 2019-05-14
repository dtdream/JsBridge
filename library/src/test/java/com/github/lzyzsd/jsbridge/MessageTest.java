package com.github.lzyzsd.jsbridge;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
    public void noResponseData_formJson() {
        String sample = "{\"data\":\"empty\",\"callbackId\":\"callbackId11\",\"handlerName\":\"testToJson\",\"responseId\":\"responseIdAA\"}";
        Message m = Message.formJson(sample);
        assertEquals("empty", m.getData());
        assertEquals("callbackId11", m.getCallbackId());
        assertEquals("testToJson", m.getHandlerName());
        assertEquals("responseIdAA", m.getResponseId());
        assertNull(m.getResponseData());
    }

    @Test
    public void toArrayList() {
        String sample = "[{\"data\":\"empty\",\"callbackId\":\"callbackId11\",\"handlerName\":\"testToJson\",\"responseId\":\"responseIdAA\"},{\"data\":\"empty2\",\"callbackId\":\"callbackId22\",\"handlerName\":\"testToJson\",\"responseId\":\"responseIdBB\"}]";
        List<Message> result = Message.toArrayList(sample);
        assertEquals(2, result.size());
        assertEquals("callbackId11", result.get(0).getCallbackId());
        assertEquals("empty", result.get(0).getData());
        assertEquals("responseIdAA", result.get(0).getResponseId());
        assertEquals("callbackId22", result.get(1).getCallbackId());
        assertEquals("empty2", result.get(1).getData());
        assertEquals("responseIdBB", result.get(1).getResponseId());
    }
}