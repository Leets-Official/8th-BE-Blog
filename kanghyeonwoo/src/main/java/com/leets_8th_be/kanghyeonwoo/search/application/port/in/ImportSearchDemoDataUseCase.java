package com.leets_8th_be.kanghyeonwoo.search.application.port.in;

import com.leets_8th_be.kanghyeonwoo.search.application.contract.SearchDemoData;

public interface ImportSearchDemoDataUseCase {
    void importData(SearchDemoData data);
}
