package com.kryptokrauts.codegen.jackson;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import lombok.RequiredArgsConstructor;
import org.javatuples.Decade;
import org.javatuples.Ennead;
import org.javatuples.Octet;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Septet;
import org.javatuples.Sextet;
import org.javatuples.Triplet;
import org.javatuples.Unit;

/** @deprecated custom deserializer currently not necessary, keep logic in case we need it later */
@Deprecated
@RequiredArgsConstructor
public class JacksonDeserializerGenerator {

  public List<TypeSpec> generateJacksonDeserializers() {
    return CUSTOM_JACKSON_DESERIALIZERS.stream()
        .map(jd -> generateJacksonDeserializer(jd))
        .collect(Collectors.toList());
  }

  private TypeSpec generateJacksonDeserializer(AbstractJacksonDeserializer config) {
    return TypeSpec.classBuilder(config.getDeserializerName())
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .superclass(config.getDeserializerClassType())
        .addMethod(
            MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PROTECTED)
                .addParameter(
                    ParameterSpec.builder(
                            ParameterizedTypeName.get(
                                ClassName.get(Class.class),
                                WildcardTypeName.subtypeOf(Object.class)),
                            "vc")
                        .build())
                .addStatement("super(vc)")
                .build())
        .addMethod(config.getDeserializeMethod())
        .build();
  }

  public final List<AbstractJacksonDeserializer> CUSTOM_JACKSON_DESERIALIZERS =
      new LinkedList(
          Arrays.asList(
              new JacksonOptionalDeserializer(),
              new JacksonTuplesDeserializer(Unit.class, 1),
              new JacksonTuplesDeserializer(Pair.class, 2),
              new JacksonTuplesDeserializer(Triplet.class, 3),
              new JacksonTuplesDeserializer(Quartet.class, 4),
              new JacksonTuplesDeserializer(Quintet.class, 5),
              new JacksonTuplesDeserializer(Sextet.class, 6),
              new JacksonTuplesDeserializer(Septet.class, 7),
              new JacksonTuplesDeserializer(Octet.class, 8),
              new JacksonTuplesDeserializer(Ennead.class, 9),
              new JacksonTuplesDeserializer(Decade.class, 10)));
}
