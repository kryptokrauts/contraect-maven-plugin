package com.kryptokrauts.codegen.datatypes;

import com.kryptokrauts.codegen.datatypes.defaults.CustomType;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import org.apache.tuweni.bytes.Bytes;

public class BytesMapper extends AbstractDatatypeMapper {

  public BytesMapper(DatatypeMappingHandler resolveInstance) {
    super(resolveInstance);
  }

  public boolean applies(TypeName type) {
    return TypeName.get(Bytes.class).equals(type);
  }

  @Override
  public boolean appliesToJSON(Object typeDefForResolvingInnerClass) {

    return CustomType.BYTES_TYPE.equals(typeDefForResolvingInnerClass)
        || CustomType.HASH_TYPE.equals(typeDefForResolvingInnerClass)
        || CustomType.SIGNATURE_TYPE.equals(typeDefForResolvingInnerClass);
  }

  @Override
  public CodeBlock mapToReturnValue(TypeName type, String variableName) {
    return CodeBlock.builder().add("$S+$L.toString()", "#", variableName).build();
  }

  @Override
  public TypeName getTypeNameFromJSON(Object typeDefForResolvingInnerClass) {
    return TypeName.get(Byte.class);
  }

  @Override
  public CodeBlock encodeValue(TypeName type, String variableName) {
    return CodeBlock.builder().add("#$L.toString()", variableName).build();
  }
}
