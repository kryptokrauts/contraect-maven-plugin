package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import java.lang.reflect.Type;
import java.math.BigInteger;

public class IntMapper extends AbstractSophiaTypeMapper {

  public IntMapper(TypeResolverRefactored typeResolverInstance) {
    super(typeResolverInstance);
    // TODO Auto-generated constructor stub
  }

  @Override
  public CodeBlock getReturnStatement(Object resultToReturn) {
    return CodeBlock.builder()
        .addStatement("return new $T($L.toString())", BigInteger.class, resultToReturn)
        .build();
  }

  @Override
  public Type getJavaType() {
    return BigInteger.class;
  }

  @Override
  public boolean applies(Object type) {
    return "int".equalsIgnoreCase(getType(type));
  }

  @Override
  public TypeName getReturnType(Object valueTypeString) {
    return TypeName.get(BigInteger.class);
  }

  @Override
  public TypeName getReturnType() {
    return ClassName.get(BigInteger.class);
  }
}
