package cc.kitpvp.core.profiles;

import com.google.gson.annotations.Expose;
import lombok.Data;

public @Data class Settings {
    private @Expose boolean globalChat = true, privateMessages = true, staffChat = false, staffMessages = true;
}
