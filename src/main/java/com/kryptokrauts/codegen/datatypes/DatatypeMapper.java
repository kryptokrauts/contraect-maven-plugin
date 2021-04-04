package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

/**
 * defines methods which need to be implemented by a type mapper
 *
 * @author mitch
 */
public interface DatatypeMapper {

  /**
   * validate if mapper applies to handle given type
   *
   * @param type one of {@link TypeName}
   * @return true|false
   */
  boolean applies(TypeName type);

  /**
   * encodeValue javacode
   *
   * @param type one of {@link TypeName}
   * @param variableName name of the variable
   * @return instance of {@link CodeBlock}
   */
  CodeBlock encodeValue(TypeName type, String variableName);

  /**
   * map result back to java object
   *
   * @param type one of {@link TypeName}
   * @param variableName name of the variable
   * @return instance of {@link CodeBlock}
   */
  CodeBlock mapToReturnValue(TypeName type, String variableName);

  /**
   * check if
   *
   * @param typeDefForResolvingInnerClass the jsonTypeDef to check
   * @return true|false
   */
  boolean appliesToJSON(Object typeDefForResolvingInnerClass);

  /**
   * construct the type name from given definition, parameter is necessary in case of nested types
   *
   * @param jsonTypeDef the jsonTypeDef to use for resolving the TypeName
   * @return one of {@link TypeName}
   */
  TypeName getTypeNameFromJSON(Object jsonTypeDef);
}
