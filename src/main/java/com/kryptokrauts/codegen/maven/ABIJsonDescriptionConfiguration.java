package com.kryptokrauts.codegen.maven;

import lombok.Getter;
import lombok.Setter;

/*
 * describes JSON values for contract abi
 */

@Getter
@Setter
public class ABIJsonDescriptionConfiguration {

  /*
   * name of the init function
   */
  private String initFunctionName = "init";

  /** root contract element */
  private String abiJSONRootElement = "contract";

  /** json element name of the contract */
  private String abiJSONNameElement = "name";

  /** json element name of functions list */
  private String abiJSONFunctionsElement = "functions";

  /** json element name of functions list */
  private String abiJSONFunctionsNameElement = "name";

  /** json element name of function return type */
  private String abiJSONFunctionsReturnTypeElement = "returns";

  /** json element name of function arguments */
  private String abiJSONFunctionArgumentElement = "arguments";

  /** json element name of function argument type */
  private String abiJSONFunctionArgumentTypeElement = "type";

  /** json element name of function argument name */
  private String abiJSONFunctionArgumentNameElement = "name";

  /** json element name for stateful */
  private String abiJSONFunctionStatefulElement = "stateful";

  /** json element name for payable */
  private String abiJSONFunctionPayableElement = "payable";

  /** json element name for custom datatypes */
  private String abiJSONTypesElement = "type_defs";

  /** json element name for name of custom datatype */
  private String abiJSONTypesNameElement = "name";

  /** json element name for type definition array of custom datatype */
  private String abiJSONTypesTypedefElement = "typedef";

  /** indicates an abort of a function call */
  private String resultAbortKey = "abort";
}
