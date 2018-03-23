package nl.quintor.studybits.student;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

public class RandomDataGenerator {

    private static final Integer randomStringLength = 10;

    public static String randString() {
        return RandomStringUtils.randomAlphabetic(randomStringLength);
    }

    public static Long randLong() {
        return new Random().nextLong();
    }
}
