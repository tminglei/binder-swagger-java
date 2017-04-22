package com.github.tminglei.swagger.fake;

/**
 * Created by minglei on 4/15/17.
 */
public class ConstDataProvider extends AbstractDataProvider implements DataProvider {
    private Object value;

    public ConstDataProvider(Object value) {
        this(value, "root");
    }
    public ConstDataProvider(Object value, String name) {
        super(name);
        this.value = value;
    }

    @Override
    protected Object create() {
        return value;
    }
}
