package com.kryptokrauts.codegen.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * deserializes general objects of type datatype - for every generated datatype on instance of this
 * deserializer will be created
 *
 * @author mitch
 */
@RequiredArgsConstructor
public class JacksonDatatypeDeserializer extends AbstractJacksonDeserializer {

  @NonNull private String concreteType;

  @Override
  public String getDeserializerName() {
    return "Jackson" + concreteType + "Deserializer";
  }

  @Override
  public ClassName getDeserializerType() {
    return ClassName.get("", concreteType);
  }

  @Override
  protected CodeBlock getDeserializeMethodCodeblock() {
    return CodeBlock.builder()
        .addStatement(
            "$T $L = $L.getCodec().readTree($L)",
            JsonNode.class,
            VAR_MP_NODE,
            MP_JSON_PARSER,
            MP_JSON_PARSER)
        .addStatement("return new $N($L.asText())", this.concreteType, VAR_MP_NODE)
        .build();
  }
}
