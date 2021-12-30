package com.kryptokrauts.codegen.datatypes.defaults;

import com.kryptokrauts.codegen.CodegenUtil;
import com.kryptokrauts.codegen.maven.ABIJsonConfiguration;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;

public class AENSNameType implements CustomType {

  private static final String CP_CHAIN_TTL = "chainTTL";

  private static final String CP_POINTERS = "pointers";

  private static final ClassName POINTEE_CLASSNAME =
      ClassName.get("", CodegenUtil.getUppercaseClassName(POINTEE_TYPE));

  private static final ParameterizedTypeName POINTERS_MAP_TYPE =
      ParameterizedTypeName.get(
          ClassName.get(Map.class), TypeName.get(String.class), POINTEE_CLASSNAME.box());

  private static final ClassName CHAIN_TTL_CLASSNAME =
      ClassName.get("", CodegenUtil.getUppercaseClassName(CHAIN_TTL_TYPE));

  private static final ClassName AENS_NAME_CLASSNAME = ClassName.get("", AENS_NAME_TYPE);

  private static final ClassName ADDRESS_TYPE_CLASSNAME =
      ClassName.get("", CodegenUtil.getUppercaseClassName(ADDRESS_TYPE));

  @Override
  public JsonObject getTypeDefinition(ABIJsonConfiguration abiJsonConfiguration) {
    return new JsonObject()
        .put(abiJsonConfiguration.getCustomTypeNameElement(), AENS_NAME_TYPE)
        .put(
            abiJsonConfiguration.getCustomTypeTypedefElement(),
            new JsonObject()
                .put(
                    RECORD,
                    new JsonArray()
                        .add(
                            new JsonObject()
                                .put(
                                    abiJsonConfiguration.getCustomTypeTypedefNameElement(),
                                    ADDRESS_TYPE)
                                .put(
                                    abiJsonConfiguration.getCustomTypeTypedefTypeElement(),
                                    ADDRESS_TYPE))))
        .put(abiJsonConfiguration.getCustomTypeAbstractClass(), Boolean.FALSE);
  }

  @Override
  public List<FieldSpec> fieldList() {
    return List.of(
        FieldSpec.builder(CHAIN_TTL_CLASSNAME, CP_CHAIN_TTL)
            .addModifiers(Modifier.PROTECTED)
            .build(),
        FieldSpec.builder(POINTERS_MAP_TYPE, CP_POINTERS, Modifier.PROTECTED).build());
  }

  @Override
  public CodeBlock encodeValueCodeblock(String MP_PARAM) {
    return CodeBlock.builder()
        .add(
            "$S+$T.encodeValue($L.getAddress())+$S+$T.encodeValue($L.getChainTTL())+$S+$L.getPointers().keySet().stream().map(r->$S+r+$S+$T.encodeValue($L.getPointers().get(r))).collect($T.joining($S))+$S",
            "AENS.Name(",
            ADDRESS_TYPE_CLASSNAME,
            MP_PARAM,
            ",",
            CHAIN_TTL_CLASSNAME,
            MP_PARAM,
            ",{",
            MP_PARAM,
            "[\"",
            "\"]=",
            POINTEE_CLASSNAME,
            MP_PARAM,
            Collectors.class,
            ",",
            "})")
        .build();
  }

  @Override
  public CodeBlock mapToReturnValueCodeblock(String MP_PARAM) {
    return CodeBlock.builder().add("$N($L)", "mapInternal", MP_PARAM).build();
  }

  @Override
  public MethodSpec constructorMethod() {
    return MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterSpec.builder(ADDRESS_TYPE_CLASSNAME, ADDRESS_TYPE).build())
        .addParameter(ParameterSpec.builder(CHAIN_TTL_CLASSNAME, CP_CHAIN_TTL).build())
        .addParameter(ParameterSpec.builder(POINTERS_MAP_TYPE, CP_POINTERS).build())
        .addStatement("this.$L=$L", ADDRESS_TYPE, ADDRESS_TYPE)
        .addStatement("this.$L=$L", CP_CHAIN_TTL, CP_CHAIN_TTL)
        .addStatement("this.$L=$L", CP_POINTERS, CP_POINTERS)
        .build();
  }

  @Override
  public MethodSpec customToStringMethod() {
    return MethodSpec.methodBuilder("toString")
        .returns(String.class)
        .addModifiers(Modifier.PUBLIC)
        .addStatement(
            "return $S+this.$L+$S+this.$L.toString()+$S+this.$L.toString()+$S",
            "{",
            ADDRESS_TYPE,
            ",",
            CP_CHAIN_TTL,
            ",",
            CP_POINTERS,
            "}")
        .build();
  }

  @Override
  public List<MethodSpec> methodList() {
    String MP_IDENTIFIER = "identifier";
    String MP_POINTEE = "pointee";
    String MP_PARAM = "p";

    String P_JSONARRAY = "jsonArray";
    String P_NAME = "name";
    String P_POINTER = "ptr";

    return List.of(
        MethodSpec.methodBuilder("getPointers")
            .addModifiers(Modifier.PUBLIC)
            .returns(POINTERS_MAP_TYPE)
            .addStatement("return this.$L", CP_POINTERS)
            .build(),
        MethodSpec.methodBuilder("getChainTTL")
            .addModifiers(Modifier.PUBLIC)
            .returns(ClassName.get("", CHAIN_TTL_TYPE))
            .addStatement("return this.$L", CP_CHAIN_TTL)
            .build(),
        MethodSpec.methodBuilder("addPointer")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, MP_IDENTIFIER)
            .addParameter(ClassName.get("", POINTEE_TYPE), MP_POINTEE)
            .beginControlFlow("if ($L == null)", CP_POINTERS)
            .addStatement(
                "$L = new $T()",
                CP_POINTERS,
                ParameterizedTypeName.get(
                    ClassName.get(HashMap.class),
                    TypeName.get(String.class),
                    POINTEE_CLASSNAME.box()))
            .endControlFlow()
            .addStatement("$L.put($L,$L)", CP_POINTERS, MP_IDENTIFIER, MP_POINTEE)
            .build(),
        MethodSpec.methodBuilder("mapInternal")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .addParameter(Object.class, MP_PARAM)
            .returns(AENS_NAME_CLASSNAME)
            .addStatement(
                "$T $L = $T.mapFrom($L).getJsonArray($S)",
                JsonArray.class,
                P_JSONARRAY,
                JsonObject.class,
                MP_PARAM,
                "AENS.Name")
            .addStatement(
                "$T $L = new $T($T.mapToReturnValue($L.getValue(0)),$T.mapToReturnValue($L.getValue(1)),null)",
                AENS_NAME_CLASSNAME,
                P_NAME,
                AENS_NAME_CLASSNAME,
                ADDRESS_TYPE_CLASSNAME,
                P_JSONARRAY,
                CHAIN_TTL_CLASSNAME,
                P_JSONARRAY)
            .beginControlFlow("$L.getJsonArray(2).forEach(e->", P_JSONARRAY)
            .addStatement("$T $L = ($T)e", JsonArray.class, P_POINTER, JsonArray.class)
            .addStatement(
                "$L.addPointer($L.getString(0),$T.mapToReturnValue($L.getValue(1)))",
                P_NAME,
                P_POINTER,
                POINTEE_CLASSNAME,
                P_POINTER)
            .endControlFlow(");")
            .addStatement("return $L", P_NAME)

            // JsonArray arr = values.getJsonArray(2);
            // arr.forEach(e -> {
            // JsonArray ptr = (JsonArray) e;
            // System.out.println(ptr);
            // n.addPointer(ptr.getString(0), Pointee.mapToReturnValue(ptr.getValue(1)));
            // });
            .build());
  }

  // @Override
  // public List<MethodSpec> methodList() {
  // String MP_PARAM = "p";
  // String P_JSONOBJECT = "jsonObject";
  // String P_KEY = "key";
  // String P_VALUE = "value";
  // return Arrays.asList(
  // MethodSpec.methodBuilder("getType").addModifiers(Modifier.PUBLIC)
  // .returns(ClassName.get("", CodegenUtil.getUppercaseClassName(POINTEE_ENUM_TYPE)))
  // .addStatement("return this.$L", CP_TYPE).build(),
  // MethodSpec.methodBuilder("mapInternal").addModifiers(Modifier.PRIVATE, Modifier.STATIC)
  // .addParameter(ParameterSpec.builder(Object.class, MP_PARAM).build())
  // .returns(ClassName.get("", CodegenUtil.getUppercaseClassName(POINTEE_TYPE)))
  // .addStatement("$T $L = $T.mapFrom($L)", JsonObject.class, P_JSONOBJECT,
  // JsonObject.class, MP_PARAM)
  // .addStatement("$T $L = $L.iterator().next().getKey()", String.class, P_KEY,
  // P_JSONOBJECT)
  // .addStatement("$T $L = $L.getJsonArray($L).getValue(0)", Object.class, P_VALUE,
  // P_JSONOBJECT, P_KEY)
  // .addStatement(
  // "return new $T(new $T($L.toString()),$T.valueOf(key.replace(\"AENS.\", \"\")))",
  // ClassName.get("", CodegenUtil.getUppercaseClassName(POINTEE_TYPE)),
  // ClassName.get("", CodegenUtil.getUppercaseClassName(ADDRESS_TYPE)), P_VALUE,
  // ClassName.get("", CodegenUtil.getUppercaseClassName(POINTEE_ENUM_TYPE)))
  // .build());
  // }
}
