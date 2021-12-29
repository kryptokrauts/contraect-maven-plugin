package com.kryptokrauts.codegen.datatypes.defaults;

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

public class PointeeType implements CustomType {

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
        .put(abiJsonConfiguration.getCustomTypeAbstractClass(), Boolean.TRUE);
  }

  @Override
  public CodeBlock encodeValueCodeblock(String MP_PARAM) {
    return CodeBlock.builder()
        .add(
            "$T.encodeValue($L.get" + CodegenUtil.getUppercaseClassName(ADDRESS_TYPE) + "())",
            ClassName.get("", CodegenUtil.getUppercaseClassName(ADDRESS_TYPE)),
            MP_PARAM)
        .build();
  }

  @Override
  public CodeBlock mapToReturnValueCodeblock(String MP_PARAM) {
    return CodeBlock.builder().add("null").build();
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
        .addStatement("this.$L=$L", ADDRESS_TYPE, ADDRESS_TYPE)
        .build();
  }

  @Override
  public List<MethodSpec> methodList() {
    return Arrays.asList(
        MethodSpec.methodBuilder("setAddress")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(
                ParameterSpec.builder(
                        ClassName.get("", CodegenUtil.getUppercaseClassName(ADDRESS_TYPE)),
                        ADDRESS_TYPE)
                    .build())
            .addStatement("this.$L=$L", ADDRESS_TYPE, ADDRESS_TYPE)
            .build());
  }
}
