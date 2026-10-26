package com.leets_8th_be.kanghyeonwoo.config;

import java.util.Locale;
import java.util.Random;
import net.datafaker.Faker;

/** Generates repeatable fixture text; database relationships and states belong to the seeder. */
public final class MockDataGenerator {
    private final Faker faker;

    public MockDataGenerator(long seed) {
        faker = new Faker(Locale.KOREAN, new Random(seed));
    }

    public String userName() {
        return limit(faker.name().fullName(), 50);
    }

    public String postTitle() {
        return "[MOCK] " + limit(faker.lorem().sentence(6), 143);
    }

    public String postContent() {
        return faker.lorem().paragraph(5);
    }

    public String commentContent() {
        return faker.lorem().sentence(12);
    }

    private static String limit(String value, int maxLength) {
        return value.substring(0, Math.min(value.length(), maxLength));
    }
}
