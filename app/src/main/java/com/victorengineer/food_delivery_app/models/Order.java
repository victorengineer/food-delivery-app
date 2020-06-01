package com.victorengineer.food_delivery_app.models;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Order {

    private String orderId;

    private String userId;

    private String food;

    private int quantity;

    private String address;

    private GeoPoint geo_point;

    private int estatus;

    private String imgUri;

    private float totalPrice;

    public Order() {
    }


    public Order(String orderId, String userId, String food, int quantity, String address, GeoPoint geo_point, int estatus, String imgUri, float totalPrice, Date timestamp) {
        this.orderId = orderId;
        this.userId = userId;
        this.food = food;
        this.quantity = quantity;
        this.address = address;
        this.geo_point = geo_point;
        this.estatus = estatus;
        this.imgUri = imgUri;
        this.totalPrice = totalPrice;
        this.timestamp = timestamp;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public GeoPoint getGeo_point() {
        return geo_point;
    }

    public void setGeo_point(GeoPoint geo_point) {
        this.geo_point = geo_point;
    }

    public int getEstatus() {
        return estatus;
    }

    public void setEstatus(int estatus) {
        this.estatus = estatus;
    }

    public String getFood() {
        return food;
    }

    public void setFood(String food) {
        this.food = food;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    private @ServerTimestamp Date timestamp;

    public String getImgUri() {
        return imgUri;
    }

    public void setImgUri(String imgUri) {
        this.imgUri = imgUri;
    }

    public float getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(float totalPrice) {
        this.totalPrice = totalPrice;
    }
}
