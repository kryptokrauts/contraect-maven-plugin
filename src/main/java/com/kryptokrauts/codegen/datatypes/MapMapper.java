package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.javatuples.Pair;
import org.javatuples.Triplet;

public class MapMapper extends AbstractSophiaTypeMapper {

  public MapMapper(TypeResolverRefactored typeResolverInstance) {
    super(typeResolverInstance);
  }

  @Override
  public boolean applies(Object typeString) {
    return typeString instanceof Map;
  }

  @Override
  public TypeName getReturnType(Object typeString) {
    Set<?> keySet = ((Map<?, ?>) typeString).keySet();

    // it must be a key of size 1 which holds the datatype definition - it's
    // one of
    if (keySet.size() == 1) {
      Object key = keySet.iterator().next();
      // list type
      if ("list".equalsIgnoreCase(key.toString())) {
        return typeResolverInstance
            .getTypeMapper("list")
            .getReturnType(((Map<?, ?>) typeString).get(key));
      }
      // map type
      else if ("map".equalsIgnoreCase(key.toString())) {
        List<?> mapValues = (List<?>) ((Map<?, ?>) typeString).get(key);
        if (mapValues.size() == 2) {
          return ParameterizedTypeName.get(
              ClassName.get(Map.class),
              typeResolverInstance.getTypeMapper(mapValues.get(0)).getReturnType(mapValues.get(0)),
              typeResolverInstance.getTypeMapper(mapValues.get(1)).getReturnType(mapValues.get(0)));
        }
      }
      // tuple type
      else if ("tuple".equalsIgnoreCase(key.toString())) {
        List<?> tupleValues = (List<?>) ((Map<?, ?>) typeString).get(key);
        _logger.debug("Resolved tuple of length " + tupleValues.size());
        switch (tupleValues.size()) {
          case 2:
            return ParameterizedTypeName.get(
                ClassName.get(Pair.class),
                typeResolverInstance
                    .getTypeMapper(tupleValues.get(0))
                    .getReturnType(tupleValues.get(0)),
                typeResolverInstance
                    .getTypeMapper(tupleValues.get(1))
                    .getReturnType(tupleValues.get(1)));
          case 3:
            return ParameterizedTypeName.get(
                ClassName.get(Triplet.class),
                typeResolverInstance
                    .getTypeMapper(tupleValues.get(0))
                    .getReturnType(tupleValues.get(0)),
                typeResolverInstance
                    .getTypeMapper(tupleValues.get(1))
                    .getReturnType(tupleValues.get(1)),
                typeResolverInstance
                    .getTypeMapper(tupleValues.get(2))
                    .getReturnType(tupleValues.get(2)));
        }
      }
    }
    throw new RuntimeException(
        getUnsupportedMappingException(
            typeString, this.getClass().getName(), "getReturnType", "tuple size > 10"));
  }

  @Override
  public CodeBlock getReturnStatement(Object resultToReturn) {
    // TODO Auto-generated method stub
    return null;
  }
}
