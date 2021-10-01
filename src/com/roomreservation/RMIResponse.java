package com.roomreservation;

import com.roomreservation.protobuf.protos.RequestObject;
import com.roomreservation.protobuf.protos.RequestObjectActions;
import com.roomreservation.protobuf.protos.ResponseObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RMIResponse {
    private String message;
    private Date datetime;
    private RequestObjectActions requestType;
    private String requestParameters;
    private boolean status;

    public RMIResponse(){}

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public RequestObjectActions getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestObjectActions requestType) {
        this.requestType = requestType;
    }

    public String getRequestParameters() {
        return requestParameters;
    }

    public void setRequestParameters(String requestParameters) {
        this.requestParameters = requestParameters;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public RMIResponse fromResponseObject(ResponseObject responseObject) throws ParseException {
        DateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        RMIResponse rmiResponse = new RMIResponse();
        rmiResponse.setMessage(responseObject.getMessage());
        rmiResponse.setDatetime(dateTimeFormat.parse(responseObject.getDateTime()));
        rmiResponse.setRequestType(RequestObjectActions.valueOf(responseObject.getRequestType()));
        rmiResponse.setRequestParameters(responseObject.getRequestParameters());
        rmiResponse.setStatus(responseObject.getStatus());
        return rmiResponse;
    }

    public ResponseObject toResponseObject(RMIResponse rmiResponse){
        DateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        ResponseObject.Builder responseObject = ResponseObject.newBuilder();
        responseObject.setMessage(rmiResponse.message);
        responseObject.setDateTime(dateTimeFormat.format(rmiResponse.getDatetime()));
        responseObject.setRequestType(rmiResponse.getRequestType().toString());
        responseObject.setRequestParameters(rmiResponse.getRequestParameters());
        responseObject.setStatus(rmiResponse.status);
        return responseObject.build();
    }
}
