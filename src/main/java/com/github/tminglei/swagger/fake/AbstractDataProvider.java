package com.github.tminglei.swagger.fake;

import java.util.Map;
import java.util.Random;

/**
 * Created by minglei on 4/16/17.
 */
public abstract class AbstractDataProvider implements DataProvider {
    protected String name;
    protected Map<String, String> params;
    protected boolean required;

    private Random random;

    protected AbstractDataProvider(String name) {
        this.name = name;
        this.random = new Random();
    }

    @Override
    public void setRequestParams(Map<String, String> params) {
        this.params = params;
    }

    @Override
    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Object get() {
        return required || random.nextBoolean() ? create() : null;
    }

    ///---

    protected abstract Object create();
}
