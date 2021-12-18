package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class MapMapper extends AbstractDatatypeMapper {

  private static final int KEY_POS = 0;
  private static final int VALUE_POS = 1;

  public MapMapper(DatatypeMappingHandler resolveInstance) {
    super(resolveInstance);
  }

  public boolean applies(TypeName type) {
    if (type instanceof ParameterizedTypeName) {
      return ((ParameterizedTypeName) type).rawType.simpleName().equals(Map.class.getSimpleName());
    }
    return TypeName.get(Map.class).equals(type);
  }

  @Override
  public boolean appliesToJSON(Object typeDefForResolvingInnerClass) {
    return parseToJsonArray(typeDefForResolvingInnerClass) != null;
  }

  public CodeBlock encodeValue(TypeName type, String variableName) {
    String keyName = "var_" + UUID.randomUUID().toString().substring(0, 8);

    return CodeBlock.builder()
        .add("\"{\"+")
        .add("$L.keySet().stream()", variableName)
        .add(
            ".map($L -> \"[\"+$L+\"] = \"+$L)",
            keyName,
            this.resolveInstance.encodeParameter(getInnerType(type, KEY_POS), keyName),
            this.resolveInstance.encodeParameter(
                getInnerType(type, VALUE_POS),
                CodeBlock.builder().add("$L.get($L)", variableName, keyName).build().toString()))
        .add(".collect($T.joining(\",\"))", Collectors.class)
        .add("+\"}\"")
        .build();
  }

  public CodeBlock mapToReturnValue(TypeName type, String variableName) {
    String keyName = "var_" + UUID.randomUUID().toString().substring(0, 8);
    String valueName = "var_" + UUID.randomUUID().toString().substring(0, 8);

    return CodeBlock.builder()
        .add(
            "($L instanceof $T ? (($T)(($T)$L).getList()): ($T)$L).stream().collect($T.toMap(",
            variableName,
            JsonArray.class,
            LIST_WITH_WILDCARD_TYPDEF,
            JsonArray.class,
            variableName,
            LIST_WITH_WILDCARD_TYPDEF,
            variableName,
            Collectors.class)
        .add(
            "$L->$L",
            keyName,
            this.resolveInstance.mapToReturnValue(
                getInnerType(type, KEY_POS), getMapListReturnValueField(KEY_POS, keyName)))
        .add(
            ",$L->$L",
            valueName,
            this.resolveInstance.mapToReturnValue(
                getInnerType(type, VALUE_POS), getMapListReturnValueField(VALUE_POS, valueName)))
        .add("))")
        .build();
  }

  private String getMapListReturnValueField(int pos, String variableName) {
    return CodeBlock.builder()
        .add("(($T)$L).get(" + pos + ")", LIST_WITH_WILDCARD_TYPDEF, variableName)
        .build()
        .toString();
  }

  @Override
  public TypeName getTypeNameFromJSON(Object type) {
    return ParameterizedTypeName.get(
        ClassName.get(Map.class),
        this.resolveInstance.getTypeNameFromJSON(parseToJsonArray(type).getValue(0)),
        this.resolveInstance.getTypeNameFromJSON(parseToJsonArray(type).getValue(1)));
  }

  private JsonArray parseToJsonArray(Object type) {
    if (type != null) {
      if (type instanceof JsonObject) {
        Object list = JsonObject.mapFrom(type).getValue(JSON_MAP_IDENTIFIER);
        if (list instanceof JsonArray) {
          JsonArray mapArray = (JsonArray) list;
          if (mapArray.size() > 1) {
            return mapArray;
          }
        }
      }
    }
    return null;
  }

  private TypeName getInnerType(TypeName type, int pos) {
    if (type instanceof ParameterizedTypeName) {
      return ((ParameterizedTypeName) type).typeArguments.get(pos);
    }
    throw new RuntimeException(
        getUnforseenMappingMessage(
            "given argument for resolving map type seems to be not parametrized",
            MapMapper.class.getName(),
            "getInnerType",
            type.toString()));
  }
}
