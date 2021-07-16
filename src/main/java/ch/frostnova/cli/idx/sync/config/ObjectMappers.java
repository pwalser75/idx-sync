package ch.frostnova.cli.idx.sync.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT;
import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.MINIMIZE_QUOTES;

public class ObjectMappers {

    enum Type {
        JSON, //  JavaScript Object Notation
        YAML, //  Yet Another Markup Language
        XML, //  eXtensible Markup Language
        PROPERTIES, // Java Properties format
        CBOR // Concise Binary Object Representation (https://www.rfc-editor.org/info/rfc7049)
    }

    private static final Map<Type, ObjectMapper> objectMappers = new ConcurrentHashMap<>();

    private ObjectMappers() {

    }

    public static ObjectMapper json() {
        return objectMappers.computeIfAbsent(Type.JSON, type -> {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
            return configure(mapper);
        });
    }

    public static ObjectMapper yaml() {
        return objectMappers.computeIfAbsent(Type.YAML, type -> {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory().enable(MINIMIZE_QUOTES));
            mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
            return configure(mapper);
        });
    }

    public static ObjectMapper configure(ObjectMapper mapper) {
        return mapper
                .registerModule(new JavaTimeModule())
                .setDateFormat(new StdDateFormat())
                .enable(INDENT_OUTPUT)
                .enable(ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .enable(ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .disable(WRITE_DATES_AS_TIMESTAMPS)
                .disable(WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
                .disable(FAIL_ON_UNKNOWN_PROPERTIES)
                .setSerializationInclusion(NON_EMPTY);
    }
}
