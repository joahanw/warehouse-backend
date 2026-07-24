package com.johanwork.warehouse.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramUpdate(
        @JsonProperty("update_id") Long updateId,
        @JsonProperty("message")   TelegramIncomingMessage message
) {
    public record TelegramIncomingMessage(
            @JsonProperty("chat")      Chat chat,
            @JsonProperty("from")      From from,
            @JsonProperty("text")      String text
    ) {}

    public record Chat(
            @JsonProperty("id") Long id
    ) {}

    public record From(
            @JsonProperty("first_name") String firstName,
            @JsonProperty("username")   String username
    ) {}
}