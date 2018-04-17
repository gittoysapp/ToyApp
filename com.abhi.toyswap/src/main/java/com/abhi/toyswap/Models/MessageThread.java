package com.abhi.toyswap.Models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Abhishek28.Gupta on 12-01-2018.
 */

public class MessageThread  implements Parcelable {

    private String itemId;
    private String itemName;
    private String userName;
    private String userId;
    private String itemImageUrl;
    private String userImageUr;

    private String lastMessage;


    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public MessageThread createFromParcel(Parcel in) {
            return new MessageThread(in);
        }

        public MessageThread[] newArray(int size) {
            return new MessageThread[size];
        }
    };

    public MessageThread(Parcel in) {

        this.itemId=in.readString();
        this.itemName=in.readString();
        this.userName=in.readString();
        this.userId=in.readString();
        this.itemImageUrl=in.readString();
        this.userImageUr=in.readString();

        this.lastMessage=in.readString();

    }

    public MessageThread() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.itemId);
        dest.writeString(this.itemName);
        dest.writeString(this.userName);
        dest.writeString(this.userId);

        dest.writeString(this.itemImageUrl);
        dest.writeString(this.userImageUr);
        dest.writeString(this.lastMessage);
    }


    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getItemImageUrl() {
        return itemImageUrl;
    }

    public void setItemImageUrl(String itemImageUrl) {
        this.itemImageUrl = itemImageUrl;
    }

    public String getUserImageUr() {
        return userImageUr;
    }

    public void setUserImageUr(String userImageUr) {
        this.userImageUr = userImageUr;
    }


    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

}
