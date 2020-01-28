package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

public class BoolEncoder extends AbstractDatatypeMapper {

  public BoolEncoder(DatatypeMappingHandler resolveInstance) {
    super(resolveInstance);
  }

  public boolean applies(TypeName type) {
    return TypeName.get(Boolean.class).equals(type);
  }

  @Override
  public boolean appliesToJSON(Object typeDefForResolvingInnerClass) {
    return "bool".equals(typeDefForResolvingInnerClass);
  }

  @Override
  public CodeBlock mapToReturnValue(TypeName type, String variableName) {
    return CodeBlock.builder()
        .add("$T.valueOf($L.toString())", Boolean.class, variableName)
        .build();
  }

  @Override
  public TypeName getTypeNameFromJSON(Object type) {
    return TypeName.BOOLEAN.box();
  }

  @Override
  public CodeBlock encodeValue(TypeName type, String variableName) {
    return CodeBlock.builder().add("$L.toString()", variableName).build();
  }
}
