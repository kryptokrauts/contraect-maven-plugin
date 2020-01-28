package com.kryptokrauts.codegen.datatypes;

import com.kryptokrauts.codegen.CodegenUtil;
import com.kryptokrauts.codegen.CustomTypesGenerator;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

public class CustomTypeMapper extends AbstractDatatypeMapper {

  public CustomTypeMapper(DatatypeMappingHandler resolveInstance) {
    super(resolveInstance);
  }

  public boolean applies(TypeName type) {
    return type.equals(getTypeNameFromJSON(type.toString()));
  }

  @Override
  public boolean appliesToJSON(Object typeDefForResolvingInnerClass) {
    return getCustomTypeName(typeDefForResolvingInnerClass) != null;
  }

  public CodeBlock encodeValue(TypeName type, String variableName) {
    return CodeBlock.builder()
        .add("$T." + CustomTypesGenerator.M_ENCODE_VALUE + "($L)", type, variableName)
        .build();
  }

  @Override
  public CodeBlock mapToReturnValue(TypeName type, String variableName) {
    return CodeBlock.builder()
        .add("$T." + CustomTypesGenerator.M_MAP_TO_RETURN_VALUE + "($L)", type, variableName)
        .build();
  }

  @Override
  public TypeName getTypeNameFromJSON(Object type) {
    return ClassName.get(this.resolveInstance.generatedContractClassName, getCustomTypeName(type));
  }

  private String getCustomTypeName(Object type) {
    if (type != null
        && (type.toString()
                .toLowerCase()
                .startsWith(this.resolveInstance.generatedContractClassName.toLowerCase())
            || CustomTypesGenerator.PREDEFINED_TYPES
                .keySet()
                .contains(type.toString().toLowerCase()))) {
      return CodegenUtil.getUppercaseClassName(
          type.toString().substring(type.toString().indexOf(".") + 1));
    }
    return null;
  }
}
