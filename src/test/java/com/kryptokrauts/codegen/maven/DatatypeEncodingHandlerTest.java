package com.kryptokrauts.codegen.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.common.collect.ImmutableMap;
import com.kryptokrauts.runtime.datatypes.DatatypeEncodingHandler;
import com.squareup.javapoet.TypeName;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class DatatypeEncodingHandlerTest {
  private DatatypeEncodingHandler datatypeEncodingHandler = new DatatypeEncodingHandler();

  @Test
  public void testParameterEncoding() {
    // test cases
    Map<String, Map<TypeName, Object>> parameterEncodings =
        new HashMap<String, Map<TypeName, Object>>();
    parameterEncodings.put("\"test\"", ImmutableMap.of(TypeName.get(String.class), "test"));

    // test the encoding
    parameterEncodings.forEach(
        (expected, typeValueDefinition) -> {
          try {
            TypeName typeDefinition = typeValueDefinition.keySet().iterator().next();
            Object value = typeValueDefinition.get(typeDefinition);
            assertEquals(expected, datatypeEncodingHandler.encodeParameter(typeDefinition, value));
          } catch (Exception e) {
            fail("Failed testing type encoding", e);
          }
        });
  }
}
