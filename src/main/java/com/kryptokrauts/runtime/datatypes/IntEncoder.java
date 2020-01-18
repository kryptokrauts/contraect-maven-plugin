package com.kryptokrauts.runtime.datatypes;

import com.squareup.javapoet.TypeName;
import java.math.BigInteger;

public class IntEncoder implements DatatypeEncoder {

  private DatatypeEncodingHandler resolveInstance;

  public IntEncoder(DatatypeEncodingHandler resolveInstance) {
    this.resolveInstance = resolveInstance;
  }

  public boolean applies(TypeName type) {
    return TypeName.get(BigInteger.class).equals(type);
  }

  public String encodeValue(Object value) {
    return value.toString();
  }
}
