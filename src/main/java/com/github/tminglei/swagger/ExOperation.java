package com.github.tminglei.swagger;

import io.swagger.models.*;
import io.swagger.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Extend `Operation` to provide some more helper methods
 */
public class ExOperation extends Operation {
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
        this.addSecurity(security.getName(), security.getScopes());
        return this;
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

    ///
    ExOperation merge(SharingHolder sharing) {
        if (!sharing.tags().isEmpty()) {    // turn unmodifiable list to modifiable list
            this.setTags(new ArrayList<>(sharing.tags()));
        }
        if (!sharing.schemes().isEmpty()) {
            this.setSchemes(new ArrayList<>(sharing.schemes()));
        }
        if (!sharing.consumes().isEmpty()) {
            this.setConsumes(new ArrayList<>(sharing.consumes()));
        }
        if (!sharing.produces().isEmpty()) {
            this.setProduces(new ArrayList<>(sharing.produces()));
        }
        if (!sharing.securities().isEmpty()) {
            this.setSecurity(new ArrayList<>());
            sharing.securities().forEach((name, scopes) -> this.security(name, scopes));
        }
        if (!sharing.parameters().isEmpty()) {
            this.setParameters(new ArrayList<>(sharing.parameters()));
        }
        if (!sharing.responses().isEmpty()) {
            this.setResponses(new HashMap<>());
            sharing.responses().forEach((code, response) -> this.response(code, response));
        }
        return this;
    }
}