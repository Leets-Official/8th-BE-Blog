package com.leets_8th_be.kanghyeonwoo.config;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MockDataGeneratorTest {
    @Test
    void seedReproducesTextAndDifferentSeedsProvideDifferentData() {
        var original = sample(new MockDataGenerator(20261026L));
        assertThat(sample(new MockDataGenerator(20261026L))).isEqualTo(original);
        assertThat(sample(new MockDataGenerator(20261027L))).isNotEqualTo(original);
        assertThat(original.get(0)).isNotBlank().hasSizeLessThanOrEqualTo(50).matches(".*[가-힣].*");
        assertThat(original.get(1)).startsWith("[MOCK] ").hasSizeLessThanOrEqualTo(150);
        assertThat(original.get(2)).isNotBlank();
        assertThat(original.get(3)).isNotBlank();
    }

    private List<String> sample(MockDataGenerator generator) {
        return List.of(generator.userName(), generator.postTitle(), generator.postContent(), generator.commentContent());
    }
}
