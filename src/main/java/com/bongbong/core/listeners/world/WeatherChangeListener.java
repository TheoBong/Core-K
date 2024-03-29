package com.bongbong.core.listeners.world;

import com.bongbong.core.CorePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WeatherChangeListener implements Listener {

    public WeatherChangeListener(CorePlugin plugin) {
        plugin.registerListener(this);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(event.toWeatherState());
    }
}
