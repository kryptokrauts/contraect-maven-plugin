package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Map;
import java.util.Optional;

/**
 * option is mapped to {@link Optional}
 *
 * @author mitch
 */
public class OptionMapper extends AbstractDatatypeMapper {

  public static final String HAS_VALUE_STRING = "Some";

  public static final String HAS_NO_VALUE_STRING = "None";

  public OptionMapper(DatatypeMappingHandler resolveInstance) {
    super(resolveInstance);
  }

  public boolean applies(TypeName type) {
    return type instanceof ParameterizedTypeName
        && ((ParameterizedTypeName) type).rawType.equals(ClassName.get(Optional.class));
  }

  @Override
  public boolean appliesToJSON(Object typeDefForResolvingInnerClass) {
    return getOptionInnerType(typeDefForResolvingInnerClass) != null;
  }

  public CodeBlock encodeValue(TypeName type, String variableName) {
    return CodeBlock.builder()
        .add(
            "($L.isPresent()?\"" + HAS_VALUE_STRING + "(\"+$L+\")\":$S)",
            variableName,
            this.resolveInstance.encodeParameter(
                this.getOptionInnerType(type), variableName + ".get()"),
            HAS_NO_VALUE_STRING)
        .build();
  }

  @Override
  public CodeBlock mapToReturnValue(TypeName type, String variableName) {
    CodeBlock getJsonArrayValue =
        CodeBlock.builder()
            .add(
                "((($T)((($T)$L).get($S))).get(0))",
                LIST_WITH_WILDCARD_TYPDEF,
                Map.class,
                variableName,
                HAS_VALUE_STRING)
            .build();

    return CodeBlock.builder()
        .add(
            "($L instanceof $T)?$T.of($L):$T.empty()",
            variableName,
            Map.class,
            Optional.class,
            this.resolveInstance.mapToReturnValue(
                getOptionInnerType(type), getJsonArrayValue.toString()),
            Optional.class)
        .build();
  }

  @Override
  public TypeName getTypeNameFromJSON(Object type) {
    return ParameterizedTypeName.get(
        ClassName.get(Optional.class),
        this.resolveInstance.getTypeNameFromJSON(getOptionInnerType(type)));
  }

  private TypeName getOptionInnerType(TypeName type) {
    if (type instanceof ParameterizedTypeName) {
      return ((ParameterizedTypeName) type).typeArguments.get(0);
    }
    throw new RuntimeException(
        getUnforseenMappingMessage(
            "given argument for resolving option type seems to be not parametrized",
            OptionMapper.class.getName(),
            "getOptionInnerType",
            type.toString()));
  }

  private Object getOptionInnerType(Object type) {
    if (type != null) {
      if (type instanceof JsonObject) {
        Object list = JsonObject.mapFrom(type).getValue(OPTION_JSON_IDENTIFIER);
        if (list instanceof JsonArray) {
          JsonArray array = (JsonArray) list;
          if (array.size() > 0) {
            return array.getValue(0);
          }
        }
      }
    }
    return null;
  }
}
