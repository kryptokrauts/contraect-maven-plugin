package com.kryptokrauts.runtime.datatypes;

import com.squareup.javapoet.TypeName;

public interface DatatypeEncoder {

  public boolean applies(TypeName type);

  public String encodeValue(Object value);
}
