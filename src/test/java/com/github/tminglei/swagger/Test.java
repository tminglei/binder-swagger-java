package com.github.tminglei.swagger;

import static com.github.tminglei.swagger.SwaggerContext.*;
import static com.github.tminglei.bind.Simple.*;
import static com.github.tminglei.bind.Mappings.*;
import static com.github.tminglei.bind.Constraints.*;
import static com.github.tminglei.bind.Processors.*;

/**
 * Created by tminglei on 9/4/15.
 */
public class Test {

    static {
        operation("get", "/xxx").operationId("")
                .summary("")
                .parameter(param(text()).name("ttt").in("query").desc("ttttt"))
                .response(200, response(mapping()).description("tttt"))
        ;
    }
    public void test() {

    }
}
