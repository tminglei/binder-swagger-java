package com.github.tminglei.swagger.fake;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by minglei on 4/15/17.
 */
public class ObjectDataProvider extends AbstractDataProvider implements DataProvider {
    private Map<String, DataProvider> fields;

    public ObjectDataProvider(Map<String, DataProvider> fields, boolean required) {
        setRequired(required);
        this.fields = fields;
    }

    @Override
    protected Object create() {
        Map<String, Object> valueMap = new HashMap<>();
        for (String name : fields.keySet()) {
            valueMap.put(name, fields.get(name).get());
        }
        return valueMap;
    }
}
