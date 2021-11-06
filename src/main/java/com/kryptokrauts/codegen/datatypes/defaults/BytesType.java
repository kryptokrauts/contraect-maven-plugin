package com.kryptokrauts.codegen.datatypes.defaults;

import com.kryptokrauts.aeternity.sdk.exception.InvalidParameterException;
import com.kryptokrauts.codegen.CodegenUtil;
import com.kryptokrauts.codegen.maven.ABIJsonConfiguration;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Arrays;
import java.util.List;
import javax.lang.model.element.Modifier;

public class BytesType implements CustomType {

  private int length = 64;

  private String className = BYTES_TYPE + length;

  public BytesType(int length, String className) {
    super();
    this.length = length;
    this.className = className;
  }

  protected String CP_LENGTH = "length";

  @Override
  public JsonObject getTypeDefinition(ABIJsonConfiguration abiJsonConfiguration) {
    return new JsonObject()
        .put(abiJsonConfiguration.getCustomTypeNameElement(), className)
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
                                    className)
                                .put(
                                    abiJsonConfiguration.getCustomTypeTypedefTypeElement(),
                                    "string"))));
  }

  @Override
  public CodeBlock encodeValueCodeblock(String MP_PARAM) {
    return CodeBlock.builder()
        .add("$L.get" + CodegenUtil.getUppercaseClassName(className) + "()", MP_PARAM)
        .build();
  }

  @Override
  public CodeBlock mapToReturnValueCodeblock(String MP_PARAM) {
    return CodeBlock.builder()
        .add(
            "new $T($L.toString())",
            ClassName.get("", CodegenUtil.getUppercaseClassName(className)),
            MP_PARAM)
        .build();
  }

  @Override
  public MethodSpec constructorMethod() {
    return MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameters(Arrays.asList(ParameterSpec.builder(String.class, className).build()))
        .addCode(getValidateBytelengthCodeblock())
        .build();
  }

  @Override
  public List<MethodSpec> methodList() {
    return Arrays.asList(
        MethodSpec.methodBuilder("setBytes")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ParameterSpec.builder(String.class, className).build())
            .addCode(getValidateBytelengthCodeblock())
            .build());
  }

  @Override
  public List<FieldSpec> fieldList() {
    return Arrays.asList(
        FieldSpec.builder(TypeName.INT, CP_LENGTH).initializer(String.valueOf(length * 2)).build());
  }

  private static final String BYTES_PATTERN = "#[0-9A-Fa-f]+(_[0-9A-Fa-f]+)*";

  private CodeBlock getValidateBytelengthCodeblock() {
    return CodeBlock.builder()
        .beginControlFlow("if(!$L.startsWith($S))", className, "#")
        .addStatement("$L=$T.join(\"\",$S,$L)", className, String.class, "#", className)
        .endControlFlow()
        .beginControlFlow("if(!$L.matches($S))", className, BYTES_PATTERN)
        .addStatement(
            "throw new $T($T.format($S,$L,$S))",
            InvalidParameterException.class,
            String.class,
            "Given bytes value %s is invalid - only characters that match pattern %s are allowed",
            className,
            BYTES_PATTERN)
        .endControlFlow()
        .addStatement("$L=$L.replaceAll($S,$S)", className, className, "_", "")
        .beginControlFlow("if($L.length() <= $L-1)", className, CP_LENGTH)
        .addStatement(
            "throw new $T($T.format($S,$L,($L/2)-1,$L-1))",
            InvalidParameterException.class,
            String.class,
            "Bytes value %s must have at least a length of %d (%d characters)",
            className,
            CP_LENGTH,
            CP_LENGTH)
        .endControlFlow()
        .beginControlFlow("if($L.length() > $L+1)", className, CP_LENGTH)
        .addStatement(
            "throw new $T($T.format($S,$L,$L/2,$L))",
            InvalidParameterException.class,
            String.class,
            "Given bytes value %s exceeds maximum length of %d (%d characters)",
            className,
            CP_LENGTH,
            CP_LENGTH)
        .endControlFlow()
        .addStatement("this.$L=$L", className, className)
        .build();
  }
}
