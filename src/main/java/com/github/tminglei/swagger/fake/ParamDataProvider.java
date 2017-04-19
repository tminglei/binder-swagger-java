package com.github.tminglei.swagger.fake;

/**
 * Created by minglei on 4/15/17.
 */
public class ParamDataProvider extends AbstractDataProvider implements DataProvider {
    private String paramKey;

    public ParamDataProvider(String paramKey) {
        this.paramKey = paramKey;
    }

    @Override
    protected Object create() {
        return params != null ? params.get(paramKey)
            : null;
    }
}