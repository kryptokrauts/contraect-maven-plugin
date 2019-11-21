package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.CodeBlock;
import java.lang.reflect.Type;

public class BytesMapper extends AbstractSophiaTypeMapper {

  @Override
  public CodeBlock getReturnStatement(Object resultToReturn) {
    // @TODO split result into list
    return CodeBlock.builder()
        .addStatement("return new $T($L)", Byte[].class, resultToReturn)
        .build();
  }

  @Override
  public Type getJavaType() {
    return Byte[].class;
  }

  @Override
  public boolean applies(Object type) {
    return getType(type).startsWith("bytes");
  }
}
