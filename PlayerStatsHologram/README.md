# PlayerStatsHologram - Installation Guide

A Minecraft Paper 1.21.11 plugin that displays floating holograms with player stats when crouching and looking at another player.

## Features

- **Instant Hologram Display**: Appears immediately when a player crouches and looks at another player within ~10 blocks
- **Per-Player Visibility**: Only the crouching player can see their hologram
- **Smooth Following**: Hologram follows the target player while you're crouched and looking at them
- **Anti-Spam Protection**: Prevents duplicate holograms from rapid crouch toggling
- **Rich Stats Display**: Shows comprehensive player statistics with color formatting

## Required Dependencies

Before installing this plugin, ensure you have the following plugins installed on your Paper 1.21.11 server:

### Mandatory
1. **PlaceholderAPI** (v2.11.6 or higher)
   - Download: https://www.spigotmc.org/resources/placeholderapi.6245/
   - After installing, run: `/papi ecloud download all` then `/papi reload`

### Optional (for specific placeholders)
2. **Vault** - For economy and permissions integration
   - Download: https://www.spigotmc.org/resources/vault.34315/
   
3. **LuckPerms** - For permission/rank prefixes
   - Download: https://www.spigotmc.org/resources/luckperms.28140/
   
4. **PlayerPoints** - For gems/currency display
   - Download: https://www.spigotmc.org/resources/playerpoints.83369/

5. **AdvancedJoinLeaveMessages** or similar - For custom statistics
   - The plugin uses `ajlb_value_statistic_*` placeholders

6. **LevelledMobs** or **CustomLeveling** - For level/exp placeholders (`clv_player_level`, etc.)

7. **TempFly** - For flight time placeholder (`tempfly_time-formatted`)

## Installation Steps

### Step 1: Install Dependencies
1. Download and install PlaceholderAPI (required)
2. Download and install any optional plugins you want to use for stats
3. Restart your server after installing dependencies

### Step 2: Install PlayerStatsHologram
1. Copy `PlayerStatsHologram-1.0.0.jar` to your server's `plugins/` folder
2. Restart your server

### Step 3: Verify Installation
After restart, check the console for:
```
[PlayerStatsHologram] PlayerStatsHologram enabled successfully!
```

If you see an error about PlaceholderAPI missing, make sure PlaceholderAPI is installed first.

## Placeholder Setup

To ensure all stats display correctly, you need to set up the PlaceholderAPI expansions:

```bash
/papi ecloud download Player
/papi ecloud download Vault
/papi ecloud download LuckPerms
/papi ecloud download PlayerPoints
/papi ecloud download Statistics
/papi reload
```

For custom placeholders (like `ajlb_`, `clv_`, `tempfly_`), refer to the respective plugin documentation.

## Customizing the Hologram

The hologram layout is defined in the plugin's source code. To customize it:

1. Edit `PlayerStatsHologramPlugin.java`
2. Find the `HOLOGRAM_LINES` array
3. Modify the lines as needed

Current format:
```java
private static final String[] HOLOGRAM_LINES = {
    "&6<b>(!)</b> &eStats of %player_name%",
    "",
    " &f☆ &fRank: &c%changeoutput_equals_input:default_matcher:{vault_rank}_ifmatch:¡Get one!_else:{luckperms_prefix}%",
    " &#B6ADFD☀ &fGems: &#B6ADFD%playerpoints_points_formatted%",
    " &#feb801⛁ &fBalance: &#feb801%vault_eco_balance_formatted%",
    " &#FFEB50📋 &fRankUp: %vault_suffix%",
    " &#ecff00🧪 &fLevel: &#ecff00%clv_player_level% &7(%clv_player_exp_percent%)",
    " &#789FFF🦋 &fFlight: %tempfly_time-formatted%",
    " &#4789FF⛏ &fBlocks: &#4789FF%ajlb_value_statistic_mine_block_alltime%",
    " &#FF3535🗡 &fKills: &#FF3535%ajlb_value_statistic_player_kills_alltime%",
    " &#FFCF53☠ &fDeaths: &#FFCF53%ajlb_value_statistic_deaths_alltime%",
    " &#946FEC⌚ &fPlayTime: &#946FEC%statistic_time_played%"
};
```

### Color Code Format
- Use `&` followed by a character for legacy colors (e.g., `&c` = red)
- Use `&#RRGGBB` for hex colors (e.g., `&#B6ADFD` = light purple)

## Common Placeholder References

| Placeholder | Plugin | Description |
|------------|--------|-------------|
| `%player_name%` | Built-in | Target player's name |
| `%vault_eco_balance_formatted%` | Vault + Economy | Formatted money balance |
| `%luckperms_prefix%` | LuckPerms | Player's rank prefix |
| `%vault_rank%` | Vault | Player's rank |
| `%playerpoints_points_formatted%` | PlayerPoints | Formatted points/gems |
| `%statistic_time_played%` | PAPI Statistics | Total playtime |
| `%clv_player_level%` | CustomLeveling | Player's level |
| `%tempfly_time-formatted%` | TempFly | Remaining flight time |

## Troubleshooting

### Hologram not appearing
1. Ensure you're crouching (Shift key)
2. Make sure you're looking directly at another player
3. Check that the target player is within 10 blocks
4. Verify PlaceholderAPI is installed and working

### Stats showing as blank or "null"
1. Run `/papi ecloud download all` to get all expansions
2. Run `/papi reload`
3. Check that the required plugins (Vault, LuckPerms, etc.) are installed
4. Verify the player has data for those stats

### Console errors
1. Check that you're running Paper 1.21.11 (or compatible version)
2. Ensure all dependencies are installed
3. Check for plugin conflicts

### Hologram visible to all players
This plugin uses TextDisplay entities which should be per-player visible on Paper 1.21+. If you're seeing issues:
1. Make sure you're using Paper (not Spigot)
2. Update to the latest Paper build

## Technical Details

- **Server Version**: Paper 1.21.11 (compatible with 1.21.x)
- **Java Version**: Java 17+
- **Render Method**: Uses native Minecraft TextDisplay entities (no invisible armor stands)
- **Update Rate**: Checks every tick (20 times/second) for smooth following
- **Debounce Delay**: 150ms to prevent spam on rapid crouch toggle

## Building from Source

If you want to modify the plugin:

```bash
cd PlayerStatsHologram
mvn clean package
```

The compiled JAR will be in `target/PlayerStatsHologram-1.0.0.jar`

## Support

For issues or feature requests, please provide:
1. Server version (`/version`)
2. Plugin list (`/plugins`)
3. Console errors (if any)
4. Steps to reproduce the issue
