## Commands
`/drop2inv` - Toggle Drop2Inventory for current player
`/drop2inv <player>` - Toggle Drop2Inventory for <player>
`/drop2inv reload` - Reloads the config file
`/drop2inv debug` - Enable debug mode

## Permissions
`drop2inventory.use` - Allows to use `/drop2inv`
`drop2inventory.others` - Allows to use `/drop2inv <player>`
`drop2inventory.autocondense` - Automatically condenses ingots to blocks when enabled in the config
`drop2inventory.reload` - Allows to use `/drop2inv reload`
`drop2inventory.debug` - Allows to use `/drop2inv debug`

## Todo
List of blocks that will have a sound effect when picked up @hungvipso88 on discord

## Added
Inv full message
Hopper detection
dispenser/dropper detection
custom event priority

## Added in 1.1.0
Prefer offhand slot if it already contains the item and there is space left

## To check
dispenser detection

## Folia Update Guide
This fork modernizes Drop2Inventory-Plus with Paper's Folia scheduler and cleans
out several optional dependencies. The highlights of this update are:

- **Folia Scheduler** – Added a new `Scheduler` class that automatically detects
  Folia and chooses the correct scheduler. All previous calls to `Bukkit#getScheduler()`
  now route through this helper so the plugin works on both standard Paper and
  Folia servers.
- **Paper API** – Replaced Spigot API dependencies with Paper, giving access to
  Folia classes while remaining backwards compatible.
- **Removed Metrics and Update Checks** – The optional bStats metrics and
  SpigotUpdateChecker have been entirely removed. No statistics are collected and
  the plugin no longer checks Spigot for updates.
- **Dropped Unused Hooks** – Integrations for WildChests, WildStacker and
  SuperiorSkyblock2 have been removed. If you rely on these plugins you may need
  to implement your own hooks.
- **Simplified Configuration** – Update‑related settings were deleted from
  `config.yml`. Make sure to remove those entries from your existing config or
  regenerate it.

Update your server by replacing the old jar with this one and removing the
now-unused dependencies from your plugin folder. The rest of the configuration
and permissions remain unchanged.
