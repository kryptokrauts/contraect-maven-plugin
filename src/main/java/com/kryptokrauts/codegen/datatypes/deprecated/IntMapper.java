package com.kryptokrauts.codegen.datatypes.deprecated;

import com.squareup.javapoet.CodeBlock;
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

  @Override
  public CodeBlock getReturnStatement(Object resultToReturn) {
    return CodeBlock.builder()
        .add("new $T($L.toString())", BigInteger.class, resultToReturn)
        .build();
  }
}
