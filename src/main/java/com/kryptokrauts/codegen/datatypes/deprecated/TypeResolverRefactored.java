package com.kryptokrauts.codegen.datatypes.deprecated;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;

/** @author mitch */
public class TypeResolverRefactored {

  private List<SophiaTypeMapper> typeMapperList;

  private SophiaTypeMapper defaultTypeMapper;

  /*
   * custom types müssen einen eigenen mapper definieren, der hier in die
   * liste aufgenommen werden kann
   */
  public TypeResolverRefactored() {
    this.typeMapperList =
        Arrays.asList(
            new IntMapper(this),
            new ListMapper(this),
            new MapMapper(this),
            new StringMapper(this),
            new BoolMapper(this),
            new CustomDatatypeMapper(this));
    this.defaultTypeMapper = new DefaultMapper(this);
  }

  /**
   * resolve typeMapper to use for given sophia datatype string
   *
   * @param typeString with definition of sophia datatype
   * @return typeMapper
   */
  protected SophiaTypeMapper getTypeMapper(Object typeString) {
    return typeMapperList.stream()
        .filter(t -> t.applies(typeString))
        .findFirst()
        .orElse(defaultTypeMapper);
  }
  /**
   * resolve typename for given sophia datatype string
   *
   * @param typeDefinition
   * @return
   * @throws MojoExecutionException
   */
  public TypeName getReturnType(Object typeString) {
    return getTypeMapper(typeString).getReturnType(typeString);
  }

  /**
   * decodes the given result object to the corresponding result type
   *
   * @param type
   * @param result
   * @return
   */
  public CodeBlock decodeResult(TypeName type, Object result) {
    return getTypeMapper(type).getReturnStatement(result);
  }
}
