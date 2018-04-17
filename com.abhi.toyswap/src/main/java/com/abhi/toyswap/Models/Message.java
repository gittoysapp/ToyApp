package com.abhi.toyswap.Models;

/**
 * Created by Abhishek28.Gupta on 12-01-2018.
 */

public class Message {

    private String Message;
    private String MessageFrom;
    private String MessageDateTime;
    private String MessageFromUserId;
    private String MessageToUserId;


    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getMessageFrom() {
        return MessageFrom;
    }

    public void setMessageFrom(String messageFrom) {
        MessageFrom = messageFrom;
    }

    public String getMessageDateTime() {
        return MessageDateTime;
    }

    public void setMessageDateTime(String messageDateTime) {
        MessageDateTime = messageDateTime;
    }

    public String getMessageFromUserId() {
        return MessageFromUserId;
    }

    public void setMessageFromUserId(String messageFromUserId) {
        MessageFromUserId = messageFromUserId;
    }

    public String getMessageToUserId() {
        return MessageToUserId;
    }

    public void setMessageToUserId(String messageToUserId) {
        MessageToUserId = messageToUserId;
    }
}
