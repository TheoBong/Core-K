package cc.kitpvp.core;

import cc.kitpvp.core.commands.BaseCommand;
import cc.kitpvp.core.commands.friends.FriendCommand;
import cc.kitpvp.core.commands.general.*;
import cc.kitpvp.core.commands.messaging.ChatCommand;
import cc.kitpvp.core.commands.messaging.IgnoreCommand;
import cc.kitpvp.core.commands.messaging.MessageCommand;
import cc.kitpvp.core.commands.messaging.UnignoreCommand;
import cc.kitpvp.core.commands.moderation.CheckPunishmentsCommand;
import cc.kitpvp.core.commands.moderation.PunishCommand;
import cc.kitpvp.core.commands.moderation.TempPunishCommand;
import cc.kitpvp.core.commands.moderation.UnpunishCommand;
import cc.kitpvp.core.commands.ranks.AddRankCommand;
import cc.kitpvp.core.commands.ranks.GetRanksCommand;
import cc.kitpvp.core.commands.ranks.RankCommand;
import cc.kitpvp.core.commands.ranks.RemoveRankCommand;
import cc.kitpvp.core.commands.staff.*;
import cc.kitpvp.core.commands.tags.AddTagCommand;
import cc.kitpvp.core.commands.tags.RemoveTagCommand;
import cc.kitpvp.core.commands.tags.TagCommand;
import cc.kitpvp.core.commands.tags.TagsCommand;
import cc.kitpvp.core.listeners.player.AsyncPlayerChatListener;
import cc.kitpvp.core.listeners.player.PlayerJoinListener;
import cc.kitpvp.core.listeners.player.PlayerPreLoginListener;
import cc.kitpvp.core.listeners.player.PlayerQuitListener;
import cc.kitpvp.core.networking.CoreRedisMessageListener;
import cc.kitpvp.core.networking.mongo.Mongo;
import cc.kitpvp.core.networking.redis.RedisPublisher;
import cc.kitpvp.core.networking.redis.RedisSubscriber;
import cc.kitpvp.core.papi.CoreExpansion;
import cc.kitpvp.core.profiles.ProfileManager;
import cc.kitpvp.core.punishments.PunishmentManager;
import cc.kitpvp.core.ranks.RankManager;
import cc.kitpvp.core.server.CoreServer;
import cc.kitpvp.core.server.filter.Filter;
import cc.kitpvp.core.tags.TagManager;
import cc.kitpvp.core.commands.general.ListCommand;
import cc.kitpvp.core.commands.general.ReportCommand;
import cc.kitpvp.core.commands.general.SettingsCommand;
import cc.kitpvp.core.commands.general.SocialsCommand;
import cc.kitpvp.core.commands.staff.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import xyz.leuo.gooey.Gooey;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.lang.reflect.Field;

public class Core extends JavaPlugin {
    public static Core INSTANCE;

    private @Getter
    Mongo mongo;

    private @Getter
    RedisPublisher redisPublisher;
    private @Getter
    RedisSubscriber redisSubscriber;

    private CommandMap commandMap;

    public Gooey gooey;

    @Getter private ProfileManager profileManager;
    @Getter private PunishmentManager punishmentManager;
    @Getter private RankManager rankManager;
    @Getter private TagManager tagManager;

    @Getter private Filter filter;

    @Getter private CoreServer coreServer;

    private CoreRedisMessageListener coreRedisMessageListener;

    @Override
    public void onEnable() {
        INSTANCE = this;

        this.saveDefaultConfig();

        this.coreServer = new CoreServer(getConfig());

        // Mongo
        if (getConfig().getBoolean("networking.mongo.enabled")) {
            this.mongo = new Mongo(this);
        }

        // Redis
        if (getConfig().getBoolean("networking.redis.enabled")) {
            redisPublisher = new RedisPublisher(new Jedis(getConfig().getString("networking.redis.host"), getConfig().getInt("networking.redis.port")), this);
            redisSubscriber = new RedisSubscriber(new Jedis(getConfig().getString("networking.redis.host"), getConfig().getInt("networking.redis.port")), this);
        }

        this.gooey = new Gooey(this);

        this.profileManager = new ProfileManager(this);
        this.punishmentManager = new PunishmentManager(this);
        this.tagManager = new TagManager(this);
        this.rankManager = new RankManager(this);

        this.filter = new Filter();

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new CoreExpansion(this).register();
        }

        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);

            commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (Exception e) {
            e.printStackTrace();
        }

        new AsyncPlayerChatListener(this);
        new PlayerJoinListener(this);
        new PlayerPreLoginListener(this);
        new PlayerQuitListener(this);

        // General
        registerCommand(new ListCommand(this, "list"));
        registerCommand(new ReportCommand(this, "report"));
        registerCommand(new SettingsCommand(this, "coresettings"));

        // Messaging
        registerCommand(new IgnoreCommand(this, "ignore"));
        registerCommand(new MessageCommand(this, "message"));
        registerCommand(new UnignoreCommand(this, "unignore"));

        // Moderation
        registerCommand(new CheckPunishmentsCommand(this, "checkpunishments"));
        registerCommand(new PunishCommand(this, "punish"));
        registerCommand(new TempPunishCommand(this, "temppunish"));
        registerCommand(new UnpunishCommand(this, "unpunish"));

        // Ranks
        registerCommand(new AddRankCommand(this, "addrank"));
        registerCommand(new GetRanksCommand(this, "getranks"));
        registerCommand(new RankCommand(this, "rank"));
        registerCommand(new RemoveRankCommand(this, "removerank"));

        // Staff
        registerCommand(new BroadcastCommand(this, "broadcast"));
        registerCommand(new FeedCommand(this, "feed"));
        registerCommand(new GamemodeCommand(this, "gamemode"));
        registerCommand(new HealCommand(this, "heal"));
        registerCommand(new MoreCommand(this, "more"));
        registerCommand(new SudoCommand(this, "sudo"));
        registerCommand(new ChatCommand(this, "chat"));
        registerCommand(new WhitelistCommand(this, "whitelist"));

        // Tags
        registerCommand(new AddTagCommand(this, "addtag"));
        registerCommand(new RemoveTagCommand(this, "removetag"));
        registerCommand(new TagCommand(this, "tag"));
        registerCommand(new TagsCommand(this, "tags"));

        //Random
        registerCommand(new SocialsCommand("socials"));
        registerCommand(new FriendCommand(this, "friend"));

        this.coreRedisMessageListener = new CoreRedisMessageListener(this);

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {

                    public java.security.cert.X509Certificate[] getAcceptedIssuers()
                    {
                        return null;
                    }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
                    {
                        //No need to implement.
                    }
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
                    {
                        //No need to implement.
                    }
                }
        };

        try
        {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        profileManager.shutdown();

        coreRedisMessageListener.close();

        coreServer.export(getConfig());

        this.saveConfig();
    }

    public Core registerCommand(BaseCommand command) {
        commandMap.register(command.getName(), command);
        return this;
    }

    public Core registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
        return this;
    }

    public Core unregisterListener(Listener listener) {
        HandlerList.unregisterAll(listener);
        return this;
    }

    public static Core get() {
        return INSTANCE;
    }

}
