package com.kryptokrauts.codegen.datatypes.defaults;

public class HashType extends BytesType {

  public HashType() {
    super(32, CustomType.HASH_TYPE);
  }
}
