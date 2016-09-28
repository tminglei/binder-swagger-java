package com.github.tminglei.swagger;

import com.github.tminglei.bind.Framework;

import static com.github.tminglei.bind.OptionsOps.*;

/**
 * Extension class to be used to associate extra data to a `com.github.tminglei.bind.Framework.Mapping`
 */
public class Attachment {
    public static final Attachment NULL_OBJECT = new Attachment();

    private String in;
    private String desc;
    private String format;
    private Object example;
    private String refName;

    public String in() {
        return this.in;
    }

    public String desc() {
        return this.desc;
    }

    public String format() {
        return this.format;
    }

    public Object example() {
        return this.example;
    }

    public String refName() {
        return this.refName;
    }

    ///

    protected Attachment clone() {
        Attachment clone = new Attachment();
        clone.in = this.in;
        clone.desc = this.desc;
        clone.format = this.format;
        clone.example = this.example;
        clone.refName = this.refName;
        return clone;
    }

    ///---

    public static <T> Attachment.Builder<T> $(Framework.Mapping<T> mapping) {
        return new Attachment.Builder<>(mapping);
    }

    public static <T> Attachment attach(Framework.Mapping<T> mapping) {
        return _attachment(mapping.options()) == null ? NULL_OBJECT : (Attachment) _attachment(mapping.options());
    }

    public static <T> Framework.Mapping<T> mergeAttach(Framework.Mapping<T> mapping, Attachment other) {
        Attachment merged = attach(mapping).clone();
        merged.in = other.in;
        return mapping.options(o -> _attachment(o, merged));
    }

    ///---

    public static class Builder<T> {
        public final Framework.Mapping<T> $$;
        private final Attachment _attach;

        Builder(Framework.Mapping<T> mapping) {
            this._attach = attach(mapping).clone();
            this.$$ = mapping.options(o -> _attachment(o, _attach));
        }

        public Builder<T> in(String in) {
            Builder<T> clone = new Builder<>($$);
            clone._attach.in = in;
            return clone;
        }

        public Builder<T> desc(String desc) {
            Builder<T> clone = new Builder<>($$);
            clone._attach.desc = desc;
            return clone;
        }

        public Builder<T> format(String format) {
            Builder<T> clone = new Builder<>($$);
            clone._attach.format = format;
            return clone;
        }

        public Builder<T> example(Object example) {
            Builder<T> clone = new Builder<>($$);
            clone._attach.example = example;
            return clone;
        }

        public Builder<T> refName(String refName) {
            Builder<T> clone = new Builder<>($$);
            clone._attach.refName = refName;
            return clone;
        }
    }
}