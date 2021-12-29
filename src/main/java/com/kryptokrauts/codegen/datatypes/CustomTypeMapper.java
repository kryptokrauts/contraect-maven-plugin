package com.kryptokrauts.codegen.datatypes;

import com.kryptokrauts.codegen.CodegenUtil;
import com.kryptokrauts.codegen.CustomTypesGenerator;
import com.kryptokrauts.codegen.datatypes.defaults.CustomType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import io.vertx.core.json.JsonObject;

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
    } else return tryResolveSpecialPredefinedCustomTypes(type);
  }

  private String tryResolveSpecialPredefinedCustomTypes(Object type) {
    if (type != null) {
      // N-Bytes Type
      if (type.toString().contains("bytes")) {
        try {
          JsonObject json = JsonObject.mapFrom(type);
          if (json.getValue("bytes") != null) {
            return CodegenUtil.getUppercaseClassName(
                this.resolveInstance.customTypesGenerator.addByteType(json.getInteger("bytes")));
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        // Chain.TTL Type
      } else if ("Chain.ttl".equals(type)) {
        return CustomType.CHAIN_TTL_TYPE;
      }
      // AENS.pointee type
      else if ("AENS.pointee".equals(type)) {
        return CustomType.POINTEE_TYPE;
      } else if (type instanceof JsonObject) {
        JsonObject json = JsonObject.mapFrom(type);
        // Oracle Types
        if (json.containsKey(CustomType.ORACLE_TYPE)) {
          return CodegenUtil.getUppercaseClassName(CustomType.ORACLE_TYPE);
        } else if (json.containsKey(CustomType.ORACLE_QUERY_TYPE)) {
          return CodegenUtil.getUppercaseClassName(CustomType.ORACLE_QUERY_TYPE);
        }
      }
    }
    return null;
  }
}
