package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import java.lang.reflect.Type;

public class DefaultMapper extends AbstractSophiaTypeMapper {

  public DefaultMapper(TypeResolverRefactored typeResolverInstance) {
    super(typeResolverInstance);
    // TODO Auto-generated constructor stub
  }

  @Override
  public CodeBlock getReturnStatement(Object resultToReturn) {
    return CodeBlock.builder().addStatement("return ($T)$L", Object.class, resultToReturn).build();
  }

  @Override
  public Type getJavaType() {
    return Object.class;
  }

  @Override
  public boolean applies(Object type) {
    return false;
  }

  @Override
  public TypeName getReturnType(Object valueTypeString) {
    // TODO Auto-generated method stub
    return null;
  }
}
