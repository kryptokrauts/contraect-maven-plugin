package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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

  @NonNull protected DatatypeMappingHandler resolveInstance;

  /**
   * necessary for recursive calls using lambda
   *
   * @param variableName also replace dots to omit compiler errors because they indicate function
   *     calls
   * @return
   */
  protected String getUniqueVariableName(String variableName) {
    return "_" + variableName.replaceAll("\\.", "_");
  }

  protected String getUnsupportedMappingException(
      Object unsupportedTypeCaseString,
      String typeMapperClass,
      String typeMapperClassMethod,
      String additionalInfo) {
    return String.format(
        "This is an unforseen contract type definition case for mapper %s.%s - cannot map java type to given sophia type. Please create an issue on https://github.com/kryptokrauts/contraect-maven-plugin along with your contract code and the following strings\nTypeString:\n%s"
            + (additionalInfo != null ? "\nAdditional information: " + additionalInfo : ""),
        typeMapperClass,
        typeMapperClassMethod,
        unsupportedTypeCaseString);
  }
}
