package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import java.lang.reflect.Type;

public interface SophiaTypeMapper {

  public CodeBlock getReturnStatement(Object resultToReturn);

  public Type getJavaType();

  public TypeName getReturnType(Object valueTypeString);

  public boolean applies(Object type);
}
