package com.leets_8th_be.kanghyeonwoo.search.application.port.in;

public interface RecordSearchUseCase {
    void record(String input, String sessionKey, boolean hasResults);
}
