package com.bongbong.core.punishments;

import com.bongbong.core.CorePlugin;
import com.bongbong.core.networking.CoreRedisAction;
import com.bongbong.core.networking.redis.RedisMessage;
import com.bongbong.core.utils.Colors;
import com.bongbong.core.utils.ThreadUtil;
import com.bongbong.core.utils.Webhook;
import com.bongbong.core.web.WebPlayer;
import com.google.gson.JsonObject;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

public @Data class Punishment {

    public enum Type {
        BAN, BLACKLIST, KICK, MUTE;

        public String pastMessage() {
            switch(this) {
                case BAN:
                    return "banned";
                case BLACKLIST:
                    return "blacklisted";
                case KICK:
                    return "kicked";
                case MUTE:
                    return "muted";
                default:
                    return null;
            }
        }
    }

    private final CorePlugin plugin;
    private final UUID uuid;
    private UUID victim, issuer, pardoner;
    private String issueReason, pardonReason;
    private Date issued, expires, pardoned;
    private Type type;
    private boolean silentIssue, silentPardon;

    public String expiry() {
        if (expires == null) {
            return "Never";
        } else {
            return expires.toString();
        }
    }

    public boolean isActive() {
        boolean b = !type.equals(Type.KICK);

        if(expires != null) {
            if(expires.before(new Date())) {
                b = false;
            }
        }

        if(pardoned != null) {
            b = false;
        }

        return b;
    }

    @SuppressWarnings("unchecked")
    public void execute() {
        Player player = Bukkit.getPlayer(victim);

        String victimName, issuerName;

        if(isActive() || type.equals(Type.KICK)) {
            if (issuer != null) {
                Player p = Bukkit.getPlayer(issuer);
                issuerName = p.getName();
            } else {
                issuerName = "Console";
            }
        } else {
            if (pardoner != null) {
                Player p = Bukkit.getPlayer(pardoner);
                issuerName = p.getName();
            } else {
                issuerName = "Console";
            }
        }

        if(player != null && player.isOnline()) {
            victimName = player.getName();
            if(isActive() || type.equals(Type.KICK)) {
                switch (type) {
                    case BAN:
                        player.kickPlayer(Colors.get("&cYour account has been banned (Expiry: " + expiry() + ").\n&fReason: " + issueReason));
                        break;
                    case BLACKLIST:
                        player.kickPlayer(Colors.get("&4Your account has been blacklisted.\n&fReason: " + issueReason));
                        break;
                    case MUTE:
                        player.sendMessage(Colors.get("&cYou have been muted for: " + ChatColor.WHITE + this.issueReason + ".\n&cExpiry: " + expiry()));
                        break;
                    case KICK:
                        player.kickPlayer(ChatColor.RED + "You have been kicked for: " + ChatColor.WHITE + this.issueReason);
                        break;
                }
            }
        } else {
            WebPlayer wp = new WebPlayer(victim);
            victimName = wp.getName();
        }

        StringBuilder sb = new StringBuilder();
        if(isActive() || type.equals(Type.KICK)) {
            sb.append("&f&l * &c&l" + StringUtils.capitalize(type.toString().toLowerCase()) + (silentIssue ? " &7[Silent]" : ""));
            sb.append("\n&cVictim: &f" + victimName);
            sb.append("\n&cIssuer: &f" + issuerName);
            sb.append("\n&cReason: &f" + this.issueReason);
            if(!type.equals(Type.KICK)) {
                sb.append("\n&cExpires: &f" + (this.expires == null ? "Never" : this.expires.toString()));
            }

            ThreadUtil.runTask(true, plugin, () -> {
                JSONArray jsonArray = new JSONArray();
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("title", "New Punishment (" + type.toString() + ")");
                jsonObject.put("description", "A new punishment has been issued!");
                jsonObject.put("color", 15258703);
                JSONArray fields = new JSONArray();

                JSONObject field1 = new JSONObject();
                field1.put("name", "Victim");
                field1.put("value", victimName);
                field1.put("inline", true);
                fields.add(field1);

                JSONObject field2 = new JSONObject();
                field2.put("name", "Staff Member");
                field2.put("value", issuerName);
                field2.put("inline", true);
                fields.add(field2);

                JSONObject field3 = new JSONObject();
                field3.put("name", "Reason");
                field3.put("value", issueReason);
                fields.add(field3);

                JSONObject field4 = new JSONObject();
                field4.put("name", "Expires");
                field4.put("value", this.expires == null ? "Never" : this.expires.toString());
                fields.add(field4);

                jsonObject.put("fields", fields);

                jsonArray.add(jsonObject);
                Webhook.sendWebhook("", jsonArray);
            });
        } else {
            sb.append("&f&l * &c&lUn" + type.toString().toLowerCase() + (silentIssue ? " &7[Silent]" : ""));
            sb.append("\n&cVictim: &f" + victimName);
            sb.append("\n&cReason: &f" + issueReason);
            sb.append("\n&cPardoner: &f" + issuerName);
            sb.append("\n&cPardon Reason: &f" + pardonReason);

            ThreadUtil.runTask(true, plugin, () -> {
                JSONArray jsonArray = new JSONArray();
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("title", "New Punishment (UN" + type.toString() + ")");
                jsonObject.put("description", "A punishment has been removed!");
                jsonObject.put("color", 15258703);
                JSONArray fields = new JSONArray();

                JSONObject field1 = new JSONObject();
                field1.put("name", "Victim");
                field1.put("value", victimName);
                field1.put("inline", true);
                fields.add(field1);

                JSONObject field2 = new JSONObject();
                field2.put("name", "Original Reason");
                field2.put("value", issueReason);
                fields.add(field2);

                JSONObject field3 = new JSONObject();
                field3.put("name", "Pardoner");
                field3.put("value", issuerName);
                field3.put("inline", true);
                fields.add(field3);

                JSONObject field4 = new JSONObject();
                field4.put("name", "Pardon Reason");
                field4.put("value", pardonReason);
                fields.add(field4);

                jsonObject.put("fields", fields);

                jsonArray.add(jsonObject);
                Webhook.sendWebhook("", jsonArray);
            });
        }

//        JsonObject json = new JsonObject();
//        json.addProperty("action", CoreRedisAction.STAFF_BROADCAST.toString());
//        json.addProperty("message", sb.toString());
//        RedisMessage staffMessage = new RedisMessage("core", json);

        String message;
        if(type.equals(Type.KICK)) {
            message = "&f" + victimName + "&a was " + type.pastMessage() + " by " + issuerName + "&a.";
        } else {
            message = "&f" + victimName + "&a was " + (this.isActive() ? (this.expires == null ? "permanently " : "temporarily ") : "un") + type.pastMessage() + " by " + issuerName + "&a.";
        }
        JsonObject j = new JsonObject();
        RedisMessage rm = new RedisMessage("core", j);
        if(silentIssue) {
            j.addProperty("action", CoreRedisAction.PUNISHMENT_SILENT.toString());
            j.addProperty("message", "&7[Silent] " + message);
            j.addProperty("hover", sb.toString());
        } else {
            j.addProperty("action", CoreRedisAction.PUNISHMENT.toString());
            j.addProperty("message", message);
            j.addProperty("hover", sb.toString());
        }


        Queue<RedisMessage> queue = plugin.getRedisPublisher().getMessageQueue();
        queue.add(rm);
    }

    public void importFromDocument(Document d) {
        setVictim(d.get("victim", UUID.class));
        setIssuer(d.get("issuer", UUID.class));
        setPardoner(d.get("pardoner", UUID.class));

        setIssueReason(d.getString("issue_reason"));
        setPardonReason(d.getString("pardon_reason"));
        setIssued(d.getDate("issued"));
        setExpires(d.getDate("expires"));
        setPardoned(d.getDate("pardoned"));
        setType(Type.valueOf(d.getString("type")));
        setSilentIssue(d.getBoolean("silent_issue"));
        setSilentPardon(d.getBoolean("silent_pardon"));
    }

    public Map<String, Object> export() {
        Map<String, Object> map = new HashMap<>();
        map.put("victim", victim);
        map.put("issuer", issuer);
        map.put("pardoner", pardoner);

        map.put("issue_reason", issueReason);
        map.put("pardon_reason", pardonReason);
        map.put("issued", issued);
        map.put("expires", expires);
        map.put("pardoned", pardoned);
        map.put("type", type.toString());
        map.put("silent_issue", silentIssue);
        map.put("silent_pardon", silentPardon);
        return map;
    }
}