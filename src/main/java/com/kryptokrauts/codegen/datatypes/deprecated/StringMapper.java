package com.kryptokrauts.codegen.datatypes.deprecated;

import com.squareup.javapoet.TypeName;

public class StringMapper extends AbstractSophiaTypeMapper {

  public StringMapper(TypeResolverRefactored typeResolverInstance) {
    super(typeResolverInstance);
  }

  @Override
  public boolean applies(Object typeString) {
    return "string".equalsIgnoreCase(valueToString(typeString))
        || TypeName.get(String.class).equals(typeString);
  }

  @Override
  public TypeName getReturnType(Object typeString) {
    return TypeName.get(String.class);
  }
}
