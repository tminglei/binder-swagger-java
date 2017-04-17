/*
 * Copyright (C) 2014 BigTesting.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tminglei.swagger.route.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 * @author Luis Antunes
 */
public class RouteHelper {

    public static final String PATH_ELEMENT_SEPARATOR = "/";
    public static final String PARAM_PREFIX = ":";
    public static final char CUSTOM_REGEX_START = '<';
    public static final char CUSTOM_REGEX_END = '>';
    public static final String WILDCARD = "*";

    /*
     * From the Java API documentation for the Pattern class:
     *
     * Instances of this (Pattern) class are immutable and are safe for use by
     * multiple concurrent threads. Instances of the Matcher class are not safe
     * for such use.
     */
    public static final Pattern CUSTOM_REGEX_PATTERN = Pattern.compile("<[^>]+>");

    /*
     * set of regex special chars to escape
     */
    private static final Set<Character> REGEX_SPECIAL_CHARS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            '[',']','(',')','{','}','+','*','^','?','$','.','\\')));

    public static String[] getPathElements(String path) {
        return getPathElements(path, true);
    }

    public static String[] getPathElements(String path, boolean ignoreTrailingSeparator) {
        if (path == null) throw new IllegalArgumentException("path cannot be null");
        path = path.trim();
        if (path.length() == 0) throw new IllegalArgumentException("path cannot be empty");
        path = path.startsWith(PATH_ELEMENT_SEPARATOR) ? path.substring(1) : path;
        return path.split(PATH_ELEMENT_SEPARATOR, ignoreTrailingSeparator ? 0 : -1);
    }

    public static String urlDecodeForPathParams(String s) {
        s = s.replaceAll("\\+", "%2b");
        return urlDecode(s);
    }

    public static String urlDecodeForRouting(String s) {
        s = s.replaceAll("%2f|%2F", "%252f");
        return urlDecode(s);
    }

    public static String urlDecode(String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("could not URL decode string: " + s, e);
        }
    }

    public static String escapeNonCustomRegex(String path) {
        /*
         * TODO replace with a regular expression
         */
        StringBuilder sb = new StringBuilder();
        boolean inCustomRegion = false;
        CharacterIterator it = new StringCharacterIterator(path);
        for (char ch = it.first(); ch != CharacterIterator.DONE; ch = it.next()) {

            if (ch == CUSTOM_REGEX_START) {
                inCustomRegion = true;
            } else if (ch == CUSTOM_REGEX_END) {
                inCustomRegion = false;
            }

            if (REGEX_SPECIAL_CHARS.contains(ch) && !inCustomRegion) {
                sb.append('\\');
            }

            sb.append(ch);
        }

        return sb.toString();
    }
}