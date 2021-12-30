package com.kryptokrauts.codegen.datatypes.defaults;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kryptokrauts.codegen.maven.ABIJsonConfiguration;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;

public class ChainTTLType implements CustomType {

  private static final String CP_TTL = "ttl";

  private static final String TTL_TYPE_ENUM = "ChainTTLType";

  private static final String CP_TTL_TYPE = "ttlType";

  @Override
  public JsonObject getTypeDefinition(ABIJsonConfiguration abiJsonConfiguration) {
    return new JsonObject()
        .put(abiJsonConfiguration.getCustomTypeNameElement(), CHAIN_TTL_TYPE)
        .put(
            abiJsonConfiguration.getCustomTypeTypedefElement(),
            new JsonObject()
                .put(
                    RECORD,
                    new JsonArray()
                        .add(
                            new JsonObject()
                                .put(abiJsonConfiguration.getCustomTypeTypedefNameElement(), CP_TTL)
                                .put(
                                    abiJsonConfiguration.getCustomTypeTypedefTypeElement(),
                                    "int"))));
  }

  @Override
  public List<FieldSpec> fieldList() {
    return Arrays.asList(
        FieldSpec.builder(ClassName.bestGuess(TTL_TYPE_ENUM), CP_TTL_TYPE).build());
  }

  @Override
  public CodeBlock encodeValueCodeblock(String MP_PARAM) {
    return CodeBlock.builder()
        .add("$L.getTTLType().toString()+$S+$L.getTtl()+$S", MP_PARAM, "(", MP_PARAM, ")")
        .build();
  }

  @Override
  public boolean complexReturnTypeMethod() {
    return true;
  }

  @Override
  public CodeBlock mapToReturnValueCodeblock(String MP_PARAM) {
    String P_ATTRIBUTES = "attributes";
    String P_CHAIN_TYPE_KEY = "chainTypeKey";
    return CodeBlock.builder()
        .beginControlFlow("try")
        .addStatement(
            "$T $L = new $T().readValue($T.encode($L),$T.class)",
            ParameterizedTypeName.get(Map.class, String.class, Object.class),
            P_ATTRIBUTES,
            ObjectMapper.class,
            Json.class,
            MP_PARAM,
            Map.class)
        .addStatement(
            "$T $L = $L.keySet().stream().findFirst().get()",
            String.class,
            P_CHAIN_TYPE_KEY,
            P_ATTRIBUTES)
        .addStatement(
            "return new $T($T.valueOf((($T)$L.get($L)).get(0)),$T.valueOf($L))",
            ClassName.bestGuess(CHAIN_TTL_TYPE),
            BigInteger.class,
            ParameterizedTypeName.get(ClassName.get(List.class), TypeName.get(Integer.class)),
            P_ATTRIBUTES,
            P_CHAIN_TYPE_KEY,
            ClassName.bestGuess(TTL_TYPE_ENUM),
            P_CHAIN_TYPE_KEY)
        .endControlFlow()
        .beginControlFlow("catch($T e)", Exception.class)
        .addStatement("e.printStackTrace()")
        .endControlFlow()
        .addStatement("return new $T()", ClassName.bestGuess(CHAIN_TTL_TYPE))
        .build();
  }

  @Override
  public MethodSpec constructorMethod() {
    return MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameter(BigInteger.class, CP_TTL)
        .addParameter(ClassName.bestGuess(TTL_TYPE_ENUM), CP_TTL_TYPE)
        .addStatement("this.$L = $L", CP_TTL, CP_TTL)
        .addStatement("this.$L = $L", CP_TTL_TYPE, CP_TTL_TYPE)
        .build();
  }

  @Override
  public List<MethodSpec> methodList() {
    return Arrays.asList(
        MethodSpec.methodBuilder("getTTLType")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return this.$L", CP_TTL_TYPE)
            .returns(ClassName.bestGuess(TTL_TYPE_ENUM))
            .build());
  }

  @Override
  public MethodSpec customToStringMethod() {
    return MethodSpec.methodBuilder("toString")
        .returns(String.class)
        .addModifiers(Modifier.PUBLIC)
        .addStatement("return this.$L+$S+this.$L", CP_TTL_TYPE, "=", CP_TTL)
        .build();
  }

  @Override
  public List<TypeSpec> additionalInnerTypes() {
    return Arrays.asList(
        TypeSpec.enumBuilder(TTL_TYPE_ENUM)
            .addModifiers(Modifier.PUBLIC)
            .addEnumConstant("RelativeTTL")
            .addEnumConstant("FixedTTL")
            .build());
  }
}
