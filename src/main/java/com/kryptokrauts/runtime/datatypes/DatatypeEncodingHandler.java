package com.kryptokrauts.runtime.datatypes;

import com.squareup.javapoet.TypeName;
import java.util.Arrays;
import java.util.List;

public class DatatypeEncodingHandler {

  private List<DatatypeEncoder> datatypeHandlers;

  public DatatypeEncodingHandler() {
    this.datatypeHandlers = Arrays.asList(new StringEncoder(this), new IntEncoder(this));
  }

  public String encodeParameter(TypeName type, Object value) {
    return getHandler(type).encodeValue(value);
  }

  public DatatypeEncoder getHandler(TypeName type) {
    return datatypeHandlers.stream().filter(t -> t.applies(type)).findFirst().orElse(null);
  }
}
