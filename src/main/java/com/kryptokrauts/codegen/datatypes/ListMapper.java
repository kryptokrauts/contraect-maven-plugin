package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.stream.Collectors;

public class ListMapper extends AbstractDatatypeMapper {

  private static final int LIST_INNER_TYPE_POS = 0;

  public ListMapper(DatatypeMappingHandler resolveInstance) {
    super(resolveInstance);
  }

  public boolean applies(TypeName type) {
    if (type instanceof ParameterizedTypeName) {
      return ((ParameterizedTypeName) type).rawType.simpleName().equals(List.class.getSimpleName());
    }
    return TypeName.get(List.class).equals(type);
  }

  @Override
  public boolean appliesToJSON(Object typeDefForResolvingInnerClass) {
    return parseToJsonArray(typeDefForResolvingInnerClass) != null;
  }

  public CodeBlock encodeValue(TypeName type, String variableName) {
    String uniqueVariableName = this.getUniqueVariableName(variableName);
    TypeName innerType = this.getListInnerType(type);

    return CodeBlock.builder()
        .add("$L.stream().map($L->", variableName, uniqueVariableName)
        .add("$L", this.resolveInstance.encodeParameter(innerType, uniqueVariableName))
        .add(").collect($T.toList()).toString()", Collectors.class)
        .build();
  }

  public CodeBlock mapToReturnValue(TypeName type, String variableName) {
    String uniqueVariableName = this.getUniqueVariableName(variableName);
    TypeName innerType = this.getListInnerType(type);

    return CodeBlock.builder()
        .add(
            "(($T)$L).stream().map($L->",
            LIST_WITH_WILDCARD_TYPDEF,
            variableName,
            uniqueVariableName)
        .add("$L", this.resolveInstance.mapToReturnValue(innerType, uniqueVariableName))
        .add(").collect($T.toList())", Collectors.class)
        .build();
  }

  @Override
  public TypeName getTypeNameFromJSON(Object type) {
    return ParameterizedTypeName.get(
        ClassName.get(List.class),
        this.resolveInstance.getTypeNameFromJSON(
            parseToJsonArray(type).getValue(LIST_INNER_TYPE_POS)));
  }

  private JsonArray parseToJsonArray(Object type) {
    if (type != null) {
      if (type instanceof JsonObject) {
        Object list = JsonObject.mapFrom(type).getValue(LIST_JSON_IDENTIFIER);
        if (list instanceof JsonArray) {
          return (JsonArray) list;
        }
      }
    }
    return null;
  }

  private TypeName getListInnerType(TypeName type) {
    if (type instanceof ParameterizedTypeName) {
      return ((ParameterizedTypeName) type).typeArguments.get(LIST_INNER_TYPE_POS);
    }
    throw new RuntimeException(
        getUnsupportedMappingException(
            "given argument for resolving list type seems to be not parametrized",
            ListMapper.class.getName(),
            "getListInnerType",
            type.toString()));
  }
}
