package com.leets_8th_be.kanghyeonwoo.util;

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
        Node(Word keyword) { this.keyword = keyword; }
    }
    private Node root;

    public void add(long id, String value) {
        Word keyword = new Word(id, value);
        if (root == null) { root = new Node(keyword); return; }
        Node node = root;
        while (true) {
            int distance = distance(keyword.value(), node.keyword.value());
            if (distance == 0) return;
            Node child = node.children.get(distance);
            if (child == null) { node.children.put(distance, new Node(keyword)); return; }
            node = child;
        }
    }

    public List<Match> find(String input, int radius) {
        List<Match> matches = new ArrayList<>();
        if (root == null) return matches;
        ArrayDeque<Node> pending = new ArrayDeque<>();
        pending.add(root);
        while (!pending.isEmpty()) {
            Node node = pending.removeFirst();
            int distance = distance(input, node.keyword.value());
            if (distance > 0 && distance <= radius) {
                matches.add(new Match(node.keyword.id(), node.keyword.value(), distance));
            }
            node.children.forEach((edge, child) -> {
                if (edge >= distance - radius && edge <= distance + radius) pending.add(child);
            });
        }
        return matches;
    }

    public static int distance(String first, String second) {
        int[] a = first.codePoints().toArray(), b = second.codePoints().toArray();
        int[] row = new int[b.length + 1];
        for (int j = 0; j <= b.length; j++) row[j] = j;
        for (int i = 1; i <= a.length; i++) {
            int[] next = new int[b.length + 1];
            next[0] = i;
            for (int j = 1; j <= b.length; j++) {
                next[j] = Math.min(Math.min(next[j - 1] + 1, row[j] + 1),
                        row[j - 1] + (a[i - 1] == b[j - 1] ? 0 : 1));
            }
            row = next;
        }
        return row[b.length];
    }
}
