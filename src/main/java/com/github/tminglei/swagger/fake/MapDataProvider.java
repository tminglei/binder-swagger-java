package com.github.tminglei.swagger.fake;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by minglei on 4/15/17.
 */
public class MapDataProvider extends AbstractDataProvider implements DataProvider {
    private DataProvider valueProvider;

    public MapDataProvider(DataProvider valueProvider, boolean required) {
        setRequired(required);
        this.valueProvider = valueProvider;
    }

    @Override
    protected Object create() {
        Map map = new HashMap();
        for (int i = 0; i < 5; i++) {
            map.put("key"+i, valueProvider.get());
        }
        return map;
    }
}
