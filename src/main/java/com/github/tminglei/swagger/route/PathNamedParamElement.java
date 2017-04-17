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
package com.github.tminglei.swagger.route;

/**
 *
 * @author Luis Antunes
 */
public class PathNamedParamElement extends PathElement {
    private final String regex;

    public PathNamedParamElement(String name, int index, String regex) {
        super(name, index);
        this.regex = regex;
    }

    /**
     * Returns the regex pattern for the element if it exists,
     * or null if no regex pattern was provided.
     *
     * @return the regex pattern for the element if it exists,
     * or null otherwise
     */
    public String regex() {
        return regex;
    }

    public boolean hasRegex() {
        return regex != null && regex.trim().length() > 0;
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof PathNamedParamElement)) return false;
        PathNamedParamElement that = (PathNamedParamElement) o;

        return super.equals(o)
           && (this.regex == null ? that.regex == null : this.regex.equals(that.regex));
    }
}