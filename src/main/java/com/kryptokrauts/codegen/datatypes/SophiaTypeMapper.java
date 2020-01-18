package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

public interface SophiaTypeMapper {

  public CodeBlock getReturnStatement(Object resultToReturn);

  /**
   * returns the type for the given sophia typeString
   *
   * @param valueTypeString
   * @return
   */
  public TypeName getReturnType(Object typeString);

  /**
   * check if typeMapper can be used for the given sophia typeString
   *
   * @param typeString
   * @return
   */
  public boolean applies(Object typeString);
}
