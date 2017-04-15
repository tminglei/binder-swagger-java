package com.github.tminglei.swagger.bind;

import com.github.tminglei.bind.Framework;
import io.swagger.models.Model;
import io.swagger.models.Response;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

import java.util.List;
import java.util.Map;

/**
 * Used to convert form-binder mapping to swagger components
 */
public interface MappingConverter {

  /**
   * convert mapping to swagger parameters
   *
   * @param name      param name
   * @param mapping   the mapping
   * @return  converted parameters
   */
  List<Parameter> mToParameters(String name, Framework.Mapping<?> mapping);

  /**
   * convert mapping to swagger parameter
   *
   * @param name      param name
   * @param mapping   the mapping
   * @return  converted parameter
   */
  Parameter mToParameter(String name, Framework.Mapping<?> mapping);

  /**
   * convert mapping to swagger response
   *
   * @param mapping the mapping
   * @return converted response
   */
  Response mToResponse(Framework.Mapping<?> mapping);

  /**
   * convert mapping to swagger property
   *
   * @param mapping the mapping
   * @return  converted property
   */
  Property mToProperty(Framework.Mapping<?> mapping);

  /**
   * convert mapping to swagger model
   *
   * @param mapping the mapping
   * @return  converted model
   */
  Model mToModel(Framework.Mapping<?> mapping);

  /**
   * scan mapping and find/convert swagger models
   *
   * @param mapping the mapping
   * @return  found models
   */
  List<Map.Entry<String, Model>> scanModels(Framework.Mapping<?> mapping);

}
