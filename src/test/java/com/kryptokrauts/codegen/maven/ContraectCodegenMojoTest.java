package com.kryptokrauts.codegen.maven;

import com.kryptokrauts.codegen.CodegenConfiguration;
import com.kryptokrauts.codegen.ContraectGenerator;
import java.io.File;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ContraectCodegenMojoTest {

  private static final String AETERNITY_BASE_URL = "AETERNITY_BASE_URL";

  private static final String COMPILER_BASE_URL = "COMPILER_BASE_URL";

  private static final String targetPath = "target/generated-sources/contraect";

  private static final String targetPackage = "com.kryptokrauts.contraect.generated";

  private static CodegenConfiguration config;

  @BeforeAll
  public static void initConfig() throws MojoExecutionException {
    ABIJsonDescription abiJsonDescription = new ABIJsonDescription();
    config =
        CodegenConfiguration.builder()
            .compilerBaseUrl(getCompilerBaseUrl())
            .targetPath(targetPath)
            .targetPackage(targetPackage)
            .datatypePackage(targetPackage + ".datatypes")
            .numTrials(60)
            .initFunctionName(abiJsonDescription.getInitFunctionName())
            .abiJSONFunctionArgumentElement(abiJsonDescription.getAbiJSONFunctionArgumentElement())
            .abiJSONFunctionArgumentNameElement(
                abiJsonDescription.getAbiJSONFunctionArgumentNameElement())
            .abiJSONFunctionArgumentTypeElement(
                abiJsonDescription.getAbiJSONFunctionArgumentTypeElement())
            .abiJSONFunctionsElement(abiJsonDescription.getAbiJSONFunctionsElement())
            .abiJSONFunctionsNameElement(abiJsonDescription.getAbiJSONFunctionsNameElement())
            .abiJSONFunctionsReturnTypeElement(
                abiJsonDescription.getAbiJSONFunctionsReturnTypeElement())
            .abiJSONFunctionStatefulElement(abiJsonDescription.getAbiJSONFunctionStatefulElement())
            .abiJSONNameElement(abiJsonDescription.getAbiJSONNameElement())
            .abiJSONRootElement(abiJsonDescription.getAbiJSONRootElement())
            .abiJSONTypesElement(abiJsonDescription.getAbiJSONTypesElement())
            .abiJSONTypesNameElement(abiJsonDescription.getAbiJSONTypesNameElement())
            .abiJSONTypesTypedefElement(abiJsonDescription.getAbiJSONTypesTypedefElement())
            .build();
  }

  @Test
  public void testCompileContraects() throws MojoExecutionException {

    ContraectGenerator generator = new ContraectGenerator(config);
    generator.generate(
        new File("src/test/resources/contraects/DatatypeTest.aes").getAbsolutePath());
    generator.generate(
        new File("src/test/resources/contraects/AENSNameUpdater.aes").getAbsolutePath());
    // generator.generate(new
    // File("src/test/resources/contraects/SophiaTypes.aes").getAbsolutePath());
    // generator.generate(
    // new
    // File("src/test/resources/contraects/CryptoHamster.aes").getAbsolutePath());
  }

  protected static String getNodeBaseUrl() throws MojoExecutionException {
    String nodeBaseUrl = System.getenv(AETERNITY_BASE_URL);
    if (nodeBaseUrl == null) {
      throw new MojoExecutionException("ENV variable missing: AETERNITY_BASE_URL");
    }
    return nodeBaseUrl;
  }

  protected static String getCompilerBaseUrl() throws MojoExecutionException {
    String compilerBaseUrl = System.getenv(COMPILER_BASE_URL);
    if (compilerBaseUrl == null) {
      throw new MojoExecutionException("ENV variable missing: COMPILER_BASE_URL");
    }
    return compilerBaseUrl;
  }
}
