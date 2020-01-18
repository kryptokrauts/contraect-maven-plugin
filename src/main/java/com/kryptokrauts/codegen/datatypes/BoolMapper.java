package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

public class BoolMapper extends AbstractSophiaTypeMapper {

  public BoolMapper(TypeResolverRefactored typeResolverInstance) {
    super(typeResolverInstance);
  }

  @Override
  public CodeBlock getReturnStatement(Object resultToReturn) {
    return CodeBlock.builder()
        .addStatement("return $T.valueOf($L.toString())", Boolean.class, resultToReturn)
        .build();
  }

  @Override
  public boolean applies(Object typeString) {
    return "bool".equalsIgnoreCase(valueToString(typeString));
  }

  @Override
  public TypeName getReturnType(Object typeString) {
    return TypeName.get(Boolean.class);
  }
}
