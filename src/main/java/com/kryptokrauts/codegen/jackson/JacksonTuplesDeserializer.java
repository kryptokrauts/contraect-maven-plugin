package com.kryptokrauts.codegen.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterators;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import java.util.LinkedList;
import java.util.List;

public class JacksonTuplesDeserializer extends AbstractJacksonDeserializer {

  private Class<?> tupleClass;

  private int numParams;

  public JacksonTuplesDeserializer(Class<?> tupleClass, int numParams) {
    this.tupleClass = tupleClass;
    this.numParams = numParams;
  }

  @Override
  public String getDeserializerName() {
    return "Jackson" + tupleClass.getSimpleName() + "Deserializer";
  }

  @Override
  public ClassName getDeserializerType() {
    return ClassName.get(tupleClass);
  }

  @Override
  protected CodeBlock getDeserializeMethodCodeblock() {

    List<CodeBlock> tupleParamsInitCodeblocks = new LinkedList<>();

    for (int i = 0; i < numParams; i++) {
      tupleParamsInitCodeblocks.add(
          CodeBlock.of("$T.get($L.elements(),$L)", Iterators.class, VAR_MP_NODE, i));
    }

    List<CodeBlock> tupleParamsInitEmptyCodeblocks = new LinkedList<>();

    for (int i = 0; i < numParams; i++) {
      tupleParamsInitEmptyCodeblocks.add(CodeBlock.of("\"\""));
    }

    return CodeBlock.builder()
        .addStatement(
            "$T $L = $L.getCodec().readTree($L)",
            JsonNode.class,
            VAR_MP_NODE,
            MP_JSON_PARSER,
            MP_JSON_PARSER)
        .beginControlFlow(
            "if($T.size($L.elements()) == $L)", Iterators.class, VAR_MP_NODE, this.numParams)
        .add(CodeBlock.builder().add("return new $T(", tupleClass).build())
        .add(CodeBlock.join(tupleParamsInitCodeblocks, ","))
        .addStatement(")")
        .endControlFlow()
        .add("return new $T(", tupleClass)
        .add(CodeBlock.join(tupleParamsInitEmptyCodeblocks, ","))
        .addStatement(")")
        .build();
  }
}
