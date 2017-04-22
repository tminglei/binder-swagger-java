package com.github.tminglei.swagger.fake;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by minglei on 4/15/17.
 */
public class ListDataProvider extends AbstractDataProvider implements DataProvider {
    private DataProvider itemProvider;

    public ListDataProvider(DataProvider itemProvider) {
        this(itemProvider, "root");
    }
    public ListDataProvider(DataProvider itemProvider, String name) {
        super(name);
        this.itemProvider = itemProvider;
    }

    @Override
    protected Object create() {
        List list = new ArrayList();
        for (int i = 0; i < 5; i++) {
            itemProvider.setRequestParams(params);
            list.add(itemProvider.get());
        }
        return list;
    }
}
