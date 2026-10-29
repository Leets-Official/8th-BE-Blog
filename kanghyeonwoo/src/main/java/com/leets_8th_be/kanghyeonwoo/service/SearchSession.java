package com.leets_8th_be.kanghyeonwoo.service;

/** Resolves the search identity only when a search needs to be counted. */
@FunctionalInterface
public interface SearchSession {
    String key();
}
