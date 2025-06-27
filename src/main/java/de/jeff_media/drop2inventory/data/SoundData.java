package de.jeff_media.drop2inventory.data;

import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simple sound descriptor used to play sounds by key.
 */
public class SoundData {

    private final String sound;
    private final float volume;
    private final float pitch;
    private final float pitchVariant;
    private final SoundCategory soundCategory;

    public SoundData(String sound) {
        this(sound, 1f, 1f, 0f, SoundCategory.MASTER);
    }

    public SoundData(String sound, float volume, float pitch, float pitchVariant, SoundCategory category) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.pitchVariant = pitchVariant;
        this.soundCategory = category;
    }

    /**
     * Loads a sound from a configuration section using the given prefix.
     */
    public static SoundData fromConfigurationSection(ConfigurationSection section, String prefix) {
        if (prefix == null) {
            prefix = "";
        }
        String effect = section.getString(prefix + "effect");
        if (effect == null || effect.isEmpty()) {
            throw new IllegalArgumentException("No sound effect defined");
        }
        effect = toSoundKey(effect);
        float volume = (float) section.getDouble(prefix + "volume", 1f);
        float pitch = (float) section.getDouble(prefix + "pitch", 1f);
        float pitchVariant = (float) section.getDouble(prefix + "pitch-variant", 0f);
        String catString = section.getString(prefix + "sound-category", SoundCategory.MASTER.name());
        SoundCategory category;
        try {
            category = SoundCategory.valueOf(catString.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown sound category: " + catString);
        }
        return new SoundData(effect.toLowerCase(Locale.ROOT), volume, pitch, pitchVariant, category);
    }

    private static String toSoundKey(String input) {
        // If value looks like a legacy enum constant, convert it
        if (input.equals(input.toUpperCase(Locale.ROOT)) && !input.contains(".") && !input.contains(":")) {
            return input.toLowerCase(Locale.ROOT).replace('_', '.');
        }
        return input;
    }

    private float getFinalPitch() {
        if (pitchVariant == 0f) {
            return pitch;
        }
        return (float) (pitch + (ThreadLocalRandom.current().nextDouble(pitchVariant) - pitchVariant / 2));
    }

    public void playToPlayer(Player player) {
        playToPlayer(player, player.getLocation());
    }

    public void playToPlayer(Player player, Location location) {
        player.playSound(location, sound, soundCategory, volume, getFinalPitch());
    }

    public void playToWorld(Location location) {
        Objects.requireNonNull(location.getWorld()).playSound(location, sound, soundCategory, volume, getFinalPitch());
    }
}
