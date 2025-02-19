package com.example.datn_trendsetter.DTO;

import lombok.Data;

@Data
public class ShippingCostResponse {
    private Integer total;
    private Integer serviceFee;
    private Integer insuranceFee;
}