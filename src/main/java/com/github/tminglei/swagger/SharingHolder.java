package com.github.tminglei.swagger;

import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.parameters.Parameter;

import java.util.*;

/**
 * Used to hold sharing configurations (unmodifiable)
 */
public class SharingHolder {
    private String commonPath = "";
    private List<String> tags = new ArrayList<>();
    private List<Scheme> schemes = new ArrayList<>();
    private List<String> consumes = new ArrayList<>();
    private List<String> produces = new ArrayList<>();
    private Map<String, List<String>> securities = new HashMap<>();
    private List<Parameter> params = new ArrayList<>();
    private Map<Integer, Response> responses = new HashMap<>();

    ///
    public String commonPath() {
        return this.commonPath;
    }
    public SharingHolder commonPath(String path) {
        SharingHolder clone = this.clone();
        clone.commonPath = path;
        return clone;
    }
    public SharingHolder reCommonPath() {
        return commonPath("");
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
    public SharingHolder tag(String tag) {
        SharingHolder clone = this.clone();
        clone.tags.add(tag);
        return clone;
    }
    public SharingHolder reTags() {
        return tags(new ArrayList<>());
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
    public SharingHolder scheme(Scheme scheme) {
        SharingHolder clone = this.clone();
        clone.schemes.add(scheme);
        return clone;
    }
    public SharingHolder reSchemes() {
        return schemes(new ArrayList<>());
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
    public SharingHolder consume(String consume) {
        SharingHolder clone = this.clone();
        clone.consumes.add(consume);
        return clone;
    }
    public SharingHolder reConsumes() {
        return consumes(new ArrayList<>());
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
    public SharingHolder produce(String produce) {
        SharingHolder clone = this.clone();
        clone.produces.add(produce);
        return clone;
    }
    public SharingHolder reProduces() {
        return produces(new ArrayList<>());
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
    public SharingHolder reSecurities() {
        return securities(new HashMap<>());
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
    public SharingHolder parameter(Parameter param) {
        SharingHolder clone = this.clone();
        clone.params.add(param);
        return clone;
    }
    public SharingHolder reParameters() {
        return parameters(new ArrayList<>());
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
    public SharingHolder reResponses() {
        return responses(new HashMap<>());
    }

    ///
    @Override
    protected SharingHolder clone() {
        SharingHolder clone = new SharingHolder();
        clone.commonPath = this.commonPath;
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
