package cc.kitpvp.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomNoPermission {

    public static String getRandomPermission() {
        final List<String> messages = new ArrayList<>();
        messages.add(Colors.get("&cHippity hoppity, you don't have permissiony."));
        messages.add(Colors.get("&cRoses are red, violets are blue, you don't have permission."));
        messages.add(Colors.get("&cImpay says you don't have permission to do that! Impay's word is law."));
        messages.add(Colors.get("&cWe just asked your mom if you have permission to do that. She said you don't."));
        messages.add(Colors.get("&cIt seems as if Thanos snapped away your permissions to do that!"));
        messages.add(Colors.get("&cI can't seem to find your permissions, refresh my memory by clicking this: https://youtu.be/dQw4w9WgXcQ"));
        messages.add(Colors.get("&cA wise man once said that you don't have permission to do that."));

        Random rand = new Random();
        return messages.get(rand.nextInt(messages.size()));
    }
}
