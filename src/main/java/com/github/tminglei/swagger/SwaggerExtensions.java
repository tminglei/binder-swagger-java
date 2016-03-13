package com.github.tminglei.swagger;

import com.github.tminglei.bind.Framework;
import com.github.tminglei.bind.spi.Extensible;

import static com.github.tminglei.bind.OptionsOps.*;

/**
 * Extension class to be used to associate extra data to a `com.github.tminglei.bind.Framework.Mapping`
 */
public class SwaggerExtensions implements Extensible {
    private String in;
    private String desc;
    private String format;
    private Object example;
    private String refName;

    public String in() {
        return this.in;
    }
    public SwaggerExtensions in(String in) {
        this.in = in;
        return this;
    }

    public String desc() {
        return this.desc;
    }
    public SwaggerExtensions desc(String desc) {
        this.desc = desc;
        return this;
    }

    public String format() {
        return this.format;
    }
    public SwaggerExtensions format(String format) {
        this.format = format;
        return this;
    }

    public Object example() {
        return this.example;
    }
    public SwaggerExtensions example(Object example) {
        this.example = example;
        return this;
    }

    public String refName() {
        return this.refName;
    }
    public SwaggerExtensions refName(String refName) {
        this.refName = refName;
        return this;
    }

    ///
    public SwaggerExtensions merge(SwaggerExtensions other) {
        SwaggerExtensions merged = this.clone();
        merged.in = other.in;
        return merged;
    }

    @Override
    public SwaggerExtensions clone() {
        SwaggerExtensions clone = new SwaggerExtensions();
        clone.in = this.in;
        clone.desc = this.desc;
        clone.format = this.format;
        clone.example = this.example;
        clone.refName = this.refName;
        return clone;
    }

    /// --- static helper methods --------
    public static SwaggerExtensions ext(Extensible ext) {
        return ext != null ? (SwaggerExtensions) ext : new SwaggerExtensions();
    }
    public static SwaggerExtensions ext(Framework.Mapping<?> mapping) {
        return ext(_ext(mapping.options()));
    }
}