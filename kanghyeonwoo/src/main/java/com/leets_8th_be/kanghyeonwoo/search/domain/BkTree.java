package com.leets_8th_be.kanghyeonwoo.search.domain;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A metric tree using code-point Levenshtein distance, including for Korean syllables. */
public final class BkTree {
    public record Match(long keywordId, String keyword, int distance) {}

    private record Word(long id, String value) {}

    private static final class Node {
        final Word keyword;
        final Map<Integer, Node> children = new HashMap<>();

        Node(Word keyword) {
            this.keyword = keyword;
        }
    }

    private Node root;

    public void add(long id, String value) {
        Word keyword = new Word(id, value);
        if (root == null) {
            root = new Node(keyword);
            return;
        }
        Node node = root;
        while (true) {
            int distance = distance(keyword.value(), node.keyword.value());
            if (distance == 0) {
                return;
            }
            Node child = node.children.get(distance);
            if (child == null) {
                node.children.put(distance, new Node(keyword));
                return;
            }
            node = child;
        }
    }

    public List<Match> find(String input, int radius) {
        List<Match> matches = new ArrayList<>();
        if (root == null) {
            return matches;
        }
        ArrayDeque<Node> pending = new ArrayDeque<>();
        pending.add(root);
        while (!pending.isEmpty()) {
            Node node = pending.removeFirst();
            int distance = distance(input, node.keyword.value());
            if (distance > 0 && distance <= radius) {
                matches.add(new Match(node.keyword.id(), node.keyword.value(), distance));
            }
            node.children.forEach((edge, child) -> {
                if (edge >= distance - radius && edge <= distance + radius) {
                    pending.add(child);
                }
            });
        }
        return matches;
    }

    public static int distance(String first, String second) {
        int[] firstCodePoints = first.codePoints().toArray();
        int[] secondCodePoints = second.codePoints().toArray();
        int[] previousRow = new int[secondCodePoints.length + 1];
        for (int column = 0; column <= secondCodePoints.length; column++) {
            previousRow[column] = column;
        }
        for (int row = 1; row <= firstCodePoints.length; row++) {
            int[] currentRow = new int[secondCodePoints.length + 1];
            currentRow[0] = row;
            for (int column = 1; column <= secondCodePoints.length; column++) {
                int insertion = currentRow[column - 1] + 1;
                int deletion = previousRow[column] + 1;
                boolean sameCharacter = firstCodePoints[row - 1] == secondCodePoints[column - 1];
                int substitution = previousRow[column - 1] + (sameCharacter ? 0 : 1);
                currentRow[column] = Math.min(Math.min(insertion, deletion), substitution);
            }
            previousRow = currentRow;
        }
        return previousRow[secondCodePoints.length];
    }
}
