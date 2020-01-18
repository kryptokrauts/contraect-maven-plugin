package com.kryptokrauts.runtime.datatypes;

import com.squareup.javapoet.TypeName;

public class StringEncoder implements DatatypeEncoder {

  private DatatypeEncodingHandler resolveInstance;

  public StringEncoder(DatatypeEncodingHandler resolveInstance) {
    this.resolveInstance = resolveInstance;
  }

  public boolean applies(TypeName type) {
    return TypeName.get(String.class).equals(type);
  }

  public String encodeValue(Object value) {
    return "\"" + value + "\"";
  }
}
