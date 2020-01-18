package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

public class DefaultMapper extends AbstractSophiaTypeMapper {

  public DefaultMapper(TypeResolverRefactored typeResolverInstance) {
    super(typeResolverInstance);
    // TODO Auto-generated constructor stub
  }

  @Override
  public CodeBlock getReturnStatement(Object resultToReturn) {
    return CodeBlock.builder().add("($T)$L", Object.class, resultToReturn).build();
  }

  @Override
  public boolean applies(Object type) {
    return false;
  }

  @Override
  public TypeName getReturnType(Object typeString) {
    return TypeName.get(Object.class);
  }
}
