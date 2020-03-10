package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

public class StringMapper extends AbstractDatatypeMapper {

  public StringMapper(DatatypeMappingHandler resolveInstance) {
    super(resolveInstance);
  }

  public boolean applies(TypeName type) {
    return TypeName.get(String.class).equals(type);
  }

  @Override
  public boolean appliesToJSON(Object typeDefForResolvingInnerClass) {
    return "string".equals(typeDefForResolvingInnerClass);
  }

  public CodeBlock encodeValue(TypeName type, String variableName) {
    return CodeBlock.builder().add("$S+$L+$S", "\"", variableName, "\"").build();
  }

  @Override
  public CodeBlock mapToReturnValue(TypeName type, String variableName) {
    return CodeBlock.builder().add("$L.toString()", variableName).build();
  }

  @Override
  public TypeName getTypeNameFromJSON(Object type) {
    return TypeName.get(String.class);
  }
}
