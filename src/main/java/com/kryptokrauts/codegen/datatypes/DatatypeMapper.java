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
   * @param type
   * @return
   */
  public boolean applies(TypeName type);

  /**
   * encodeValue javacode
   *
   * @param type
   * @param variableName
   * @return
   */
  public CodeBlock encodeValue(TypeName type, String variableName);

  /**
   * map result back to java object
   *
   * @param type
   * @param variableName
   * @return
   */
  public CodeBlock mapToReturnValue(TypeName type, String variableName);

  /**
   * check if
   *
   * @param typeDefForResolvingInnerClass
   * @return
   */
  public boolean appliesToJSON(Object typeDefForResolvingInnerClass);

  /**
   * construct the type name from given definition, parameter is necessary in case of nested types
   *
   * @param jsonTypeDef
   * @return
   */
  public TypeName getTypeNameFromJSON(Object jsonTypeDef);
}
