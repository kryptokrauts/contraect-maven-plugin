package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.TypeName;

public class StringMapper extends AbstractSophiaTypeMapper {

  public StringMapper(TypeResolverRefactored typeResolverInstance) {
    super(typeResolverInstance);
  }

  @Override
  public boolean applies(Object type) {
    return "string".equalsIgnoreCase(valueToString(type))
        || TypeName.get(String.class).equals(type);
  }

  @Override
  public TypeName getReturnType(Object typeString) {
    return TypeName.get(String.class);
  }
}
