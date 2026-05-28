package com.hologram.stats;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for converting Minecraft color codes (&) to section symbol (§) format.
 * Supports both legacy color codes (&a, &c, etc.) and hex colors (&#RRGGBB).
 */
public class ChatColorConverter {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final char COLOR_CHAR = '§';

    public static String convert(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // First, handle hex colors (&#RRGGBB)
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder();
            
            // Convert each pair of hex digits to a color code
            for (int i = 0; i < hex.length(); i += 2) {
                replacement.append(COLOR_CHAR).append('x');
                replacement.append(COLOR_CHAR).append(hex.charAt(i));
                replacement.append(COLOR_CHAR).append(hex.charAt(i + 1));
            }
            
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(buffer);
        text = buffer.toString();

        // Then handle legacy color codes (&a, &c, &l, etc.)
        text = text.replace('&', COLOR_CHAR);

        // Handle gradient-like formatting tags (remove HTML-style tags)
        text = text.replaceAll("<b>", "");
        text = text.replaceAll("</b>", "");

        return text;
    }
}
