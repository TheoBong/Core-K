package cc.kitpvp.core.profiles;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.punishments.Punishment;
import cc.kitpvp.core.ranks.Rank;
import cc.kitpvp.core.tags.Tag;
import cc.kitpvp.core.tags.TagManager;
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
    private List<UUID> ignored, transactionIds, ranks, punishments, tags;
    private Map<Cooldown, Date> cooldowns;

    public Profile(UUID uuid) {
        this.name = "null";
        this.uuid = uuid;
        this.settings = new Settings();
        this.ipHistory = new ArrayList<>();
        this.ignored = new ArrayList<>();
        this.friends = new HashMap<>();
        this.pendingFriends = new HashMap<>();
        this.transactionIds = new ArrayList<>();
        this.ranks = new ArrayList<>();
        this.punishments = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.cooldowns = new HashMap<>();
    }

    public Profile(Player player) {
        this(player.getUniqueId());
        this.name = player.getName();
        this.currentIp = player.getAddress().getAddress().getHostAddress();
    }

    public Tag getAppliedTag() {
        if(appliedTag != null) {
            return Core.INSTANCE.getTagManager().getTag(appliedTag);
        }
        return null;
    }

    public List<Tag> getAllTags() {
        List<Tag> list = new ArrayList<>();
        TagManager tagManager = Core.INSTANCE.getTagManager();
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
        Map<UUID, Rank> allRanks = Core.INSTANCE.getRankManager().getRanks();
        for(Rank rank : allRanks.values()) {
            if(getRanks().contains(rank.getUuid()) || (rank.isNameMc() && nameMc)) {
                ranks.add(rank);
            }
        }

        return ranks;
    }

    public Punishment getActivePunishment(Punishment.Type type) {
        for(Punishment punishment : getPunishments(type)) {
            if (punishment.isActive()) {
                return punishment;
            }
        }
        return null;
    }

    public List<Punishment> getPunishments(Punishment.Type type) {
        List<Punishment> punishments = new ArrayList<>();
        for(UUID uuid : this.punishments) {
            Punishment punishment = Core.INSTANCE.getPunishmentManager().getPunishment(uuid);
            if(punishment != null && punishment.getType().equals(type)) {
                punishments.add(punishment);
            }
        }
        return punishments;
    }

    public List<Punishment> getPunishmentsTest() {
        List<Punishment> punishments = new ArrayList<>();
        for(UUID uuid : this.punishments) {
            Punishment punishment = Core.INSTANCE.getPunishmentManager().getPunishment(uuid);
            if(punishment != null) {
                punishments.add(punishment);
            }
        }
        return punishments;
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

    public Punishment punish(Punishment.Type type, UUID issuer, String reason, Date expires, boolean silent) {
        Punishment punishment = Core.INSTANCE.getPunishmentManager().create(type, this, issuer, reason, expires, silent);
        if(punishment != null) {
            punishment.execute();
        }
        return punishment;
    }

    public void update() {
        Core core = Core.INSTANCE;
        punishments.removeIf(uuid -> core.getPunishmentManager().getPunishment(uuid) == null);
        ranks.removeIf(uuid -> core.getRankManager().getRank(uuid) == null);
        tags.removeIf(uuid -> core.getTagManager().getTag(uuid) == null);

        Player player = getPlayer();
        if(player != null) {
            PermissionAttachment permissionAttachment = player.addAttachment(core);

            //Take away every permission from player
            for(PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
                permissionAttachment.setPermission(permission.getPermission(), false);
            }

            //Add back only the permissions in their rank
            for(Rank rank : getAllRanks()) {
                for(Map.Entry<String, Boolean> entry : rank.getAllPermissions(core.getConfig().getString("general.server_category")).entrySet()) {
                    permissionAttachment.setPermission(entry.getKey(), entry.getValue());
                }
            }
        }

        if(getAllRanks().isEmpty()) {
            Rank rank = Core.INSTANCE.getRankManager().getDefaultRank();
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
        setPunishments(d.getList("punishments", UUID.class));
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
        map.put("punishments", punishments);
        return map;
    }
}
