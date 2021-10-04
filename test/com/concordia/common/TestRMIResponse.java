package com.concordia.common;

import com.roomreservation.common.RMIResponse;
import com.roomreservation.protobuf.protos.ResponseObject;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class TestRMIResponse {

    @Test
    public void testFromResponseObject() throws ParseException {
        ResponseObject.Builder responseObject = ResponseObject.newBuilder();
        responseObject.setMessage("test");
        responseObject.setDateTime("2021/01/01 01:01:01");
        responseObject.setRequestType("test");
        responseObject.setRequestParameters("test");
        responseObject.setStatus(true);
        RMIResponse rmiResponse = new RMIResponse().fromResponseObject(responseObject.build());
        assertEquals(responseObject.getMessage(), rmiResponse.getMessage());
        assertEquals(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(responseObject.getDateTime()), rmiResponse.getDatetime());
        assertEquals(responseObject.getRequestType(), rmiResponse.getRequestType());
        assertEquals(responseObject.getRequestParameters(), rmiResponse.getRequestParameters());
        assertEquals(responseObject.getStatus(), rmiResponse.getStatus());
    }

    @Test
    public void testToResponseObject(){
        Date date = new Date();
        RMIResponse rmiResponse = new RMIResponse();
        rmiResponse.setMessage("test");
        rmiResponse.setDatetime(date);
        rmiResponse.setRequestType("test");
        rmiResponse.setRequestParameters("test");
        rmiResponse.setStatus(true);

        ResponseObject responseObject = new RMIResponse().toResponseObject(rmiResponse);
        assertEquals(rmiResponse.getMessage(), responseObject.getMessage());
        assertEquals(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(rmiResponse.getDatetime()), responseObject.getDateTime());
        assertEquals(rmiResponse.getRequestType(), responseObject.getRequestType());
        assertEquals(rmiResponse.getRequestParameters(), responseObject.getRequestParameters());
        assertEquals(rmiResponse.getStatus(), responseObject.getStatus());
    }

    @Test
    public void testToString(){
        Date date = new Date();
        RMIResponse rmiResponse = new RMIResponse();
        rmiResponse.setMessage("test");
        rmiResponse.setDatetime(date);
        rmiResponse.setRequestType("test");
        rmiResponse.setRequestParameters("test");
        rmiResponse.setStatus(true);
        assertEquals(date.toString() + ",test,test,test,true" ,rmiResponse.toString());
    }
}
