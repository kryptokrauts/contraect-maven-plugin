package com.kryptokrauts.codegen.maven;

import lombok.Getter;
import lombok.Setter;

/**
 * this class configures the elements described within the JSON ABI represenation of a contract,
 * which are parsed during the code generation process.
 *
 * <p>"contract": {
 *
 * <ul>
 *   <li>"functions": [
 *       <ul>
 *         <li>"arguments: [
 *       </ul>
 * </ul>
 *
 * "contract": { "functions": [ { "arguments": [ { "name": "stringValue", "type": "string" } ],
 * "name": "testPayableString", "payable": true, "returns": "string", "stateful": true }], "name":
 * "SophiaTypes", "payable": false, "type_defs": [ { "name": "customContractType", "typedef": {
 * "record": [ { "name": "value1", "type": "address" } ] }, "vars": [] } ] }
 *
 * @author mitch
 */
@Getter
@Setter
public class ABIJsonConfiguration {

  /** root contract element */
  private String rootElement = "contract";

  /** name of the json event element */
  private String eventElement = "event";

  /** name of the json functions list element */
  private String functionsElement = "functions";

  /** name of the json contract name element */
  private String contractNameElement = "name";

  /** name of the json payable element */
  private String payableElement = "payable";

  /** name of the json contract state element */
  private String stateElement = "state";

  /** name of the json custom datatype list element */
  private String customTypeElement = "type_defs";

  /** json element name of functions list */
  private String functionNameElement = "name";

  /** json element name of function return type */
  private String functionReturnTypeElement = "returns";

  /** name of the function arguments list element */
  private String functionArgumentsElement = "arguments";

  /** json element name of function argument type */
  private String functionArgumentTypeElement = "type";

  /** json element name of function argument name */
  private String functionArgumentNameElement = "name";

  /** json element name for stateful */
  private String functionStatefulElement = "stateful";

  /** json element name for name of custom datatype */
  private String customTypeNameElement = "name";

  /** json element name for type definition array of custom datatype */
  private String customTypeTypedefElement = "typedef";

  /** name of the json element identifying a field within a custom type */
  private String customTypeTypedefNameElement = "name";

  /** name of the json element identifying a fields type within a custom type */
  private String customTypeTypedefTypeElement = "type";
}
