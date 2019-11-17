package com.kryptokrauts.codegen.maven;

import org.apache.maven.plugins.annotations.Parameter;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
/*
 * describes JSON values for contract abi
 */
@Builder
@Getter
public class ABIJsonDescription {

	/*
	 * name of the init function
	 */
	@Parameter(defaultValue = "init")
	@Default
	private String initFunctionName = "init";

	/**
	 * root contract element
	 */
	@Parameter(defaultValue = "contract")
	@Default
	private String abiJSONRootElement = "contract";

	/**
	 * json element name of the contract
	 */
	@Parameter(defaultValue = "name")
	@Default
	private String abiJSONNameElement = "name";

	/**
	 * json element name of functions list
	 */
	@Parameter(defaultValue = "functions")
	@Default
	private String abiJSONFunctionsElement = "functions";
	/**
	 * json element name of functions list
	 */
	@Parameter(defaultValue = "name")
	@Default
	private String abiJSONFunctionsNameElement = "name";
	/**
	 * json element name of function return type
	 */
	@Parameter(defaultValue = "returns")
	@Default
	private String abiJSONFunctionsReturnTypeElement = "returns";

	/**
	 * json element name of function arguments
	 */
	@Parameter(defaultValue = "arguments")
	@Default
	private String abiJSONFunctionArgumentElement = "arguments";

	/**
	 * json element name of function argument type
	 */
	@Parameter(defaultValue = "type")
	@Default
	private String abiJSONFunctionArgumentTypeElement = "type";

	/**
	 * json element name of function argument name
	 */
	@Parameter(defaultValue = "name")
	@Default
	private String abiJSONFunctionArgumentNameElement = "name";

	/**
	 * json element name for stateful
	 */
	@Parameter(defaultValue = "stateful")
	@Default
	private String abiJSONFunctionStatefulElement = "stateful";
}
