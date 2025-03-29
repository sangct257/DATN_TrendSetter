package com.example.datn_trendsetter.DTO;

public class ResponseMessage {
    private String type;    // Loại thông báo: 'success' hoặc 'error'
    private String message; // Nội dung thông báo

    // Constructor để tạo đối tượng ResponseMessage
    public ResponseMessage(String type, String message) {
        this.type = type;
        this.message = message;
    }

    // Getter và setter cho 'type' và 'message'
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
