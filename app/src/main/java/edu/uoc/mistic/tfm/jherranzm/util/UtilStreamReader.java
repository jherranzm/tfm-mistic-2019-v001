package edu.uoc.mistic.tfm.jherranzm.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class UtilStreamReader {

    /**
     *
     * Converting InputStream to String
     *
     * @param in the input stream. The caller instance is responsible of closing it
     * @return the String retrieved from the InputStream
     * @throws IOException if there is some exception in reading the inputStream
     */
    public static String readStream(InputStream in) throws IOException {
        BufferedReader reader;
        StringBuilder builder = new StringBuilder();
        reader = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        return builder.toString();
    }

}
