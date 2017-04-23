package com.github.tminglei.swagger.fake;

import java.io.IOException;
import java.io.Writer;

/**
 * Used to write data to target writer with specified format
 */
public interface DataWriter {

    /**
     * transform inputting data to target format, and write it to target writer
     *
     * @param writer    target writer
     * @param format    target format
     * @param provider  data provider
     * @throws IOException
     */
    void write(Writer writer, String format, DataProvider provider) throws IOException;

}
