package io.puharesource.mc.titlemanager.backend.utils;

import io.puharesource.mc.titlemanager.Config;
import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.api.animations.FrameSequence;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Pattern;

public final class MiscellaneousUtils {
    public static String format(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static FrameSequence isValidAnimationString(String text) {
        if (text == null) return null;
        text = text.toUpperCase().trim();

        return text.startsWith("ANIMATION:") ?
                TitleManager.getInstance().getConfigManager().getAnimation(text.substring(10)) : null;
    }

    public static TitleObject generateTitleObjectFromArgs(int offset, String[] args) {
        int fadeIn = -1;
        int stay = -1;
        int fadeOut = -1;

        StringBuilder sb = new StringBuilder();
        boolean isReadingTimes = true;
        for (int i = offset; args.length > i; i++) {
            if (isReadingTimes) {
                String lower = args[i].toLowerCase();
                int amount = -1;
                try {
                    amount = Integer.parseInt(lower.replaceAll("\\D", ""));
                } catch (NumberFormatException ignored) {
                }

                if (lower.startsWith("-fadein=")) {
                    if (amount != -1) fadeIn = amount;
                    continue;
                } else if (lower.startsWith("-stay=")) {
                    if (amount != -1) stay = amount;
                    continue;
                } else if (lower.startsWith("-fadeout=")) {
                    if (amount != -1) fadeOut = amount;
                    continue;
                } else {
                    isReadingTimes = false;
                    sb.append(args[i]);
                    continue;
                }

            }

            sb.append(" ").append(args[i]);
        }


        String title = format(sb.toString());
        String subtitle = null;

        if (title.contains("{nl}")) title = title.replace("{nl}", "<nl>");
        if (title.contains("<nl>")) {
            String[] titles = title.split("<nl>", 2);
            title = titles[0];
            subtitle = titles[1];
        }

        TitleObject object = subtitle == null ? new TitleObject(title, TitleObject.TitleType.TITLE) : new TitleObject(title, subtitle);

        ConfigurationSection section = TitleManager.getInstance().getConfigManager().getConfig().getConfigurationSection("welcome_message");

        object.setFadeIn(fadeIn != -1 ? fadeIn : section.getInt("fadeIn"));
        object.setStay(stay != -1 ? stay : section.getInt("stay"));
        object.setFadeOut(fadeOut != -1 ? fadeOut : section.getInt("fadeOut"));

        return object;
    }

    public static String combineArray(int offset, String[] array) {
        StringBuilder sb = new StringBuilder(array[offset]);
        for (int i = offset + 1; array.length > i; i++) sb.append(" ").append(array[i]);
        return format(sb.toString());
    }

    public static String formatNumber(double number) {
        return formatNumber(new BigDecimal(number));
    }

    public static String formatNumber(BigDecimal number) {
        Config config = TitleManager.getInstance().getConfigManager();

        if (config.isNumberFormatEnabled()) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            return new DecimalFormat(config.getNumberFormat(), symbols).format(number);
        }

        return String.valueOf(number.doubleValue());
    }

    public static Player getPlayer(final String name) {
        if (!Pattern.matches("^[a-z0-9A-Z_]+", name) && name.length() <= 16) return null;
        Player correctPlayer = Bukkit.getPlayerExact(name);
        if (correctPlayer == null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (StringUtils.containsIgnoreCase(player.getName(), name)) {
                    return player;
                }
            }
        }

        return correctPlayer;
    }
}
