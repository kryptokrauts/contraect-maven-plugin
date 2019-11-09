package com.kryptokrauts.codegen;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kryptokrauts.aeternity.sdk.service.aeternity.AeternityServiceConfiguration;
import com.kryptokrauts.aeternity.sdk.service.aeternity.AeternityServiceFactory;
import com.kryptokrauts.aeternity.sdk.service.aeternity.impl.AeternityService;
import com.kryptokrauts.codegen.util.GeneratorUtil;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import io.netty.util.internal.StringUtil;
import io.vertx.core.json.JsonObject;

/**
 * @deprecated will be refactored into ContraectGenerator
 * @author mitch
 */
public class Generator {

	private static String GENERATOR_UTIL_VARIABLE = "generatorUtil";

	private static String DEPLOYED_CONTRACT_ID_VARIABLE = "deployedContractId";

	private static String AE_SERVICE_VARIABLE = "aeternityService";

	public static void generateContract(String contractName, Object abiJson,
			String targetPackage, String targetPath) throws IOException {
		generateContract(JsonObject.mapFrom(abiJson).getMap(), contractName,
				"init", targetPackage, targetPath);
	}

	// public static void generateContract(String contractName,
	// String deployMethod, String... packagePath)
	// throws JsonParseException, JsonMappingException, IOException {
	// // generate all methods and deploy method
	//
	// /**
	// * @TODO parse Via VertxJSONOBject
	// */
	// Map jsonMap = readContractABIFromFile(contractName.toLowerCase());
	// generateContract(jsonMap, contractName, deployMethod,
	// String.join(",", packagePath), null);
	// }

	public static void generateContract(Map contractACI, String contractName,
			String deployMethod, String targetPackage, String targetPath)
			throws IOException {

		// String contractName = "PaymentSplitter";
		/**
		 * this parameter must be configured in maven plugin config later
		 */
		// String deployMethod = "init";

		List<MethodSpec> methods = new LinkedList<MethodSpec>();

		for (Map function : getListFromContractABI("functions", contractACI)) {
			System.out.println(function);
			MethodSpec currentMethod = parseFunctionToMethodSpec(function);
			if (currentMethod.name.equals(deployMethod)) {
				methods.add(buildDeployMethod(currentMethod));
			} else {
				methods.add(currentMethod);
			}
		}

		// add constructor
		methods.add(buildConstructor());

		TypeSpec contractTypeSpec = TypeSpec.classBuilder(contractName)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addMethods(methods)
				.addField(AeternityService.class, AE_SERVICE_VARIABLE,
						Modifier.PRIVATE)
				.addField(GeneratorUtil.class, GENERATOR_UTIL_VARIABLE,
						Modifier.PRIVATE)
				.addField(String.class, DEPLOYED_CONTRACT_ID_VARIABLE,
						Modifier.PRIVATE)
				.build();

		JavaFile javaFile = JavaFile.builder(targetPackage, contractTypeSpec)
				.build();

		Path path = Paths.get("", targetPath);

		javaFile.writeTo(path);
	}

	private static MethodSpec buildDeployMethod(MethodSpec parsedMethodSpec) {
		String CALL_DATA_VARIABLE = "callData";

		CodeBlock callDataStatement = CodeBlock.of(
				"String $L = this.$L.getCalldataForFunction(\"$L\")",
				CALL_DATA_VARIABLE, GENERATOR_UTIL_VARIABLE,
				parsedMethodSpec.name);
		if (parsedMethodSpec.parameters.size() > 0) {
			callDataStatement = CodeBlock.of(
					"String $L = this.$L.getCalldataForFunction(\"$L\",$L)",
					CALL_DATA_VARIABLE, GENERATOR_UTIL_VARIABLE,
					parsedMethodSpec.name, parsedMethodSpec.parameters.stream()
							.map(p -> p.name).collect(Collectors.joining(",")));
		}

		CodeBlock codeBlock = CodeBlock.builder()
				.addStatement(callDataStatement)
				.addStatement("this.$L = this.$L.deployContract($L)",
						DEPLOYED_CONTRACT_ID_VARIABLE, GENERATOR_UTIL_VARIABLE,
						CALL_DATA_VARIABLE)
				.addStatement("return this.$L", DEPLOYED_CONTRACT_ID_VARIABLE)
				.build();
		MethodSpec deploy = MethodSpec.methodBuilder("deploy")
				.addModifiers(Modifier.PUBLIC)
				.addParameters(parsedMethodSpec.parameters)
				.returns(String.class).addCode(codeBlock).build();
		return deploy;
	}

	private static MethodSpec buildConstructor() {
		String GENERATOR_CONFIG_PARAM = "generatorConfig";
		String CONTRACT_URL_PARAM = "contractUrl";
		String AE_CONFIG_PARAM = "config";

		List<ParameterSpec> params = new LinkedList<ParameterSpec>();
		params.add(ParameterSpec
				.builder(AeternityServiceConfiguration.class, AE_CONFIG_PARAM)
				.build());
		params.add(ParameterSpec
				.builder(GeneratorConfiguration.class, GENERATOR_CONFIG_PARAM)
				.build());
		params.add(ParameterSpec
				.builder(String.class, DEPLOYED_CONTRACT_ID_VARIABLE).build());
		params.add(ParameterSpec.builder(String.class, CONTRACT_URL_PARAM)
				.build());

		CodeBlock codeBlock = CodeBlock.builder()
				.addStatement("this.$L = $L", DEPLOYED_CONTRACT_ID_VARIABLE,
						DEPLOYED_CONTRACT_ID_VARIABLE)
				.addStatement("this.$L = new $T().getService($L)",
						AE_SERVICE_VARIABLE, AeternityServiceFactory.class,
						AE_CONFIG_PARAM)
				.addStatement("this.$L = new GeneratorUtil($L,$L,$L,$L)",
						GENERATOR_UTIL_VARIABLE, AE_SERVICE_VARIABLE,
						GENERATOR_CONFIG_PARAM, AE_CONFIG_PARAM,
						CONTRACT_URL_PARAM)
				.addStatement("this.$L.contractExists($L)",
						GENERATOR_UTIL_VARIABLE, DEPLOYED_CONTRACT_ID_VARIABLE)
				.build();
		MethodSpec deploy = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC).addParameters(params)
				.addCode(codeBlock).build();
		return deploy;
	}

	public static Map readContractABIFromFile(String contract)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Path contractPath = Paths.get("", "src", "test", "resources",
				"contracts", "abi", contract + ".json");
		File file = new File(contractPath.toAbsolutePath().toString());
		return mapper.readValue(file, Map.class);
	}

	/**
	 * reads one main part of the contract definition
	 * 
	 * @param part
	 *            the respective part, f.e. functions
	 * @param jsonMap
	 * @return
	 */
	public static List<Map> getListFromContractABI(String part, Map jsonMap) {
		if (jsonMap != null) {
			Map<String, List> contractDefinition = (Map) jsonMap
					.get("contract");
			if (contractDefinition != null) {
				List<Map> partList = contractDefinition.get(part);
				if (partList != null) {
					return partList;
				}
			}
		}
		throw new RuntimeException("Cannot parse part " + part);
	}

	private static MethodSpec parseFunctionToMethodSpec(
			Map functionDescription) {
		if (functionDescription != null) {
			// name of the function
			String functionName = functionDescription.get("name").toString();
			MethodSpec method = MethodSpec.methodBuilder(functionName)
					.addModifiers(Modifier.PUBLIC).build();
			// return types
			String returnType = functionDescription.get("returns").toString();
			method = method.toBuilder().returns(mapClass(returnType)).build();
			// list of parameters
			List<Map<String, Object>> functionParameters = (List) functionDescription
					.get("arguments");
			if (functionParameters != null) {
				for (Map<String, Object> parameterMap : functionParameters) {
					method = method.toBuilder()
							.addParameter(mapClass(parameterMap.get("type")),
									parameterMap.get("name").toString()
											.replace("'", ""))
							.build();
				}
			}
			// stateful true/false
			Boolean stateful = Boolean.parseBoolean(
					functionDescription.get("stateful").toString());
			if (stateful) {
				method = method.toBuilder().addJavadoc("Stateful function")
						.build();
			} else {
				String DRY_RUN_RESULT_VARIABLE = "dryRunResult";
				method = method.toBuilder()
						.addStatement(
								"String $L = this.$L.dryRunCall($L, \"$L\")",
								DRY_RUN_RESULT_VARIABLE,
								GENERATOR_UTIL_VARIABLE,
								DEPLOYED_CONTRACT_ID_VARIABLE, functionName)
						.addStatement(mapReturnTypeFromCall(returnType,
								DRY_RUN_RESULT_VARIABLE))
						.build();
			}

			return method;
		}
		throw new RuntimeException(
				"Cannot create function for functionDescription "
						+ functionDescription);
	}

	private static CodeBlock mapReturnTypeFromCall(Object classType,
			String result) {
		String classTypeString = sanitizedClassType(classType);
		if (classTypeString != null) {
			switch (classTypeString) {
				// case "address" :
				// return CodeBlock.of("return new $T($L)", Address.class,
				// result);
				case "int" :
					return CodeBlock.of("return new $T($L)", BigInteger.class,
							result);
				case "bool" :
					return CodeBlock.of("return $T.valueOf($L)", Boolean.class,
							result);
				default :
					return CodeBlock.of("return (Object)$L", result);
			}
		}
		throw new RuntimeException("Cannot map given return type " + classType
				+ " for result " + result);
	}

	private static String sanitizedClassType(Object classType) {
		String classTypeString = null;
		if (!StringUtil.isNullOrEmpty(classType.toString())) {
			classTypeString = classType.toString().toLowerCase();
			if (classTypeString.startsWith("{")) {
				if (classTypeString.contains("tuple")) {
					classTypeString = "list";
				}
				if (classTypeString.contains("map")) {
					classTypeString = "map";
				}
			}
		}
		return classTypeString;
	}

	private static Class mapClass(Object classType) {
		String classTypeString = sanitizedClassType(classType);
		if (classTypeString != null) {
			switch (classTypeString) {
				case "bool" :
					return Boolean.class;
				// case "address" :
				// return Address.class;
				case "int" :
					return BigInteger.class;
				case "list" :
					return List.class;
				case "map" :
					return Map.class;
				default :
					return Object.class;
			}
		}
		throw new RuntimeException(
				"Cannot map java class for given contract type " + classType);
	}
}
