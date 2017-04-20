package com.github.tminglei.swagger.fake;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.XStream;

import java.io.Writer;

/**
 * Created by minglei on 4/17/17.
 */
public class DataWriterImpl implements DataWriter {
    private static final String FORMAT_JSON = "application/json";
    private static final String FORMAT_XML  = "application/xml";

    @Override
    public void write(Writer writer, String format, Object data) {
        try {
            switch (format.toLowerCase()) {
                case FORMAT_JSON:
                    String dataJson = new ObjectMapper()
                        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                        .writer().withDefaultPrettyPrinter()
                        .writeValueAsString(data);
                    writer.write(dataJson);
                    break;
                case FORMAT_XML:
                    String dataXml = new XStream().toXML(data);
                    writer.write(dataXml);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported format: " + format);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
