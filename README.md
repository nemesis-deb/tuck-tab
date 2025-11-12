# Mob Head Leaderboard Plugin

A Minecraft Spigot/Paper plugin that displays a leaderboard in the tab list showing players with the most mob head achievements collected.

## Features

- Real-time tab list leaderboard with modern design
- Animated title and visual effects
- Server information display (TPS, online players)
- Player statistics section with rank and progress
- Skills integration - displays progression bar for last skill that received XP (requires SkillsPlugin)
- Tracks mob head achievements from your datapack
- Configurable update intervals and display format
- Player statistics command
- Automatic updates when players earn achievements
- Fully customizable colors, formats, and sections

## Installation

1. Build the plugin using Maven: `mvn clean package`
2. Copy the generated JAR from `target/` to your server's `plugins/` folder
3. Restart your server
4. Configure the plugin in `plugins/MobHeadLeaderboard/config.yml`

## Configuration

Edit `config.yml` to customize:
- Update interval for tab list
- Number of players shown in leaderboard
- Animated title frames or static title
- Server info display (TPS, online players, time)
- Player stats section (heads, rank, progress)
- Skill progression bar (when SkillsPlugin is installed)
- Header and footer text
- Leaderboard formatting with medals
- Colors, separators, and all visual elements

## Important: Configure Your Datapack Advancements

Open `src/main/java/com/mobheads/leaderboard/HeadTracker.java` and update the `mobHeadAdvancements` array with the actual advancement keys from your datapack. Replace the example namespace `mobheads:` with your datapack's namespace.

## Commands

- `/headstats [player]` - View mob head achievement stats
- `/headreload` - Reload plugin configuration

## Permissions

- `mobheads.stats` - View head statistics (default: true)
- `mobheads.reload` - Reload plugin configuration (default: op)

## Building

Requires Java 17+ and Maven:
```
mvn clean package
```

## Compatibility

- Minecraft 1.20.4+
- Spigot/Paper server
- Optional: SkillsPlugin integration for skill progression display
