package com.example.qltd.common.util;

import java.text.Normalizer;

public final class SlugUtil {

    private SlugUtil() {
    }

    public static String toSlug(String input) {

        if (input == null || input.isBlank()) {
            return "";
        }

        String slug = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-zA-Z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .toLowerCase();

        return slug;
    }

}