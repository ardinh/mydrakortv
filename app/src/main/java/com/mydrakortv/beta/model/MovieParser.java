package com.mydrakortv.beta.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mydrakortv.beta.model.RelatedMovie;

import java.io.Serializable;
import java.util.List;

public class MovieParser implements Serializable {

    @SerializedName("status_code")
    @Expose
    private String name_parser;
    @SerializedName("status_message")
    @Expose
    private String script;

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getName_parser() {
        return name_parser;
    }

    public void setName_parser(String name_parser) {
        this.name_parser = name_parser;
    }
}
