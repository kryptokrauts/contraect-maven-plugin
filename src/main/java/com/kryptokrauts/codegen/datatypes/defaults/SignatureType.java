package com.kryptokrauts.codegen.datatypes.defaults;

public class SignatureType extends BytesType {

  public SignatureType() {
    super(64, CustomType.SIGNATURE_TYPE);
  }
}
