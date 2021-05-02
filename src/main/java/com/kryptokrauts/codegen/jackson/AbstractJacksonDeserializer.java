package com.kryptokrauts.codegen.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import java.io.IOException;
import javax.lang.model.element.Modifier;

public abstract class AbstractJacksonDeserializer {

  public abstract String getDeserializerName();

  public abstract ClassName getDeserializerType();

  protected abstract CodeBlock getDeserializeMethodCodeblock();

  protected static String VAR_MP_NODE = "node";

  protected static String MP_JSON_PARSER = "jsonParser";

  protected static String MP_CONTEXT = "context";

  public ParameterizedTypeName getDeserializerClassType() {
    return ParameterizedTypeName.get(ClassName.get(StdDeserializer.class), getDeserializerType());
  }

  public MethodSpec getDeserializeMethod() {
    return MethodSpec.methodBuilder("deserialize")
        .addModifiers(Modifier.PUBLIC)
        .addException(IOException.class)
        .addException(JsonProcessingException.class)
        .addAnnotation(Override.class)
        .addParameter(ParameterSpec.builder(JsonParser.class, MP_JSON_PARSER).build())
        .addParameter(ParameterSpec.builder(DeserializationContext.class, MP_CONTEXT).build())
        .returns(getDeserializerType())
        .addCode(getDeserializeMethodCodeblock())
        .build();
  }
}
