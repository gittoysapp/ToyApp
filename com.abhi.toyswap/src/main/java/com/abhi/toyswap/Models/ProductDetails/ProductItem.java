package com.abhi.toyswap.Models.ProductDetails;

import android.os.Parcel;
import android.os.Parcelable;

import com.abhi.toyswap.Models.Filters.Filters;

import java.util.List;

/**
 * Created by Abhishek28.Gupta on 11-12-2017.
 */

public class ProductItem implements Parcelable {

    private String productId;
    private String productUniqueKey;
    private String productName;
    private String categoryId;
    private String price;
    private String categoryName;
    private String userId;
    private String userName;
    private String userImage;
    private String description;
    private String ageGroup;
    private String itemAddress;
    private String itemUniqueKey;
    private List<ItemImage> productImages;
    private List<ItemType> itemTypes;
    private boolean isAvailableForSwap;
    private boolean isAvailableForBuy;
    private boolean isAvailableForBid;
    private boolean isFavourite;
    private boolean isFollowed;
    private boolean isAlreadyReported;
    private String postedTime;
    private int numberOfLikes;
    private int numberOfDislikes;
    private String isCustom;
    private boolean isAlreadySold;


    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Filters createFromParcel(Parcel in) {
            return new Filters(in);
        }

        public Filters[] newArray(int size) {
            return new Filters[size];
        }
    };

    public ProductItem(Parcel in) {
        this.productId = in.readString();
        this.productName = in.readString();
        this.categoryId = in.readString();
        this.price = in.readString();
        this.categoryName = in.readString();
        this.userId = in.readString();
        this.userName = in.readString();
        this.userImage = in.readString();
        this.description = in.readString();
        this.ageGroup = in.readString();
        this.itemAddress = in.readString();
        this.itemUniqueKey=in.readString();
        this.productImages = in.readArrayList(null);
        this.itemTypes = in.readArrayList(null);
        this.isAvailableForSwap = in.readByte() != 0;
        this.isAvailableForBuy = in.readByte() != 0;
        this.isAvailableForBid = in.readByte() != 0;
        this.isFavourite = in.readByte() != 0;
        this.isFollowed = in.readByte() != 0;
        this.isAlreadyReported=in.readByte() != 0;
        this.postedTime = in.readString();
        this.numberOfLikes = in.readInt();
        this.numberOfDislikes = in.readInt();
        this.isCustom=in.readString();
        this.isAlreadySold = in.readByte() != 0;

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.productId);
        dest.writeString(this.productName);
        dest.writeString(this.categoryId);
        dest.writeString(this.price);
        dest.writeString(this.categoryName);
        dest.writeString(this.userId);
        dest.writeString(this.userName);
        dest.writeString(this.userImage);
        dest.writeString(this.description);
        dest.writeString(this.ageGroup);
        dest.writeString(this.itemAddress);
        dest.writeString(this.itemUniqueKey);
        try {
            dest.writeList(this.productImages);
            dest.writeList(this.itemTypes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dest.writeByte((byte) (isAvailableForSwap ? 1 : 0));
        dest.writeByte((byte) (isAvailableForBuy ? 1 : 0));
        dest.writeByte((byte) (isAvailableForBid ? 1 : 0));
        dest.writeByte((byte) (isFavourite ? 1 : 0));
        dest.writeByte((byte) (isFollowed ? 1 : 0));
        dest.writeByte((byte) (isAlreadyReported ? 1 : 0));
        dest.writeString(this.postedTime);
        dest.writeInt(this.numberOfLikes);
        dest.writeInt(this.numberOfDislikes);
        dest.writeString(this.isCustom);
        dest.writeByte((byte) (isAlreadySold ? 1 : 0));


    }

    @Override
    public int describeContents() {
        return 0;
    }


    public ProductItem() {
    }


    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public List<ItemImage> getProductImageUrl() {
        return productImages;
    }

    public void setProductImageUrl(List<ItemImage> productImageUrl) {
        this.productImages = productImageUrl;
    }

    public boolean isAvailableForSwap() {
        return isAvailableForSwap;
    }

    public void setAvailableForSwap(boolean availableForSwap) {
        isAvailableForSwap = availableForSwap;
    }

    public boolean isAvailableForBuy() {
        return isAvailableForBuy;
    }

    public void setAvailableForBuy(boolean availableForBuy) {
        isAvailableForBuy = availableForBuy;
    }

    public boolean isAvailableForBid() {
        return isAvailableForBid;
    }

    public void setAvailableForBid(boolean availableForBid) {
        isAvailableForBid = availableForBid;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAgeGroup() {
        return ageGroup;
    }

    public void setAgeGroup(String ageGroup) {
        this.ageGroup = ageGroup;
    }

    public String getItemAddress() {
        return itemAddress;
    }

    public void setItemAddress(String itemAddress) {
        this.itemAddress = itemAddress;
    }

    public String getItemUniqueKey() {
        return itemUniqueKey;
    }

    public void setItemUniqueKey(String itemUniqueKey) {
        this.itemUniqueKey = itemUniqueKey;
    }

    public List<ItemType> getItemTypes() {
        return itemTypes;
    }

    public void setItemTypes(List<ItemType> itemTypes) {
        this.itemTypes = itemTypes;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPostedTime() {
        return postedTime;
    }

    public void setPostedTime(String postedTime) {
        this.postedTime = postedTime;
    }

    public int getNumberOfLikes() {
        return numberOfLikes;
    }

    public void setNumberOfLikes(int numberOfLikes) {
        this.numberOfLikes = numberOfLikes;
    }

    public int getNumberOfDislikes() {
        return numberOfDislikes;
    }

    public void setNumberOfDislikes(int numberOfDislikes) {
        this.numberOfDislikes = numberOfDislikes;
    }

    public String getIsCustom() {
        return isCustom;
    }

    public void setIsCustom(String isCustom) {
        this.isCustom = isCustom;
    }
    public boolean isFollowed() {
        return isFollowed;
    }

    public void setFollowed(boolean followed) {
        isFollowed = followed;
    }

    public void setIsAlreadySold(boolean isAlreadySold) {
        this.isAlreadySold = isAlreadySold;
    }

    public boolean getIsAlreadySold() {
        return this.isAlreadySold;
    }

    public void setIsAlreadyReported(boolean isAlreadyReported) {
        this.isAlreadyReported = isAlreadyReported;
    }

    public boolean getIsAlreadyReported() {
        return this.isAlreadyReported;
    }


}
