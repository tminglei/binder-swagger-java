package com.github.tminglei.swagger;

import com.github.tminglei.bind.Framework;
import com.github.tminglei.bind.spi.Constraint;
import com.github.tminglei.bind.spi.ExtraConstraint;
import io.swagger.models.*;
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

import static com.github.tminglei.swagger.Attachment.*;
import static com.github.tminglei.swagger.SwaggerUtils.*;
import static com.github.tminglei.bind.OptionsOps.*;

/**
 * Helper class to build swagger elements from `com.github.tminglei.bind.Framework.Mapping`
 */
public class MSwaggerHelper {

    public List<Parameter> mToParameters(String name, Framework.Mapping<?> mapping) {
        if ("body".equalsIgnoreCase( attach(mapping).in() )) {
            return Arrays.asList(new BodyParameter()
                    .schema(mToModel(mapping))
                    .name(name)
                    .description(attach(mapping).desc()));
        }

        if (isEmpty( attach(mapping).in() ) && mapping instanceof Framework.GroupMapping) {
            return ((Framework.GroupMapping) mapping).fields().stream().flatMap(m -> {
                if (isEmpty(attach(m.getValue()).in())) throw new IllegalArgumentException("in is required!!!");
                return mToParameters(mergedName(name, m.getKey()), m.getValue()).stream();
            }).collect(Collectors.toList());
        }

        if (isEmpty(attach(mapping).in())) throw new IllegalArgumentException("in is required!!!");

        if (mapping instanceof Framework.GroupMapping) {
            return ((Framework.GroupMapping) mapping).fields().stream().flatMap(m -> {
                Framework.Mapping<?> fMapping = mergeAttach(m.getValue(), attach(mapping));
                return mToParameters(mergedName(name, m.getKey()), fMapping).stream();
            }).collect(Collectors.toList());
        } else {
            if (!isPrimitive(mapping)) throw new IllegalArgumentException("must be primitives or primitive list!!!");
            return Arrays.asList(mToParameter(name, mapping));
        }

    }

    public Parameter mToParameter(String name, Framework.Mapping<?> mapping) {
        if (isEmpty( attach(mapping).in() )) throw new IllegalArgumentException("in is required!!!");

        if ("body".equalsIgnoreCase(attach(mapping).in())) {
            return new BodyParameter()
                    .schema(mToModel(mapping))
                    .name(name)
                    .description(attach(mapping).desc());
        }

        if (isEmpty(name)) throw new IllegalArgumentException("name is required!!!");
        if (!isPrimitive(mapping)) throw new IllegalArgumentException("must be primitives or primitive list!!!");

        if ("form".equalsIgnoreCase( attach(mapping).in() ) || "formData".equalsIgnoreCase( attach(mapping).in() )) {
            return fillParameter(new FormParameter(), mapping).name(name);
        }
        if ("path".equalsIgnoreCase( attach(mapping).in() )) {
            return fillParameter(new PathParameter(), mapping).name(name).required(true);
        }
        if ("query".equalsIgnoreCase( attach(mapping).in() )) {
            return fillParameter(new QueryParameter(), mapping).name(name);
        }
        if ("cookie".equalsIgnoreCase( attach(mapping).in())) {
            return fillParameter(new CookieParameter(), mapping).name(name);
        }
        if ("header".equalsIgnoreCase( attach(mapping).in() )) {
            return fillParameter(new HeaderParameter(), mapping).name(name);
        }

        throw new IllegalArgumentException("Unsupported in type: '" + attach(mapping).in() + "'!!!");
    }

    protected AbstractSerializableParameter fillParameter(AbstractSerializableParameter parameter, Framework.Mapping<?> mapping) {
        parameter = parameter.type(targetType(mapping))
                .format(format(mapping))
                .required(required(mapping))
                .description(attach(mapping).desc())
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
        String refName = attach(mapping).refName();
        if (!isEmpty(refName)) {
            return new RefModel(refName);
        }

        if (mapping.meta().targetType == List.class) {
            ArrayModel model = new ArrayModel()
                    .description(attach(mapping).desc())
                    .items(items(mapping));
            model.setExample(attach(mapping).example());
            return model;
        }

        ModelImpl model = new ModelImpl()
                .type(targetType(mapping))
                .format(format(mapping))
                .description(attach(mapping).desc())
                .example(attach(mapping).example());

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
                .description(attach(mapping).desc());
    }

    public Property mToProperty(Framework.Mapping<?> mapping) {
        String refName = attach(mapping).refName();
        if (!isEmpty(refName)) {
            return new RefProperty(refName);
        }

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
                    .options(o -> append_constraints(o, _constraints(mapping.options()))));
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
        property.setTitle(_label(mapping.options()).orElse(""));
        property.setDescription(attach(mapping).desc());
        property.setFormat(format(mapping));
        property.setRequired(required(mapping));
        property.setExample(attach(mapping).example() != null ? attach(mapping).example().toString() : null);

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

    public List<Map.Entry<String, Model>> scanModels(Framework.Mapping<?> mapping) {
        List<Map.Entry<String, Model>> models = new ArrayList<>();

        String refName = attach(mapping).refName();
        if (!isEmpty(refName)) {
            Framework.Mapping<?> mappingNoRef = new Attachment.Builder(mapping).refName(null).$$;
            models.add(entry(refName, mToModel(mappingNoRef)));
        }

        if (mapping instanceof Framework.GroupMapping) {
            ((Framework.GroupMapping) mapping).fields()
                    .forEach(m -> models.addAll(scanModels(m.getValue())));
        }
        if (mapping.meta().targetType == Map.class) {
            models.addAll(scanModels(mapping.meta().baseMappings[1]));
        }
        if (mapping.meta().targetType == List.class) {
            models.addAll(scanModels(mapping.meta().baseMappings[0]));
        }
        if (mapping.meta().targetType == Optional.class) {
            models.addAll(scanModels(mapping.meta().baseMappings[0]));
        }

        return models;
    }

    ///
    protected boolean isPrimitive(Framework.Mapping<?> mapping) {
        return isPrimitive(mapping, true);
    }
    protected boolean isPrimitive(Framework.Mapping<?> mapping, boolean isTop) {
        if (mapping instanceof Framework.GroupMapping)
            return false;
        if (mapping.meta().targetType == Map.class)
            return false;
        if (mapping.meta().targetType == List.class) {
            return isTop && isPrimitive(mapping.meta().baseMappings[0], false);
        }
        if (mapping.meta().targetType == Optional.class) {
            return isTop && isPrimitive(mapping.meta().baseMappings[0], false);
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

        return attach(mapping).format();
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
        Constraint oneOf = findConstraint(mapping, "oneOf");
        return oneOf != null ? new ArrayList<>((Collection<String>) oneOf.meta().params.get(0)) : null;
    }

    protected String pattern(Framework.Mapping<?> mapping) {
        Constraint pattern = findConstraint(mapping, "pattern");
        return pattern != null ? (String) pattern.meta().params.get(0) : null;
    }

    protected Double maximum(Framework.Mapping<?> mapping) {
        ExtraConstraint<?> max = findExtraConstraint(mapping, "max");
        return max != null && max.meta().params.get(0) instanceof Number
                ? ((Number) max.meta().params.get(0)) .doubleValue()
                : null;
    }

    protected Boolean exclusiveMaximum(Framework.Mapping<?> mapping) {
        ExtraConstraint<?> max = findExtraConstraint(mapping, "max");
        return max != null ? (Boolean) max.meta().params.get(1) : null;
    }

    protected Double minimum(Framework.Mapping<?> mapping) {
        ExtraConstraint<?> min = findExtraConstraint(mapping, "min");
        return min != null && min.meta().params.get(0) instanceof Number
                ? ((Number) min.meta().params.get(0)) .doubleValue()
                : null;
    }

    protected Boolean exclusiveMinimum(Framework.Mapping<?> mapping) {
        ExtraConstraint<?> max = findExtraConstraint(mapping, "min");
        return max != null ? (Boolean) max.meta().params.get(1) : null;
    }

    protected Integer maxLength(Framework.Mapping<?> mapping) {
        Constraint maxlen = findConstraint(mapping, "maxLength");
        return maxlen != null ? (Integer) maxlen.meta().params.get(0) : null;
    }

    protected Integer minLength(Framework.Mapping<?> mapping) {
        Constraint minlen = findConstraint(mapping, "minLength");
        return minlen != null ? (Integer) minlen.meta().params.get(0) : null;
    }

    ///
    protected String mergedName(String parent, String current) {
        return isEmpty(parent) ? current : parent + "." + current;
    }

    protected Constraint findConstraint(Framework.Mapping<?> mapping, String name) {
        for(Constraint c : _constraints(mapping.options())) {
            if (c.meta() != null && c.meta().name.equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    protected ExtraConstraint<?> findExtraConstraint(Framework.Mapping<?> mapping, String name) {
        for(ExtraConstraint<?> c : _extraConstraints(mapping.options())) {
            if (c.meta() != null && c.meta().name.equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

}
