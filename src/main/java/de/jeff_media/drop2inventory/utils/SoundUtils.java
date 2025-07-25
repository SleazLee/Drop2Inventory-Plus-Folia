package de.jeff_media.drop2inventory.utils;

import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.config.Config;
import com.jeff_media.jefflib.data.Cooldown;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class SoundUtils {

    public static final long SOUND_COOLDOWN = 50L;
    @Getter private static final Cooldown cooldown = new Cooldown();

    private static final ThreadLocalRandom random = ThreadLocalRandom.current();

    private final String sound;
    private final boolean soundEnabled;
    private final boolean soundGlobal;
    private final float soundPitch;
    private final float soundVolume;
    private final float soundPitchVariant;

    private static String toSoundKey(String input) {
        if (input.equals(input.toUpperCase()) && !input.contains(".") && !input.contains(":")) {
            return input.toLowerCase().replace('_', '.');
        }
        return input;
    }

    public SoundUtils() {
        final Main main = Main.getInstance();
        final String soundName = main.getConfig().getString(Config.SOUND_EFFECT);
        sound = toSoundKey(soundName);
        soundEnabled = main.getConfig().getBoolean(Config.SOUND_ENABLED);
        soundGlobal = main.getConfig().getBoolean(Config.SOUND_GLOBAL);
        soundVolume = (float) main.getConfig().getDouble(Config.SOUND_VOLUME);
        soundPitch = (float) main.getConfig().getDouble(Config.SOUND_PITCH);
        soundPitchVariant = (float) main.getConfig().getDouble(Config.SOUND_PITCH_VARIANT,0.0);

    }

    public void playPickupSound(Player player) {
        if(!soundEnabled) {
            return;
        }
        if(cooldown.hasCooldown(player)) return;
        cooldown.setCooldown(player, SOUND_COOLDOWN, TimeUnit.MILLISECONDS);
        final float pitchVariant = soundPitchVariant == 0 ? 0 : (float) (random.nextDouble(soundPitchVariant) - (soundPitchVariant / 2));
        if(soundGlobal) {
            player.getWorld().playSound(player.getLocation(), sound, soundVolume, soundPitch + pitchVariant);
        } else {
            player.playSound(player.getLocation(), sound, soundVolume, soundPitch + pitchVariant);
        }
    }

}

