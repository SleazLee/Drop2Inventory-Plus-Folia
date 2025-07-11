package de.jeff_media.drop2inventory.handlers;

import de.jeff_media.drop2inventory.Main;
import de.jeff_media.drop2inventory.data.WorldBoundingBox;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Manages who the owner is of drops that have not yet been spawned.
 */
public class DropOwnerManager {

    private final static HashMap<WorldBoundingBox, UUID> dropLocationMap = new HashMap<>();
    //private final static HashMap<WorldBoundingBox, UUID> hangingDropLocationMap = new HashMap<>();
    private final static Main main = Main.getInstance();

    static {

        /*
          Clear bounding boxes
         */
        de.jeff_media.drop2inventory.utils.Scheduler.runTimer(()->{
            //for(HashMap<WorldBoundingBox, UUID> map : new HashMap[] {dropLocationMap, hangingDropLocationMap}) {
                Iterator<WorldBoundingBox> iterator = dropLocationMap.keySet().iterator();
                while (iterator.hasNext()) {
                    WorldBoundingBox boundingBox = iterator.next();
                    if (boundingBox.isExpired()) {
                        //dropLocationMap.remove(boundingBox);
                        iterator.remove();
                        if (main.isDebug()) main.debug("BoundingBox expired");
                    }
                }
            //}
        }, 1, 1);
    }

    public static @Nullable Player getDropOwner(Location location) {
        Player player = getDropOwner(location, dropLocationMap);
        if(player == null || player.isDead()) return null;
        return player;
    }

    public static void registerSimple(Player player, Location location) {
        if(main.isDebug()) main.debug("Registering DropOwner (SIMPLE) " + player.getName() + " for location " + location);
        dropLocationMap.put(WorldBoundingBoxGenerator.getSimpleBoundingBox(location), player.getUniqueId());
    }

    public static void registerManually(Player player, Location location, WorldBoundingBox box) {
        if(main.isDebug()) main.debug("Registering DropOwner (MANUALLY) " + player.getName() + " for location " + location);
        dropLocationMap.put(box, player.getUniqueId());
    }

    public static void register(Player player, Location location, @Nullable Block block) {
        if(main.isDebug()) main.debug("Registering DropOwner " + player.getName() + " for location " + location);
        WorldBoundingBox boundingBox = WorldBoundingBoxGenerator.getAppropriateBoundingBox(location, block, player);
        dropLocationMap.put(boundingBox, player.getUniqueId());
        //System.out.println("BoundingBox registered");
    }

    public static void register(Player player, WorldBoundingBox boundingBox) {
        dropLocationMap.put(boundingBox, player.getUniqueId());
    }

    /*public static void registerHanging(Player player, Location location, @NotNull Block block) {
        if(main.debug) main.debug("Registering DropOwner " + player.getName() + " for location " + location);
        WorldBoundingBox boundingBox = new WorldBoundingBox(block.getLocation(), 200, 0, 0, 0);
        dropLocationMap.put(boundingBox, player.getUniqueId());
    }*/

    private static @Nullable Player getDropOwner(Location location, HashMap<WorldBoundingBox, UUID> map) {
        //List<WorldBoundingBox> possibleBoundingBoxes = new ArrayList<>();
        WorldBoundingBox match = null;
        double bestDistance = Double.MAX_VALUE;
        for (WorldBoundingBox boundingBox : map.keySet()) {
            if (boundingBox.contains(location)) {
                double distance = boundingBox.getBoundingBox().getCenter().distanceSquared(location.toVector());
                if(distance < bestDistance) {
                    //possibleBoundingBoxes.add(boundingBox);
                    match = boundingBox;
                    bestDistance = distance;
                }
            }
        }
        //if (possibleBoundingBoxes.size() == 0) return null;
        //possibleBoundingBoxes.sort(new WorldBoundingBox.BoundingBoxComparator(location));
        return match == null ? null : Bukkit.getPlayer(dropLocationMap.get(match));
    }

    /*public static @Nullable Player getHangingDropOwner(Location location) {
        return getDropOwner(location, hangingDropLocationMap);
    }*/


}
