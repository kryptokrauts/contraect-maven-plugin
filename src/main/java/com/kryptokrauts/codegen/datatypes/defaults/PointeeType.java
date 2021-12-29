package com.kryptokrauts.codegen.datatypes.defaults;

import com.kryptokrauts.codegen.CodegenUtil;
import com.kryptokrauts.codegen.maven.ABIJsonConfiguration;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Arrays;
import java.util.List;
import javax.lang.model.element.Modifier;

public class PointeeType implements CustomType {

  private static final String POINTEE_ENUM_TYPE = "PointeeType";

  private static final String CP_TYPE = "type";

  @Override
  public JsonObject getTypeDefinition(ABIJsonConfiguration abiJsonConfiguration) {
    return new JsonObject()
        .put(abiJsonConfiguration.getCustomTypeNameElement(), POINTEE_TYPE)
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
    return Arrays.asList(
        FieldSpec.builder(ClassName.bestGuess(POINTEE_ENUM_TYPE), CP_TYPE)
            .addModifiers(Modifier.PROTECTED)
            .build());
  }

  @Override
  public List<TypeSpec> additionalInnerTypes() {
    return List.of(
        TypeSpec.enumBuilder(POINTEE_ENUM_TYPE)
            .addEnumConstant("AccountPt")
            .addEnumConstant("OraclePt")
            .addEnumConstant("ContractPt")
            .addEnumConstant("ChannelPt")
            .addModifiers(Modifier.PUBLIC)
            .build());
  }

  @Override
  public CodeBlock encodeValueCodeblock(String MP_PARAM) {
    return CodeBlock.builder()
        .add(
            "$S+$L.getType()+$S+$T.encodeValue($L.get"
                + CodegenUtil.getUppercaseClassName(ADDRESS_TYPE)
                + "())+$S",
            "AENS.",
            MP_PARAM,
            "(",
            ClassName.get("", CodegenUtil.getUppercaseClassName(ADDRESS_TYPE)),
            MP_PARAM,
            ")")
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
        .addParameter(
            ParameterSpec.builder(
                    ClassName.get("", CodegenUtil.getUppercaseClassName(ADDRESS_TYPE)),
                    ADDRESS_TYPE)
                .build())
        .addParameter(
            ParameterSpec.builder(
                    ClassName.get("", CodegenUtil.getUppercaseClassName(POINTEE_ENUM_TYPE)),
                    CP_TYPE)
                .build())
        .addStatement("this.$L=$L", ADDRESS_TYPE, ADDRESS_TYPE)
        .addStatement("this.$L=$L", CP_TYPE, CP_TYPE)
        .build();
  }

  @Override
  public List<MethodSpec> methodList() {
    String MP_PARAM = "p";
    String P_JSONOBJECT = "jsonObject";
    String P_KEY = "key";
    String P_VALUE = "value";
    return Arrays.asList(
        MethodSpec.methodBuilder("getType")
            .addModifiers(Modifier.PUBLIC)
            .returns(ClassName.get("", CodegenUtil.getUppercaseClassName(POINTEE_ENUM_TYPE)))
            .addStatement("return this.$L", CP_TYPE)
            .build(),
        MethodSpec.methodBuilder("mapInternal")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .addParameter(ParameterSpec.builder(Object.class, MP_PARAM).build())
            .returns(ClassName.get("", CodegenUtil.getUppercaseClassName(POINTEE_TYPE)))
            .addStatement(
                "$T $L = $T.mapFrom($L)",
                JsonObject.class,
                P_JSONOBJECT,
                JsonObject.class,
                MP_PARAM)
            .addStatement(
                "$T $L = $L.iterator().next().getKey()", String.class, P_KEY, P_JSONOBJECT)
            .addStatement(
                "$T $L = $L.getJsonArray($L).getValue(0)",
                Object.class,
                P_VALUE,
                P_JSONOBJECT,
                P_KEY)
            .addStatement(
                "return new $T(new $T($L.toString()),$T.valueOf(key.replace(\"AENS.\", \"\")))",
                ClassName.get("", CodegenUtil.getUppercaseClassName(POINTEE_TYPE)),
                ClassName.get("", CodegenUtil.getUppercaseClassName(ADDRESS_TYPE)),
                P_VALUE,
                ClassName.get("", CodegenUtil.getUppercaseClassName(POINTEE_ENUM_TYPE)))
            .build());
  }
}
