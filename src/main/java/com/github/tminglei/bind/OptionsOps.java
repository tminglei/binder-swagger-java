package com.github.tminglei.bind;

import com.github.tminglei.bind.spi.Constraint;
import com.github.tminglei.bind.spi.ExtraConstraint;

import java.util.List;
import java.util.Optional;

/**
 * Util methods to access `form-binder-java` [[Options]]'s non public properties
 */
public class OptionsOps {

    public static Optional<String> _label(Options o) {
        return o._label();
    }

    public static List<Constraint> _constraints(Options o) {
        return o._constraints();
    }

    public static Options append_constraints(Options o, List<Constraint> constraints) {
        return o.append_constraints(constraints);
    }

    public static <T> List<ExtraConstraint<T>> _extraConstraints(Options o) {
        return o._extraConstraints();
    }

    public static Object _attachment(Options o) {
        return o._attachment();
    }

    public static Options _attachment(Options o, Object attachment) {
        return o._attachment(attachment);
    }
}
