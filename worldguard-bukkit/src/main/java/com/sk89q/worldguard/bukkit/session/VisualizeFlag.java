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

package com.sk89q.worldguard.bukkit.session;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.BukkitConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Runs a configurable command as the player when they enter a region that has the visualize flag set,
 * if they have permission worldguard.visualize. Cooldown prevents spamming.
 */
public class VisualizeFlag extends FlagValueChangeHandler<Boolean> {

    public static final Factory FACTORY = new Factory();

    public static class Factory extends Handler.Factory<VisualizeFlag> {
        @Override
        public VisualizeFlag create(Session session) {
            return new VisualizeFlag(session);
        }
    }

    private static final String VISUALIZE_PERMISSION = "worldguard.visualize";
    private static final long MS_PER_TICK = 50L;

    private long lastRunTimeMs = 0;

    public VisualizeFlag(Session session) {
        super(session, Flags.VISUALIZE);
    }

    @Override
    protected void onInitialValue(LocalPlayer player, ApplicableRegionSet set, Boolean value) {
    }

    @Override
    protected boolean onSetValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Boolean currentValue, Boolean lastValue, MoveType moveType) {
        if (!Boolean.TRUE.equals(currentValue)) {
            return true;
        }
        if (!player.hasPermission(VISUALIZE_PERMISSION)) {
            return true;
        }
        WorldGuardPlugin plugin = WorldGuardPlugin.inst();
        if (!(plugin.getConfigManager() instanceof BukkitConfigurationManager)) {
            return true;
        }
        BukkitConfigurationManager cfg = (BukkitConfigurationManager) plugin.getConfigManager();
        String commandTemplate = cfg.getVisualizeCommand();
        if (commandTemplate == null || commandTemplate.isEmpty()) {
            return true;
        }
        ProtectedRegion region = null;
        for (ProtectedRegion r : toSet.getRegions()) {
            if (Boolean.TRUE.equals(r.getFlag(Flags.VISUALIZE))) {
                region = r;
                break;
            }
        }
        if (region == null) {
            return true;
        }
        int cooldownTicks = cfg.getVisualizeCooldownTicks();
        long cooldownMs = cooldownTicks * MS_PER_TICK;
        long now = System.currentTimeMillis();
        if ((now - lastRunTimeMs) < cooldownMs) {
            return true;
        }
        if (!(player instanceof BukkitPlayer)) {
            return true;
        }
        Player bukkitPlayer = ((BukkitPlayer) player).getPlayer();
        if (bukkitPlayer == null || !bukkitPlayer.isOnline()) {
            return true;
        }
        String rawCommand = commandTemplate.replace("%region%", region.getId()).trim();
        final String command = rawCommand.startsWith("/") ? rawCommand.substring(1) : rawCommand;
        lastRunTimeMs = now;
        Bukkit.getScheduler().runTask(plugin, () -> bukkitPlayer.performCommand(command));
        return true;
    }

    @Override
    protected boolean onAbsentValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Boolean lastValue, MoveType moveType) {
        return true;
    }
}
