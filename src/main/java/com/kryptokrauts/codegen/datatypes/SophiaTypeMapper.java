package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.CodeBlock;
import java.lang.reflect.Type;

public interface SophiaTypeMapper {

  public CodeBlock getReturnStatement(Object resultToReturn);

  public Type getJavaType();

  public boolean applies(Object type);
}
