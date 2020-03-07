package com.kryptokrauts.codegen;

public enum CmpErrorCode {
  FAIL_IMPORT_INCLUDES("005"),
  FAIL_CREATE_ABI("010"),
  FAIL_PARSE_ROOT("020"),
  FAIL_GENERATE_CONTRACT("030"),
  FAIL_READ_CONTRACT_FILE("100"),
  UNFORSEEN_MAPPING("200"),
  UNFORSEEN_CUSTOM_TYPEDEF("300");

  private String errorCode;

  private CmpErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }
}
