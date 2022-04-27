package com.example.uploadmultiplefiles.model;

import java.util.List;

public class OrderSlip {
    private String userId;
    private int orderNo;
    private String orderId;
    private String trackingNo;
    private List<ProductInfo> items;
    private String remarkFromBuyer;
    private String sellerNote;

    public OrderSlip(String userId, int orderNo, String orderId, String trackingNo, List<ProductInfo> items, String remarkFromBuyer, String sellerNote)
    {
        this.userId = userId;
        this.orderNo = orderNo;
        this.orderId = orderId;
        this.trackingNo = trackingNo;
        this.items = items;
        this.remarkFromBuyer = remarkFromBuyer;
        this.sellerNote = sellerNote;
    }

    public OrderSlip() {
    }

    public String getUserId() {
        return this.userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public int getOrderNo() {
        return this.orderNo;
    }
    public void setOrderNo(int i) {
        this.orderNo = i;
    }
    public String getOrderId() {
        return this.orderId;
    }
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    public String getTrackingNo() {
        return this.trackingNo;
    }
    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
    }
    public List<ProductInfo> getItems() {
        return this.items;
    }
    public void setItems(List<ProductInfo> productInfo) {
        this.items = productInfo;
    }
    public String getRemarkFromBuyer() {
        return this.remarkFromBuyer;
    }
    public void setRemarkFromBuyer(String remarkFromBuyer) {
        this.remarkFromBuyer = remarkFromBuyer;
    }
    public String getSellerNote() {
        return this.sellerNote;
    }
    public void setSellerNote(String sellerNote) {
        this.sellerNote = sellerNote;
    }

    public String toString() {
        return "[orderNo=" + this.orderNo + ", orderId=" + this.orderId + ", trackingNo=" + this.trackingNo +
                ", remarkFromBuyer=" + this.remarkFromBuyer + ", sellerNote=" + this.sellerNote +
                "]";
    }
}
