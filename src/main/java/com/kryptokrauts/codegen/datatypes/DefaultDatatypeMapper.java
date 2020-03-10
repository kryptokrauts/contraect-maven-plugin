package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultDatatypeMapper extends AbstractDatatypeMapper {

  private static Logger log = LoggerFactory.getLogger(DefaultDatatypeMapper.class);

  public DefaultDatatypeMapper(DatatypeMappingHandler resolveInstance) {
    super(resolveInstance);
  }

  public boolean applies(TypeName type) {
    return false;
  }

  @Override
  public boolean appliesToJSON(Object typeDefForResolvingInnerClass) {
    return false;
  }

  @Override
  public CodeBlock encodeValue(TypeName type, String variableName) {
    log.warn(
        "Fallback to "
            + DefaultDatatypeMapper.class.getSimpleName()
            + ".encodeValue for type "
            + type);
    return CodeBlock.builder().add("$L.toString()", variableName).build();
  }

  @Override
  public CodeBlock mapToReturnValue(TypeName type, String variableName) {
    log.warn(
        "Fallback to "
            + DefaultDatatypeMapper.class.getSimpleName()
            + ".mapToReturnValue for type "
            + type);
    return CodeBlock.builder().add("$L", variableName).build();
  }

  @Override
  public TypeName getTypeNameFromJSON(Object type) {
    return TypeName.get(Object.class);
  }
}
