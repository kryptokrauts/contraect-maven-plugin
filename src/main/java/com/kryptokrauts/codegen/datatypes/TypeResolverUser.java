package com.kryptokrauts.codegen.datatypes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import javax.lang.model.element.Modifier;

public class TypeResolverUser {

  public static void main(String[] args) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    Map parsed = mapper.readValue(sophiaTypes, Map.class);
    System.out.println(parsed.get("type"));
    // Map parsed2 = (Map) parsed.get("type");
    // Object key = parsed2.keySet().toArray()[0];
    // Object value = parsed2.get(key);
    // System.out.println("parsed2: " + parsed2);
    // System.out.println("key:" + key);
    // System.out.println("value: " + value);
    // System.out.println(value instanceof List<?>);

    TypeName result = new TypeResolverRefactored().getReturnType(parsed.get("type"));

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

  private static String listMapType =
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

  private static String listListMapType =
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

  private static String mapType =
      "{\"type\": {\r\n"
          + "							\"map\": [\r\n"
          + "								\"int\",\r\n"
          + "								\"bool\"\r\n"
          + "							]\r\n"
          + "						}}";

  private static String justInt = "{\"type\": \"int\"}";

  private static String justTuple =
      "{\"type\": {\r\n"
          + "					\"tuple\": [\r\n"
          + "						\"int\",\r\n"
          + "						\"int\",\r\n"
          + "						\"string\"\r\n"
          + "					]\r\n"
          + "				}}";

  private static String listTuple =
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

  private static String listIntType =
      "{\"type\": {\r\n"
          + "						\"list\": [\r\n"
          + "							\"int\"\r\n"
          + "						]\r\n"
          + "					}}";

  private static String sophiaTypes =
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
