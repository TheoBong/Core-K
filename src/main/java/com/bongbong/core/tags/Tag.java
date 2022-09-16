package com.bongbong.core.tags;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import lombok.Data;

import java.util.UUID;

public @Data class Tag {

    private final @Expose UUID uuid;
    private @Expose String name, displayName, tag, color, description;
    private @Expose boolean visible;

    public Tag(UUID uuid) {
        this.uuid = uuid;
        this.color = "&a";
        this.visible = true;
    }

    public String serialize() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }
}
