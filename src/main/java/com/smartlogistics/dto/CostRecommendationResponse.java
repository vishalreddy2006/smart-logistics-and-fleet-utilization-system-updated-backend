package com.smartlogistics.dto;

public class CostRecommendationResponse {

    private final String type;
    private final String title;
    private final String message;

    public CostRecommendationResponse(String type, String title, String message) {
        this.type = type;
        this.title = title;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }
}
