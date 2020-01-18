package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import java.lang.reflect.Type;

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
  public Type getJavaType() {
    return Boolean.class;
  }

  @Override
  public boolean applies(Object type) {
    return "bool".equalsIgnoreCase(getType(type));
  }

  @Override
  public TypeName getReturnType(Object valueTypeString) {
    return TypeName.get(Boolean.class);
  }

  @Override
  public TypeName getReturnType() {
    return ClassName.get(Boolean.class);
  }
}
