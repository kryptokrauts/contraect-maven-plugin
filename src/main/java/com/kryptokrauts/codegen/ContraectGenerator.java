package com.kryptokrauts.codegen;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kryptokrauts.aeternity.sdk.constants.BaseConstants;
import com.kryptokrauts.aeternity.sdk.service.aeternity.AeternityServiceConfiguration;
import com.kryptokrauts.aeternity.sdk.service.aeternity.AeternityServiceFactory;
import com.kryptokrauts.aeternity.sdk.service.aeternity.impl.AeternityService;
import com.kryptokrauts.aeternity.sdk.service.compiler.domain.ACIResult;
import com.kryptokrauts.aeternity.sdk.service.transaction.domain.DryRunRequest;
import com.kryptokrauts.aeternity.sdk.service.transaction.domain.DryRunTransactionResult;
import com.kryptokrauts.aeternity.sdk.service.transaction.domain.DryRunTransactionResults;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.ContractCallTransactionModel;
import com.kryptokrauts.codegen.datatypes.BitsMapper;
import com.kryptokrauts.codegen.datatypes.BoolMapper;
import com.kryptokrauts.codegen.datatypes.BytesMapper;
import com.kryptokrauts.codegen.datatypes.DefaultMapper;
import com.kryptokrauts.codegen.datatypes.IntMapper;
import com.kryptokrauts.codegen.datatypes.SophiaTypeMapper;
import com.kryptokrauts.codegen.datatypes.StringMapper;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ContraectGenerator {

	Logger log = LoggerFactory.getLogger(ContraectGenerator.class);

	/**
	 * ------------------------------
	 * <p>
	 * Generated contract variables
	 * <p>
	 * ------------------------------
	 */
	private static String GCV_DEPLOYED_CONTRACT_ID = "deployedContractId";

	private static String GCV_AETERNITY_SERVICE = "aeternityService";

	private static String GCV_AES_SOURCECODE = "aesSourcode";

	private static String GCV_CONFIG = "config";

	/**
	 * ------------------------------
	 * <p>
	 * Generated contract private methods for referencing
	 * <p>
	 * ------------------------------
	 */

	private MethodSpec GCPM_CONTRACT_EXISTS;

	private MethodSpec GCPM_NEXT_NONCE;

	private MethodSpec GCPM_CALLDATA_FOR_FCT;

	private MethodSpec GCPM_GENERATE_MAP_PARAM;

	private MethodSpec GCPM_DRY_RUN;

	@NonNull
	private CodegenConfiguration config;

	public void generate(String aesFile) throws MojoExecutionException {
		String aesContent = readFile(aesFile);
		ACIResult abiContent = config.getAeternityService().compiler
				.blockingGenerateACI(aesContent, null, null);
		if (abiContent.getEncodedAci() != null) {
			this.generateContractClass(
					this.checkABI(abiContent.getEncodedAci()));
		} else {
			throw new MojoExecutionException(
					"Cannot create ABI for contract " + aesFile);
		}
	}

	/**
	 * Check the ABI if it contains the root element and return the childrens
	 * map
	 * 
	 * @param abiContent
	 * @return
	 * @throws MojoExecutionException
	 */
	private JsonObject checkABI(Object abiContent)
			throws MojoExecutionException {
		return JsonObject.mapFrom(abiContent)
				.getJsonObject(config.getAbiJSONRootElement());
	}

	/**
	 * construct the contract class
	 * 
	 * @param abiJson
	 * @throws MojoExecutionException
	 */
	private void generateContractClass(JsonObject abiJson)
			throws MojoExecutionException {
		try {
			TypeSpec contractTypeSpec = TypeSpec
					.classBuilder(
							abiJson.getString(config.getAbiJSONNameElement()))
					.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
					.addField(AeternityService.class, GCV_AETERNITY_SERVICE,
							Modifier.PRIVATE)
					.addField(AeternityServiceConfiguration.class, GCV_CONFIG,
							Modifier.PRIVATE)
					.addField(String.class, GCV_DEPLOYED_CONTRACT_ID,
							Modifier.PRIVATE)
					.addField(String.class, GCV_AES_SOURCECODE,
							Modifier.PRIVATE)
					.addMethods(buildContractPrivateMethods())
					.addMethod(buildConstructor())
					.addMethods(this.buildContractMethods(abiJson)).build();

			JavaFile javaFile = JavaFile
					.builder(config.getTargetPackage(), contractTypeSpec)
					.build();

			Path path = Paths.get("", config.getTargetPath());

			javaFile.writeTo(path);
		} catch (Exception e) {
			throw new MojoExecutionException(
					"Error generating contract "
							+ abiJson.getString(config.getAbiJSONNameElement()),
					e);
		}
	}

	/**
	 * contraect java class constructor
	 * 
	 * @return
	 */
	private MethodSpec buildConstructor() {

		// Declare parameter names
		String PARAM_AETERNITY_SERVICE_CONFIGURATION = "aeternityServiceConfiguration";

		// Declare Code / Statements
		CodeBlock codeBlock = CodeBlock.builder()
				.addStatement("this.$L = $L", GCV_DEPLOYED_CONTRACT_ID,
						GCV_DEPLOYED_CONTRACT_ID)
				.addStatement("this.$L = new $T().getService($L)",
						GCV_AETERNITY_SERVICE, AeternityServiceFactory.class,
						PARAM_AETERNITY_SERVICE_CONFIGURATION)
				.addStatement("this.$N()", GCPM_CONTRACT_EXISTS).build();

		return MethodSpec.constructorBuilder().addParameters(Arrays.asList(
				ParameterSpec.builder(AeternityServiceConfiguration.class,
						PARAM_AETERNITY_SERVICE_CONFIGURATION).build(),
				ParameterSpec.builder(String.class, GCV_DEPLOYED_CONTRACT_ID)
						.build(),
				ParameterSpec.builder(String.class, GCV_AES_SOURCECODE)
						.build()))
				.addCode(codeBlock).addModifiers(Modifier.PUBLIC)
				.addException(MojoExecutionException.class).build();
	}

	/**
	 * 
	 */
	private List<MethodSpec> buildContractMethods(JsonObject abi) {
		List<MethodSpec> methods = new LinkedList<MethodSpec>();
		if (abi != null
				&& abi.getValue(config.getAbiJSONFunctionsElement()) != null) {
			JsonArray functions = abi
					.getJsonArray(config.getAbiJSONFunctionsElement());
			for (Object methodDescription : functions.getList()) {
				methods.add(this.parseFunctionToMethodSpec(
						JsonObject.mapFrom(methodDescription)));
			}
		} else {
			log.warn("Given contract source seems to have no methods defined!");
		}
		return methods;
	}

	/**
	 * parse a single function to method spec
	 * 
	 * @param functionDescription
	 * @return
	 */
	private MethodSpec parseFunctionToMethodSpec(
			JsonObject functionDescription) {
		return MethodSpec
				.methodBuilder(functionDescription
						.getString(config.getAbiJSONFunctionsNameElement()))
				.addCode(CodeBlock.builder().add(this.mapReturnTypeFromCall(
						functionDescription.getValue(
								config.getAbiJSONFunctionsReturnTypeElement()),
						"test")).build())
				.returns(this.mapClass(functionDescription.getValue(
						config.getAbiJSONFunctionsReturnTypeElement())))
				.addModifiers(Modifier.PUBLIC).build();

		// (
		// functionDescription
		// .getString(config.getAbiJSONFunctionsNameElement()),
		// new LinkedList<>(),
		// CodeBlock.builder().add(this.mapReturnTypeFromCall(
		// functionDescription.getValue(
		// config.getAbiJSONFunctionsReturnTypeElement()),
		// "test")).build(),
		// this.mapClass(functionDescription.getValue(
		// config.getAbiJSONFunctionsReturnTypeElement())),
		// Modifier.PUBLIC);
		// if (functionDescription != null) {
		// // name of the function
		// String functionName = functionDescription.get("name").toString();
		// MethodSpec method = MethodSpec.methodBuilder(functionName)
		// .addModifiers(Modifier.PUBLIC).build();
		// // return types
		// String returnType = functionDescription.get("returns").toString();
		// method = method.toBuilder().returns(mapClass(returnType)).build();
		// // list of parameters
		// List<Map<String, Object>> functionParameters = (List)
		// functionDescription
		// .get("arguments");
		// if (functionParameters != null) {
		// for (Map<String, Object> parameterMap : functionParameters) {
		// method = method.toBuilder()
		// .addParameter(mapClass(parameterMap.get("type")),
		// parameterMap.get("name").toString()
		// .replace("'", ""))
		// .build();
		// }
		// }
		// // stateful true/false
		// Boolean stateful = Boolean.parseBoolean(
		// functionDescription.get("stateful").toString());
		// if (stateful) {
		// method = method.toBuilder().addJavadoc("Stateful function")
		// .build();
		// } else {
		// String DRY_RUN_RESULT_VARIABLE = "dryRunResult";
		// method = method.toBuilder()
		// .addStatement(
		// "String $L = this.$L.dryRunCall($L, \"$L\")",
		// DRY_RUN_RESULT_VARIABLE,
		// GENERATOR_UTIL_VARIABLE,
		// DEPLOYED_CONTRACT_ID_VARIABLE, functionName)
		// .addStatement(mapReturnTypeFromCall(returnType,
		// DRY_RUN_RESULT_VARIABLE))
		// .build();
		// }
		//
		// return method;
		// }
		// throw new ContraectCodegenException(
		// "Cannot create function for functionDescription "
		// + functionDescription);
	}

	/**
	 * ------------------------------
	 * <p>
	 * Generated contract private methods
	 * <p>
	 * ------------------------------
	 */

	private List<MethodSpec> buildContractPrivateMethods() {
		return Arrays.asList(buildContractExistsMethod(),
				buildGetNextNonceMethod(), buildGenerateMapParam(),
				buildGetCalldataForFunctionMethod(), buildDryRunMethod());
	}

	private MethodSpec buildContractExistsMethod() {
		String VAR_BYTECODE = "byteCode";

		this.GCPM_CONTRACT_EXISTS = MethodSpec.methodBuilder("contractExists")
				.addException(MojoExecutionException.class)
				.addCode(CodeBlock.builder()
						.beginControlFlow(
								"if($L != null && $L.trim().length() > 0)",
								GCV_DEPLOYED_CONTRACT_ID,
								GCV_DEPLOYED_CONTRACT_ID)
						.addStatement(
								"String $L = this.$L.info.blockingGetContractByteCode($L)",
								VAR_BYTECODE, GCV_AETERNITY_SERVICE,
								GCV_AES_SOURCECODE)
						.beginControlFlow("if($L == null)", VAR_BYTECODE)
						.addStatement("throw new $T($S+$L+$S)",
								MojoExecutionException.class,
								"Given contract with publickey ",
								GCV_DEPLOYED_CONTRACT_ID, " is not deployed")
						.endControlFlow()
						.beginControlFlow(
								"if(!$L.equals($L.compiler.blockingCompile($L, null, null)))",
								VAR_BYTECODE, GCV_AETERNITY_SERVICE,
								GCV_AES_SOURCECODE)
						.addStatement("throw new $T($S+$L+$S)",
								MojoExecutionException.class,
								"Given contract with publickey ",
								GCV_DEPLOYED_CONTRACT_ID,
								" is not equal to contract source used to generate this class")
						.endControlFlow().addStatement("return true")
						.endControlFlow().addStatement("return false").build())
				.returns(boolean.class).addModifiers(Modifier.PRIVATE).build();
		return GCPM_CONTRACT_EXISTS;
	}

	private MethodSpec buildGetNextNonceMethod() {
		this.GCPM_NEXT_NONCE = MethodSpec.methodBuilder("getNextNonce")
				.addCode(CodeBlock.builder().addStatement(
						"return this.$L.accounts.blockingGetAccount($T.empty()).getNonce().add($T.ONE)",
						GCV_AETERNITY_SERVICE, Optional.class, BigInteger.class)
						.build())
				.returns(BigInteger.class).addModifiers(Modifier.PRIVATE)
				.build();
		return this.GCPM_NEXT_NONCE;
	}

	private MethodSpec buildGenerateMapParam() {
		String VAR_RECP_COND_SET = "recipientConditionSet";
		String MP_PARAMS = "params";
		this.GCPM_GENERATE_MAP_PARAM = MethodSpec
				.methodBuilder("generateMapParam")
				.addParameter(
						ParameterSpec.builder(Map.class, MP_PARAMS).build())
				.addCode(CodeBlock.builder()
						.addStatement("$T $L = new $T()",
								ParameterizedTypeName
										.get(Set.class, String.class),
								VAR_RECP_COND_SET, HashSet.class)
						.addStatement(
								"$L.forEach((k,v)->$L.add(\"[\"+k+\"] =\"+v))",
								MP_PARAMS, VAR_RECP_COND_SET)
						.addStatement(
								"return \"{\"+$L.stream().collect($T.joining(\",\"))+\"}\"",
								VAR_RECP_COND_SET, Collectors.class)
						.build())
				.returns(String.class).addModifiers(Modifier.PRIVATE).build();
		return this.GCPM_GENERATE_MAP_PARAM;
	}

	private MethodSpec buildGetCalldataForFunctionMethod() {
		String VAR_ARGUMENTS = "arguments";
		String VAR_PARAM = "param";
		String MP_PARAMS = "params";
		String MP_FUNCTION = "function";

		this.GCPM_CALLDATA_FOR_FCT = MethodSpec
				.methodBuilder("getCalldataForFunction").varargs(true)
				.addParameters(Arrays.asList(
						ParameterSpec.builder(String.class, MP_FUNCTION)
								.build(),
						ParameterSpec.builder(Object[].class, MP_PARAMS)
								.build()))
				.addCode(CodeBlock.builder()
						.addStatement("$T $N = null",
								ParameterizedTypeName.get(List.class,
										String.class),
								VAR_ARGUMENTS)
						.beginControlFlow("if($L != null)", MP_PARAMS)
						.addStatement("$L = new $T()", VAR_ARGUMENTS,
								ParameterizedTypeName.get(LinkedList.class,
										String.class))
						.beginControlFlow("for($T $L : $L)", Object.class,
								VAR_PARAM, MP_PARAMS)
						.beginControlFlow("if($L instanceof $T)", VAR_PARAM,
								Map.class)
						.addStatement("$L.add($N(($T) $L))", VAR_ARGUMENTS,
								GCPM_GENERATE_MAP_PARAM, Map.class, VAR_PARAM)
						.nextControlFlow("else")
						.addStatement("$L.add($L.toString())", VAR_ARGUMENTS,
								VAR_PARAM)
						.endControlFlow().endControlFlow().endControlFlow()
						.addStatement(
								"return $L.compiler.blockingEncodeCalldata($L,$L,$L)",
								GCV_AETERNITY_SERVICE, GCV_AES_SOURCECODE,
								MP_FUNCTION, VAR_ARGUMENTS)
						.build())
				.returns(String.class).addModifiers(Modifier.PRIVATE).build();
		return this.GCPM_CALLDATA_FOR_FCT;
	}

	private MethodSpec buildDryRunMethod() {
		String MP_PARAMS = "params";
		String MP_FUNCTION = "function";

		String VAR_CALLDATA = "callData";
		String VAR_DR_RESULTS = "dryRunResults";
		String VAR_DR_RESULT = "dryRunResult";

		this.GCPM_DRY_RUN = MethodSpec.methodBuilder("dryRunCall")
				.addParameters(Arrays.asList(
						ParameterSpec.builder(String.class, MP_FUNCTION)
								.build(),
						ParameterSpec.builder(Object[].class, MP_PARAMS)
								.build()))
				.addCode(CodeBlock.builder()
						.addStatement("$T $L = this.$N($L,$L)", String.class,
								VAR_CALLDATA, GCPM_CALLDATA_FOR_FCT,
								MP_FUNCTION, MP_PARAMS)
						.addStatement(
								"$T $N = $L.transactions.blockingDryRunTransactions($T.builder().build().transactionInputItem($T.builder().callData($L)"
										+ ".gas($T.valueOf(1579000))"
										+ ".contractId($L)"
										+ ".gasPrice($T.valueOf($T.MINIMAL_GAS_PRICE))"
										+ ".amount($T.ZERO)"
										+ ".nonce(this.$N())"
										+ ".callerId(this.$L.getBaseKeyPair().getPublicKey())"
										+ ".ttl($T.ZERO)"
										+ ".virtualMachine(this.$L.getTargetVM())"
										+ ".build()))",
								DryRunTransactionResults.class, VAR_DR_RESULTS,
								GCV_AETERNITY_SERVICE, DryRunRequest.class,
								ContractCallTransactionModel.class,
								VAR_CALLDATA, BigInteger.class,
								GCV_DEPLOYED_CONTRACT_ID, BigInteger.class,
								BaseConstants.class, BigInteger.class,
								GCPM_NEXT_NONCE, GCV_CONFIG, BigInteger.class,
								GCV_CONFIG)
						.beginControlFlow(
								"if($L.getResults() != null && $L.getResults().size()>0)",
								VAR_DR_RESULTS, VAR_DR_RESULTS)
						.addStatement("$T $L = $L.getResults().get(0)",
								DryRunTransactionResult.class, VAR_DR_RESULT,
								VAR_DR_RESULTS)
						.beginControlFlow(
								"if(\"ok\".equalsIgnoreCase($L.getResult()))",
								VAR_DR_RESULT)
						.addStatement(
								"return this.$L.compiler.blockingDecodeCallResult($L,$L,$L.getResult(),$L.getContractCallObject().getReturnValue()).toString()",
								GCV_AETERNITY_SERVICE, GCV_AES_SOURCECODE,
								MP_FUNCTION, VAR_DR_RESULT, VAR_DR_RESULT)
						.endControlFlow().endControlFlow()
						.addStatement(
								"throw new $T(\"call of function \" + function + \" with params \" + params + \" failed\")",
								RuntimeException.class)
						.build())
				.addModifiers(Modifier.PRIVATE).returns(String.class)
				.varargs(true).build();
		return GCPM_DRY_RUN;
	}

	/**
	 * ------------------------------
	 * <p>
	 * Codegen Util Methods
	 * <p>
	 * ------------------------------
	 */

	private String readFile(String filePath) throws MojoExecutionException {
		try {
			return IOUtils.toString(Paths.get("", filePath).toUri(),
					StandardCharsets.UTF_8.toString());
		} catch (IOException e) {
			throw new MojoExecutionException(String
					.format("Cannot read contract from file %s", filePath), e);
		}
	}

	/**
	 * get the return statement which intializes the resultType with the value
	 * 
	 * @param classType
	 * @param result
	 * @return
	 */
	private CodeBlock mapReturnTypeFromCall(Object classType, String result) {
		return typeMapperList.stream().filter(t -> t.applies(classType))
				.findFirst().orElse(new DefaultMapper())
				.getReturnStatement(result);
	}

	/**
	 * maps aeternity type to java type
	 * 
	 * @param classType
	 * @return
	 */
	private Type mapClass(Object classType) {
		return typeMapperList.stream().filter(t -> t.applies(classType))
				.findFirst().orElse(new DefaultMapper()).getJavaType();
	}

	private List<SophiaTypeMapper> typeMapperList = Arrays.asList(
			new BoolMapper(), new BitsMapper(), new BytesMapper(),
			new StringMapper(), new IntMapper());
}
