package com.github.tminglei.swagger.fake;

import java.util.Map;
import java.util.Optional;

/**
 * Created by minglei on 4/23/17.
 */
public class OrDataProvider implements DataProvider {
    private DataProvider first;
    private DataProvider second;

    public OrDataProvider(DataProvider first, DataProvider second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public void setRequestParams(Map<String, String> params) {
        first.setRequestParams(params);
        second.setRequestParams(params);
    }

    @Override
    public void setRequired(boolean required) {
        first.setRequired(required);
        second.setRequired(required);
    }

    @Override
    public String name() {
        return first.name();
    }

    @Override
    public Object get() {
        return Optional.ofNullable(first.get()).orElseGet(second::get);
    }
}
