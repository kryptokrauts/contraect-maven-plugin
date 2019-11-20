package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.CodeBlock;
import java.lang.reflect.Type;

public class BitsMapper extends AbstractSophiaTypeMapper {

  @Override
  public CodeBlock getReturnStatement(Object resultToReturn) {
    return CodeBlock.builder()
        .addStatement("return new $T($L)", Byte.class, resultToReturn)
        .build();
  }

  @Override
  public Type getJavaType() {
    return Byte.class;
  }

  @Override
  public boolean applies(Object type) {
    return "bool".equalsIgnoreCase(getType(type));
  }
}
