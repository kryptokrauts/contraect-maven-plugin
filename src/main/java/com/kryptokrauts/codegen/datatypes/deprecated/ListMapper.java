package com.kryptokrauts.codegen.datatypes.deprecated;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.vertx.core.json.JsonArray;
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
  }

  @Override
  public CodeBlock getReturnStatement(Object resultToReturn) {
    // CodeBlock returnStatement = CodeBlock.builder()
    // .add("$T.of(", Arrays.class).build();
    // List<?> values = (List<?>) resultToReturn;
    // values.forEach(
    // v -> returnStatement.toBuilder().add(this.typeResolverInstance
    // .decodeResult(TypeName.get(v.getClass()), v)));
    // return returnStatement.toBuilder().add(")").build();
    CodeBlock returnStatement =
        CodeBlock.builder()
            .add("$T<?> values = ($T<?>) $L", List.class, List.class, resultToReturn)
            .addStatement("$T.of(", Arrays.class)
            .add("values.forEach(v->(")
            // .add(this.typeResolverInstance.decodeResult(
            // TypeName.get(values.get(0).getClass()), values.get(0)))
            .add("))")
            .build();
    // List<?> values = (List<?>) resultToReturn;
    // values.forEach(
    // v -> returnStatement.toBuilder().add(this.typeResolverInstance
    // .decodeResult(TypeName.get(v.getClass()), v)));
    return returnStatement.toBuilder().add(")").build();
  }

  public TypeName getReturnType(Object typeString) {
    // List<?> values = (List<?>) typeString;
    JsonArray values = (JsonArray) typeString;
    Object innerType = values.getValue(0);
    return ParameterizedTypeName.get(
        ClassName.get(List.class),
        typeResolverInstance.getTypeMapper(innerType).getReturnType(innerType));
  }

  @Override
  public boolean applies(Object typeString) {
    return "list".equalsIgnoreCase(valueToString(typeString))
        || typeString instanceof JsonArray
        || (typeString instanceof ParameterizedTypeName
            && ((ParameterizedTypeName) typeString).rawType.equals(ClassName.get(List.class)));
  }
}
