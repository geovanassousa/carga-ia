package com.cargaia.generator;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
    @JsonProperty("id") private String id;
    @JsonProperty("type") private String type;
    @JsonProperty("image_url") private String imageUrl;
    @JsonProperty("timestamp") private String timestamp;
    @JsonProperty("meta") private MessageMeta meta;

    public Message() {}
    public Message(String id, String type, String imageUrl, String timestamp, MessageMeta meta) {
        this.id = id; this.type = type; this.imageUrl = imageUrl; this.timestamp = timestamp; this.meta = meta;
    }
    public String getId() { return id; }
    public String getType() { return type; }
    public String getImageUrl() { return imageUrl; }
    public String getTimestamp() { return timestamp; }
    public MessageMeta getMeta() { return meta; }
    public void setId(String id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public void setMeta(MessageMeta meta) { this.meta = meta; }
}