package com.kryptokrauts.codegen.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import java.util.HashMap;
import java.util.Map;

public class JacksonMapDeserializer extends AbstractJacksonDeserializer {

  @Override
  public String getDeserializerName() {
    return "JacksonMapDeserializer";
  }

  @Override
  public ClassName getDeserializerType() {
    return ClassName.get(Map.class);
  }

  @Override
  public CodeBlock getDeserializeMethodCodeblock() {
    return CodeBlock.builder()
        .addStatement(
            "$T $L = $L.getCodec().readTree($L)",
            JsonNode.class,
            VAR_MP_NODE,
            MP_JSON_PARSER,
            MP_JSON_PARSER)
        .addStatement("return new $T()", HashMap.class)
        .build();
  }
}
