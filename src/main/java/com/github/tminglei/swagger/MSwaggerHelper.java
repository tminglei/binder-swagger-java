package com.github.tminglei.swagger;

import com.github.tminglei.bind.Framework;
import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Response;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.*;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.tminglei.swagger.SwaggerExtensions.*;
import static com.github.tminglei.swagger.SwaggerUtils.*;

/**
 * Helper class to build swagger elements from `com.github.tminglei.bind.Framework.Mapping`
 */
public class MSwaggerHelper {

    public List<Parameter> mToParameters(String name, Framework.Mapping<?> mapping) {
        if ("body".equalsIgnoreCase( ext(mapping).in() )) {
            return Arrays.asList(new BodyParameter()
                    .schema(mToModel(mapping))
                    .name(name)
                    .description(ext(mapping).desc()));
        }

        if (isEmpty( ext(mapping).in() ) && mapping instanceof Framework.GroupMapping) {
            return ((Framework.GroupMapping) mapping).fields().stream().flatMap(m -> {
                if (isEmpty(ext(m.getValue()).in())) throw new IllegalArgumentException("in is required!!!");
                return mToParameters(mergedName(name, m.getKey()), m.getValue()).stream();
            }).collect(Collectors.toList());
        }

        if (isEmpty(ext(mapping).in())) throw new IllegalArgumentException("in is required!!!");

        if (mapping instanceof Framework.GroupMapping) {
            return ((Framework.GroupMapping) mapping).fields().stream().flatMap(m -> {
                Framework.Mapping<?> fMapping = m.getValue().$ext(e -> ext(e).merge(ext(mapping)));
                return mToParameters(mergedName(name, m.getKey()), fMapping).stream();
            }).collect(Collectors.toList());
        } else {
            if (!isPrimitive(mapping, true)) throw new IllegalArgumentException("must be primitives or primitive list!!!");
            return Arrays.asList(mToParameter(name, mapping));
        }

    }

    public Parameter mToParameter(String name, Framework.Mapping<?> mapping) {
        if (isEmpty( ext(mapping).in() )) throw new IllegalArgumentException("in is required!!!");

        if ("body".equalsIgnoreCase(ext(mapping).in())) {
            return new BodyParameter()
                    .schema(mToModel(mapping))
                    .name(name)
                    .description(ext(mapping).desc());
        }

        if (isEmpty(name)) throw new IllegalArgumentException("name is required!!!");
        if (!isPrimitive(mapping, true)) throw new IllegalArgumentException("must be primitives or primitive list!!!");

        if ("form".equalsIgnoreCase( ext(mapping).in() )) {
            return fillParameter(new FormParameter(), mapping).name(name);
        }
        if ("path".equalsIgnoreCase( ext(mapping).in() )) {
            return fillParameter(new PathParameter(), mapping).name(name).required(true);
        }
        if ("query".equalsIgnoreCase( ext(mapping).in() )) {
            return fillParameter(new QueryParameter(), mapping).name(name);
        }
        if ("cookie".equalsIgnoreCase(ext(mapping).in())) {
            return fillParameter(new CookieParameter(), mapping).name(name);
        }
        if ("header".equalsIgnoreCase( ext(mapping).in() )) {
            return fillParameter(new HeaderParameter(), mapping).name(name);
        }

        throw new IllegalArgumentException("Unsupported in type: '" + ext(mapping).in() + "'!!!");
    }

    protected AbstractSerializableParameter fillParameter(AbstractSerializableParameter parameter, Framework.Mapping<?> mapping) {
        parameter = parameter.type(targetType(mapping))
                .format(format(mapping))
                .required(required(mapping))
                .description(ext(mapping).desc())
                .items(items(mapping))
                ._enum(enums(mapping));

        parameter.setPattern(pattern(mapping));
        parameter.setMaximum(maximum(mapping));
        parameter.setExclusiveMaximum(exclusiveMaximum(mapping));
        parameter.setMinimum(minimum(mapping));
        parameter.setExclusiveMinimum(exclusiveMinimum(mapping));

        return parameter;
    }

    public Model mToModel(Framework.Mapping<?> mapping) {
        if (mapping.meta().targetType == List.class) {
            ArrayModel model = new ArrayModel()
                    .description(ext(mapping).desc())
                    .items(mToProperty(mapping.meta().baseMappings[0]));
            model.setExample(ext(mapping).example());
            return model;
        }

        ModelImpl model = new ModelImpl()
                .type(targetType(mapping))
                .format(format(mapping))
                .description(ext(mapping).desc())
                .example(ext(mapping).example());

        if (mapping instanceof Framework.GroupMapping) {
            ((Framework.GroupMapping) mapping).fields().forEach(m -> {
                model.addProperty(m.getKey(), mToProperty(m.getValue()));
                if (required(m.getValue())) model.required(m.getKey());
            });
        }

        return model;
    }

    public Response mToResponse(Framework.Mapping<?> mapping) {
        return new Response().schema(mToProperty(mapping))
                .description(ext(mapping).desc());
    }

    public Property mToProperty(Framework.Mapping<?> mapping) {
        if (mapping instanceof Framework.GroupMapping) {
            Map<String, Property> properties = ((Framework.GroupMapping) mapping).fields().stream()
                    .map(m -> entry(m.getKey(), mToProperty(m.getValue())))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue
                    ));
            return fillProperty(new ObjectProperty(properties), mapping);
        }

        if (mapping.meta().targetType == Map.class) {
            Property vProperty = mToProperty(mapping.meta().baseMappings[1]);
            return fillProperty(new MapProperty(vProperty), mapping);
        }
        if (mapping.meta().targetType == List.class) {
            Property iProperty = mToProperty(mapping.meta().baseMappings[0]);
            return fillProperty(new ArrayProperty(iProperty), mapping);
        }
        if (mapping.meta().targetType == Optional.class) {
            Property bProperty = mToProperty(mapping.meta().baseMappings[0]
                    .options(o -> o.append_constraints(mapping.options()._constraints())));
            bProperty.setRequired(false);
            return bProperty;
        }

        ///
        if (mapping.meta().targetType == Boolean.class) {
            return fillProperty(new BooleanProperty(), mapping);
        }
        if (mapping.meta().targetType == Byte[].class) {
            return fillProperty(new ByteArrayProperty(), mapping);
        }
        if (mapping.meta().targetType == LocalDate.class) {
            return fillProperty(new DateProperty(), mapping);
        }
        if (mapping.meta().targetType == LocalDateTime.class) {
            return fillProperty(new DateTimeProperty(), mapping);
        }
        if (mapping.meta().targetType == UUID.class) {
            return fillProperty(new UUIDProperty(), mapping);
        }
        if (mapping.meta().targetType == File.class) {
            return fillProperty(new FileProperty(), mapping);
        }

        if (mapping.meta().targetType == Integer.class) {
            return fillProperty(new IntegerProperty(), mapping);
        }
        if (mapping.meta().targetType == Long.class) {
            return fillProperty(new LongProperty(), mapping);
        }
        if (mapping.meta().targetType == Float.class) {
            return fillProperty(new FloatProperty(), mapping);
        }
        if (mapping.meta().targetType == Double.class) {
            return fillProperty(new DoubleProperty(), mapping);
        }

        if ("string".equalsIgnoreCase(targetType(mapping))) {
            return fillProperty(new StringProperty(), mapping);
        }

        throw new IllegalArgumentException("Unsupported target type: " + mapping.meta().targetType);
    }

    protected AbstractProperty fillProperty(AbstractProperty property, Framework.Mapping<?> mapping) {
        property.setTitle(mapping.options()._label().orElse(""));
        property.setDescription(ext(mapping).desc());
        property.setFormat(format(mapping));
        property.setRequired(required(mapping));
        property.setExample(ext(mapping).example() != null ? ext(mapping).example().toString() : null);

        if (property instanceof AbstractNumericProperty) {
            AbstractNumericProperty numericProperty = (AbstractNumericProperty) property;
            numericProperty.setMaximum(maximum(mapping));
            numericProperty.setExclusiveMaximum(exclusiveMaximum(mapping));
            numericProperty.setMinimum(minimum(mapping));
            numericProperty.setExclusiveMinimum(exclusiveMinimum(mapping));
        }

        if (property instanceof StringProperty) {
            StringProperty stringProperty = (StringProperty) property;
            stringProperty.setPattern(pattern(mapping));
            stringProperty.setEnum(enums(mapping));
            stringProperty.setMaxLength(maxLength(mapping));
            stringProperty.setMinLength(minLength(mapping));
        }

        return property;
    }

    ///
    protected boolean isPrimitive(Framework.Mapping<?> mapping, boolean top) {
        if (mapping instanceof Framework.GroupMapping)
            return false;
        if (mapping.meta().targetType == Map.class)
            return false;
        if (mapping.meta().targetType == List.class) {
            return top && isPrimitive(mapping.meta().baseMappings[0], false);
        }
        if (mapping.meta().targetType == Optional.class) {
            return top && isPrimitive(mapping.meta().baseMappings[0], false);
        }

        return true;
    }

    protected String targetType(Framework.Mapping<?> mapping) {
        if (mapping instanceof Framework.GroupMapping
                || mapping.meta().targetType == Map.class) {
            return "object";
        }
        if (mapping.meta().targetType == List.class) {
            return "array";
        }

        if (mapping.meta().targetType == String.class
                || mapping.meta().targetType == Byte.class
                || mapping.meta().targetType == Byte[].class
                || mapping.meta().targetType == LocalDate.class
                || mapping.meta().targetType == LocalDateTime.class
                || mapping.meta().targetType == LocalTime.class
                || mapping.meta().targetType == UUID.class
                || mapping.meta().targetType == URL.class) {
            return "string";
        }
        if (mapping.meta().targetType == Integer.class
                || mapping.meta().targetType == Long.class
                || mapping.meta().targetType == BigInteger.class) {
            return "integer";
        }
        if (mapping.meta().targetType == Float.class
                || mapping.meta().targetType == Double.class
                || mapping.meta().targetType == BigDecimal.class) {
            return "number";
        }
        if (mapping.meta().targetType == Boolean.class) {
            return "boolean";
        }

        if (mapping.meta().targetType == File.class) {
            return "file";
        }

        throw new IllegalArgumentException("Unsupported target type: " + mapping.meta().targetType);
    }

    protected String format(Framework.Mapping<?> mapping) {
        if (mapping.meta().targetType == Byte.class)
            return "byte";
        if (mapping.meta().targetType == Byte[].class)
            return "binary";
        if (mapping.meta().targetType == LocalDate.class)
            return "date";
        if (mapping.meta().targetType == LocalDateTime.class)
            return "date-time";
        if (mapping.meta().targetType == LocalTime.class)
            return "time";
        if (mapping.meta().targetType == UUID.class)
            return "uuid";
        if (mapping.meta().targetType == URL.class)
            return "url";
        if (mapping.meta().targetType == String.class && findConstraint(mapping, "email") != null)
            return "email";
        if (mapping.meta().targetType == Integer.class)
            return "int32";
        if (mapping.meta().targetType == Long.class || mapping.meta().targetType == BigInteger.class)
            return "int64";
        if (mapping.meta().targetType == Float.class)
            return "float";
        if (mapping.meta().targetType == Double.class || mapping.meta().targetType == BigDecimal.class) {
            return "double";
        }

        return ext(mapping).format();
    }

    protected boolean required(Framework.Mapping<?> mapping) {
        if (mapping.meta().targetType == Optional.class) return false;
        else return findConstraint(mapping, "required") != null;
    }

    protected Property items(Framework.Mapping<?> mapping) {
        if (mapping.meta().targetType == List.class) {
            return mToProperty(mapping.meta().baseMappings[0]);
        } else return null;
    }

    protected List<String> enums(Framework.Mapping<?> mapping) {
        Framework.Constraint oneOf = findConstraint(mapping, "oneOf");
        return oneOf != null ? new ArrayList<>((Collection<String>) oneOf.meta().params.get(0)) : null;
    }

    protected String pattern(Framework.Mapping<?> mapping) {
        Framework.Constraint pattern = findConstraint(mapping, "pattern");
        return pattern != null ? (String) pattern.meta().params.get(0) : null;
    }

    protected Double maximum(Framework.Mapping<?> mapping) {
        Framework.ExtraConstraint<?> max = findExtraConstraint(mapping, "max");
        return max != null && max.meta().params.get(0) instanceof Number
                ? ((Number) max.meta().params.get(0)) .doubleValue()
                : null;
    }

    protected Boolean exclusiveMaximum(Framework.Mapping<?> mapping) {
        Framework.ExtraConstraint<?> max = findExtraConstraint(mapping, "max");
        return max != null ? (Boolean) max.meta().params.get(1) : null;
    }

    protected Double minimum(Framework.Mapping<?> mapping) {
        Framework.ExtraConstraint<?> min = findExtraConstraint(mapping, "min");
        return min != null && min.meta().params.get(0) instanceof Number
                ? ((Number) min.meta().params.get(0)) .doubleValue()
                : null;
    }

    protected Boolean exclusiveMinimum(Framework.Mapping<?> mapping) {
        Framework.ExtraConstraint<?> max = findExtraConstraint(mapping, "min");
        return max != null ? (Boolean) max.meta().params.get(1) : null;
    }

    protected Integer maxLength(Framework.Mapping<?> mapping) {
        Framework.Constraint maxlen = findConstraint(mapping, "maxLength");
        return maxlen != null ? (Integer) maxlen.meta().params.get(0) : null;
    }

    protected Integer minLength(Framework.Mapping<?> mapping) {
        Framework.Constraint minlen = findConstraint(mapping, "minLength");
        return minlen != null ? (Integer) minlen.meta().params.get(0) : null;
    }

    ///
    protected String mergedName(String parent, String current) {
        return isEmpty(parent) ? current : parent + "." + current;
    }

    protected Framework.Constraint findConstraint(Framework.Mapping<?> mapping, String name) {
        for(Framework.Constraint c : mapping.options()._constraints()) {
            if (c.meta() != null && c.meta().name.equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    protected Framework.ExtraConstraint<?> findExtraConstraint(Framework.Mapping<?> mapping, String name) {
        for(Framework.ExtraConstraint<?> c : mapping.options()._extraConstraints()) {
            if (c.meta() != null && c.meta().name.equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

}
