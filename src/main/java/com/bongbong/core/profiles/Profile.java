package com.bongbong.core.profiles;

import com.bongbong.core.CorePlugin;
import com.bongbong.core.ranks.Rank;
import com.bongbong.core.tags.Tag;
import com.bongbong.core.tags.TagManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.*;

public @Data class Profile {

    public enum Cooldown {
        CHAT, REPORT;
    }

    private final CorePlugin plugin;
    private final UUID uuid;
    private String name;
    private String currentIp;
    private boolean nameMc;
    private Settings settings;
    private UUID appliedTag;
    private UUID lastRecipient;
    private List<String> ipHistory;
    private HashMap<String, String> friends;
    private HashMap<String, String> pendingFriends;
    private List<UUID> ignored, transactionIds, ranks, tags;
    private Map<Cooldown, Date> cooldowns;

    public Profile(CorePlugin plugin, UUID uuid) {
        this.plugin = plugin;
        this.name = "null";
        this.uuid = uuid;
        this.settings = new Settings();
        this.ipHistory = new ArrayList<>();
        this.ignored = new ArrayList<>();
        this.friends = new HashMap<>();
        this.pendingFriends = new HashMap<>();
        this.transactionIds = new ArrayList<>();
        this.ranks = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.cooldowns = new HashMap<>();
    }

    public Profile(CorePlugin plugin, Player player) {
        this(plugin, player.getUniqueId());
        this.name = player.getName();
        this.currentIp = player.getAddress().getAddress().getHostAddress();
    }

    public Tag getAppliedTag() {
        if(appliedTag != null) {
            return plugin.getTagManager().getTag(appliedTag);
        }
        return null;
    }

    public List<Tag> getAllTags() {
        List<Tag> list = new ArrayList<>();
        TagManager tagManager = plugin.getTagManager();
        for(UUID uuid : tags) {
            Tag tag = tagManager.getTag(uuid);
            if(tag != null) {
                list.add(tag);
            }
        }

        return list;
    }

    public Rank getHighestRank() {
        Rank rank = null;
        for(Rank r : getAllRanks()) {
            if(rank != null) {
                if(r.getWeight() > rank.getWeight()) {
                    rank = r;
                }
            } else {
                rank = r;
            }
        }
        return rank;
    }

    public List<Rank> getAllRanks() {
        List<Rank> ranks = new ArrayList<>();
        Map<UUID, Rank> allRanks = plugin.getRankManager().getRanks();
        for(Rank rank : allRanks.values()) {
            if(getRanks().contains(rank.getUuid()) || (rank.isNameMc() && nameMc)) {
                ranks.add(rank);
            }
        }

        return ranks;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public String serialize() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }

    public void addIp(String ip) {
        this.currentIp = ip;
        if(!ipHistory.contains(ip)) {
            ipHistory.add(ip);
        }
    }

    public void addRank(UUID uuid) {
        ranks.add(uuid);
        update();
    }

    public void removeRank(UUID uuid) {
        ranks.remove(uuid);
        update();
    }

    public void update() {
        ranks.removeIf(uuid -> plugin.getRankManager().getRank(uuid) == null);
        tags.removeIf(uuid -> plugin.getTagManager().getTag(uuid) == null);

        Player player = getPlayer();
        if(player != null) {
            PermissionAttachment permissionAttachment = player.addAttachment(plugin);

            //Take away every permission from player
            for(PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
                permissionAttachment.setPermission(permission.getPermission(), false);
            }

            //Add back only the permissions in their rank
            for(Rank rank : getAllRanks()) {
                for(Map.Entry<String, Boolean> entry : rank.getAllPermissions(plugin.getConfig().getString("general.server_category")).entrySet()) {
                    permissionAttachment.setPermission(entry.getKey(), entry.getValue());
                }
            }
        }

        if(getAllRanks().isEmpty()) {
            Rank rank = plugin.getRankManager().getDefaultRank();
            if(rank != null) {
                getRanks().add(rank.getUuid());
            }
        }
    }

    public void importFromDocument(Document d) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        setName(d.getString("name"));
        setCurrentIp(d.getString("current_ip"));
        setNameMc(d.getBoolean("name_mc"));
        setSettings(gson.fromJson(d.getString("settings"), Settings.class));

        setFriends(gson.fromJson(d.getString("friends"), HashMap.class));
        setPendingFriends(gson.fromJson(d.getString("pendingFriends"), HashMap.class));

        String tag = d.getString("applied_tag");
        if(tag != null) {
            setAppliedTag(UUID.fromString(tag));
        }

        setIpHistory(d.getList("ip_history", String.class));
        setIgnored(d.getList("ignored", UUID.class));
        setTransactionIds(d.getList("transaction_ids", UUID.class));
        setRanks(d.getList("ranks", UUID.class));
        setTags(d.getList("tags", UUID.class));
    }

    public Map<String, Object> export() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("current_ip", currentIp);
        map.put("name_mc", nameMc);
        map.put("settings", gson.toJson(settings));
        map.put("friends", gson.toJson(friends));
        map.put("pendingFriends", gson.toJson(pendingFriends));

        if(appliedTag != null) {
            map.put("tag", appliedTag.toString());
        }

        map.put("ip_history", ipHistory);
        map.put("ignored", ignored);
        map.put("transaction_ids", transactionIds);
        map.put("ranks", ranks);
        map.put("tags", tags);
        return map;
    }
}
