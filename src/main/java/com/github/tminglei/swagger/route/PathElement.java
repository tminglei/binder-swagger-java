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
public abstract class PathElement {
    protected final String name;
    protected final int index;

    public PathElement(String name, int index) {
        this.name = name;
        this.index = index;
    }

    /**
     * Returns the name of the element in the route.
     *
     * @return the name of the element in the route
     */
    public String name() {
        return name;
    }

    /**
     * Returns the absolute position of the element
     * in the route.
     *
     * @return the index of the element in the route
     */
    public int index() {
        return index;
    }

    public int hashCode() {
        int result = 1;
        result = 31 * result + (name == null ? 0 : name.hashCode());
        result = 31 * result + index;
        return result;
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof PathElement)) return false;

        PathElement that = (PathElement)o;
        return this.index == that.index
           && (this.name == null ? that.name == null : this.name.equals(that.name));
    }
}