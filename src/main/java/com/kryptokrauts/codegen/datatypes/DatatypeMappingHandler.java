package com.kryptokrauts.codegen.datatypes;

import com.kryptokrauts.codegen.CustomTypesGenerator;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import java.util.Arrays;
import java.util.List;

/**
 * this class handles the recursive resolution of the applying mapper for the given type
 *
 * @author mitch
 */
public class DatatypeMappingHandler {

  protected String generatedContractClassName;

  private List<DatatypeMapper> datatypeHandlers;

  protected CustomTypesGenerator customTypesGenerator;

  public DatatypeMappingHandler(
      String generatedTypesPackageName, String generatedContractClassName) {
    this.datatypeHandlers =
        Arrays.asList(
            new StringMapper(this),
            new IntMapper(this),
            new ListMapper(this),
            new OptionMapper(this),
            new CustomTypeMapper(this),
            new MapMapper(this),
            new VoidMapper(this),
            new BoolMapper(this),
            new TupleMapper(this));
    this.generatedContractClassName = generatedContractClassName;
  }

  public void setCustomTypesGenerator(CustomTypesGenerator customTypesGenerator) {
    this.customTypesGenerator = customTypesGenerator;
  }

  public CodeBlock encodeParameter(TypeName type, String variableName) {
    return getHandler(type).encodeValue(type, variableName);
  }

  public CodeBlock mapToReturnValue(TypeName type, String variableName) {
    return getHandler(type).mapToReturnValue(type, variableName);
  }

  public TypeName getTypeNameFromJSON(Object type) {
    return getHandler(type).getTypeNameFromJSON(type);
  }

  private DatatypeMapper getHandler(TypeName type) {
    return datatypeHandlers.stream()
        .filter(t -> t.applies(type))
        .findFirst()
        .orElse(new DefaultDatatypeMapper(this));
  }

  private DatatypeMapper getHandler(Object jsonTypeDef) {
    return datatypeHandlers.stream()
        .filter(t -> t.appliesToJSON(jsonTypeDef))
        .findFirst()
        .orElse(new DefaultDatatypeMapper(this));
  }
}
