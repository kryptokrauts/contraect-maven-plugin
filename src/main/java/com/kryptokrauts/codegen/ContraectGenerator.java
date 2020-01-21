package com.kryptokrauts.codegen;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kryptokrauts.aeternity.sdk.constants.BaseConstants;
import com.kryptokrauts.aeternity.sdk.domain.ObjectResultWrapper;
import com.kryptokrauts.aeternity.sdk.service.aeternity.AeternityServiceConfiguration;
import com.kryptokrauts.aeternity.sdk.service.aeternity.AeternityServiceFactory;
import com.kryptokrauts.aeternity.sdk.service.aeternity.impl.AeternityService;
import com.kryptokrauts.aeternity.sdk.service.compiler.domain.ACIResult;
import com.kryptokrauts.aeternity.sdk.service.info.domain.TransactionInfoResult;
import com.kryptokrauts.aeternity.sdk.service.info.domain.TransactionResult;
import com.kryptokrauts.aeternity.sdk.service.transaction.domain.DryRunAccountModel;
import com.kryptokrauts.aeternity.sdk.service.transaction.domain.DryRunRequest;
import com.kryptokrauts.aeternity.sdk.service.transaction.domain.DryRunTransactionResult;
import com.kryptokrauts.aeternity.sdk.service.transaction.domain.DryRunTransactionResults;
import com.kryptokrauts.aeternity.sdk.service.transaction.domain.PostTransactionResult;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.ContractCallTransactionModel;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.ContractCreateTransactionModel;
import com.kryptokrauts.codegen.datatypes.deprecated.TypeResolverRefactored;
import com.kryptokrauts.runtime.datatypes.DatatypeEncodingHandler;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.NonNull;

public class ContraectGenerator {

	Logger log = LoggerFactory.getLogger(ContraectGenerator.class);

	/**
	 * ------------------------------
	 * <p>
	 * Generated contract variables
	 * <p>
	 * ------------------------------
	 */
	private static String GCV_LOGGER = "logger";

	private static String GCV_DEPLOYED_CONTRACT_ID = "deployedContractId";

	private static String GCV_AETERNITY_SERVICE = "aeternityService";

	private static String GCV_AES_SOURCECODE = "aesSourcecode";

	private static String GCV_CONFIG = "config";

	private static String GCV_NUM_TRIALS = "numTrials";

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

	private MethodSpec GCPM_DRY_RUN;

	private MethodSpec GCPM_WAIT_FOR_TX_INFO;

	private MethodSpec GCPM_WAIT_FOR_TX_MINED;

	private MethodSpec GCPM_DEPLOY_CONTRACT;

	private MethodSpec GCPM_DEPLOY;

	private MethodSpec GCPM_CREATE_CCM;

	@NonNull
	private CodegenConfiguration config;

	private TypeResolverRefactored typeResolver;

	private DatatypeEncodingHandler datatypeEncodingHandler;

	public ContraectGenerator(CodegenConfiguration config) {
		this.config = config;
		this.typeResolver = new TypeResolverRefactored();
		this.datatypeEncodingHandler = new DatatypeEncodingHandler(
				config.getTargetPackage());
	}

	public void generate(String aesFile) throws MojoExecutionException {
		String aesContent = readFile(aesFile);
		ACIResult abiContent = config.getAeternityService().compiler
				.blockingGenerateACI(aesContent, null, null);
		if (abiContent.getEncodedAci() != null) {
			this.generateContractClass(
					this.checkABI(abiContent.getEncodedAci()), aesContent);
		} else {
			throw new MojoExecutionException(String.format(
					"Cannot create ABI for contract code generation %s: %s\n%s",
					aesFile, abiContent.getAeAPIErrorMessage(),
					abiContent.getRootErrorMessage()));
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
		return Optional
				.ofNullable(JsonObject.mapFrom(abiContent)
						.getJsonObject(config.getAbiJSONRootElement()))
				.orElseThrow(() -> new MojoExecutionException(String.format(
						"Invalid json or configuration - cannot parse root element %s from json %s",
						config.getAbiJSONRootElement(), abiContent)));
	}

	/**
	 * construct the contract class
	 *
	 * @param abiJson
	 * @throws MojoExecutionException
	 */
	private void generateContractClass(JsonObject abiJson, String aesContent)
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
					.addField(FieldSpec
							.builder(String.class, GCV_AES_SOURCECODE,
									Modifier.PRIVATE)
							.initializer("$S", aesContent).build())
					.addField(FieldSpec
							.builder(Logger.class, GCV_LOGGER, Modifier.PRIVATE,
									Modifier.STATIC)
							.initializer("$T.getLogger($L)",
									LoggerFactory.class,
									abiJson.getString(
											config.getAbiJSONNameElement())
											+ ".class")
							.build())
					.addField(FieldSpec
							.builder(int.class, GCV_NUM_TRIALS,
									Modifier.PRIVATE)
							.initializer("$L", config.getNumTrials()).build())
					.addTypes(generateCustomTypes(abiJson
							.getJsonArray(config.getAbiJSONTypesElement())))
					.addMethods(buildContractPrivateMethods())
					.addMethod(buildConstructor())
					.addMethods(this.buildContractMethods(abiJson)).build();

			/**
			 * add default deploy method if no explicit method defined in
			 * contract
			 */
			if (GCPM_DEPLOY == null) {
				contractTypeSpec = contractTypeSpec.toBuilder()
						.addMethod(buildDeployMethod(null)).build();
			}

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
	 * @TODO check if record before -> if not, create not type
	 */
	private List<TypeSpec> generateCustomTypes(JsonArray customTypes) {
		return customTypes.stream()
				.map(typeDefinition -> generateCustomType(
						JsonObject.mapFrom(typeDefinition)))
				.collect(Collectors.toList());
	}

	/**
	 * @TODO refactor json names
	 */
	private TypeSpec generateCustomType(JsonObject typeDefinition) {
		List<Pair<String, Object>> fields = new LinkedList<>();

		JsonArray record = typeDefinition.getJsonObject("typedef")
				.getJsonArray("record");
		// if record is defined, it is a custom type
		if (record != null) {
			fields = record.stream().map(definition -> {
				JsonObject oneField = JsonObject.mapFrom(definition);
				String name = oneField.getString("name");
				Object type = oneField.getValue("type");
				return Pair.with(name, type);
			}).collect(Collectors.toList());
		}
		String name = typeDefinition.getString("name");
		return TypeSpec
				.classBuilder(
						name.substring(0, 1).toUpperCase() + name.substring(1))
				.addFields(buildFields(fields)).addModifiers(Modifier.PUBLIC)
				.build();
	}

	private List<MethodSpec> buildGetter(List<Pair<String, String>> fields) {
		return fields.stream()
				.map(field -> MethodSpec
						.methodBuilder("get"
								+ field.getValue0().substring(0, 1)
										.toUpperCase()
								+ field.getValue0().substring(1))
						.addModifiers(Modifier.PUBLIC)
						.returns(this.typeResolver
								.getReturnType(field.getValue1()))
						.addStatement("return this.$L", field.getValue0())
						.build())
				.collect(Collectors.toList());
	}

	private List<MethodSpec> buildSetter(List<Pair<String, String>> fields) {
		return fields
				.stream().map(
						field -> MethodSpec
								.methodBuilder("get"
										+ field.getValue0().substring(0, 1)
												.toUpperCase()
										+ field.getValue0().substring(1))
								.addModifiers(Modifier.PUBLIC)
								.addStatement("this.$L = $L", field.getValue0(),
										field.getValue0())
								.addParameter(
										this.typeResolver.getReturnType(
												field.getValue1()),
										field.getValue0())
								.build())
				.collect(Collectors.toList());
	}

	private List<FieldSpec> buildFields(List<Pair<String, Object>> fields) {
		return fields
				.stream().map(
						field -> FieldSpec
								.builder(
										this.datatypeEncodingHandler
												.getTypeName(field.getValue1()),
										field.getValue0(), Modifier.PRIVATE)
								.build())
				.collect(Collectors.toList());
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
				.addStatement("this.$L = $L", GCV_CONFIG,
						PARAM_AETERNITY_SERVICE_CONFIGURATION)
				.addStatement("this.$L = new $T().getService($L)",
						GCV_AETERNITY_SERVICE, AeternityServiceFactory.class,
						PARAM_AETERNITY_SERVICE_CONFIGURATION)
				// .addStatement("this.$N()", GCPM_CONTRACT_EXISTS)
				.build();

		return MethodSpec.constructorBuilder().addParameters(Arrays.asList(
				ParameterSpec.builder(AeternityServiceConfiguration.class,
						PARAM_AETERNITY_SERVICE_CONFIGURATION).build(),
				ParameterSpec.builder(String.class, GCV_DEPLOYED_CONTRACT_ID)
						.build()))
				.addCode(codeBlock).addModifiers(Modifier.PUBLIC)
				.addException(RuntimeException.class).build();
	}

	/** iterate over method declarations and create method spec */
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
		String VAR_DR_RESULT = "dryRunResult";
		String VAR_RESULT_OBJECT = "resultObject";

		String VAR_CC_MODEL = "contractCallModel";
		String VAR_CC_POST_TX_RESULT = "contractCallPostTxResult";
		String VAR_CC_POST_TX_INFO = "contractCallPostTxInfo";
		String VAR_ENCODED_PARAM_LIST = "encodedParameterList";

		String functionName = replaceInvalidChars(functionDescription
				.getString(config.getAbiJSONFunctionsNameElement()));

		if (config.getInitFunctionName().equalsIgnoreCase(functionName)) {
			return buildDeployMethod(functionDescription);
		}

		List<ParameterSpec> params = getParameterSpecFromSignature(
				functionDescription);

		TypeName resultType = this.mapClass(functionDescription
				.getValue(config.getAbiJSONFunctionsReturnTypeElement()));

		CodeBlock codeBlock = CodeBlock.builder()
				.addStatement("$T $L = $T.asList($L)",
						ParameterizedTypeName.get(List.class, String.class),
						VAR_ENCODED_PARAM_LIST, Arrays.class,
						getParameterEncoding(params))
				.addStatement("$T $L = this.$N($S,$L)",
						ContractCallTransactionModel.class, VAR_CC_MODEL,
						GCPM_CREATE_CCM, functionName, VAR_ENCODED_PARAM_LIST)
				.addStatement("$T $L = this.$N($L)",
						DryRunTransactionResult.class, VAR_DR_RESULT,
						GCPM_DRY_RUN, VAR_CC_MODEL)
				.addStatement(
						"$T $L = this.$L.compiler.blockingDecodeCallResult($L,$S,$L.getContractCallObject().getReturnType(),$L.getContractCallObject().getReturnValue())",
						ObjectResultWrapper.class, VAR_RESULT_OBJECT,
						GCV_AETERNITY_SERVICE, GCV_AES_SOURCECODE, functionName,
						VAR_DR_RESULT, VAR_DR_RESULT)
				.beginControlFlow("if($S.equalsIgnoreCase($L.getResult()))",
						"ok", VAR_DR_RESULT)
				.build();

		Boolean stateful = functionDescription
				.getBoolean(config.getAbiJSONFunctionStatefulElement());

		/** stateful call - add gas and gasPrice to transaction and post */
		if (stateful) {
			codeBlock = codeBlock.toBuilder()
					.addStatement("$L = $L.toBuilder()"
							+ ".gas($L.getContractCallObject().getGasUsed())"
							+ ".build()", VAR_CC_MODEL, VAR_CC_MODEL,
							VAR_DR_RESULT)
					.addStatement(
							"$T $L = this.$L.transactions.blockingPostTransaction($L)",
							PostTransactionResult.class, VAR_CC_POST_TX_RESULT,
							GCV_AETERNITY_SERVICE, VAR_CC_MODEL)
					.beginControlFlow("if($L == null)", VAR_CC_POST_TX_RESULT)
					.addStatement("throw new $T($S)", RuntimeException.class,
							"Transaction could not be posted")
					.endControlFlow()
					.addStatement("$T $L = this.$N($L.getTxHash())",
							TransactionInfoResult.class, VAR_CC_POST_TX_INFO,
							GCPM_WAIT_FOR_TX_INFO, VAR_CC_POST_TX_RESULT)
					.addStatement(
							"$L = this.$L.compiler.blockingDecodeCallResult($L,$S,$L.getCallInfo().getReturnType(),$L.getCallInfo().getReturnValue())",
							VAR_RESULT_OBJECT, GCV_AETERNITY_SERVICE,
							GCV_AES_SOURCECODE, functionName,
							VAR_CC_POST_TX_INFO, VAR_CC_POST_TX_INFO)
					.beginControlFlow(
							"if($S.equalsIgnoreCase($L.getCallInfo().getReturnType()))",
							"ok", VAR_CC_POST_TX_INFO)
					.add(mapResultCodeblock(VAR_RESULT_OBJECT, resultType))
					.endControlFlow()
					.addStatement("throw new $T($T.format($S,$L))",
							RuntimeException.class, String.class,
							"Post transaction call failed: %s",
							VAR_RESULT_OBJECT)
					.build();
		}

		/** not a stateful call, return dryRun result */
		else {
			codeBlock = codeBlock.toBuilder()
					.add(mapResultCodeblock(VAR_RESULT_OBJECT, resultType))
					.build();
		}

		codeBlock = codeBlock.toBuilder().endControlFlow()
				.addStatement("throw new $T($T.format($S,$L))",
						RuntimeException.class, String.class,
						"DryRun call failed: %s", VAR_RESULT_OBJECT)
				.build();

		return MethodSpec.methodBuilder(functionName).addParameters(params)
				.addCode(codeBlock)
				.addJavadoc(stateful ? "Stateful function" : "")
				.returns(resultType).addJavadoc("")
				.addModifiers(Modifier.PUBLIC).build();
	}

	private CodeBlock mapResultCodeblock(String VAR_RESULT_OBJECT,
			TypeName resultType) {
		String VAR_UNWRAPPED_RESULT_OBJECT = "unwrappedResultObject";
		return CodeBlock.builder()
				.addStatement("$T $L = $L.getResult()", Object.class,
						VAR_UNWRAPPED_RESULT_OBJECT, VAR_RESULT_OBJECT)
				.add(this.mapReturnTypeFromCall(resultType,
						VAR_UNWRAPPED_RESULT_OBJECT))
				.build();
	}

	private String replaceInvalidChars(String value) {
		if (value != null) {
			value = value.replace("'", "");
		}
		return value;
	}

	private List<ParameterSpec> getParameterSpecFromSignature(
			JsonObject functionDescription) {
		List<ParameterSpec> params = functionDescription
				.getJsonArray(config.getAbiJSONFunctionArgumentElement())
				.stream().map(param -> {
					JsonObject paramMap = JsonObject.mapFrom(param);
					return ParameterSpec.builder(
							mapClass(paramMap.getValue(config
									.getAbiJSONFunctionArgumentTypeElement())),
							replaceInvalidChars(paramMap.getString(config
									.getAbiJSONFunctionArgumentNameElement())))
							.build();
				}).collect(Collectors.toList());
		return params;
	}

	/**
	 * build the deploy method
	 *
	 * @return
	 */
	private MethodSpec buildDeployMethod(JsonObject functionDescription) {
		String VAR_CALLDATA = "calldata";
		List<ParameterSpec> parameters = new LinkedList<>();

		CodeBlock getInitFunctionCalldata = CodeBlock.builder()
				.addStatement("$T $L = this.$N($S, new $T<>())", String.class,
						VAR_CALLDATA, GCPM_CALLDATA_FOR_FCT,
						config.getInitFunctionName(), LinkedList.class)
				.build();
		if (functionDescription != null) {
			parameters = getParameterSpecFromSignature(functionDescription);
			getInitFunctionCalldata = CodeBlock.builder()
					.addStatement("$T $L = this.$N($S,$L)", String.class,
							VAR_CALLDATA, GCPM_CALLDATA_FOR_FCT,
							config.getInitFunctionName(),
							getParameterEncoding(parameters))
					.build();
		}

		GCPM_DEPLOY = MethodSpec.methodBuilder("deploy")
				.addCode(CodeBlock.builder().add(getInitFunctionCalldata)
						.addStatement("return this.$N($L)",
								GCPM_DEPLOY_CONTRACT, VAR_CALLDATA)
						.build())
				.addParameters(parameters).returns(String.class)
				.addModifiers(Modifier.PUBLIC).build();

		return GCPM_DEPLOY;
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
				buildGetNextNonceMethod(), buildGetCalldataForFunctionMethod(),
				buildCreateTransactionCallModelMethod(), buildDryRunMethod(),
				buildWaitForTxMinedMethod(), buildWaitForTxInfoMethod(),
				buildDeployContractMethod());
	}

	private MethodSpec buildDeployContractMethod() {
		String MP_INIT_CALLDATA = "initCalldata";
		String VAR_BYTECODE = "bytecode";
		String VAR_CC_MODEL = "contractCreateModel";
		String VAR_CC_UNSIGNED_TX = "unsignedContractCreateTX";
		String VAR_CC_DR_RESULTS = "dryRunContractCreateResults";
		String VAR_CC_DR_RESULT = "dryRunContractCreateResult";
		String VAR_CC_POST_TX_RESULT = "contractCreatePostTransactionResult";
		String VAR_CC_POST_TX_INFO = "contractCreatePostTransactionInfo";

		this.GCPM_DEPLOY_CONTRACT = MethodSpec.methodBuilder("deployContract")
				.addParameter(ParameterSpec
						.builder(String.class, MP_INIT_CALLDATA).build())
				.addCode(CodeBlock.builder().addStatement(
						"$T $L = this.$L.compiler.blockingCompile($L,null,null).getResult()",
						String.class, VAR_BYTECODE, GCV_AETERNITY_SERVICE,
						GCV_AES_SOURCECODE)
						.addStatement("$T $L = $T.builder()"
								+ ".amount($T.ZERO)" + ".callData($L)"
								+ ".contractByteCode($L)" + ".deposit($T.ZERO)"
								+ ".gas($T.valueOf(48000000l))"
								+ ".gasPrice($T.valueOf($T.MINIMAL_GAS_PRICE))"
								+ ".nonce(this.$N())"
								+ ".ownerId(this.$L.getBaseKeyPair().getPublicKey())"
								+ ".ttl($T.ZERO)"
								+ ".virtualMachine(this.$L.getTargetVM())"
								+ ".build()",
								ContractCreateTransactionModel.class,
								VAR_CC_MODEL,
								ContractCreateTransactionModel.class,
								BigInteger.class, MP_INIT_CALLDATA,
								VAR_BYTECODE, BigInteger.class,
								BigInteger.class, BigInteger.class,
								BaseConstants.class, GCPM_NEXT_NONCE,
								GCV_CONFIG, BigInteger.class, GCV_CONFIG)
						.addStatement(
								"$T $L = this.$L.transactions.blockingCreateUnsignedTransaction($L).getResult()",
								String.class, VAR_CC_UNSIGNED_TX,
								GCV_AETERNITY_SERVICE, VAR_CC_MODEL)
						.addStatement(
								"$T $L = this.$L.transactions.blockingDryRunTransactions("
										+ "$T.builder().build()"
										+ ".account($T.builder().publicKey(this.$L.getBaseKeyPair().getPublicKey()).build())"
										+ ".transactionInputItem($L))",
								DryRunTransactionResults.class,
								VAR_CC_DR_RESULTS, GCV_AETERNITY_SERVICE,
								DryRunRequest.class, DryRunAccountModel.class,
								GCV_CONFIG, VAR_CC_UNSIGNED_TX)
						.addStatement("$T $L = $L.getResults().get(0)",
								DryRunTransactionResult.class, VAR_CC_DR_RESULT,
								VAR_CC_DR_RESULTS)
						.addStatement("$L = $L.toBuilder()"
								+ ".gas($L.getContractCallObject().getGasUsed())"
								+ ".gasPrice($L.getContractCallObject().getGasPrice())"
								+ ".build()", VAR_CC_MODEL, VAR_CC_MODEL,
								VAR_CC_DR_RESULT, VAR_CC_DR_RESULT)
						.addStatement(
								"$T $L = this.$L.transactions.blockingPostTransaction($L)",
								PostTransactionResult.class,
								VAR_CC_POST_TX_RESULT, GCV_AETERNITY_SERVICE,
								VAR_CC_MODEL)
						.addStatement("$T $L = this.$N($L.getTxHash())",
								TransactionInfoResult.class,
								VAR_CC_POST_TX_INFO, GCPM_WAIT_FOR_TX_INFO,
								VAR_CC_POST_TX_RESULT)
						.addStatement(
								"this.$L = $L.getCallInfo().getContractId()",
								GCV_DEPLOYED_CONTRACT_ID, VAR_CC_POST_TX_INFO)
						.addStatement("return $L.getCallInfo().getContractId()",
								VAR_CC_POST_TX_INFO)
						.build())
				.returns(String.class).addModifiers(Modifier.PRIVATE).build();
		return GCPM_DEPLOY_CONTRACT;
	}

	private MethodSpec buildWaitForTxInfoMethod() {
		String MP_TXHASH = "txHash";

		this.GCPM_WAIT_FOR_TX_INFO = MethodSpec.methodBuilder("waitForTxInfo")
				.addParameter(
						ParameterSpec.builder(String.class, MP_TXHASH).build())
				.addCode(CodeBlock.builder()
						.addStatement("this.$N($L)", GCPM_WAIT_FOR_TX_MINED,
								MP_TXHASH)
						.addStatement(
								"return this.$L.info.blockingGetTransactionInfoByHash($L)",
								GCV_AETERNITY_SERVICE, MP_TXHASH)
						.build())
				.returns(TransactionInfoResult.class)
				.addModifiers(Modifier.PRIVATE).build();
		return GCPM_WAIT_FOR_TX_INFO;
	}

	private MethodSpec buildWaitForTxMinedMethod() {
		String MP_TXHASH = "txHash";

		String VAR_BLOCK_HEIGHT = "blockHeight";
		String VAR_MINED_TX = "minedTx";
		String VAR_DONE_TRIALS = "doneTrials";

		this.GCPM_WAIT_FOR_TX_MINED = MethodSpec.methodBuilder("waitForTxMined")
				.addParameter(
						ParameterSpec.builder(String.class, MP_TXHASH).build())
				.addCode(CodeBlock.builder()
						.addStatement("int $L = -1", VAR_BLOCK_HEIGHT)
						.addStatement("$T $L = null", TransactionResult.class,
								VAR_MINED_TX)
						.addStatement("int $L = 1", VAR_DONE_TRIALS)
						.beginControlFlow("while($L == -1 && $L < $L)",
								VAR_BLOCK_HEIGHT, VAR_DONE_TRIALS,
								GCV_NUM_TRIALS)
						.addStatement(
								"$L = $L.info.blockingGetTransactionByHash($L)",
								VAR_MINED_TX, GCV_AETERNITY_SERVICE, MP_TXHASH)
						.beginControlFlow(
								"if($L.getBlockHeight().intValue() > 1)",
								VAR_MINED_TX)
						.addStatement("$L.debug($S+$L)", GCV_LOGGER,
								"Mined transaction is: ", VAR_MINED_TX)
						.addStatement("$L = $L.getBlockHeight().intValue()",
								VAR_BLOCK_HEIGHT, VAR_MINED_TX)
						.nextControlFlow("else")
						.addStatement("$L.info($T.format($S,$L,$L))",
								GCV_LOGGER, String.class,
								"Transaction not mined yet, trying again in 1 second (%s of %s)...",
								VAR_DONE_TRIALS, GCV_NUM_TRIALS)
						.beginControlFlow("try")
						.addStatement("$T.sleep(1000l)", Thread.class)
						.nextControlFlow("catch($T e)",
								InterruptedException.class)
						.addStatement("throw new $T($T.format($S,$L,$L))",
								RuntimeException.class, String.class,
								"Waiting for transaction %s to be mined was interrupted due to technical error: %s",
								MP_TXHASH, "e.getMessage()")
						.endControlFlow().addStatement("$L++", VAR_DONE_TRIALS)
						.endControlFlow().endControlFlow()
						.beginControlFlow("if($L == -1)", VAR_BLOCK_HEIGHT)
						.addStatement("throw new $T($T.format($S,$L,$L))",
								RuntimeException.class, String.class,
								"Transaction %s was not mined after %s trials, aborting",
								MP_TXHASH, VAR_DONE_TRIALS)
						.endControlFlow()
						.addStatement("return $L", VAR_MINED_TX).build())
				.returns(TransactionResult.class).addModifiers(Modifier.PRIVATE)
				.build();
		return GCPM_WAIT_FOR_TX_MINED;
	}

	private MethodSpec buildContractExistsMethod() {
		String VAR_BYTECODE = "byteCode";

		this.GCPM_CONTRACT_EXISTS = MethodSpec.methodBuilder("contractExists")
				.addException(RuntimeException.class)
				.addCode(CodeBlock.builder()
						.beginControlFlow(
								"if($L != null && $L.trim().length() > 0)",
								GCV_DEPLOYED_CONTRACT_ID,
								GCV_DEPLOYED_CONTRACT_ID)
						.addStatement(
								"String $L = this.$L.info.blockingGetContractByteCode($L).getResult()",
								VAR_BYTECODE, GCV_AETERNITY_SERVICE,
								GCV_DEPLOYED_CONTRACT_ID)
						.beginControlFlow("if($L == null)", VAR_BYTECODE)
						.addStatement("throw new $T($S+$L+$S)",
								RuntimeException.class,
								"Given contract with publickey ",
								GCV_DEPLOYED_CONTRACT_ID, " is not deployed")
						.endControlFlow()
						.beginControlFlow(
								"if(!$L.equals($L.compiler.blockingCompile($L, null, null)))",
								VAR_BYTECODE, GCV_AETERNITY_SERVICE,
								GCV_AES_SOURCECODE)
						.addStatement("throw new $T($S+$L+$S)",
								RuntimeException.class,
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

	private MethodSpec buildGetCalldataForFunctionMethod() {
		String MP_PARAMS = "params";
		String MP_FUNCTION = "function";

		this.GCPM_CALLDATA_FOR_FCT = MethodSpec
				.methodBuilder("getCalldataForFunction")
				.addParameters(Arrays.asList(
						ParameterSpec.builder(String.class, MP_FUNCTION)
								.build(),
						ParameterSpec.builder(ParameterizedTypeName
								.get(List.class, String.class), MP_PARAMS)
								.build()))
				.addCode(CodeBlock.builder().addStatement(
						"return $L.compiler.blockingEncodeCalldata($L,$L,$L).getResult()",
						GCV_AETERNITY_SERVICE, GCV_AES_SOURCECODE, MP_FUNCTION,
						MP_PARAMS).build())
				.returns(String.class).addModifiers(Modifier.PRIVATE).build();
		return this.GCPM_CALLDATA_FOR_FCT;
	}

	private MethodSpec buildCreateTransactionCallModelMethod() {
		String MP_PARAMS = "params";
		String MP_FUNCTION = "function";

		String VAR_CALLDATA = "callData";

		this.GCPM_CREATE_CCM = MethodSpec
				.methodBuilder("createContractCallModel")
				.addParameters(Arrays.asList(
						ParameterSpec.builder(String.class, MP_FUNCTION)
								.build(),
						ParameterSpec.builder(ParameterizedTypeName
								.get(List.class, String.class), MP_PARAMS)
								.build()))
				.addCode(CodeBlock.builder()
						.addStatement("$T $L = this.$N($L,$L)", String.class,
								VAR_CALLDATA, GCPM_CALLDATA_FOR_FCT,
								MP_FUNCTION, MP_PARAMS)
						.addStatement("return $T.builder().callData($L)"
								+ ".gas($T.valueOf(1579000l))"
								+ ".contractId($L)"
								+ ".gasPrice($T.valueOf($T.MINIMAL_GAS_PRICE))"
								+ ".amount($T.ZERO)" + ".nonce(this.$N())"
								+ ".callerId(this.$L.getBaseKeyPair().getPublicKey())"
								+ ".ttl($T.ZERO)"
								+ ".virtualMachine(this.$L.getTargetVM())"
								+ ".build()",
								ContractCallTransactionModel.class,
								VAR_CALLDATA, BigInteger.class,
								GCV_DEPLOYED_CONTRACT_ID, BigInteger.class,
								BaseConstants.class, BigInteger.class,
								GCPM_NEXT_NONCE, GCV_CONFIG, BigInteger.class,
								GCV_CONFIG)
						.build())
				.returns(ContractCallTransactionModel.class).build();

		return this.GCPM_CREATE_CCM;
	}

	private MethodSpec buildDryRunMethod() {
		String MP_CC_MODEL = "contractCallModel";

		String VAR_DR_RESULTS = "dryRunResults";

		this.GCPM_DRY_RUN = MethodSpec.methodBuilder("dryRunCall")
				.addParameters(Arrays.asList(ParameterSpec
						.builder(ContractCallTransactionModel.class,
								MP_CC_MODEL)
						.build()))
				.addCode(CodeBlock.builder().addStatement(
						"$T $N = $L.transactions.blockingDryRunTransactions($T.builder().build().transactionInputItem($L)"
								+ ".account($T.builder()"
								+ ".publicKey(this.$L.getBaseKeyPair().getPublicKey())"
								+ ".build()))",
						DryRunTransactionResults.class, VAR_DR_RESULTS,
						GCV_AETERNITY_SERVICE, DryRunRequest.class, MP_CC_MODEL,
						DryRunAccountModel.class, GCV_CONFIG)
						.beginControlFlow(
								"if($L.getResults() != null && $L.getResults().size()>0)",
								VAR_DR_RESULTS, VAR_DR_RESULTS)
						.addStatement("return $L.getResults().get(0)",
								VAR_DR_RESULTS)
						.endControlFlow()
						.addStatement(
								"throw new $T($S+$L.getRootErrorMessage())",
								RuntimeException.class,
								"Dry run call of function failed:\n",
								VAR_DR_RESULTS)
						.build())
				.addModifiers(Modifier.PRIVATE)
				.returns(DryRunTransactionResult.class).build();
		return GCPM_DRY_RUN;
	}

	private CodeBlock getParameterEncoding(List<ParameterSpec> params) {
		return CodeBlock.join(params.stream().map(p -> {
			// TypeName type = p.type;
			// if (type instanceof ParameterizedTypeName) {
			// type = ((ParameterizedTypeName) type).rawType;
			// }
			return this.datatypeEncodingHandler.encodeParameter(p.type, p.name);
			// return CodeBlock.builder()
			// .add("this.$L.encodeParameter($T.get($T.class),$L)",
			// DATATYPE_ENCODING_HANDLER, TypeName.class, type,
			// p.name)
			// .build();
		}).collect(Collectors.toList()), ",");
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
	private CodeBlock mapReturnTypeFromCall(TypeName resultType,
			String returnValueVariable) {
		return CodeBlock.builder()
				.addStatement("return $L", this.datatypeEncodingHandler
						.mapToReturnValue(resultType, returnValueVariable))
				.build();
	}

	/**
	 * maps aeternity type to java type
	 *
	 * @param classType
	 * @return
	 */
	private TypeName mapClass(Object classType) {
		return this.typeResolver.getReturnType(classType);
	}
}
