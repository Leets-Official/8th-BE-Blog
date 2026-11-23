package com.leets_8th_be.kanghyeonwoo.search.application.port.out;

import com.leets_8th_be.kanghyeonwoo.search.application.contract.SearchDemoData;

public interface SearchDemoDataStore {
    void importDemo(SearchDemoData data);
}
