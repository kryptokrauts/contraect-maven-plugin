package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class VoidMapper extends AbstractDatatypeMapper {

  public VoidMapper(DatatypeMappingHandler resolveInstance) {
    super(resolveInstance);
  }

  public boolean applies(TypeName type) {
    return getTypeNameFromJSON(type).equals(type);
  }

  @Override
  public boolean appliesToJSON(Object typeDefForResolvingInnerClass) {
    if (typeDefForResolvingInnerClass instanceof JsonObject) {
      JsonObject json = (JsonObject) typeDefForResolvingInnerClass;
      if (json.getValue("tuple") != null && json.getValue("tuple") instanceof JsonArray) {
        return JsonObject.mapFrom(typeDefForResolvingInnerClass).getJsonArray("tuple").size() == 0;
      }
    }
    return false;
  }

  // return empty codeblock because it does not apply for void arguments
  public CodeBlock encodeValue(TypeName type, String variableName) {
    return CodeBlock.builder().build();
  }

  @Override
  // return empty codeblock because it does not apply methods
  public CodeBlock mapToReturnValue(TypeName type, String variableName) {
    return CodeBlock.builder().build();
  }

  @Override
  public TypeName getTypeNameFromJSON(Object type) {
    return TypeName.VOID;
  }
}
