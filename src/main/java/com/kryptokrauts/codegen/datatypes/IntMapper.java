package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import java.math.BigInteger;

public class IntMapper extends AbstractDatatypeMapper {

  public IntMapper(DatatypeMappingHandler resolveInstance) {
    super(resolveInstance);
  }

  public boolean applies(TypeName type) {
    return TypeName.get(BigInteger.class).equals(type);
  }

  @Override
  public boolean appliesToJSON(Object typeDefForResolvingInnerClass) {
    return "int".equals(typeDefForResolvingInnerClass);
  }

  @Override
  public CodeBlock mapToReturnValue(TypeName type, String variableName) {
    return CodeBlock.builder().add("new $T($L.toString())", BigInteger.class, variableName).build();
  }

  @Override
  public TypeName getTypeNameFromJSON(Object typeDefForResolvingInnerClass) {
    return TypeName.get(BigInteger.class);
  }

  @Override
  public CodeBlock encodeValue(TypeName type, String variableName) {
    return CodeBlock.builder().add("$L.toString()", variableName).build();
  }
}
