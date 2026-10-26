package com.leets_8th_be.kanghyeonwoo.search;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BkTreeTest {
    @Test
    void findsAllNearbyWordsRegardlessOfInsertionOrder() {
        for (List<String> words : List.of(List.of("book", "books", "boo", "boon", "cook", "cake"),
                List.of("cake", "cook", "boon", "boo", "books", "book"))) {
            BkTree tree = new BkTree();
            long id = 1;
            for (String word : words) tree.add(new SearchStore.Keyword(id++, word, true));
            assertThat(tree.find("book", 1)).extracting(BkTree.Match::keyword)
                    .containsExactlyInAnyOrder("books", "boo", "boon", "cook");
            assertThat(tree.find("bok", 1)).extracting(BkTree.Match::keyword)
                    .containsExactlyInAnyOrder("book", "boo");
            assertThat(tree.find("zzzzz", 1)).isEmpty();
        }
    }

    @Test
    void measuresCharactersRatherThanUtf16CodeUnits() {
        assertThat(BkTree.distance("sprng", "spring")).isEqualTo(1);
        assertThat(BkTree.distance("스프링부투", "스프링부트")).isEqualTo(1);
        assertThat(BkTree.distance("😀", "가")).isEqualTo(1);
    }
}
