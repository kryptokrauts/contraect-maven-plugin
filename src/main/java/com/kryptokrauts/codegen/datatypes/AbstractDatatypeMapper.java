package com.kryptokrauts.codegen.datatypes;

import com.kryptokrauts.codegen.CmpErrorCode;
import com.kryptokrauts.codegen.CodegenUtil;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.javatuples.Pair;

/** @author mitch */
@RequiredArgsConstructor
public abstract class AbstractDatatypeMapper implements DatatypeMapper {

  protected static final String JSON_TUPLE_IDENTIFIER = "tuple";

  protected static final String LIST_JSON_IDENTIFIER = "list";

  protected static final String JSON_MAP_IDENTIFIER = "map";

  protected static final String OPTION_JSON_IDENTIFIER = "option";

  protected static final TypeName LIST_WITH_WILDCARD_TYPDEF =
      ParameterizedTypeName.get(
          ClassName.get(List.class), WildcardTypeName.subtypeOf(Object.class));

  protected static final TypeName MAP_WITH_WILDCARD_TYPDEF =
      ParameterizedTypeName.get(
          ClassName.get(Map.class),
          WildcardTypeName.subtypeOf(Object.class),
          WildcardTypeName.subtypeOf(Object.class));

  @NonNull protected DatatypeMappingHandler resolveInstance;

  /**
   * necessary for recursive calls using lambda
   *
   * @param variableName also replace dots to omit compiler errors because they indicate function
   *     calls
   * @return
   */
  protected String getUniqueVariableName(String variableName) {
    return "_"
        + variableName
            .replaceAll("\\.", "_")
            .replaceAll("\\(", "_")
            .replaceAll("\\)", "_")
            .replaceAll("\"", "_")
            .replaceAll("\\?", "_")
            .replaceAll("\\>", "_")
            .replaceAll("\\<", "_");
  }

  public static String getUnforseenMappingMessage(
      String additionalInfo,
      String typeMapperClass,
      String typeMapperClassMethod,
      String unsupportedTypeCaseString) {
    return CodegenUtil.getBaseErrorMessage(
        CmpErrorCode.UNFORSEEN_MAPPING,
        String.format(
            "This is an unforseen contract type definition case for mapper %s.%s - cannot map java type to given sophia type",
            typeMapperClass, typeMapperClassMethod),
        Arrays.asList(
            Pair.with("TypeString", unsupportedTypeCaseString),
            Pair.with("Additional information", additionalInfo)));
  }
}
