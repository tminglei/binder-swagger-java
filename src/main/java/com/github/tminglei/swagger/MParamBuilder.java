package com.github.tminglei.swagger;

import com.github.tminglei.bind.Framework;
import io.swagger.models.parameters.Parameter;

import java.util.List;

import static com.github.tminglei.swagger.SwaggerExtensions.*;

/**
 * Helper class to build `Parameter` from a `com.github.tminglei.bind.Framework.Mapping`
 */
public class MParamBuilder {
    private Framework.Mapping<?> mapping;
    private String name;

    MParamBuilder(Framework.Mapping<?> mapping) {
        this.mapping = mapping;
    }
    public MParamBuilder name(String name) {
        this.name = name;
        return this;
    }

    public MParamBuilder in(String where) {
        mapping = mapping.$ext(e -> ext(e).in(where));
        return this;
    }
    public MParamBuilder desc(String desc) {
        mapping = mapping.$ext(e -> ext(e).desc(desc));
        return this;
    }
    public MParamBuilder example(Object example) {
        mapping = mapping.$ext(e -> ext(e).example(example));
        return this;
    }

    ///
    public Parameter get() {
        List<Parameter> ret = build();
        if (ret.size() > 1) throw new RuntimeException("MORE than 1 parameters were built!!!");
        return ret.get(0);
    }
    public List<Parameter> build() {
        SwaggerContext.scanRegisterNamedModels(mapping);
        return SwaggerContext.mHelper.mToParameters(name, mapping);
    }
}
