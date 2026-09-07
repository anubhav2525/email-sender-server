package com.scheduler.email.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class StringOperations {
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = UPPER.toLowerCase();
    private static final String DIGITS = "0123456789";
    private static final String DEFAULT_CHARACTERS = UPPER + LOWER + DIGITS;

    private static final SecureRandom random = new SecureRandom();

    /**
     * This function is used to generate random string using numbers[0-9], alphabets[a-z, A- Z].
     *
     * @param length - how many characters you want
     * @return - String value
     */
    public String generateRandomString(int length) {
        return generate(length, DEFAULT_CHARACTERS);
    }

    public static String generate(int length, String characters) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than zero");
        }

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    /**
     * Trims whitespace and converts to uppercase.
     */
    public String normalizeNameInUppercase(String name) {
        return name.trim().toUpperCase().replaceAll("\\s+", "_");
    }

    /**
     * Trims whitespace and check null.
     */
    public String trimOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

}
