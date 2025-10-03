package com.cargaia.consumer.face;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageMeta {
    @JsonProperty("source") public String source;
    @JsonProperty("version") public String version;

    public MessageMeta() {}
}
