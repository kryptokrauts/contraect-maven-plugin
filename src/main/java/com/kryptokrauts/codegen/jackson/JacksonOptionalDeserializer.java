package com.kryptokrauts.codegen.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.kryptokrauts.codegen.datatypes.OptionMapper;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import java.util.Optional;

public class JacksonOptionalDeserializer extends AbstractJacksonDeserializer {

  @Override
  public String getDeserializerName() {
    return "JacksonOptionalDeserializer";
  }

  @Override
  public ClassName getDeserializerType() {
    return ClassName.get(Optional.class);
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
        .beginControlFlow("if($L.get($S) != null)", VAR_MP_NODE, OptionMapper.HAS_VALUE_STRING)
        .addStatement("return $T.of($L.asText())", Optional.class, VAR_MP_NODE)
        .nextControlFlow("else")
        .addStatement("return $T.empty()", Optional.class)
        .endControlFlow()
        .build();
  }
}
