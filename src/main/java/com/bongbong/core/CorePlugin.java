package com.bongbong.core;

import com.bongbong.core.commands.BaseCommand;
import com.bongbong.core.commands.friends.FriendCommand;
import com.bongbong.core.commands.general.*;
import com.bongbong.core.commands.messaging.ChatCommand;
import com.bongbong.core.commands.messaging.IgnoreCommand;
import com.bongbong.core.commands.messaging.MessageCommand;
import com.bongbong.core.commands.messaging.UnignoreCommand;
import com.bongbong.core.commands.ranks.AddRankCommand;
import com.bongbong.core.commands.ranks.GetRanksCommand;
import com.bongbong.core.commands.ranks.RankCommand;
import com.bongbong.core.commands.ranks.RemoveRankCommand;
import com.bongbong.core.commands.staff.*;
import com.bongbong.core.commands.tags.AddTagCommand;
import com.bongbong.core.commands.tags.RemoveTagCommand;
import com.bongbong.core.commands.tags.TagCommand;
import com.bongbong.core.commands.tags.TagsCommand;
import com.bongbong.core.listeners.player.AsyncPlayerChatListener;
import com.bongbong.core.listeners.player.PlayerJoinListener;
import com.bongbong.core.listeners.player.PlayerPreLoginListener;
import com.bongbong.core.listeners.player.PlayerQuitListener;
import com.bongbong.core.listeners.world.LeavesDecayListener;
import com.bongbong.core.listeners.world.WeatherChangeListener;
import com.bongbong.core.networking.CoreRedisMessageListener;
import com.bongbong.core.networking.mongo.Mongo;
import com.bongbong.core.networking.redis.RedisPublisher;
import com.bongbong.core.networking.redis.RedisSubscriber;
import com.bongbong.core.profiles.ProfileManager;
import com.bongbong.core.ranks.RankManager;
import com.bongbong.core.server.CoreServer;
import com.bongbong.core.tags.TagManager;
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

public class CorePlugin extends JavaPlugin {
    private @Getter
    Mongo mongo;

    private @Getter
    RedisPublisher redisPublisher;
    private @Getter
    RedisSubscriber redisSubscriber;

    private CommandMap commandMap;

    public Gooey gooey;

    @Getter private ProfileManager profileManager;
    @Getter private RankManager rankManager;
    @Getter private TagManager tagManager;

    @Getter private CoreServer coreServer;

    private CoreRedisMessageListener coreRedisMessageListener;

    @Override
    public void onEnable() {

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
        this.tagManager = new TagManager(this);
        this.rankManager = new RankManager(this);

        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);

            commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (Exception e) {
            e.printStackTrace();
        }

        new LeavesDecayListener(this);
        new WeatherChangeListener(this);
        new AsyncPlayerChatListener(this);
        new PlayerJoinListener(this);
        new PlayerPreLoginListener(this);
        new PlayerQuitListener(this);

        // General
        registerCommand(new ListCommand(this, "list"));
        registerCommand(new SettingsCommand(this, "coresettings"));

        // Messaging
        registerCommand(new IgnoreCommand(this, "ignore"));
        registerCommand(new MessageCommand(this, "message"));
        registerCommand(new UnignoreCommand(this, "unignore"));

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
        registerCommand(new PluginsCommand("software"));

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

    public CorePlugin registerCommand(BaseCommand command) {
        commandMap.register(command.getName(), command);
        return this;
    }

    public CorePlugin registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
        return this;
    }

    public CorePlugin unregisterListener(Listener listener) {
        HandlerList.unregisterAll(listener);
        return this;
    }

}
