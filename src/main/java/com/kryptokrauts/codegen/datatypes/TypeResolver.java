package com.kryptokrauts.codegen.datatypes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.javatuples.Pair;
import org.javatuples.Triplet;

public class TypeResolver {

  public static void main(String[] args) throws Exception {
    TypeResolver r = new TypeResolver();
    ObjectMapper mapper = new ObjectMapper();
    Map parsed = mapper.readValue(r.listListMapType, Map.class);
    System.out.println(parsed.get("type"));
    // Map parsed2 = (Map) parsed.get("type");
    // Object key = parsed2.keySet().toArray()[0];
    // Object value = parsed2.get(key);
    // System.out.println("parsed2: " + parsed2);
    // System.out.println("key:" + key);
    // System.out.println("value: " + value);
    // System.out.println(value instanceof List<?>);

    TypeName result = new TypeResolver().getReturnType(parsed.get("type"));

    MethodSpec testIt = MethodSpec.methodBuilder("testIt").returns(result).build();

    TypeSpec contractTypeSpec =
        TypeSpec.classBuilder("TestClass")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addMethod(testIt)
            .build();

    JavaFile javaFile =
        JavaFile.builder("com.kryptokrauts.generated.test", contractTypeSpec).build();

    Path path = Paths.get("", "target/generated-sources/test");

    javaFile.writeTo(path);

    // TypeName resolved = getTypeMapper(key).getReturnType(value);
    // System.out.println(resolved);
  }

  private void resolveType(Map parsedType) {
    MethodSpec loadListInteger =
        MethodSpec.methodBuilder("loadListInteger")
            .returns(ParameterizedTypeName.get(List.class, Integer.class))
            .addParameter(ParameterizedTypeName.get(List.class, Integer.class), "list")
            .addParameter(
                ParameterizedTypeName.get(
                    ClassName.get(List.class),
                    ParameterizedTypeName.get(List.class, Integer.class)),
                "t1")
            .build();
  }

  private static List<SophiaTypeMapper> typeMapperList =
      Arrays.asList(new IntMapper(null), new ListMapper(null));

  /**
   * get the return statement which intializes the resultType with the value
   *
   * @param classType
   * @param result
   * @return
   */
  public CodeBlock mapReturnTypeFromCall(Object classType, String result) {
    return typeMapperList.stream()
        .filter(t -> t.applies(classType))
        .findFirst()
        .orElse(new DefaultMapper(null))
        .getReturnStatement(result);
  }

  private CodeBlock mapReturnTypeFromCall(
      Object classType, String result, SophiaTypeMapper parent) {
    return typeMapperList.stream()
        .filter(t -> t.applies(classType))
        .findFirst()
        .orElse(new DefaultMapper(null))
        .getReturnStatement(result);
  }

  public static SophiaTypeMapper getTypeMapper(Object typeString) {
    return typeMapperList.stream()
        .filter(t -> t.applies(typeString))
        .findFirst()
        .orElse(new DefaultMapper(null));
  }

  /**
   * maps aeternity type to java type
   *
   * @param classType
   * @return
   */
  private Type mapClass(Object classType) {
    return typeMapperList.stream()
        .filter(t -> t.applies(classType))
        .findFirst()
        .orElse(new DefaultMapper(null))
        .getJavaType();
  }

  // problem - wenn rückgabewert konstruiert werden soll - Logik muss in
  // eigenen Mapper ausgelagert werden
  protected TypeName getReturnType(Object typeDefinition) {
    if (typeDefinition instanceof Map) {
      Set keySet = ((Map) typeDefinition).keySet();

      if (keySet.size() == 1) {
        Object key = keySet.iterator().next();
        if ("list".equalsIgnoreCase(key.toString())) {
          return ParameterizedTypeName.get(
              ClassName.get(List.class), getReturnType(((Map) typeDefinition).get(key)));
        } else if ("map".equalsIgnoreCase(key.toString())) {
          List mapValues = (List) ((Map) typeDefinition).get(key);
          if (mapValues.size() == 2) {
            return ParameterizedTypeName.get(
                ClassName.get(Map.class),
                getReturnType(mapValues.get(0)),
                getReturnType(mapValues.get(1)));
          }
        } else if ("tuple".equalsIgnoreCase(key.toString())) {
          List<?> tupleValues = (List<?>) ((Map<?, ?>) typeDefinition).get(key);
          System.out.println("its a tuple of length " + tupleValues.size());
          switch (tupleValues.size()) {
            case 2:
              return ParameterizedTypeName.get(
                  ClassName.get(Pair.class),
                  getReturnType(tupleValues.get(0)),
                  getReturnType(tupleValues.get(1)));
            case 3:
              return ParameterizedTypeName.get(
                  ClassName.get(Triplet.class),
                  getReturnType(tupleValues.get(0)),
                  getReturnType(tupleValues.get(1)),
                  getReturnType(tupleValues.get(2)));
          }
        }
      }
    } else if (typeDefinition instanceof List) {
      System.out.println("its a list");
      List list = (List) typeDefinition;
      if (list.size() == 1) {
        return getReturnType(list.get(0));
      } else {
        System.out.println("list definition has more than one element");
      }
    } else if (typeDefinition instanceof String) {
      switch (typeDefinition.toString().toLowerCase()) {
        case "int":
          return ClassName.get(Integer.class);
        case "bool":
          return ClassName.get(Boolean.class);
        case "string":
          return ClassName.get(String.class);
          // case "address":
          // return ClassName.get(Address.class);
      }
    }
    throw new RuntimeException("should not be here");
  }

  private String listMapType =
      "{\"type\": {\r\n"
          + "					\"list\": [\r\n"
          + "						{\r\n"
          + "							\"map\": [\r\n"
          + "								\"int\",\r\n"
          + "								\"bool\"\r\n"
          + "							]\r\n"
          + "						}\r\n"
          + "					]\r\n"
          + "				}}";

  private String listListMapType =
      "{\"type\":{\r\n"
          + "					\"list\": [\r\n"
          + "						{\r\n"
          + "							\"list\": [\r\n"
          + "								{\r\n"
          + "									\"map\": [\r\n"
          + "										\"int\",\r\n"
          + "										\"bool\"\r\n"
          + "									]\r\n"
          + "								}\r\n"
          + "							]\r\n"
          + "						}\r\n"
          + "					]\r\n"
          + "				}}";

  private String mapType =
      "{\"type\": {\r\n"
          + "							\"map\": [\r\n"
          + "								\"int\",\r\n"
          + "								\"bool\"\r\n"
          + "							]\r\n"
          + "						}}";

  private String justInt = "{\"type\": \"int\"}";

  private String justTuple =
      "{\"type\": {\r\n"
          + "					\"tuple\": [\r\n"
          + "						\"int\",\r\n"
          + "						\"int\",\r\n"
          + "						\"string\"\r\n"
          + "					]\r\n"
          + "				}}";

  private String listTuple =
      "{\"type\": {\r\n"
          + "						\"list\": [\r\n"
          + "						{\r\n"
          + "							\"tuple\": [\r\n"
          + "								\"int\",\r\n"
          + "								\"int\",\r\n"
          + "								\"string\"\r\n"
          + "							]\r\n"
          + "						}\r\n"
          + "					]"
          + "					}}";

  private String listIntType =
      "{\"type\": {\r\n"
          + "						\"list\": [\r\n"
          + "							\"int\"\r\n"
          + "						]\r\n"
          + "					}}";

  private String sophiaTypes =
      "{\"type\": {\r\n"
          + "						\"list\": [\r\n"
          + "							{\r\n"
          + "								\"tuple\": [\r\n"
          + "									\"address\",\r\n"
          + "									\"int\"\r\n"
          + "								]\r\n"
          + "							}\r\n"
          + "						]\r\n"
          + "					}}";
}
