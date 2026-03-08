/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.bukkit;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.commands.region.RegionInfoCommandType;
import com.sk89q.worldguard.config.YamlConfigurationManager;
import com.sk89q.worldedit.util.report.Unreported;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BukkitConfigurationManager extends YamlConfigurationManager {

    @Unreported private WorldGuardPlugin plugin;
    @Unreported private ConcurrentMap<String, BukkitWorldConfiguration> worlds = new ConcurrentHashMap<>();

    private boolean hasCommandBookGodMode;
    boolean extraStats;

    private ProtectionMessageDisplay protectionMessageDisplay = ProtectionMessageDisplay.CHAT;
    private String visualizeCommand = "";
    private int visualizeCooldownTicks = 80;

    private boolean denySoundEnabled = true;
    private int denySoundCooldownTicks = 80;
    private String denySoundName = "minecraft:entity.experience_orb.pickup";
    private String denySoundSource = "MASTER";
    private float denySoundVolume = 0.1f;
    private float denySoundPitch = 1.0f;

    private String regionInfoAddOwnerCommand;
    private String regionInfoAddMemberCommand;
    private String regionInfoRemoveOwnerCommand;
    private String regionInfoRemoveMemberCommand;
    private boolean regionInfoAddOwnerRun;
    private boolean regionInfoAddMemberRun;
    private boolean regionInfoRemoveOwnerRun;
    private boolean regionInfoRemoveMemberRun;

    /**
     * Construct the object.
     *
     * @param plugin The plugin instance
     */
    public BukkitConfigurationManager(WorldGuardPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    public Collection<BukkitWorldConfiguration> getWorldConfigs() {
        return worlds.values();
    }

    @Override
    public void load() {
        super.load();
        this.extraStats = getConfig().getBoolean("custom-metrics-charts", true);
        this.protectionMessageDisplay = ProtectionMessageDisplay.fromString(getConfig().getString("regions.message-display", "chat"));
        this.visualizeCommand = getConfig().getString("regions.Visualize", "");
        this.visualizeCooldownTicks = getConfig().getInt("regions.VisualizeCooldown", 80);
        this.denySoundEnabled = getConfig().getBoolean("regions.deny-sound.enabled", true);
        this.denySoundCooldownTicks = getConfig().getInt("regions.deny-sound.cooldown", 80);
        this.denySoundName = getConfig().getString("regions.deny-sound.sound.name", "minecraft:entity.experience_orb.pickup");
        this.denySoundSource = getConfig().getString("regions.deny-sound.sound.source", "MASTER");
        this.denySoundVolume = (float) getConfig().getDouble("regions.deny-sound.sound.volume", 0.1);
        this.denySoundPitch = (float) getConfig().getDouble("regions.deny-sound.sound.pitch", 1.0);
        this.regionInfoAddOwnerCommand = getConfig().getString("region-info.commands.add-owner.command");
        this.regionInfoAddMemberCommand = getConfig().getString("region-info.commands.add-member.command");
        this.regionInfoRemoveOwnerCommand = getConfig().getString("region-info.commands.remove-owner.command");
        this.regionInfoRemoveMemberCommand = getConfig().getString("region-info.commands.remove-member.command");
        this.regionInfoAddOwnerRun = "run".equalsIgnoreCase(getConfig().getString("region-info.commands.add-owner.type", "suggest"));
        this.regionInfoAddMemberRun = "run".equalsIgnoreCase(getConfig().getString("region-info.commands.add-member.type", "suggest"));
        this.regionInfoRemoveOwnerRun = "run".equalsIgnoreCase(getConfig().getString("region-info.commands.remove-owner.type", "suggest"));
        this.regionInfoRemoveMemberRun = "run".equalsIgnoreCase(getConfig().getString("region-info.commands.remove-member.type", "suggest"));
    }

    /**
     * Where to display protection deny messages (chat, actionbar, title).
     */
    public ProtectionMessageDisplay getProtectionMessageDisplay() {
        return protectionMessageDisplay;
    }

    /**
     * Command template run as player on region enter when they have worldguard.visualize. Use %region% for region id.
     */
    public String getVisualizeCommand() {
        return visualizeCommand == null ? "" : visualizeCommand;
    }

    /**
     * Cooldown in ticks before the Visualize command can run again.
     */
    public int getVisualizeCooldownTicks() {
        return visualizeCooldownTicks;
    }

    public boolean isDenySoundEnabled() {
        return denySoundEnabled;
    }

    public int getDenySoundCooldownTicks() {
        return denySoundCooldownTicks;
    }

    public String getDenySoundName() {
        return denySoundName == null ? "minecraft:entity.experience_orb.pickup" : denySoundName;
    }

    public String getDenySoundSource() {
        return denySoundSource == null ? "MASTER" : denySoundSource;
    }

    public float getDenySoundVolume() {
        return denySoundVolume;
    }

    public float getDenySoundPitch() {
        return denySoundPitch;
    }

    /**
     * Get region-info button command template for the given type, or null to use default.
     * Placeholders {world}, {region_name}, {player} should be replaced by the caller.
     */
    public String getRegionInfoCommandTemplate(RegionInfoCommandType type) {
        if (type == null) return null;
        switch (type) {
            case ADD_OWNER: return regionInfoAddOwnerCommand;
            case ADD_MEMBER: return regionInfoAddMemberCommand;
            case REMOVE_OWNER: return regionInfoRemoveOwnerCommand;
            case REMOVE_MEMBER: return regionInfoRemoveMemberCommand;
            default: return null;
        }
    }

    /**
     * Whether the region-info command for this type should be run as the player (true) or suggested into chat (false).
     */
    public boolean isRegionInfoCommandRun(RegionInfoCommandType type) {
        if (type == null) return false;
        switch (type) {
            case ADD_OWNER: return regionInfoAddOwnerRun;
            case ADD_MEMBER: return regionInfoAddMemberRun;
            case REMOVE_OWNER: return regionInfoRemoveOwnerRun;
            case REMOVE_MEMBER: return regionInfoRemoveMemberRun;
            default: return false;
        }
    }

    @Override
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    @Override
    public void copyDefaults() {
        // Create the default configuration file
        plugin.createDefaultConfiguration(new File(plugin.getDataFolder(), "config.yml"), "config.yml");
    }

    @Override
    public void unload() {
        worlds.clear();
    }

    @Override
    public void postLoad() {
        // Load configurations for each world
        for (World world : WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getWorlds()) {
            get(world);
        }
        getConfig().save();
    }

    /**
     * Get the configuration for a world.
     *
     * @param world The world to get the configuration for
     * @return {@code world}'s configuration
     */
    @Override
    public BukkitWorldConfiguration get(World world) {
        String worldName = world.getName();
        return get(worldName);
    }

    public BukkitWorldConfiguration get(String worldName) {
        BukkitWorldConfiguration config = worlds.get(worldName);
        BukkitWorldConfiguration newConfig = null;

        while (config == null) {
            if (newConfig == null) {
                newConfig = new BukkitWorldConfiguration(plugin, worldName, this.getConfig());
            }
            worlds.putIfAbsent(worldName, newConfig);
            config = worlds.get(worldName);
        }

        return config;
    }

    public void updateCommandBookGodMode() {
        try {
            if (plugin.getServer().getPluginManager().isPluginEnabled("CommandBook")) {
                Class.forName("com.sk89q.commandbook.GodComponent");
                hasCommandBookGodMode = true;
                return;
            }
        } catch (ClassNotFoundException ignore) {}
        hasCommandBookGodMode = false;
    }

    public boolean hasCommandBookGodMode() {
        return hasCommandBookGodMode;
    }
}
