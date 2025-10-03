package com.cargaia.consumer.face;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
    @JsonProperty("id") public String id;
    @JsonProperty("type") public String type;
    @JsonProperty("image_bytes") public String imageBytes; // Base64 encoded image
    @JsonProperty("timestamp") public String timestamp;
    @JsonProperty("meta") public MessageMeta meta;

    public Message() {}
}
