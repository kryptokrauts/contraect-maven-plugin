package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.javatuples.Decade;
import org.javatuples.Ennead;
import org.javatuples.Octet;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Septet;
import org.javatuples.Sextet;
import org.javatuples.Triplet;
import org.javatuples.Tuple;
import org.javatuples.Unit;

public class TupleMapper extends AbstractDatatypeMapper {

  public TupleMapper(DatatypeMappingHandler resolveInstance) {
    super(resolveInstance);
  }

  public boolean applies(TypeName type) {
    if (type instanceof ParameterizedTypeName) {
      return ((ParameterizedTypeName) type)
          .rawType
          .packageName()
          .equals(Tuple.class.getPackage().getName());
    }
    return false;
  }

  @Override
  public boolean appliesToJSON(Object typeDefForResolvingInnerClass) {
    return parseToTupleType(typeDefForResolvingInnerClass) != null;
  }

  /**
   * tuples are mapped as one string, separated by comma and enclosed by brackets: ("hello",0,true)
   */
  public CodeBlock encodeValue(TypeName type, String variableName) {
    AtomicInteger index = new AtomicInteger(0);
    return CodeBlock.builder()
        .add("\"(\"+")
        .add(
            this.getInnerTypes(type).stream()
                .map(
                    t ->
                        this.resolveInstance
                            .encodeParameter(
                                t, variableName + ".getValue" + index.getAndIncrement() + "()")
                            .toString())
                .collect(Collectors.joining(","))
                .replace(",", "+\",\"+"))
        .add("+\")\"")
        .build();
  }

  public CodeBlock mapToReturnValue(TypeName type, String variableName) {
    AtomicInteger cnt = new AtomicInteger();
    return CodeBlock.builder()
        .add("$T.with(", getTupleSizeClass(this.getInnerTypes(type).size()))
        .add(
            this.getInnerTypes(type).stream()
                .map(
                    t -> {
                      CodeBlock currentListElement =
                          CodeBlock.builder()
                              .add(
                                  "(($T)(($T)$L).getList()).get($L)",
                                  LIST_WITH_WILDCARD_TYPDEF,
                                  JsonArray.class,
                                  variableName,
                                  cnt.getAndIncrement())
                              .build();
                      return this.resolveInstance
                          .mapToReturnValue(t, currentListElement.toString())
                          .toString();
                    })
                .collect(Collectors.joining(",")))
        .add(")")
        .build();
  }

  @Override
  public TypeName getTypeNameFromJSON(Object type) {
    return parseToTupleType(type);
  }

  private TypeName parseToTupleType(Object type) {
    if (type != null) {
      if (type instanceof JsonObject) {
        Object list = JsonObject.mapFrom(type).getValue(JSON_TUPLE_IDENTIFIER);
        if (list instanceof JsonArray) {
          JsonArray tupleTypesArray = (JsonArray) list;
          return ParameterizedTypeName.get(
              ClassName.get(getTupleSizeClass(tupleTypesArray.size())),
              this.getTupleTypes(tupleTypesArray));
        }
      }
    }
    return null;
  }

  private Class<?> getTupleSizeClass(int size) {
    switch (size) {
      case 1:
        return Unit.class;
      case 2:
        return Pair.class;
      case 3:
        return Triplet.class;
      case 4:
        return Quartet.class;
      case 5:
        return Quintet.class;
      case 6:
        return Sextet.class;
      case 7:
        return Septet.class;
      case 8:
        return Octet.class;
      case 9:
        return Ennead.class;
      case 10:
        return Decade.class;
      default:
        throw new UnsupportedOperationException(
            String.format(
                "Given tuple datatype is of size %s - tuples with size > 10 are currently not supported",
                size));
    }
  }

  private TypeName[] getTupleTypes(JsonArray innerTypes) {
    return innerTypes.stream()
        .map(innerType -> this.resolveInstance.getTypeNameFromJSON(innerType))
        .collect(Collectors.toList())
        .toArray(new TypeName[innerTypes.size()]);
  }

  private List<TypeName> getInnerTypes(TypeName type) {
    return ((ParameterizedTypeName) type).typeArguments;
  }
}
