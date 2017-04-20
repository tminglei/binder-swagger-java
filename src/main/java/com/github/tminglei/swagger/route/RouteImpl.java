package com.github.tminglei.swagger.route;

import com.github.tminglei.swagger.fake.DataProvider;
import io.swagger.models.HttpMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static com.github.tminglei.swagger.SimpleUtils.*;

/**
 * Created by minglei on 4/17/17.
 */
public class RouteImpl implements Route {
    private final HttpMethod method;
    private final String pathPattern;
    private final boolean implemented;
    private final DataProvider dataProvider;

    private final List<PathElement> allPathElements;
    private final List<PathNamedParamElement> namedParamElements;
    private final List<PathSplatParamElement> splatParamElements;
    private final List<PathStaticElement> staticPathElements;

    public RouteImpl(HttpMethod method, String pathPattern,
                     boolean implemented, DataProvider dataProvider) {
        this.method = notEmpty(method, "method cannot be null");
        this.pathPattern = notEmpty(pathPattern, "pathPattern cannot be null");
        this.implemented = implemented;
        this.dataProvider = notEmpty(dataProvider, "dataProvider cannot be null");

        this.allPathElements = new ArrayList<>();
        this.splatParamElements = new ArrayList<>();
        this.staticPathElements = new ArrayList<>();
        this.namedParamElements = new ArrayList<>();

        extractPathElements(pathPattern);
    }

    @Override
    public HttpMethod getMethod() {
        return method;
    }

    @Override
    public String getPathPattern() {
        return pathPattern;
    }

    @Override
    public Map<String, String> getPathParams(String path) {
        Map<String, String> params = new HashMap<>();
        String[] pathTokens = RouteHelper.getPathElements(path);
        for (PathNamedParamElement pathParam : namedParamElements) {
            String value = RouteHelper.urlDecodeForPathParams(pathTokens[pathParam.index()]);
            params.put(pathParam.name(), value);
        }
        return params;
    }

    @Override
    public boolean isImplemented() {
        return implemented;
    }

    @Override
    public DataProvider getDataProvider() {
        return dataProvider;
    }

    private void extractPathElements(String pattern) {
        pattern = pattern.replaceAll("/\\{([^\\}]+)\\}", "/:$1");
        Matcher m = RouteHelper.CUSTOM_REGEX_PATTERN.matcher(pattern);
        Map<String, String> regexMap = getRegexMap(m);
        String path = m.replaceAll("");

        String[] pathElements = RouteHelper.getPathElements(path);
        for (int i = 0; i < pathElements.length; i++) {
            String currentElement = pathElements[i];
            if (currentElement.startsWith(RouteHelper.PARAM_PREFIX)) {
                currentElement = currentElement.substring(1);
                PathNamedParamElement named =
                    new PathNamedParamElement(currentElement, i, regexMap.get(currentElement));
                namedParamElements.add(named);
                allPathElements.add(named);
            } else if (currentElement.equals(RouteHelper.WILDCARD)) {
                PathSplatParamElement splat = new PathSplatParamElement(i);
                splatParamElements.add(splat);
                allPathElements.add(splat);
            } else {
                if (currentElement.trim().length() < 1) continue;
                PathStaticElement staticElem = new PathStaticElement(currentElement, i);
                staticPathElements.add(staticElem);
                allPathElements.add(staticElem);
            }
        }
    }

    /*
     * Returns a map of named param names to their regex, for
     * named params that have a regex.
     * e.g. {"name" -> "[a-z]+"}
     */
    private Map<String, String> getRegexMap(Matcher m) {
        Map<String, String> regexMap = new HashMap<>();
        while (m.find()) {
            String regex = pathPattern.substring(m.start() + 1, m.end() - 1);
            int namedParamStart = m.start() - 1;
            int namedParamEnd = m.start();
            String namedParamName = pathPattern.substring(namedParamStart, namedParamEnd);
            while (!namedParamName.startsWith(RouteHelper.PARAM_PREFIX)) {
                namedParamStart--;
                namedParamName = pathPattern.substring(namedParamStart, namedParamEnd);
            }
            namedParamName = pathPattern.substring(namedParamStart + 1, namedParamEnd);
            regexMap.put(namedParamName, regex);
        }
        return regexMap;
    }

    public List<PathElement> getPathElements() {
        return new ArrayList<>(allPathElements);
    }

    public List<PathNamedParamElement> getNamedParameterElements() {
        return new ArrayList<>(namedParamElements);
    }

    public List<PathSplatParamElement> getSplatParameterElements() {
        return new ArrayList<>(splatParamElements);
    }

    public List<PathStaticElement> getStaticPathElements() {
        return new ArrayList<>(staticPathElements);
    }

    /**
     * Use of this method assumes the path given matches this Route.
     *
     * @return the value of the named parameter in the path, or null if
     *         no named parameter exists with the given name
     */
    public String getNamedParameter(String paramName, String path) {
        List<PathNamedParamElement> pathParams = getNamedParameterElements();
        String[] pathTokens = RouteHelper.getPathElements(path);

        for (PathNamedParamElement pathParam : pathParams) {
            if (pathParam.name().equals(paramName)) {
                return RouteHelper.urlDecodeForPathParams(pathTokens[pathParam.index()]);
            }
        }
        return null;
    }

    /**
     * Use of this method assumes the path given matches this Route.
     *
     * @return the value of the splat parameter at the given index,
     *         or null if the splat parameter index does not exist
     */
    public String getSplatParameter(int index, String path) {
        String[] splat = splat(path);
        if (index > splat.length - 1) {
            return null;
        }
        return splat[index];
    }

    /**
     * Use of this method assumes the path given matches this Route.
     */
    public String[] splat(String path) {
        List<PathSplatParamElement> splatParams = getSplatParameterElements();
        String[] pathTokens = RouteHelper.getPathElements(path, false);
        String[] splat = new String[splatParams.size()];

        for (int i = 0; i < splatParams.size(); i++) {
            PathSplatParamElement splatParam = splatParams.get(i);
            splat[i] = RouteHelper.urlDecodeForPathParams(pathTokens[splatParam.index()]);

            if (i + 1 == splatParams.size() && endsWithSplat()) {
                /* this is the last splat param and the route ends with splat */
                for (int j = splatParam.index() + 1; j < pathTokens.length; j++) {
                    splat[i] = splat[i] + RouteHelper.PATH_ELEMENT_SEPARATOR + RouteHelper.urlDecodeForPathParams(pathTokens[j]);
                }
            }
        }
        return splat;
    }

    private boolean endsWithSplat() {
        return pathPattern.endsWith(RouteHelper.WILDCARD);
    }

    public boolean endsWithPathSeparator() {
        return pathPattern.endsWith(RouteHelper.PATH_ELEMENT_SEPARATOR);
    }

    public boolean hasPathElements() {
        return !allPathElements.isEmpty();
    }

    public String toString() {
        return "Route(method=" + method
            + ", pathPattern=" + pathPattern
            + ", implemented=" + implemented
            + ")";
    }

    public int hashCode() {
        int hash = 1;
        hash = hash * 13 + (pathPattern == null ? 0 : pathPattern.hashCode());
        return hash;
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof RouteImpl)) return false;

        RouteImpl that = (RouteImpl) o;
        return this.pathPattern == null ? that.pathPattern == null
            : this.pathPattern.equals(that.pathPattern);
    }
}
