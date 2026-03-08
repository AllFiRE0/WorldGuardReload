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

package com.sk89q.worldguard.util.localization;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces color macros in message strings for use in chat, action bar, and titles.
 * Supports:
 * <ul>
 *   <li>{@code &} — alternate color code (e.g. {@code &c} for red), same as {@code §}</li>
 *   <li>{@code §} — standard Minecraft color code (passed through)</li>
 *   <li>{@code {#RRGGBB}} — HEX color (1.16+), e.g. {@code {#FF5555}}</li>
 * </ul>
 */
public final class MessageColors {

    private static final char COLOR_CHAR = '\u00A7';
    private static final Pattern HEX_PATTERN = Pattern.compile("\\{#([0-9A-Fa-f]{6})}");

    private MessageColors() {
    }

    /**
     * Replace color macros in the message: {@code &} with section sign, and {@code {#RRGGBB}} with
     * legacy hex format for 1.16+.
     *
     * @param message the message (may be null)
     * @return the message with color codes applied, or empty string if null
     */
    public static String replaceColors(String message) {
        if (message == null) {
            return "";
        }
        // & -> § (same as Bukkit ChatColor.translateAlternateColorCodes)
        String out = message.replace('&', COLOR_CHAR);
        // {#RRGGBB} -> §x§R§R§G§G§B§B (each digit as a separate §-code for 1.16+)
        Matcher m = HEX_PATTERN.matcher(out);
        StringBuffer sb = new StringBuffer(out.length());
        while (m.find()) {
            String hex = m.group(1);
            StringBuilder repl = new StringBuilder().append(COLOR_CHAR).append('x');
            for (int i = 0; i < 6; i++) {
                repl.append(COLOR_CHAR).append(hex.charAt(i));
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(repl.toString()));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
