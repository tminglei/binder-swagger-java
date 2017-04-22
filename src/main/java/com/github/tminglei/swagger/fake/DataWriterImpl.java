package com.github.tminglei.swagger.fake;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

import static com.github.tminglei.swagger.SimpleUtils.isEmpty;

/**
 * Created by minglei on 4/17/17.
 */
public class DataWriterImpl implements DataWriter {
    private static final String FORMAT_JSON = "application/json";
    private static final String FORMAT_XML  = "application/xml";

    @Override
    public void write(Writer writer, String format, DataProvider provider) {
        try {
            switch (format.toLowerCase()) {
                case FORMAT_JSON:
                    String dataJson = new ObjectMapper()
                        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                        .writer().withDefaultPrettyPrinter()
                        .writeValueAsString(provider.get());
                    writer.write(dataJson);
                    break;
                case FORMAT_XML:
                    toXml(writer, provider.get(), provider.name(), 0);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported format: " + format);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    ///---

    private void toXml(Writer writer, Object obj, String name, int level) throws IOException {
        if (isEmpty(obj)) {
            emptyNode(writer, name, level);
        } else if (obj instanceof Map) {
            startNode(writer, name, level);
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) obj).entrySet()) {
                toXml(writer, entry.getValue(), entry.getKey(), level+1);
            }
            endNode(writer, name, level);
        } else if (obj instanceof Collection) {
            startNode(writer, name, level);
            String cname = name.endsWith("s") ? name.substring(0, name.length() -1) : "value";
            for (Object item : (Collection) obj) {
                toXml(writer, item, cname, level+1);
            }
            endNode(writer, name, level);
        } else {
            startNode(writer, name, level);
            writer.write(obj.toString());
            endNode(writer, name, -1);
        }
    }

    private void emptyNode(Writer writer, String name, int level) throws IOException {
        indentWithNewLine(writer, level);
        writer.write("<" + name + "/>");
    }

    private void startNode(Writer writer, String name, int level) throws IOException {
        indentWithNewLine(writer, level);
        writer.write("<" + name + ">");
    }

    private void endNode(Writer writer, String name, int level) throws IOException {
        indentWithNewLine(writer, level);
        writer.write("</" + name + ">");
    }

    private void indentWithNewLine(Writer writer, int level) throws IOException {
        if (level >= 0) {
            writer.write("\n");
            for (int i=0; i < 2*level; i++) {
                writer.write(" ");
            }
        }
    }
}
