package com.kryptokrauts.codegen;

import com.google.common.collect.ImmutableMap;
import com.kryptokrauts.aeternity.sdk.constants.BaseConstants;
import com.kryptokrauts.aeternity.sdk.domain.ObjectResultWrapper;
import com.kryptokrauts.aeternity.sdk.exception.AException;
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
import com.kryptokrauts.codegen.datatypes.DatatypeMappingHandler;
import com.kryptokrauts.codegen.maven.ABIJsonConfiguration;
import com.kryptokrauts.codegen.maven.CodegenConfiguration;
import com.squareup.javapoet.ClassName;
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
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class ContraectGenerator {

  Logger log = LoggerFactory.getLogger(ContraectGenerator.class);

  /**
   * ------------------------------
   *
   * <p>Generated contract variables
   *
   * <p>------------------------------
   */
  public static String GCV_LOGGER = "logger";

  private static String GCV_DEPLOYED_CONTRACT_ID = "deployedContractId";

  private static String GCV_AETERNITY_SERVICE = "aeternityService";

  private static String GCV_AES_SOURCECODE = "aesSourcecode";

  private static String GCV_AES_INCLUDES = "aesIncludes";

  private static String GCV_CONFIG = "config";

  private static String GCV_NUM_TRIALS = "numTrials";

  private static String GCV_CHECK_BYTECODE = "checkBytecode";

  /**
   * ------------------------------
   *
   * <p>Generated contract private methods for referencing
   *
   * <p>------------------------------
   */
  private MethodSpec GCPM_CONTRACT_EXISTS;

  private MethodSpec GCPM_NEXT_NONCE;

  private MethodSpec GCPM_CALLDATA_FOR_FCT;

  private MethodSpec GCPM_DRY_RUN;

  private MethodSpec GCPM_WAIT_FOR_TX_INFO;

  private MethodSpec GCPM_WAIT_FOR_TX;

  private MethodSpec GCPM_DEPLOY_CONTRACT;

  private MethodSpec GCPM_DEPLOY;

  private MethodSpec GCPM_CREATE_CCM;

  @NonNull private CodegenConfiguration codegenConfiguration;

  @NonNull private ABIJsonConfiguration abiJsonConfiguration;

  private DatatypeMappingHandler datatypeEncodingHandler;

  private CustomTypesGenerator customTypesGenerator;

  public void generate(String aesFile) throws MojoExecutionException {
    String aesContent = readFile(aesFile);
    Map<String, String> includes = parseIncludes(aesFile);
    if (includes != null) {
      includes.replaceAll((k, v) -> v.replace("\\\"", "\""));
    }
    ACIResult abiContent =
        codegenConfiguration
            .getAeternityService()
            .compiler
            .blockingGenerateACI(aesContent, null, includes);
    if (abiContent.getEncodedAci() != null) {
      JsonObject parsedAbi = this.checkABI(abiContent.getEncodedAci());
      if (parsedAbi != null) {
        this.generateContractClass(parsedAbi, aesContent, parseIncludes(aesFile));
      }
    } else {
      throw new MojoExecutionException(
          CodegenUtil.getBaseErrorMessage(
              CmpErrorCode.FAIL_PARSE_ROOT,
              String.format("Compiler failed to create ABI for contract %s", aesFile),
              Arrays.asList(
                  Pair.with("AeAPIErrorMessage", abiContent.getAeAPIErrorMessage()),
                  Pair.with("RootErrorMessage", abiContent.getRootErrorMessage()))));
    }
  }

  private Map<String, String> parseIncludes(String aesFile) throws MojoExecutionException {
    Map<String, String> includes = new HashMap<String, String>();
    System.out.println(aesFile);
    /** Filter 1) contains include 2) cut from first " to last " 3) default libraries */
    try (Stream<String> stream = Files.lines(Paths.get(aesFile))) {
      stream
          .filter(line -> line.contains("include"))
          .map(line -> line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\"")))
          .filter(lib -> !CodegenUtil.DEFAULT_LIBRARIES.contains(lib))
          .forEach(
              libToInclude -> {
                String fileContent = getFileContent(libToInclude);
                if (fileContent == null) {
                  fileContent =
                      getFileContent(
                          Paths.get(aesFile).getParent().resolve(libToInclude).toString());
                }
                includes.put(libToInclude, fileContent);
              });

    } catch (Exception e) {
      throw new MojoExecutionException(
          CodegenUtil.getBaseErrorMessage(
              CmpErrorCode.FAIL_IMPORT_INCLUDES,
              String.format("Error importing includes for contract defined in file %s", aesFile),
              Arrays.asList(Pair.with("exception cause", e.getMessage()))));
    }
    return includes;
  }

  private String getFileContent(String path) {
    try {
      return new String(Files.readAllBytes(Paths.get(path))).replace("\"", "\\\"");
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Check the ABI if it contains the root element and return the childrens map
   *
   * @param abiContent
   * @return
   * @throws MojoExecutionException
   */
  private JsonObject checkABI(Object abiContent) throws MojoExecutionException {
    try {
      // just check if contract
      JsonObject mappedAbi = JsonObject.mapFrom(abiContent);
      if (mappedAbi.containsKey(abiJsonConfiguration.getRootElement())) {
        return mappedAbi.getJsonObject(abiJsonConfiguration.getRootElement());
      }
    } catch (Exception e) {
      throw new MojoExecutionException(
          CodegenUtil.getBaseErrorMessage(
              CmpErrorCode.FAIL_CREATE_ABI,
              String.format(
                  "Invalid json or configuration - cannot parse root element %s",
                  abiJsonConfiguration.getRootElement()),
              Arrays.asList(Pair.with("abiContent", abiContent))));
    }
    // ignore library
    return null;
  }

  /**
   * construct the contract class
   *
   * @param abiJson
   * @throws MojoExecutionException
   */
  private void generateContractClass(
      JsonObject abiJson, String aesContent, Map<String, String> includes)
      throws MojoExecutionException {
    try {
      String className =
          CodegenUtil.getUppercaseClassName(
              abiJson.getString(abiJsonConfiguration.getContractNameElement()));

      this.datatypeEncodingHandler =
          new DatatypeMappingHandler(codegenConfiguration.getTargetPackage(), className);
      this.customTypesGenerator =
          new CustomTypesGenerator(this.datatypeEncodingHandler, this.abiJsonConfiguration);

      this.datatypeEncodingHandler.setCustomTypesGenerator(this.customTypesGenerator);

      TypeSpec contractTypeSpec =
          TypeSpec.classBuilder(className)
              .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
              .addField(AeternityService.class, GCV_AETERNITY_SERVICE, Modifier.PRIVATE)
              .addField(AeternityServiceConfiguration.class, GCV_CONFIG, Modifier.PRIVATE)
              .addField(
                  FieldSpec.builder(TypeName.BOOLEAN, GCV_CHECK_BYTECODE, Modifier.PRIVATE)
                      .initializer("false")
                      .build())
              .addField(String.class, GCV_DEPLOYED_CONTRACT_ID, Modifier.PRIVATE)
              .addField(
                  FieldSpec.builder(String.class, GCV_AES_SOURCECODE, Modifier.PRIVATE)
                      .initializer("$S", aesContent)
                      .build())
              .addField(
                  FieldSpec.builder(
                          ParameterizedTypeName.get(Map.class, String.class, String.class),
                          GCV_AES_INCLUDES,
                          Modifier.PRIVATE)
                      .initializer(
                          "$T.of($L)",
                          ImmutableMap.class,
                          includes.keySet().stream()
                              .map(
                                  libToInclude ->
                                      "\""
                                          + libToInclude
                                          + "\""
                                          + ","
                                          + "\""
                                          + includes
                                              .get(libToInclude)
                                              .replaceAll(
                                                  System.lineSeparator(),
                                                  System.lineSeparator()
                                                      .replace("\r", "\\\\r")
                                                      .replace("\n", "\\\\n"))
                                          + "\"")
                              .collect(Collectors.joining(",")))
                      .build())
              .addField(
                  FieldSpec.builder(Logger.class, GCV_LOGGER, Modifier.PRIVATE, Modifier.STATIC)
                      .initializer(
                          "$T.getLogger($L)",
                          LoggerFactory.class,
                          abiJson.getString(abiJsonConfiguration.getContractNameElement())
                              + ".class")
                      .build())
              .addField(
                  FieldSpec.builder(int.class, GCV_NUM_TRIALS, Modifier.PRIVATE)
                      .initializer("$L", codegenConfiguration.getNumTrials())
                      .build())
              .addMethods(buildContractPrivateMethods())
              .addMethods(buildConstructor())
              .addMethods(this.buildContractMethods(abiJson))
              .addTypes(
                  this.customTypesGenerator.generateCustomTypes(
                      abiJson.getJsonArray(abiJsonConfiguration.getCustomTypeElement()),
                      abiJson.getValue(abiJsonConfiguration.getStateElement())))
              .addJavadoc(CodegenUtil.LICENSE_HEADER)
              .addJavadoc(CodegenUtil.KRYPTOKRAUTS)
              .addJavadoc(CodegenUtil.LICENSE)
              .addJavadoc(CodegenUtil.GENERATED)
              .addJavadoc(CodegenUtil.SUPPORT)
              .build();

      /** add default deploy method if no explicit method defined in contract */
      if (GCPM_DEPLOY == null) {
        contractTypeSpec = contractTypeSpec.toBuilder().addMethod(buildDeployMethod(null)).build();
      }

      JavaFile javaFile =
          JavaFile.builder(codegenConfiguration.getTargetPackage(), contractTypeSpec).build();

      Path path = Paths.get("", codegenConfiguration.getTargetPath());

      javaFile.writeTo(path);
    } catch (Exception e) {
      throw new MojoExecutionException(
          CodegenUtil.getBaseErrorMessage(
              CmpErrorCode.FAIL_GENERATE_CONTRACT,
              String.format(
                  "Error generating contract %s",
                  abiJson.getString(abiJsonConfiguration.getContractNameElement())),
              Arrays.asList(Pair.with("Exception", e))));
    }
  }

  /**
   * contraect java class constructor
   *
   * @return
   */
  private List<MethodSpec> buildConstructor() {

    // Declare parameter names
    String PARAM_AETERNITY_SERVICE_CONFIGURATION = "aeternityServiceConfiguration";

    // Declare Code / Statements
    CodeBlock codeBlock =
        CodeBlock.builder()
            .addStatement("this.$L = $L", GCV_DEPLOYED_CONTRACT_ID, GCV_DEPLOYED_CONTRACT_ID)
            .addStatement("this.$L = $L", GCV_CONFIG, PARAM_AETERNITY_SERVICE_CONFIGURATION)
            .addStatement(
                "this.$L = new $T().getService($L)",
                GCV_AETERNITY_SERVICE,
                AeternityServiceFactory.class,
                PARAM_AETERNITY_SERVICE_CONFIGURATION)
            .addStatement("this.$L = $L", GCV_CHECK_BYTECODE, GCV_CHECK_BYTECODE)
            .beginControlFlow("if(this.$L)", GCV_CHECK_BYTECODE)
            .addStatement("this.$N()", GCPM_CONTRACT_EXISTS)
            .endControlFlow()
            .build();

    return Arrays.asList(
        MethodSpec.constructorBuilder()
            .addParameters(
                Arrays.asList(
                    ParameterSpec.builder(
                            AeternityServiceConfiguration.class,
                            PARAM_AETERNITY_SERVICE_CONFIGURATION)
                        .build(),
                    ParameterSpec.builder(String.class, GCV_DEPLOYED_CONTRACT_ID).build(),
                    ParameterSpec.builder(TypeName.BOOLEAN, GCV_CHECK_BYTECODE).build()))
            .addCode(codeBlock)
            .addModifiers(Modifier.PUBLIC)
            .addException(RuntimeException.class)
            .build(),
        MethodSpec.constructorBuilder()
            .addParameters(
                Arrays.asList(
                    ParameterSpec.builder(
                            AeternityServiceConfiguration.class,
                            PARAM_AETERNITY_SERVICE_CONFIGURATION)
                        .build(),
                    ParameterSpec.builder(String.class, GCV_DEPLOYED_CONTRACT_ID).build()))
            .addStatement(
                "this($L,$L,false)",
                PARAM_AETERNITY_SERVICE_CONFIGURATION,
                GCV_DEPLOYED_CONTRACT_ID)
            .addModifiers(Modifier.PUBLIC)
            .addException(RuntimeException.class)
            .build());
  }

  /** iterate over method declarations and create method spec */
  private List<MethodSpec> buildContractMethods(JsonObject abi) {
    List<MethodSpec> methods = new LinkedList<MethodSpec>();
    if (abi != null && abi.getValue(abiJsonConfiguration.getFunctionsElement()) != null) {
      JsonArray functions = abi.getJsonArray(abiJsonConfiguration.getFunctionsElement());
      for (Object methodDescription : functions.getList()) {
        methods.add(this.parseFunctionToMethodSpec(JsonObject.mapFrom(methodDescription)));
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
  private MethodSpec parseFunctionToMethodSpec(JsonObject functionDescription) {
    String VAR_DR_RESULT = "dryRunResult";
    String VAR_RESULT_OBJECT = "resultObject";

    String VAR_CC_MODEL = "contractCallModel";
    String VAR_CC_POST_TX_RESULT = "contractCallPostTxResult";
    String VAR_CC_POST_TX_INFO = "contractCallPostTxInfo";
    String VAR_ENCODED_PARAM_LIST = "encodedParameterList";
    String VAR_CC_AMOUNT = "amount";

    String functionName =
        replaceInvalidChars(
            functionDescription.getString(abiJsonConfiguration.getFunctionNameElement()));

    if (codegenConfiguration.getInitFunctionName().equalsIgnoreCase(functionName)) {
      return buildDeployMethod(functionDescription);
    }

    // resolve list of method parameters
    List<ParameterSpec> params = getParameterSpecFromSignature(functionDescription);

    boolean isStateful =
        functionDescription.getBoolean(abiJsonConfiguration.getFunctionStatefulElement());
    boolean isPayable = false;
    ParameterSpec amount = null;

    if (functionDescription.containsKey(abiJsonConfiguration.getPayableElement())) {
      if (functionDescription.getBoolean(abiJsonConfiguration.getPayableElement())) {
        isPayable = true;
        amount = ParameterSpec.builder(TypeName.get(BigInteger.class), VAR_CC_AMOUNT).build();
      }
    }
    // resolve the return type
    TypeName payloadType =
        this.datatypeEncodingHandler.getTypeNameFromJSON(
            functionDescription.getValue(abiJsonConfiguration.getFunctionReturnTypeElement()));
    boolean isVoid = TypeName.get(Void.class).equals(payloadType.box());

    TypeName returnType = payloadType;

    if (isStateful) {
      if (isVoid) {
        returnType = ClassName.get(String.class);
      } else {
        returnType =
            ParameterizedTypeName.get(
                ClassName.get(Pair.class), TypeName.get(String.class), payloadType);
      }
    }

    CodeBlock returnCodeBlock =
        mapResultCodeblock(
            VAR_RESULT_OBJECT,
            VAR_CC_POST_TX_RESULT,
            payloadType,
            functionName,
            isStateful,
            isVoid);

    // the logic block
    CodeBlock codeBlock =
        CodeBlock.builder()
            .addStatement(
                "$T $L = $T.asList($L)",
                ParameterizedTypeName.get(List.class, String.class),
                VAR_ENCODED_PARAM_LIST,
                Arrays.class,
                getParameterEncoding(params))
            .addStatement(
                "$T $L = this.$N($S,$L,$L)",
                ContractCallTransactionModel.class,
                VAR_CC_MODEL,
                GCPM_CREATE_CCM,
                functionName,
                VAR_ENCODED_PARAM_LIST,
                isPayable ? VAR_CC_AMOUNT : "null")
            .addStatement(
                "$T $L = this.$N($L,$S,$L)",
                DryRunTransactionResult.class,
                VAR_DR_RESULT,
                GCPM_DRY_RUN,
                VAR_CC_MODEL,
                functionName,
                isStateful)
            .addStatement(
                "$T $L = this.$L.compiler.blockingDecodeCallResult($L,$S,$L.getContractCallObject().getReturnType(),$L.getContractCallObject().getReturnValue(),$L)",
                ObjectResultWrapper.class,
                VAR_RESULT_OBJECT,
                GCV_AETERNITY_SERVICE,
                GCV_AES_SOURCECODE,
                functionName,
                VAR_DR_RESULT,
                VAR_DR_RESULT,
                GCV_AES_INCLUDES)
            .beginControlFlow("if($S.equalsIgnoreCase($L.getResult()))", "ok", VAR_DR_RESULT)
            .add(checkResultCodeblock(VAR_RESULT_OBJECT, VAR_DR_RESULT, functionName))
            .build();

    /** stateful call - add gas and gasPrice to transaction and post */
    if (isStateful) {
      codeBlock =
          codeBlock.toBuilder()
              .addStatement(
                  "$L = $L.toBuilder()"
                      + ".gasLimit($L.getContractCallObject().getGasUsed())"
                      + ".build()",
                  VAR_CC_MODEL,
                  VAR_CC_MODEL,
                  VAR_DR_RESULT)
              .addStatement(
                  "$T $L = this.$L.transactions.blockingPostTransaction($L)",
                  PostTransactionResult.class,
                  VAR_CC_POST_TX_RESULT,
                  GCV_AETERNITY_SERVICE,
                  VAR_CC_MODEL)
              .beginControlFlow("if($L == null)", VAR_CC_POST_TX_RESULT)
              .addStatement(
                  "throw new $T($S)", RuntimeException.class, "Transaction could not be posted")
              .endControlFlow()
              .addStatement(
                  "$T $L = this.$N($L.getTxHash())",
                  TransactionInfoResult.class,
                  VAR_CC_POST_TX_INFO,
                  GCPM_WAIT_FOR_TX_INFO,
                  VAR_CC_POST_TX_RESULT)
              .addStatement(
                  "$L = this.$L.compiler.blockingDecodeCallResult($L,$S,$L.getCallInfo().getReturnType(),$L.getCallInfo().getReturnValue(),$L)",
                  VAR_RESULT_OBJECT,
                  GCV_AETERNITY_SERVICE,
                  GCV_AES_SOURCECODE,
                  functionName,
                  VAR_CC_POST_TX_INFO,
                  VAR_CC_POST_TX_INFO,
                  GCV_AES_INCLUDES)
              .beginControlFlow(
                  "if($S.equalsIgnoreCase($L.getCallInfo().getReturnType()))",
                  "ok",
                  VAR_CC_POST_TX_INFO)
              .add(returnCodeBlock)
              .endControlFlow()
              .addStatement(
                  "throw new $T($T.format($S,$L))",
                  RuntimeException.class,
                  String.class,
                  "Post transaction call failed: %s",
                  VAR_RESULT_OBJECT)
              .build();
    }

    /** not a stateful call, return dryRun result */
    else {
      codeBlock = codeBlock.toBuilder().add(returnCodeBlock).build();
    }

    codeBlock =
        codeBlock.toBuilder()
            .endControlFlow()
            .addStatement(
                "throw new $T($T.format($S,$L))",
                RuntimeException.class,
                String.class,
                "DryRun call failed: %s",
                VAR_RESULT_OBJECT)
            .build();

    MethodSpec method =
        MethodSpec.methodBuilder(functionName)
            .addParameters(params)
            .addCode(codeBlock)
            .addJavadoc(isStateful ? "Stateful function" : "")
            .returns(returnType)
            .addJavadoc("")
            .addModifiers(Modifier.PUBLIC)
            .build();

    if (amount != null) {
      method = method.toBuilder().addParameter(amount).build();
    }

    return method;
  }

  private CodeBlock mapResultCodeblock(
      String VAR_RESULT_OBJECT,
      String VAR_POST_TX_RESULT,
      TypeName resultType,
      String functionName,
      boolean isStateful,
      boolean isVoid) {
    String VAR_UNWRAPPED_RESULT_OBJECT = "unwrappedResultObject";

    CodeBlock mappedToReturnValue =
        this.datatypeEncodingHandler.mapToReturnValue(resultType, VAR_UNWRAPPED_RESULT_OBJECT);

    CodeBlock returnStatement =
        CodeBlock.builder().addStatement("return $L", mappedToReturnValue).build();

    if (isStateful) {
      if (isVoid) {
        returnStatement =
            CodeBlock.builder().addStatement("return $L.getTxHash()", VAR_POST_TX_RESULT).build();
      } else {
        returnStatement =
            CodeBlock.builder()
                .add("return $T.with($L.getTxHash(),", Pair.class, VAR_POST_TX_RESULT)
                .add(mappedToReturnValue)
                .add(");")
                .build();
      }
    }

    return CodeBlock.builder()
        .addStatement(
            "$T $L = $L.getResult()", Object.class, VAR_UNWRAPPED_RESULT_OBJECT, VAR_RESULT_OBJECT)
        .add(checkResultCodeblock(VAR_RESULT_OBJECT, VAR_UNWRAPPED_RESULT_OBJECT, functionName))
        .add(returnStatement)
        .build();
  }

  private CodeBlock checkResultCodeblock(
      String VAR_UNWRAPPED_RESULT_OBJECT, String VAR_RESULT_OBJECT, String functionName) {
    String VAR_RESULT_JSON_MAP = "resultJSONMap";
    return CodeBlock.builder()
        .beginControlFlow(
            "if($L != null && $L.getResult() instanceof $T)",
            VAR_UNWRAPPED_RESULT_OBJECT,
            VAR_UNWRAPPED_RESULT_OBJECT,
            Map.class)
        .addStatement(
            "$T $L = $T.mapFrom($L.getResult())",
            JsonObject.class,
            VAR_RESULT_JSON_MAP,
            JsonObject.class,
            VAR_UNWRAPPED_RESULT_OBJECT)
        .beginControlFlow(
            "if($L.containsKey($S))", VAR_RESULT_JSON_MAP, codegenConfiguration.getResultAbortKey())
        .addStatement(
            "throw new $T($T.format($S,$S,$L.getValue($S)))",
            AException.class,
            String.class,
            "Calling function %s was aborted due to %s",
            functionName,
            VAR_RESULT_JSON_MAP,
            codegenConfiguration.getResultAbortKey())
        .endControlFlow()
        .beginControlFlow(
            "if($L.containsKey($S))", VAR_RESULT_JSON_MAP, codegenConfiguration.getResultErrorKey())
        .addStatement(
            "throw new $T($T.format($S,$S,$L.getValue($S)))",
            AException.class,
            String.class,
            "An error occured calling function %s: %s",
            functionName,
            VAR_RESULT_JSON_MAP,
            codegenConfiguration.getResultErrorKey())
        .endControlFlow()
        .endControlFlow()
        .build();
  }

  private String replaceInvalidChars(String value) {
    if (value != null) {
      value = value.replace("'", "");
    }
    return value;
  }

  private List<ParameterSpec> getParameterSpecFromSignature(JsonObject functionDescription) {
    List<ParameterSpec> params =
        functionDescription
            .getJsonArray(abiJsonConfiguration.getFunctionArgumentsElement())
            .stream()
            .map(
                param -> {
                  JsonObject paramMap = JsonObject.mapFrom(param);
                  return ParameterSpec.builder(
                          this.datatypeEncodingHandler.getTypeNameFromJSON(
                              paramMap.getValue(
                                  abiJsonConfiguration.getFunctionArgumentTypeElement())),
                          replaceInvalidChars(
                              paramMap.getString(
                                  abiJsonConfiguration.getFunctionArgumentNameElement())))
                      .build();
                })
            .collect(Collectors.toList());
    return params;
  }

  /**
   * build the deploy method
   *
   * @return
   */
  private MethodSpec buildDeployMethod(JsonObject functionDescription) {
    String VAR_CALLDATA = "calldata";
    String VAR_ENCODED_PARAM_LIST = "encodedParameterList";
    List<ParameterSpec> parameters = new LinkedList<>();

    CodeBlock getInitFunctionCalldata =
        CodeBlock.builder()
            .addStatement(
                "$T $L = this.$N($S, new $T<>())",
                String.class,
                VAR_CALLDATA,
                GCPM_CALLDATA_FOR_FCT,
                codegenConfiguration.getInitFunctionName(),
                LinkedList.class)
            .build();
    if (functionDescription != null) {
      parameters = getParameterSpecFromSignature(functionDescription);
      getInitFunctionCalldata =
          CodeBlock.builder()
              .addStatement(
                  "$T $L = $T.asList($L)",
                  ParameterizedTypeName.get(List.class, String.class),
                  VAR_ENCODED_PARAM_LIST,
                  Arrays.class,
                  getParameterEncoding(parameters))
              .addStatement(
                  "$T $L = this.$N($S,$L)",
                  String.class,
                  VAR_CALLDATA,
                  GCPM_CALLDATA_FOR_FCT,
                  codegenConfiguration.getInitFunctionName(),
                  VAR_ENCODED_PARAM_LIST)
              .build();
    }

    GCPM_DEPLOY =
        MethodSpec.methodBuilder("deploy")
            .addCode(
                CodeBlock.builder()
                    .add(getInitFunctionCalldata)
                    .addStatement("return this.$N($L)", GCPM_DEPLOY_CONTRACT, VAR_CALLDATA)
                    .build())
            .addParameters(parameters)
            .returns(
                ParameterizedTypeName.get(
                    ClassName.get(Pair.class),
                    TypeName.get(String.class),
                    TypeName.get(String.class)))
            .addModifiers(Modifier.PUBLIC)
            .build();

    return GCPM_DEPLOY;
  }

  /**
   * ------------------------------
   *
   * <p>Generated contract private methods
   *
   * <p>------------------------------
   */
  private List<MethodSpec> buildContractPrivateMethods() {
    return Arrays.asList(
        buildContractExistsMethod(),
        buildGetNextNonceMethod(),
        buildGetCalldataForFunctionMethod(),
        buildCreateTransactionCallModelMethod(),
        buildDryRunMethod(),
        buildWaitForTxMethod(),
        buildWaitForTxInfoMethod(),
        buildDeployContractMethod(),
        buildContractIdGetter(),
        buildContractIdSetter());
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
    String VAR_INIT_FUNC_NAME = "init";
    String VAR_DR_ERROR_MSG = "dryRunErrorMessage";

    this.GCPM_DEPLOY_CONTRACT =
        MethodSpec.methodBuilder("deployContract")
            .addParameter(ParameterSpec.builder(String.class, MP_INIT_CALLDATA).build())
            .addCode(
                CodeBlock.builder()
                    .addStatement(
                        "$T $L = this.$L.compiler.blockingCompile($L,null,$L).getResult()",
                        String.class,
                        VAR_BYTECODE,
                        GCV_AETERNITY_SERVICE,
                        GCV_AES_SOURCECODE,
                        GCV_AES_INCLUDES)
                    .addStatement(
                        "$T $L = $T.builder()"
                            + ".amount($T.ZERO)"
                            + ".callData($L)"
                            + ".contractByteCode($L)"
                            + ".deposit($T.ZERO)"
                            + ".gasLimit($T.valueOf($Ll))"
                            + ".gasPrice($T.MINIMAL_GAS_PRICE)"
                            + ".nonce(this.$N())"
                            + ".ownerId(this.$L.getKeyPair().getAddress())"
                            + ".ttl($T.ZERO)"
                            + ".virtualMachine(this.$L.getTargetVM())"
                            + ".build()",
                        ContractCreateTransactionModel.class,
                        VAR_CC_MODEL,
                        ContractCreateTransactionModel.class,
                        BigInteger.class,
                        MP_INIT_CALLDATA,
                        VAR_BYTECODE,
                        BigInteger.class,
                        BigInteger.class,
                        this.codegenConfiguration.getDryRunDeployGas(),
                        BaseConstants.class,
                        GCPM_NEXT_NONCE,
                        GCV_CONFIG,
                        BigInteger.class,
                        GCV_CONFIG)
                    .addStatement(
                        "$T $L = this.$L.transactions.blockingCreateUnsignedTransaction($L).getResult()",
                        String.class,
                        VAR_CC_UNSIGNED_TX,
                        GCV_AETERNITY_SERVICE,
                        VAR_CC_MODEL)
                    .addStatement(
                        "$T $L = this.$L.transactions.blockingDryRunTransactions("
                            + "$T.builder().build()"
                            + ".account($T.builder().publicKey(this.$L.getKeyPair().getAddress()).build())"
                            + ".transactionInputItem($L))",
                        DryRunTransactionResults.class,
                        VAR_CC_DR_RESULTS,
                        GCV_AETERNITY_SERVICE,
                        DryRunRequest.class,
                        DryRunAccountModel.class,
                        GCV_CONFIG,
                        VAR_CC_UNSIGNED_TX)
                    .addStatement(
                        "$T $L = $L.getResults().get(0)",
                        DryRunTransactionResult.class,
                        VAR_CC_DR_RESULT,
                        VAR_CC_DR_RESULTS)
                    .beginControlFlow(
                        "if($L.getContractCallObject().getReturnType().equals($S))",
                        VAR_CC_DR_RESULT,
                        this.codegenConfiguration.getResultRevertKey())
                    .addStatement(
                        "$T $L = $L.getContractCallObject().getReturnValue()",
                        Object.class,
                        VAR_DR_ERROR_MSG,
                        VAR_CC_DR_RESULT)
                    .add(
                        "try{$L=this.$L.compiler.blockingDecodeCallResult($L,$S,"
                            + "$L.getContractCallObject().getReturnType(),"
                            + "$L.getContractCallObject().getReturnValue(),$L).getResult();} catch($T e){}",
                        VAR_DR_ERROR_MSG,
                        GCV_AETERNITY_SERVICE,
                        GCV_AES_SOURCECODE,
                        VAR_INIT_FUNC_NAME,
                        VAR_CC_DR_RESULT,
                        VAR_CC_DR_RESULT,
                        GCV_AES_INCLUDES,
                        Exception.class)
                    .addStatement(
                        "throw new $T($T.format($S,$L))",
                        IllegalArgumentException.class,
                        String.class,
                        "Contract could not be deployed due to following exception: %s\n",
                        VAR_DR_ERROR_MSG)
                    .endControlFlow()
                    .addStatement(
                        "$L = $L.toBuilder()"
                            + ".gasLimit($L.getContractCallObject().getGasUsed())"
                            + ".gasPrice($L.getContractCallObject().getGasPrice())"
                            + ".build()",
                        VAR_CC_MODEL,
                        VAR_CC_MODEL,
                        VAR_CC_DR_RESULT,
                        VAR_CC_DR_RESULT)
                    .addStatement(
                        "$T $L = this.$L.transactions.blockingPostTransaction($L)",
                        PostTransactionResult.class,
                        VAR_CC_POST_TX_RESULT,
                        GCV_AETERNITY_SERVICE,
                        VAR_CC_MODEL)
                    .addStatement(
                        "$T $L = this.$N($L.getTxHash())",
                        TransactionInfoResult.class,
                        VAR_CC_POST_TX_INFO,
                        GCPM_WAIT_FOR_TX_INFO,
                        VAR_CC_POST_TX_RESULT)
                    .addStatement(
                        "this.$L = $L.getCallInfo().getContractId()",
                        GCV_DEPLOYED_CONTRACT_ID,
                        VAR_CC_POST_TX_INFO)
                    .addStatement(
                        "return $T.with($L.getTxHash(),$L.getCallInfo().getContractId())",
                        Pair.class,
                        VAR_CC_POST_TX_RESULT,
                        VAR_CC_POST_TX_INFO)
                    .build())
            .returns(
                ParameterizedTypeName.get(
                    ClassName.get(Pair.class),
                    TypeName.get(String.class),
                    TypeName.get(String.class)))
            .addModifiers(Modifier.PRIVATE)
            .build();
    return GCPM_DEPLOY_CONTRACT;
  }

  private MethodSpec buildWaitForTxInfoMethod() {
    String MP_TXHASH = "txHash";

    this.GCPM_WAIT_FOR_TX_INFO =
        MethodSpec.methodBuilder("waitForTxInfo")
            .addParameter(ParameterSpec.builder(String.class, MP_TXHASH).build())
            .addCode(
                CodeBlock.builder()
                    .addStatement("this.$N($L)", GCPM_WAIT_FOR_TX, MP_TXHASH)
                    .addStatement(
                        "return this.$L.info.blockingGetTransactionInfoByHash($L)",
                        GCV_AETERNITY_SERVICE,
                        MP_TXHASH)
                    .build())
            .returns(TransactionInfoResult.class)
            .addModifiers(Modifier.PRIVATE)
            .build();
    return GCPM_WAIT_FOR_TX_INFO;
  }

  private MethodSpec buildWaitForTxMethod() {
    String MP_TXHASH = "txHash";

    String VAR_BLOCK_HEIGHT = "blockHeight";
    String VAR_MINED_TX = "minedTx";
    String VAR_DONE_TRIALS = "doneTrials";

    this.GCPM_WAIT_FOR_TX =
        MethodSpec.methodBuilder("waitForTx")
            .addParameter(ParameterSpec.builder(String.class, MP_TXHASH).build())
            .addCode(
                CodeBlock.builder()
                    .addStatement("int $L = -1", VAR_BLOCK_HEIGHT)
                    .addStatement("$T $L = null", TransactionResult.class, VAR_MINED_TX)
                    .addStatement("int $L = 1", VAR_DONE_TRIALS)
                    .beginControlFlow(
                        "while($L == -1 && $L < $L)",
                        VAR_BLOCK_HEIGHT,
                        VAR_DONE_TRIALS,
                        GCV_NUM_TRIALS)
                    .addStatement(
                        "$L = $L.info.blockingGetTransactionByHash($L)",
                        VAR_MINED_TX,
                        GCV_AETERNITY_SERVICE,
                        MP_TXHASH)
                    .beginControlFlow("if($L != null)", VAR_MINED_TX)
                    .beginControlFlow("if($L.getBlockHeight().intValue() > 1)", VAR_MINED_TX)
                    .addStatement(
                        "$L.debug($S+$L)",
                        GCV_LOGGER,
                        "Transaction included in block: ",
                        VAR_MINED_TX)
                    .addStatement(
                        "$L = $L.getBlockHeight().intValue()", VAR_BLOCK_HEIGHT, VAR_MINED_TX)
                    .nextControlFlow("else")
                    .addStatement(
                        "$L.info($T.format($S,$L,$L))",
                        GCV_LOGGER,
                        String.class,
                        "Transaction not included in a block yet, trying again in 1 second (%s of %s)...",
                        VAR_DONE_TRIALS,
                        GCV_NUM_TRIALS)
                    .beginControlFlow("try")
                    .addStatement("$T.sleep(1000l)", Thread.class)
                    .nextControlFlow("catch($T e)", InterruptedException.class)
                    .addStatement(
                        "throw new $T($T.format($S,$L,$L))",
                        RuntimeException.class,
                        String.class,
                        "Waiting for transaction %s to be included in a block was interrupted due to technical error: %s",
                        MP_TXHASH,
                        "e.getMessage()")
                    .endControlFlow()
                    .addStatement("$L++", VAR_DONE_TRIALS)
                    .endControlFlow()
                    .nextControlFlow("else")
                    .addStatement(
                        "throw new $T($T.format($S,$L))",
                        AException.class,
                        String.class,
                        "An error occured getting transaction for hash - object is null, aborting",
                        MP_TXHASH)
                    .endControlFlow()
                    .endControlFlow()
                    .beginControlFlow("if($L == -1)", VAR_BLOCK_HEIGHT)
                    .addStatement(
                        "throw new $T($T.format($S,$L,$L))",
                        RuntimeException.class,
                        String.class,
                        "Transaction %s was not included in a block after %s trials, aborting",
                        MP_TXHASH,
                        VAR_DONE_TRIALS)
                    .endControlFlow()
                    .addStatement("return $L", VAR_MINED_TX)
                    .build())
            .returns(TransactionResult.class)
            .addModifiers(Modifier.PRIVATE)
            .build();
    return GCPM_WAIT_FOR_TX;
  }

  private MethodSpec buildContractExistsMethod() {
    String VAR_BYTECODE = "byteCode";

    this.GCPM_CONTRACT_EXISTS =
        MethodSpec.methodBuilder("contractExists")
            .addException(RuntimeException.class)
            .addCode(
                CodeBlock.builder()
                    .beginControlFlow(
                        "if($L != null && $L.trim().length() > 0)",
                        GCV_DEPLOYED_CONTRACT_ID,
                        GCV_DEPLOYED_CONTRACT_ID)
                    .addStatement(
                        "String $L = this.$L.info.blockingGetContractByteCode($L).getResult()",
                        VAR_BYTECODE,
                        GCV_AETERNITY_SERVICE,
                        GCV_DEPLOYED_CONTRACT_ID)
                    .beginControlFlow("if($L == null)", VAR_BYTECODE)
                    .addStatement(
                        "throw new $T($S+$L+$S)",
                        RuntimeException.class,
                        "Given contract with publickey ",
                        GCV_DEPLOYED_CONTRACT_ID,
                        " is not deployed")
                    .endControlFlow()
                    .beginControlFlow(
                        "if(!$L.equals($L.compiler.blockingCompile($L, null, null).getResult()))",
                        VAR_BYTECODE,
                        GCV_AETERNITY_SERVICE,
                        GCV_AES_SOURCECODE)
                    .addStatement(
                        "throw new $T($S+$L+$S)",
                        RuntimeException.class,
                        "Given contract with publickey ",
                        GCV_DEPLOYED_CONTRACT_ID,
                        " is not equal to contract source used to generate this class")
                    .endControlFlow()
                    .addStatement("return true")
                    .endControlFlow()
                    .addStatement("return false")
                    .build())
            .returns(boolean.class)
            .addModifiers(Modifier.PRIVATE)
            .build();
    return GCPM_CONTRACT_EXISTS;
  }

  private MethodSpec buildGetNextNonceMethod() {
    this.GCPM_NEXT_NONCE =
        MethodSpec.methodBuilder("getNextNonce")
            .addCode(
                CodeBlock.builder()
                    .addStatement(
                        "return this.$L.accounts.blockingGetAccount().getNonce().add($T.ONE)",
                        GCV_AETERNITY_SERVICE,
                        BigInteger.class)
                    .build())
            .returns(BigInteger.class)
            .addModifiers(Modifier.PRIVATE)
            .build();
    return this.GCPM_NEXT_NONCE;
  }

  private MethodSpec buildGetCalldataForFunctionMethod() {
    String MP_PARAMS = "params";
    String MP_FUNCTION = "function";

    this.GCPM_CALLDATA_FOR_FCT =
        MethodSpec.methodBuilder("getCalldataForFunction")
            .addParameters(
                Arrays.asList(
                    ParameterSpec.builder(String.class, MP_FUNCTION).build(),
                    ParameterSpec.builder(
                            ParameterizedTypeName.get(List.class, String.class), MP_PARAMS)
                        .build()))
            .addCode(
                CodeBlock.builder()
                    .addStatement(
                        "return $L.compiler.blockingEncodeCalldata($L,$L,$L,$L).getResult()",
                        GCV_AETERNITY_SERVICE,
                        GCV_AES_SOURCECODE,
                        MP_FUNCTION,
                        MP_PARAMS,
                        GCV_AES_INCLUDES)
                    .build())
            .returns(String.class)
            .addModifiers(Modifier.PRIVATE)
            .build();
    return this.GCPM_CALLDATA_FOR_FCT;
  }

  private MethodSpec buildCreateTransactionCallModelMethod() {
    String MP_PARAMS = "params";
    String MP_FUNCTION = "function";
    String MP_AMOUNT = "amount";

    String VAR_CALLDATA = "callData";

    this.GCPM_CREATE_CCM =
        MethodSpec.methodBuilder("createContractCallModel")
            .addParameters(
                Arrays.asList(
                    ParameterSpec.builder(String.class, MP_FUNCTION).build(),
                    ParameterSpec.builder(
                            ParameterizedTypeName.get(List.class, String.class), MP_PARAMS)
                        .build(),
                    ParameterSpec.builder(TypeName.get(BigInteger.class), MP_AMOUNT).build()))
            .addCode(
                CodeBlock.builder()
                    .addStatement(
                        "$T $L = this.$N($L,$L)",
                        String.class,
                        VAR_CALLDATA,
                        GCPM_CALLDATA_FOR_FCT,
                        MP_FUNCTION,
                        MP_PARAMS)
                    .addStatement(
                        "return $T.builder().callData($L)"
                            + ".gasLimit($T.valueOf(1579000l))"
                            + ".contractId($L)"
                            + ".gasPrice($T.MINIMAL_GAS_PRICE)"
                            + ".amount($L!=null?$L:$T.ZERO)"
                            + ".nonce(this.$N())"
                            + ".callerId(this.$L.getKeyPair().getAddress())"
                            + ".ttl($T.ZERO)"
                            + ".virtualMachine(this.$L.getTargetVM())"
                            + ".build()",
                        ContractCallTransactionModel.class,
                        VAR_CALLDATA,
                        BigInteger.class,
                        GCV_DEPLOYED_CONTRACT_ID,
                        BaseConstants.class,
                        MP_AMOUNT,
                        MP_AMOUNT,
                        BigInteger.class,
                        GCPM_NEXT_NONCE,
                        GCV_CONFIG,
                        BigInteger.class,
                        GCV_CONFIG)
                    .build())
            .returns(ContractCallTransactionModel.class)
            .build();

    return this.GCPM_CREATE_CCM;
  }

  private MethodSpec buildDryRunMethod() {
    String MP_CC_MODEL = "contractCallModel";
    String MP_CC_FUNC_NAME = "functionName";
    String MP_CC_STATEFUL_FUNC = "isStatefulFunction";

    String VAR_DR_RESULT = "dryRunResult";
    String VAR_DR_ERROR_MSG = "dryRunErrorMessage";

    this.GCPM_DRY_RUN =
        MethodSpec.methodBuilder("dryRunCall")
            .addParameters(
                Arrays.asList(
                    ParameterSpec.builder(ContractCallTransactionModel.class, MP_CC_MODEL).build(),
                    ParameterSpec.builder(String.class, MP_CC_FUNC_NAME).build(),
                    ParameterSpec.builder(Boolean.class, MP_CC_STATEFUL_FUNC).build()))
            .addCode(
                CodeBlock.builder()
                    .addStatement(
                        "$T $N = $L.transactions.blockingDryRunContractCall($L,$L)",
                        DryRunTransactionResult.class,
                        VAR_DR_RESULT,
                        GCV_AETERNITY_SERVICE,
                        MP_CC_MODEL,
                        MP_CC_STATEFUL_FUNC)
                    .beginControlFlow(
                        "if($L != null && $S.equalsIgnoreCase($L.getResult()))",
                        VAR_DR_RESULT,
                        "ok",
                        VAR_DR_RESULT)
                    .addStatement("return $L", VAR_DR_RESULT)
                    .endControlFlow()
                    .addStatement(
                        "$T $L = $L.getContractCallObject().getReturnValue()",
                        Object.class,
                        VAR_DR_ERROR_MSG,
                        VAR_DR_RESULT)
                    .add(
                        "try{$L=this.$L.compiler.blockingDecodeCallResult($L,$L,"
                            + "$L.getContractCallObject().getReturnType(),"
                            + "$L.getContractCallObject().getReturnValue(),$L).getResult();} catch($T e){}",
                        VAR_DR_ERROR_MSG,
                        GCV_AETERNITY_SERVICE,
                        GCV_AES_SOURCECODE,
                        MP_CC_FUNC_NAME,
                        VAR_DR_RESULT,
                        VAR_DR_RESULT,
                        GCV_AES_INCLUDES,
                        Exception.class)
                    .addStatement(
                        "throw new $T($T.format($S,$L,$L,$L.getAeAPIErrorMessage(),$L.getRootErrorMessage(),$S))",
                        RuntimeException.class,
                        String.class,
                        "\nDry run call of function %s failed: %s\nCausing Exception: %s%nException Details: %s\nException Hint   : %s",
                        MP_CC_FUNC_NAME,
                        VAR_DR_ERROR_MSG,
                        VAR_DR_RESULT,
                        VAR_DR_RESULT,
                        "Please validate your input data")
                    .build())
            .addModifiers(Modifier.PRIVATE)
            .returns(DryRunTransactionResult.class)
            .build();
    return GCPM_DRY_RUN;
  }

  private MethodSpec buildContractIdGetter() {
    return MethodSpec.methodBuilder("getContractId")
        .addModifiers(Modifier.PUBLIC)
        .returns(String.class)
        .addStatement("return $L", GCV_DEPLOYED_CONTRACT_ID)
        .build();
  }

  private MethodSpec buildContractIdSetter() {
    String MP_CONTRACT_ID = "contractId";
    return MethodSpec.methodBuilder("setContractId")
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterSpec.builder(String.class, MP_CONTRACT_ID).build())
        .addStatement("this.$L = $L", GCV_DEPLOYED_CONTRACT_ID, MP_CONTRACT_ID)
        .build();
  }

  private CodeBlock getParameterEncoding(List<ParameterSpec> params) {
    return CodeBlock.join(
        params.stream()
            .map(
                p -> {
                  return this.datatypeEncodingHandler.encodeParameter(p.type, p.name);
                })
            .collect(Collectors.toList()),
        ",");
  }

  /**
   * ------------------------------
   *
   * <p>Codegen Util Methods
   *
   * <p>------------------------------
   */
  private String readFile(String filePath) throws MojoExecutionException {
    try {
      return IOUtils.toString(Paths.get("", filePath).toUri(), StandardCharsets.UTF_8.toString());
    } catch (IOException e) {
      throw new MojoExecutionException(
          CodegenUtil.getBaseErrorMessage(
              CmpErrorCode.FAIL_READ_CONTRACT_FILE,
              String.format("Cannot read contract from file %s", filePath),
              Arrays.asList(Pair.with("Exception", e))));
    }
  }
}
