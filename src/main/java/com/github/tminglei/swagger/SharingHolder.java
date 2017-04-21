package com.github.tminglei.swagger;

import com.github.tminglei.swagger.bind.MParamBuilder;
import io.swagger.models.HttpMethod;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.parameters.Parameter;

import java.nio.file.Paths;
import java.util.*;

/**
 * Used to hold sharing configurations (implemented as copy-and-write)
 */
public class SharingHolder {
    private SwaggerContext context;

    private String pathPrefix = "";
    private List<String> tags = new ArrayList<>();
    private List<Scheme> schemes = new ArrayList<>();
    private List<String> consumes = new ArrayList<>();
    private List<String> produces = new ArrayList<>();
    private Map<String, List<String>> securities = new HashMap<>();
    private List<Parameter> params = new ArrayList<>();
    private Map<Integer, Response> responses = new HashMap<>();

    public SharingHolder(SwaggerContext context) {
        this.context = context;
    }

    ///
    public String pathPrefix() {
        return this.pathPrefix;
    }
    public SharingHolder pathPrefix(String path) {
        SharingHolder clone = this.clone();
        clone.pathPrefix = path;
        return clone;
    }

    ///
    public List<String> tags() {
        return Collections.unmodifiableList(this.tags);
    }
    public SharingHolder tags(List<String> tags) {
        SharingHolder clone = this.clone();
        clone.tags = tags != null ? tags : new ArrayList<>();
        return clone;
    }
    public SharingHolder tag(String... tags) {
        SharingHolder clone = this.clone();
        for (String tag : tags)
            clone.tags.add(tag);
        return clone;
    }

    ///
    public List<Scheme> schemes() {
        return Collections.unmodifiableList(this.schemes);
    }
    public SharingHolder schemes(List<Scheme> schemes) {
        SharingHolder clone = this.clone();
        clone.schemes = schemes != null ? schemes : new ArrayList<>();
        return clone;
    }
    public SharingHolder scheme(Scheme... schemes) {
        SharingHolder clone = this.clone();
        for (Scheme scheme : schemes)
            clone.schemes.add(scheme);
        return clone;
    }

    ///
    public List<String> consumes() {
        return Collections.unmodifiableList(this.consumes);
    }
    public SharingHolder consumes(List<String> consumes) {
        SharingHolder clone = this.clone();
        clone.consumes = consumes != null ? consumes : new ArrayList<>();
        return clone;
    }
    public SharingHolder consume(String... consumes) {
        SharingHolder clone = this.clone();
        for (String consume : consumes)
            clone.consumes.add(consume);
        return clone;
    }

    ///
    public List<String> produces() {
        return Collections.unmodifiableList(this.produces);
    }
    public SharingHolder produces(List<String> produces) {
        SharingHolder clone = this.clone();
        clone.produces = produces != null ? produces : new ArrayList<>();
        return clone;
    }
    public SharingHolder produce(String... produces) {
        SharingHolder clone = this.clone();
        for (String produce : produces)
            clone.produces.add(produce);
        return clone;
    }

    ///
    public Map<String, List<String>> securities() {
        return Collections.unmodifiableMap(this.securities);
    }
    public SharingHolder securities(Map<String, List<String>> securities) {
        SharingHolder clone = this.clone();
        clone.securities = securities != null ? securities : new HashMap<>();
        return clone;
    }
    public SharingHolder security(String name, List<String> scopes) {
        SharingHolder clone = this.clone();
        scopes = scopes != null ? scopes : new ArrayList<>();
        clone.securities.put(name, scopes);
        return clone;
    }

    ///
    public List<Parameter> parameters() {
        return Collections.unmodifiableList(this.params);
    }
    public SharingHolder parameters(List<Parameter> params) {
        SharingHolder clone = this.clone();
        clone.params = params != null ? params : new ArrayList<>();
        return clone;
    }
    public SharingHolder parameter(MParamBuilder builder) {
        SharingHolder clone = this.clone();
        builder.build().forEach(p -> clone.params.add(p));
        return clone;
    }
    public SharingHolder parameter(Parameter param) {
        SharingHolder clone = this.clone();
        clone.params.add(param);
        return clone;
    }

    ///
    public Map<Integer, Response> responses() {
        return Collections.unmodifiableMap(this.responses);
    }
    public SharingHolder responses(Map<Integer, Response> responses) {
        SharingHolder clone = this.clone();
        clone.responses = responses != null ? responses : new HashMap<>();
        return clone;
    }
    public SharingHolder response(int code, Response response) {
        SharingHolder clone = this.clone();
        clone.responses.put(code, response);
        return clone;
    }

    ///
    public ExOperation operation(HttpMethod method, String path) {
        path = Paths.get(pathPrefix, path).toString();
        ExOperation operation = context.operation(method, path);
        operation.setTags(new ArrayList<>(tags));   // !!!NOTE: use clone object here
        operation.setSchemes(new ArrayList<>(schemes));
        operation.setConsumes(new ArrayList<>(consumes));
        operation.setProduces(new ArrayList<>(produces));
        securities.forEach((name, scopes) -> operation.security(name, new ArrayList<>(scopes)));
        operation.setParameters(new ArrayList<>(params));
        responses.forEach((code, response) -> operation.response(code, response));
        return operation;
    }

    ///
    @Override
    protected SharingHolder clone() {
        SharingHolder clone = new SharingHolder(context);
        clone.pathPrefix = this.pathPrefix;
        clone.tags = new ArrayList<>(this.tags);
        clone.params = new ArrayList<>(this.params);
        clone.responses = new HashMap<>(this.responses);
        clone.schemes = new ArrayList<>(this.schemes);
        clone.consumes = new ArrayList<>(this.consumes);
        clone.produces = new ArrayList<>(this.produces);
        clone.securities = new HashMap<>(this.securities);
        return clone;
    }
}
