package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.CodeBlock;
import java.lang.reflect.Type;

public class BoolMapper extends AbstractSophiaTypeMapper {

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
}
