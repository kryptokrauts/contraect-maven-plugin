package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import java.lang.reflect.Type;

public class StringMapper extends AbstractSophiaTypeMapper {

  public StringMapper(TypeResolverRefactored typeResolverInstance) {
    super(typeResolverInstance);
    // TODO Auto-generated constructor stub
  }

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

  @Override
  public TypeName getReturnType(Object valueTypeString) {
    return TypeName.get(String.class);
  }

  @Override
  public TypeName getReturnType() {
    return ClassName.get(String.class);
  }
}
