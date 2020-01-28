package com.kryptokrauts.codegen.maven;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.maven.plugins.annotations.Parameter;

/*
 * describes JSON values for contract abi
 */

@Getter
@Setter
@NoArgsConstructor
public class ABIJsonDescription {

  /*
   * name of the init function
   */
  @Parameter(defaultValue = "init")
  private String initFunctionName = "init";

  /** root contract element */
  @Parameter(defaultValue = "contract")
  private String abiJSONRootElement = "contract";

  /** json element name of the contract */
  @Parameter(defaultValue = "name")
  private String abiJSONNameElement = "name";

  /** json element name of functions list */
  @Parameter(defaultValue = "functions")
  private String abiJSONFunctionsElement = "functions";
  /** json element name of functions list */
  @Parameter(defaultValue = "name")
  private String abiJSONFunctionsNameElement = "name";
  /** json element name of function return type */
  @Parameter(defaultValue = "returns")
  private String abiJSONFunctionsReturnTypeElement = "returns";

  /** json element name of function arguments */
  @Parameter(defaultValue = "arguments")
  private String abiJSONFunctionArgumentElement = "arguments";

  /** json element name of function argument type */
  @Parameter(defaultValue = "type")
  private String abiJSONFunctionArgumentTypeElement = "type";

  /** json element name of function argument name */
  @Parameter(defaultValue = "name")
  private String abiJSONFunctionArgumentNameElement = "name";

  /** json element name for stateful */
  @Parameter(defaultValue = "stateful")
  private String abiJSONFunctionStatefulElement = "stateful";

  /** json element name for custom datatypes */
  @Parameter(defaultValue = "type_defs")
  private String abiJSONTypesElement = "type_defs";

  /** json element name for name of custom datatype */
  @Parameter(defaultValue = "name")
  private String abiJSONTypesNameElement = "name";

  /** json element name for type definition array of custom datatype */
  @Parameter(defaultValue = "name")
  private String abiJSONTypesTypedefElement = "typedef";
}
