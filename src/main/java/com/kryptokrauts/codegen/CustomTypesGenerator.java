package com.kryptokrauts.codegen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.kryptokrauts.codegen.datatypes.DatatypeMappingHandler;
import com.kryptokrauts.codegen.datatypes.defaults.AddressType;
import com.kryptokrauts.codegen.datatypes.defaults.BytesType;
import com.kryptokrauts.codegen.datatypes.defaults.ChainTTLType;
import com.kryptokrauts.codegen.datatypes.defaults.CustomType;
import com.kryptokrauts.codegen.datatypes.defaults.HashType;
import com.kryptokrauts.codegen.datatypes.defaults.OracleQueryType;
import com.kryptokrauts.codegen.datatypes.defaults.OracleType;
import com.kryptokrauts.codegen.datatypes.defaults.SignatureType;
import com.kryptokrauts.codegen.maven.ABIJsonConfiguration;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.javatuples.Pair;

/**
 * this class generates type definition for custom types which are added as static subclasses to the
 * generated contract class
 *
 * @author mitch
 */
@RequiredArgsConstructor
public class CustomTypesGenerator {

  // the default parameter name
  public static final String MP_PARAM = "p";

  // the encodeValue method name
  public static final String M_ENCODE_VALUE = "encodeValue";

  // the mapToReturnValue method name
  public static final String M_MAP_TO_RETURN_VALUE = "mapToReturnValue";

  @NonNull private DatatypeMappingHandler datatypeEncodingHandler;

  @NonNull private ABIJsonConfiguration abiJsonConfiguration;

  /*
   * this map contains standard sophia types which are automatically added as represented sublasses
   * to the generated contract
   */
  public static final Map<String, CustomType> PREDEFINED_TYPES =
      ImmutableMap.<String, CustomType>builder()
          .put(CustomType.ADDRESS_TYPE, new AddressType())
          .put(CustomType.HASH_TYPE, new HashType())
          .put(CustomType.SIGNATURE_TYPE, new SignatureType())
          .put(CustomType.ORACLE_TYPE, new OracleType())
          .put(CustomType.ORACLE_QUERY_TYPE, new OracleQueryType())
          .put(CustomType.CHAIN_TTL_TYPE, new ChainTTLType())
          .build();

  private Map<String, CustomType> INSTANCE_PREDEFINED_TYPES = new HashMap<>(PREDEFINED_TYPES);

  public String addByteType(int length) {
    String className = CustomType.BYTES_TYPE + length;
    INSTANCE_PREDEFINED_TYPES.put(className, new BytesType(length, className));
    return className;
  }

  public List<TypeSpec> generateCustomTypes(JsonArray customTypes, Object state) {
    INSTANCE_PREDEFINED_TYPES.forEach(
        (name, customType) -> customTypes.add(customType.getTypeDefinition(abiJsonConfiguration)));
    if (state != null) {
      customTypes.add(
          new JsonObject()
              .put(
                  abiJsonConfiguration.getCustomTypeNameElement(),
                  abiJsonConfiguration.getStateElement())
              .put(abiJsonConfiguration.getCustomTypeTypedefElement(), state));
    }

    return customTypes.stream()
        .map(typeDefinition -> generateCustomType(JsonObject.mapFrom(typeDefinition)))
        .collect(Collectors.toList());
  }

  /*
   * if variant is defined, it is an enum, currently mapped just to string with no value checking
   * therefore we need to add this to custom jackson mapping and handle it as normal alias
   */
  @Deprecated
  private void addJacksonMapper(JsonObject typeDefinition) {
    Object fieldDefinition =
        typeDefinition.getValue(abiJsonConfiguration.getCustomTypeTypedefElement());
    if (fieldDefinition instanceof JsonObject) {
      JsonObject typedef =
          typeDefinition.getJsonObject(abiJsonConfiguration.getCustomTypeTypedefElement());
      JsonArray record = typedef.getJsonArray(CustomType.RECORD);
      JsonArray variant = typedef.getJsonArray(CustomType.VARIANT);
      if (record == null && typedef != null) {
        if (variant != null) {
          String name =
              typeDefinition.getString(abiJsonConfiguration.getCustomTypeTypedefNameElement());
        }
      }
    }
  }

  private TypeSpec generateCustomType(JsonObject typeDefinition) {
    boolean isAlias = false;
    List<Pair<String, TypeName>> fields = new LinkedList<>();
    Object fieldDefinition =
        typeDefinition.getValue(abiJsonConfiguration.getCustomTypeTypedefElement());
    boolean isVariant = false;
    if (fieldDefinition instanceof JsonObject) {
      JsonObject typedef =
          typeDefinition.getJsonObject(abiJsonConfiguration.getCustomTypeTypedefElement());
      JsonArray record = typedef.getJsonArray(CustomType.RECORD);
      JsonArray variant = typedef.getJsonArray(CustomType.VARIANT);
      isVariant = variant != null;
      // if record is defined, it is a custom type
      if (record != null) {
        fields =
            record.stream()
                .map(
                    definition -> {
                      JsonObject oneField = JsonObject.mapFrom(definition);
                      String name =
                          oneField.getString(
                              abiJsonConfiguration.getCustomTypeTypedefNameElement());
                      TypeName type =
                          this.datatypeEncodingHandler.getTypeNameFromJSON(
                              oneField.getValue(
                                  abiJsonConfiguration.getCustomTypeTypedefTypeElement()));
                      return Pair.with(name, type);
                    })
                .collect(Collectors.toList());
      }
      // its a type alias
      else if (typedef != null) {
        isAlias = true;
        String name =
            typeDefinition.getString(abiJsonConfiguration.getCustomTypeTypedefNameElement());
        TypeName type = this.datatypeEncodingHandler.getTypeNameFromJSON(typedef);
        fields.add(Pair.with(name, type));
      }
    } else if (fieldDefinition instanceof String) {
      fields.add(Pair.with(fieldDefinition.toString(), TypeName.get(String.class)));
    } else {
      throw new RuntimeException(
          CodegenUtil.getBaseErrorMessage(
              CmpErrorCode.UNFORSEEN_CUSTOM_TYPEDEF,
              "This is an unforseen custom type definition",
              Arrays.asList(Pair.with("TypeDefinition", typeDefinition.encodePrettily()))));
    }
    String name =
        CodegenUtil.getUppercaseClassName(
            typeDefinition.getString(abiJsonConfiguration.getCustomTypeNameElement()));

    List<FieldSpec> additionalFields = new LinkedList<>();
    List<TypeSpec> additionalInnerTypes = new LinkedList<>();
    MethodSpec customToStringMethod = generateToString(fields);

    Optional<CustomType> predefinedType =
        INSTANCE_PREDEFINED_TYPES.entrySet().stream()
            .filter(n -> n.getKey().toLowerCase().equals(name.toLowerCase()))
            .map(Map.Entry::getValue)
            .findFirst();
    if (predefinedType.isPresent()) {
      additionalFields = predefinedType.get().fieldList();
      additionalInnerTypes = predefinedType.get().additionalInnerTypes();
      if (predefinedType.get().customToStringMethod() != null) {
        customToStringMethod = predefinedType.get().customToStringMethod();
      }
    }

    return TypeSpec.classBuilder(name)
        .addFields(buildFields(fields))
        .addFields(additionalFields)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .addMethods(generateCustomTypeConstructors(fields, name, predefinedType))
        .addMethods(generateCustomTypeGetters(fields))
        .addMethods(generateCustomTypeSetters(fields, name, predefinedType))
        .addMethod(generateEncodeValueMethod(fields, name, predefinedType, isVariant))
        .addMethod(customToStringMethod)
        .addMethod(generateEquals(fields, name))
        .addMethod(generateMapToReturnValueMethod(fields, name, predefinedType, isAlias))
        .addTypes(additionalInnerTypes)
        .build();
  }

  private MethodSpec generateEncodeValueMethod(
      List<Pair<String, TypeName>> fields,
      String customTypeName,
      Optional<CustomType> customType,
      boolean isVariant) {
    CodeBlock encodeValueLogic = null;
    if (isVariant && fields.size() == 1) {
      Pair<String, TypeName> f = fields.get(0);
      encodeValueLogic =
          CodeBlock.builder()
              .add(
                  this.datatypeEncodingHandler.encodeParameter(
                      f.getValue1(), MP_PARAM + "." + f.getValue0()))
              .build();
    } else {
      encodeValueLogic =
          CodeBlock.builder()
              .add(" \"{")
              .add(
                  fields.stream()
                      .map(
                          f ->
                              f.getValue0()
                                  + "=\"+"
                                  + this.datatypeEncodingHandler.encodeParameter(
                                      f.getValue1(), MP_PARAM + "." + f.getValue0())
                                  + "+\"")
                      .collect(Collectors.joining(",")))
              .add("}\"")
              .build();
    }
    // if it is a predefined class use this encoding logic
    if (customType.isPresent()) {
      encodeValueLogic = customType.get().encodeValueCodeblock(MP_PARAM);
    }

    return MethodSpec.methodBuilder(M_ENCODE_VALUE)
        .addCode("return ")
        .addCode(encodeValueLogic)
        .addCode(";")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .addParameter(this.getParameterSpec(customTypeName))
        .returns(TypeName.get(String.class))
        .build();
  }

  private MethodSpec generateMapToReturnValueMethod(
      List<Pair<String, TypeName>> fields,
      String customTypeName,
      Optional<CustomType> customType,
      boolean isAlias) {

    String VAR_ATTRIBUTE_MAP = "attributes";
    String VAR_ATTRIBUTE_KEY = "attributeKey";
    String VAR_ATTRIBUTE_VALUE = "attributeValue";

    CodeBlock returnValueLogic;
    if (isAlias) {
      returnValueLogic =
          CodeBlock.builder()
              .addStatement(
                  "return new $T($L)",
                  this.getClassName(customTypeName),
                  this.datatypeEncodingHandler.mapToReturnValue(
                      fields.get(0).getValue1(), MP_PARAM))
              .build();
    } else {

      CodeBlock initFields = CodeBlock.builder().build();
      for (Pair<String, TypeName> field : fields) {
        initFields =
            initFields.toBuilder()
                .addStatement("$T $L = null", field.getValue1(), field.getValue0())
                .build();
      }

      CodeBlock returnBlock =
          CodeBlock.builder()
              .add("return new $T(", this.getClassName(customTypeName))
              .add(
                  CodeBlock.join(
                      fields.stream()
                          .map(
                              field -> {
                                return CodeBlock.of("$L", field.getValue0());
                              })
                          .collect(Collectors.toList()),
                      ","))
              .add(");")
              .build();

      CodeBlock mapIndividualFields = CodeBlock.builder().build();
      for (Pair<String, TypeName> field : fields) {
        mapIndividualFields =
            mapIndividualFields.toBuilder()
                .beginControlFlow("if($S.equals($L))", field.getValue0(), VAR_ATTRIBUTE_KEY)
                .addStatement(
                    "$L = $L",
                    field.getValue0(),
                    this.datatypeEncodingHandler.mapToReturnValue(
                        field.getValue1(), VAR_ATTRIBUTE_VALUE))
                .endControlFlow()
                .build();
      }

      returnValueLogic =
          CodeBlock.builder()
              .add(initFields)
              .beginControlFlow("try")
              .addStatement(
                  "$T $L = new $T().readValue($T.encode($L),$T.class)",
                  ParameterizedTypeName.get(Map.class, String.class, Object.class),
                  VAR_ATTRIBUTE_MAP,
                  ObjectMapper.class,
                  Json.class,
                  MP_PARAM,
                  Map.class)
              .beginControlFlow(
                  "for($T $L : $L.keySet())", String.class, VAR_ATTRIBUTE_KEY, VAR_ATTRIBUTE_MAP)
              .addStatement(
                  "$T $L = $L.get($L)",
                  Object.class,
                  VAR_ATTRIBUTE_VALUE,
                  VAR_ATTRIBUTE_MAP,
                  VAR_ATTRIBUTE_KEY)
              .add(mapIndividualFields)
              .endControlFlow()
              .endControlFlow()
              .beginControlFlow("catch($T e)", Exception.class)
              .addStatement("e.printStackTrace()")
              .endControlFlow()
              .add(returnBlock)
              .build();
    }
    // if it is a predefined class use this encoding logic
    if (customType.isPresent()) {
      if (customType.get().complexReturnTypeMethod()) {
        returnValueLogic = customType.get().mapToReturnValueCodeblock(MP_PARAM);
      } else {
        returnValueLogic =
            CodeBlock.builder()
                .add("return ")
                .add(customType.get().mapToReturnValueCodeblock(MP_PARAM))
                .add(";")
                .build();
      }
    }

    return MethodSpec.methodBuilder(M_MAP_TO_RETURN_VALUE)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .addParameter(TypeName.get(Object.class), MP_PARAM)
        .returns(this.getClassName(customTypeName))
        .addCode(returnValueLogic)
        .build();
  }

  private List<MethodSpec> generateCustomTypeConstructors(
      List<Pair<String, TypeName>> fields, String customTypeName, Optional<CustomType> customType) {
    List<MethodSpec> constructors = new LinkedList<>();

    MethodSpec valueConstructor =
        MethodSpec.constructorBuilder()
            .addParameters(
                fields.stream()
                    .map(v -> ParameterSpec.builder(v.getValue1(), v.getValue0()).build())
                    .collect(Collectors.toList()))
            .addCode(generateConstructorInitializer(fields))
            .addModifiers(Modifier.PUBLIC)
            .build();

    // if it is a predefined class add constructor logic
    if (customType.isPresent()) {
      valueConstructor = customType.get().constructorMethod();
    }
    constructors.add(valueConstructor);
    // add default constructor if fields are defined
    if (fields.size() > 0) {
      constructors.add(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build());
    }
    return constructors;
  }

  private CodeBlock generateConstructorInitializer(List<Pair<String, TypeName>> fields) {
    CodeBlock initializer = CodeBlock.builder().build();
    for (Pair<String, TypeName> pair : fields) {
      initializer =
          initializer.toBuilder()
              .addStatement("this.$L = $L", pair.getValue0(), pair.getValue0())
              .build();
    }
    return initializer;
  }

  private MethodSpec generateToString(List<Pair<String, TypeName>> fields) {
    return MethodSpec.methodBuilder("toString")
        .returns(TypeName.get(String.class))
        .addModifiers(Modifier.PUBLIC)
        .addStatement(
            "return "
                + ((fields != null && fields.size() > 0)
                    ? fields.stream()
                        .map(
                            f ->
                                "\""
                                    + f.getValue0()
                                    + "=\"+(this."
                                    + f.getValue0()
                                    + " != null?this."
                                    + f.getValue0()
                                    + ".toString():\"\")")
                        .collect(Collectors.joining("+\",\"+"))
                    : "\"\""))
        .build();
  }

  private MethodSpec generateEquals(List<Pair<String, TypeName>> fields, String customTypeName) {
    String MP_OBJ = "obj";
    String MP_OTHER = "other";
    return MethodSpec.methodBuilder("equals")
        .returns(TypeName.BOOLEAN)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(TypeName.OBJECT, MP_OBJ)
        .addStatement("if(this == $L) return true", MP_OBJ)
        .addStatement("if($L == null) return false", MP_OBJ)
        .addStatement("if(getClass() != $L.getClass()) return false", MP_OBJ)
        .addStatement(
            "$T other = ($T)$L", getClassName(customTypeName), getClassName(customTypeName), MP_OBJ)
        .addStatement(
            fields.stream()
                .map(
                    f -> {
                      if (f.getValue1().isPrimitive()) {
                        return CodeBlock.builder()
                            .addStatement(
                                "return if($L != $L.$L )", f.getValue0(), MP_OTHER, f.getValue0())
                            .build()
                            .toString();
                      } else
                        return CodeBlock.builder()
                            .beginControlFlow("if($L == null)", f.getValue0())
                            .addStatement("if($L.$L == null) return false", MP_OTHER, f.getValue0())
                            .endControlFlow()
                            .addStatement(
                                "else if(!$L.equals($L.$L)) return false",
                                f.getValue0(),
                                MP_OTHER,
                                f.getValue0())
                            .build()
                            .toString();
                    })
                .collect(Collectors.joining()))
        .addStatement("return true")
        .build();
  }

  private List<MethodSpec> generateCustomTypeGetters(List<Pair<String, TypeName>> fields) {
    return fields.stream()
        .map(
            field ->
                MethodSpec.methodBuilder(
                        "get"
                            + field.getValue0().substring(0, 1).toUpperCase()
                            + field.getValue0().substring(1))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(field.getValue1())
                    .addStatement("return this.$L", field.getValue0())
                    .build())
        .collect(Collectors.toList());
  }

  private List<MethodSpec> generateCustomTypeSetters(
      List<Pair<String, TypeName>> fields, String customTypeName, Optional<CustomType> customType) {

    if (customType.isPresent()) {
      return customType.get().methodList();
    }

    return fields.stream()
        .map(
            field ->
                MethodSpec.methodBuilder(
                        "set"
                            + field.getValue0().substring(0, 1).toUpperCase()
                            + field.getValue0().substring(1))
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("this.$L = $L", field.getValue0(), field.getValue0())
                    .addParameter(field.getValue1(), field.getValue0())
                    .build())
        .collect(Collectors.toList());
  }

  private List<FieldSpec> buildFields(List<Pair<String, TypeName>> fields) {
    return fields.stream()
        .map(
            field ->
                FieldSpec.builder(field.getValue1(), field.getValue0(), Modifier.PRIVATE).build())
        .collect(Collectors.toList());
  }

  private ClassName getClassName(String customTypeName) {
    return ClassName.get("", customTypeName);
  }

  private ParameterSpec getParameterSpec(String customTypeName) {
    return ParameterSpec.builder(this.getClassName(customTypeName), MP_PARAM).build();
  }
}
