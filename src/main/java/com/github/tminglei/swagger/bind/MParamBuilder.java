package com.github.tminglei.swagger.bind;

import com.github.tminglei.bind.Framework;
import com.github.tminglei.swagger.SwaggerContext;
import io.swagger.models.parameters.Parameter;

import java.util.List;

/**
 * Helper class to build `Parameter` from a `com.github.tminglei.bind.Framework.Mapping`
 */
public class MParamBuilder {
    private SwaggerContext context;

    private Attachment.Builder<?> attachBuilder;
    private String name;

    public MParamBuilder(SwaggerContext context, Framework.Mapping<?> mapping) {
        this.context = context;
        this.attachBuilder = new Attachment.Builder(mapping);
    }
    public MParamBuilder name(String name) {
        this.name = name;
        return this;
    }

    public MParamBuilder in(String where) {
        attachBuilder = attachBuilder.in(where);
        return this;
    }
    public MParamBuilder desc(String desc) {
        attachBuilder = attachBuilder.desc(desc);
        return this;
    }
    public MParamBuilder example(Object example) {
        attachBuilder = attachBuilder.example(example);
        return this;
    }

    ///
    public Parameter get() {
        List<Parameter> ret = build();
        if (ret.size() > 1) throw new RuntimeException("MORE than 1 parameters were built!!!");
        return ret.get(0);
    }
    public List<Parameter> build() {
        context.scanAndRegisterNamedModels(attachBuilder.$$);
        return context.getMConverter().mToParameters(name, attachBuilder.$$);
    }
}
