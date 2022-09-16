package com.bongbong.core.ranks;

import com.bongbong.core.CorePlugin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import lombok.Data;

import java.util.*;

public @Data class Rank {

    private final CorePlugin plugin;
    private final @Expose UUID uuid;
    private @Expose String name, displayName, prefix, color;
    private @Expose int weight;
    private @Expose boolean defaultRank, nameMc;
    private @Expose List<UUID> parents;
    private @Expose Map<String, Map<String, Boolean>> permissions;
    public Rank(CorePlugin plugin, UUID uuid) {
        this.plugin = plugin;
        this.uuid = uuid;
        this.parents = new ArrayList<>();
        this.permissions = new HashMap<>();
        this.color = "&a";
    }

    public Map<String, Boolean> getSpecificPermissions(String server) {
        return permissions.get(server);
    }

    public Map<String, Boolean> getPermissions(String server) {
        Map<String, Boolean> global = permissions.get(".global");
        Map<String, Boolean> serv = permissions.get(server);

        if(global == null) {
            permissions.put(".global", new HashMap<>());
            global = permissions.get(".global");
        }

        Map<String, Boolean> perms = new HashMap<>(global);

        if(server != null) {
            if (serv == null) {
                permissions.put(server, new HashMap<>());
                serv = permissions.get(server);
            }
            perms.putAll(serv);
        }
        return perms;
    }

    public Map<String, Boolean> getParentPermissions(Rank rank, String server) {
        Map<String, Boolean> perms = new HashMap<>();
        for(UUID uuid : rank.getParents()) {
            Rank pRank = plugin.getRankManager().getRank(uuid);
            perms.putAll(pRank.getPermissions(server));
            if(!pRank.getParents().isEmpty()) {
                perms.putAll(getParentPermissions(pRank, server));
            }
        }

        return perms;
    }

    public Map<String, Boolean> getAllPermissions(String server) {
        Map<String, Boolean> perms = new HashMap<>(getPermissions(server));
        perms.putAll(getParentPermissions(this, server));
        return perms;
    }

    public String serialize() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }
}
