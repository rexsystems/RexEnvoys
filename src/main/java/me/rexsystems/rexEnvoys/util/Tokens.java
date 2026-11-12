package me.rexsystems.rexEnvoys.util;

import java.util.HashMap;
import java.util.Map;

public final class Tokens {

    private Tokens() {
    }

    public static String apply(String template, Map<String, String> tokens) {
        String value = template == null ? "" : template;
        if (tokens == null || tokens.isEmpty()) {
            return value;
        }
        String prefix = tokens.get("prefix");
        if (prefix != null) {
            value = value.replace("{prefix}", prefix).replace("<prefix>", prefix);
        }
        Map<String, String> rest = new HashMap<>();
        for (Map.Entry<String, String> entry : tokens.entrySet()) {
            if (!"prefix".equals(entry.getKey())) {
                rest.put(entry.getKey(), entry.getValue());
            }
        }
        return TextParser.apply(value, rest);
    }
}
