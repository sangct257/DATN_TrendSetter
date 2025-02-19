package com.example.datn_trendsetter.DTO;

import lombok.Data;

@Data
public class ShippingCostRequest {
    private Integer toDistrictId;
    private String toWardCode;
    private float weight;
    private int length;
    private int width;
    private int height;
    private int insuranceValue;
}
