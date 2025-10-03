package com.cargaia.consumer.team;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageMeta {
    @JsonProperty("source") public String source;
    @JsonProperty("version") public String version;

    public MessageMeta() {}
}
