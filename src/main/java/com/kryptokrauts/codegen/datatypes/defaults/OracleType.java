package com.kryptokrauts.codegen.datatypes.defaults;

import com.kryptokrauts.aeternity.sdk.constants.ApiIdentifiers;
import com.kryptokrauts.aeternity.sdk.exception.InvalidParameterException;
import com.kryptokrauts.aeternity.sdk.util.EncodingUtils;
import com.kryptokrauts.codegen.CodegenUtil;
import com.kryptokrauts.codegen.maven.ABIJsonConfiguration;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Arrays;
import java.util.List;
import javax.lang.model.element.Modifier;

public class OracleType implements CustomType {

  @Override
  public JsonObject getTypeDefinition(ABIJsonConfiguration abiJsonConfiguration) {
    return new JsonObject()
        .put(abiJsonConfiguration.getCustomTypeNameElement(), ORACLE_TYPE)
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
                                    ORACLE_TYPE)
                                .put(
                                    abiJsonConfiguration.getCustomTypeTypedefTypeElement(),
                                    "string"))));
  }

  @Override
  public CodeBlock encodeValueCodeblock(String MP_PARAM) {
    return CodeBlock.builder()
        .add("$L.get" + CodegenUtil.getUppercaseClassName(ORACLE_TYPE) + "()", MP_PARAM)
        .build();
  }

  @Override
  public CodeBlock mapToReturnValueCodeblock(String MP_PARAM) {
    return CodeBlock.builder()
        .add(
            "new $T($L.toString())",
            ClassName.get("", CodegenUtil.getUppercaseClassName(ORACLE_TYPE)),
            MP_PARAM)
        .build();
  }

  @Override
  public MethodSpec constructorMethod() {
    return MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterSpec.builder(String.class, ORACLE_TYPE).build())
        .addCode(ORACLE_CHECK_CODEBLOCK)
        .build();
  }

  @Override
  public List<MethodSpec> methodList() {
    return Arrays.asList(
        MethodSpec.methodBuilder("setOracleAddress")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ParameterSpec.builder(String.class, ORACLE_TYPE).build())
            .addCode(ORACLE_CHECK_CODEBLOCK)
            .build());
  }

  private CodeBlock ORACLE_CHECK_CODEBLOCK =
      CodeBlock.builder()
          .beginControlFlow(
              "if(!$T.hasValidType($L,$T.ORACLE_PUBKEY))",
              EncodingUtils.class,
              ORACLE_TYPE,
              ApiIdentifiers.class)
          .addStatement(
              "throw new $T($T.format($S,$L))",
              InvalidParameterException.class,
              String.class,
              "Given address %s is not of type aeternity oracle public key",
              ORACLE_TYPE)
          .endControlFlow()
          .addStatement("this.$L=$L", ORACLE_TYPE, ORACLE_TYPE)
          .build();
}
