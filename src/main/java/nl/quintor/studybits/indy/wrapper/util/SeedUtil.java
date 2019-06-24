package nl.quintor.studybits.indy.wrapper.util;

import java.security.SecureRandom;

public class SeedUtil {

    private static final String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private SeedUtil() {
    }

    private static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        while (count-- != 0) {
            int character = (int)(secureRandom.nextDouble()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    public static String generateSeed() {
        return randomAlphaNumeric(32);
    }
}
