package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.TypeName;
import java.math.BigInteger;

public class IntMapper extends AbstractSophiaTypeMapper {

  public IntMapper(TypeResolverRefactored typeResolverInstance) {
    super(typeResolverInstance);
    // TODO Auto-generated constructor stub
  }

  @Override
  public boolean applies(Object type) {
    return "int".equalsIgnoreCase(valueToString(type))
        || TypeName.get(BigInteger.class).equals(type);
  }

  @Override
  public TypeName getReturnType(Object typeString) {
    return TypeName.get(BigInteger.class);
  }
}
