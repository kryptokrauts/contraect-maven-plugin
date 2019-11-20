package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.CodeBlock;
import java.lang.reflect.Type;

public class StringMapper extends AbstractSophiaTypeMapper {

  @Override
  public CodeBlock getReturnStatement(Object resultToReturn) {
    return CodeBlock.builder().addStatement("return $L.toString()", resultToReturn).build();
  }

  @Override
  public Type getJavaType() {
    return String.class;
  }

  @Override
  public boolean applies(Object type) {
    return "string".equalsIgnoreCase(getType(type));
  }
}
