package com.github.tminglei.swagger;

import com.github.tminglei.swagger.bind.MParamBuilder;
import io.swagger.models.*;
import io.swagger.models.parameters.Parameter;

import java.util.List;
import java.util.Map;

/**
 * Extend `Operation` to provide some more helper methods
 */
public class ExOperation extends Operation {
    private SwaggerContext context;
    private String path;
    private HttpMethod method;

    public ExOperation(SwaggerContext context, String path, HttpMethod method) {
        this.context = context;
        this.path = path;
        this.method = method;
    }

    @Override
    public ExOperation summary(String summary) {
        this.setSummary(summary);
        return this;
    }

    @Override
    public ExOperation description(String description) {
        this.setDescription(description);
        return this;
    }

    @Override
    public ExOperation operationId(String operationId) {
        this.setOperationId(operationId);
        return this;
    }

    @Override
    public ExOperation schemes(List<Scheme> schemes) {
        this.setSchemes(schemes);
        return this;
    }

    @Override
    public ExOperation scheme(Scheme scheme) {
        this.addScheme(scheme);
        return this;
    }

    @Override
    public ExOperation consumes(List<String> consumes) {
        this.setConsumes(consumes);
        return this;
    }

    @Override
    public ExOperation consumes(String consumes) {
        this.addConsumes(consumes);
        return this;
    }

    @Override
    public ExOperation produces(List<String> produces) {
        this.setProduces(produces);
        return this;
    }

    @Override
    public ExOperation produces(String produces) {
        this.addProduces(produces);
        return this;
    }

    @Override
    public ExOperation security(SecurityRequirement security) {
        return this.security(security.getName(), security.getScopes());
    }
    public ExOperation security(String name, List<String> scopes) {
        this.addSecurity(name, scopes);
        return this;
    }

    @Override
    public ExOperation parameter(Parameter parameter) {
        this.addParameter(parameter);
        return this;
    }
    // helper method
    public ExOperation parameter(MParamBuilder builder) {
        builder.build().forEach(p -> parameter(p));
        return this;
    }

    @Override
    public ExOperation response(int code, Response response) {
        this.addResponse(String.valueOf(code), response);
        return this;
    }

    @Override
    public ExOperation defaultResponse(Response response) {
        this.addResponse("default", response);
        return this;
    }

    @Override
    public ExOperation tags(List<String> tags) {
        this.setTags(tags);
        return this;
    }

    @Override
    public ExOperation tag(String tag) {
        this.addTag(tag);
        return this;
    }

    @Override
    public ExOperation externalDocs(ExternalDocs externalDocs) {
        this.setExternalDocs(externalDocs);
        return this;
    }

    @Override
    public ExOperation deprecated(Boolean deprecated) {
        this.setDeprecated(deprecated);
        return this;
    }

    @Override
    public ExOperation vendorExtensions(Map<String, Object> vendorExtensions) {
        super.vendorExtensions( vendorExtensions );
        return this;
    }

    // mark the operation not implemented
    public ExOperation notImplemented() {
        context.markNotImplemented(method, path);
        return this;
    }
}