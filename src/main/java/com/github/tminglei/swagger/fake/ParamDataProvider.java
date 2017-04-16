package com.github.tminglei.swagger.fake;

import java.util.Map;

/**
 * Created by minglei on 4/15/17.
 */
public class ParamDataProvider extends AbstractDataProvider implements DataProvider {
    private String paramKey;
    private volatile Map<String, String> params;

    public ParamDataProvider(String paramKey) {
        this.paramKey = paramKey;
    }

    public void setParamMap(Map<String, String> params) {
        this.params = params;
    }

    @Override
    protected Object create() {
        return params.get(paramKey);
    }
}