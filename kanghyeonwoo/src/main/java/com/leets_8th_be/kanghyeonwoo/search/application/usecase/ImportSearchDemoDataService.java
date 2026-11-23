package com.leets_8th_be.kanghyeonwoo.search.application.usecase;

import com.leets_8th_be.kanghyeonwoo.search.application.contract.SearchDemoData;
import com.leets_8th_be.kanghyeonwoo.search.application.port.in.ImportSearchDemoDataUseCase;
import com.leets_8th_be.kanghyeonwoo.search.application.port.out.SearchDemoDataStore;

public class ImportSearchDemoDataService implements ImportSearchDemoDataUseCase {
    private final SearchDemoDataStore demoData;

    public ImportSearchDemoDataService(SearchDemoDataStore demoData) {
        this.demoData = demoData;
    }

    @Override
    public void importData(SearchDemoData data) {
        demoData.importDemo(data);
    }
}
