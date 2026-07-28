package com.example.qltd.common.util;

public final class WebsiteUtil {

    private WebsiteUtil() {
    }

    public static String normalize(String website) {

        if (website == null || website.isBlank()) {
            return null;
        }

        String value = website.trim();

        if (!value.startsWith("http://") &&
                !value.startsWith("https://")) {

            value = "https://" + value;

        }

        return value;

    }

}
