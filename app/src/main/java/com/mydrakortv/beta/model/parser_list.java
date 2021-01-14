package com.mydrakortv.beta.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class parser_list {

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
