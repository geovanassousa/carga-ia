package com.cargaia.generator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
    @JsonProperty("id") public String id;
    @JsonProperty("type") public String type;
    @JsonProperty("image_url") public String imageUrl;
    @JsonProperty("timestamp") public String timestamp;
    @JsonProperty("meta") public MessageMeta meta;

    public Message() {}
    public Message(String id, String type, String imageUrl, String timestamp, MessageMeta meta) {
        this.id=id; this.type=type; this.imageUrl=imageUrl; this.timestamp=timestamp; this.meta=meta;
    }
}