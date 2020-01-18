package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import java.util.Arrays;
import java.util.List;

/**
 * @TODO support Lists, Maps, Tuples, Nesting using a recursive approach
 *
 * @author mitch
 */
public class ListMapper extends AbstractSophiaTypeMapper {

  public ListMapper(TypeResolverRefactored typeResolverInstance) {
    super(typeResolverInstance);
    // TODO Auto-generated constructor stub
  }

  @Override
  public CodeBlock getReturnStatement(Object resultToReturn) {
    // @TODO split result into list
    return CodeBlock.builder()
        .addStatement("return $T.of($L)", Arrays.class, resultToReturn)
        .build();
  }

  public TypeName getReturnType(Object typeString) {
    List<?> values = (List<?>) typeString;
    Object innerType = values.get(0);
    return ParameterizedTypeName.get(
        ClassName.get(List.class),
        typeResolverInstance.getTypeMapper(innerType).getReturnType(innerType));
  }

  @Override
  public boolean applies(Object typeString) {
    return "list".equalsIgnoreCase(valueToString(typeString));
  }
}
