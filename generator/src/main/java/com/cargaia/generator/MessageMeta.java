package com.cargaia.generator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageMeta {
    @JsonProperty("source") public String source;
    @JsonProperty("notes")  public String notes;

    public MessageMeta() {}
    public MessageMeta(String source, String notes){ this.source=source; this.notes=notes; }
}