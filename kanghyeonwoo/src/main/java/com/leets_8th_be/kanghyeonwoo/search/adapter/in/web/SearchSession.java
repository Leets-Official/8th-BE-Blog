package com.leets_8th_be.kanghyeonwoo.search.application.port.in;

/** Resolves the search identity only when a search needs to be counted. */
@FunctionalInterface
public interface SearchSession {
    String key();
}
