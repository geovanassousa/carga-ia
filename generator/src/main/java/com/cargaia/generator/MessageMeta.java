package com.cargaia.generator;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageMeta {
    @JsonProperty("source") private String source;
    @JsonProperty("notes")  private String notes;

    public MessageMeta() {}
    public MessageMeta(String source, String notes) { this.source = source; this.notes = notes; }
    public String getSource() { return source; }
    public String getNotes()  { return notes;  }
    public void setSource(String source) { this.source = source; }
    public void setNotes(String notes)   { this.notes  = notes;  }
}