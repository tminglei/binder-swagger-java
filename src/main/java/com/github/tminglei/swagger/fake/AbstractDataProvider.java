package com.github.tminglei.swagger.fake;

import java.util.Random;

/**
 * Created by minglei on 4/16/17.
 */
public abstract class AbstractDataProvider implements DataProvider {
    protected boolean required;
    private Random random;

    protected AbstractDataProvider() {
        this.random = new Random();
    }

    @Override
    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public Object get() {
        return required || random.nextBoolean() ? create() : null;
    }

    ///---

    protected abstract Object create();
}
