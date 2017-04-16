package com.github.tminglei.swagger.fake;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by minglei on 4/15/17.
 */
public class ListDataProvider extends AbstractDataProvider implements DataProvider {
    private DataProvider itemProvider;

    public ListDataProvider(DataProvider itemProvider, boolean required) {
        setRequired(required);
        this.itemProvider = itemProvider;
    }

    @Override
    protected Object create() {
        List list = new ArrayList();
        for (int i = 0; i < 5; i++) {
            list.add(itemProvider.get());
        }
        return list;
    }
}
