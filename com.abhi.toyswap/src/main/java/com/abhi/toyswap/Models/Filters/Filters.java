package com.abhi.toyswap.Models.Filters;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Abhishek28.Gupta on 13-12-2017.
 */

public class Filters implements Parcelable {
    private String category="";
    private int priceLower=0;
    private int priceUpper=99999;
    private int ageLower=0;
    private int ageUpper=15;
    private int distanceLimit=10;
    private String type="";
    private String dateLimit;
    private String searchKeyword;

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Filters createFromParcel(Parcel in) {
            return new Filters(in);
        }

        public Filters[] newArray(int size) {
            return new Filters[size];
        }
    };

    public Filters(Parcel in) {
        this.category = in.readString();
        this.priceLower = in.readInt();
        this.priceUpper = in.readInt();
        this.ageLower = in.readInt();
        this.ageUpper = in.readInt();
        this.distanceLimit = in.readInt();
        this.type = in.readString();
        this.dateLimit = in.readString();
        this.searchKeyword = in.readString();
    }

    public Filters() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.category);
        dest.writeInt(this.priceLower);
        dest.writeInt(this.priceUpper);
        dest.writeInt(this.ageLower);
        dest.writeInt(this.ageUpper);
        dest.writeInt(this.distanceLimit);
        dest.writeString(this.type);
        dest.writeString(this.dateLimit);
        dest.writeString(this.searchKeyword);
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPriceLower() {
        return priceLower;
    }

    public void setPriceLower(int priceLower) {
        this.priceLower = priceLower;
    }

    public int getPriceUpper() {
        return priceUpper;
    }

    public void setPriceUpper(int priceUpper) {
        this.priceUpper = priceUpper;
    }

    public int getAgeLower() {
        return ageLower;
    }

    public void setAgeLower(int ageLower) {
        this.ageLower = ageLower;
    }

    public int getAgeUpper() {
        return ageUpper;
    }

    public void setAgeUpper(int ageUpper) {
        this.ageUpper = ageUpper;
    }

    public int getDistanceLimit() {
        return distanceLimit;
    }

    public void setDistanceLimit(int distanceLimit) {
        this.distanceLimit = distanceLimit;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDateLimit() {
        return dateLimit;
    }

    public void setDateLimit(String dateLimit) {
        this.dateLimit = dateLimit;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }
}
