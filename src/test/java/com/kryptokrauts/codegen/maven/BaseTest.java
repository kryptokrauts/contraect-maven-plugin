package com.kryptokrauts.codegen.maven;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.BeforeAll;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.kryptokrauts.aeternity.sdk.constants.Network;
import com.kryptokrauts.aeternity.sdk.constants.VirtualMachine;
import com.kryptokrauts.aeternity.sdk.domain.secret.impl.BaseKeyPair;
import com.kryptokrauts.aeternity.sdk.service.aeternity.AeternityServiceConfiguration;
import com.kryptokrauts.aeternity.sdk.service.keypair.KeyPairService;
import com.kryptokrauts.aeternity.sdk.service.keypair.KeyPairServiceFactory;
import com.kryptokrauts.codegen.CodegenConfiguration;

public abstract class BaseTest {

	protected static CodegenConfiguration config;

	protected static final String AETERNITY_BASE_URL = "AETERNITY_BASE_URL";

	protected static final String COMPILER_BASE_URL = "COMPILER_BASE_URL";

	protected static final String targetPath = "target/generated-sources/contraect";

	protected static final String targetPackage = "com.kryptokrauts.contraect.generated";

	protected static AeternityServiceConfiguration aeternityServiceConfig;

	@BeforeAll
	public static void initConfig() throws MojoExecutionException {
		ABIJsonDescription abiJsonDescription = new ABIJsonDescription();
		config = CodegenConfiguration.builder()
				.compilerBaseUrl(getCompilerBaseUrl()).targetPath(targetPath)
				.targetPackage(targetPackage)
				.datatypePackage(targetPackage + ".datatypes").numTrials(60)
				.initFunctionName(abiJsonDescription.getInitFunctionName())
				.abiJSONFunctionArgumentElement(
						abiJsonDescription.getAbiJSONFunctionArgumentElement())
				.abiJSONFunctionArgumentNameElement(abiJsonDescription
						.getAbiJSONFunctionArgumentNameElement())
				.abiJSONFunctionArgumentTypeElement(abiJsonDescription
						.getAbiJSONFunctionArgumentTypeElement())
				.abiJSONFunctionsElement(
						abiJsonDescription.getAbiJSONFunctionsElement())
				.abiJSONFunctionsNameElement(
						abiJsonDescription.getAbiJSONFunctionsNameElement())
				.abiJSONFunctionsReturnTypeElement(abiJsonDescription
						.getAbiJSONFunctionsReturnTypeElement())
				.abiJSONFunctionStatefulElement(
						abiJsonDescription.getAbiJSONFunctionStatefulElement())
				.abiJSONNameElement(abiJsonDescription.getAbiJSONNameElement())
				.abiJSONRootElement(abiJsonDescription.getAbiJSONRootElement())
				.abiJSONTypesElement(
						abiJsonDescription.getAbiJSONTypesElement())
				.abiJSONTypesNameElement(
						abiJsonDescription.getAbiJSONTypesNameElement())
				.abiJSONTypesTypedefElement(
						abiJsonDescription.getAbiJSONTypesTypedefElement())
				.resultAbortKey(abiJsonDescription.getResultAbortKey()).build();

		KeyPairService keyPairService = new KeyPairServiceFactory()
				.getService();

		BaseKeyPair baseKeyPair = keyPairService.generateBaseKeyPairFromSecret(
				"79816BBF860B95600DDFABF9D81FEE81BDB30BE823B17D80B9E48BE0A7015ADF");

		aeternityServiceConfig = AeternityServiceConfiguration.configure()
				.compilerBaseUrl("http://compiler.aelocal:3080")
				.baseUrl("http://aelocal").network(Network.DEVNET)
				.baseKeyPair(baseKeyPair).targetVM(VirtualMachine.FATE)
				.compile();
	}

	protected static String getNodeBaseUrl() throws MojoExecutionException {
		String nodeBaseUrl = System.getenv(AETERNITY_BASE_URL);
		if (nodeBaseUrl == null) {
			throw new MojoExecutionException(
					"ENV variable missing: AETERNITY_BASE_URL");
		}
		return nodeBaseUrl;
	}

	protected static String getCompilerBaseUrl() throws MojoExecutionException {
		String compilerBaseUrl = System.getenv(COMPILER_BASE_URL);
		if (compilerBaseUrl == null) {
			throw new MojoExecutionException(
					"ENV variable missing: COMPILER_BASE_URL");
		}
		return compilerBaseUrl;
	}

	protected Map<String, ClassInfo> getCustomDatatypes() {
		try {
			ImmutableSet<ClassInfo> cls = ClassPath
					.from(Thread.currentThread().getContextClassLoader())
					.getTopLevelClasses();
			return cls.stream()
					.filter(c -> c.getPackageName().startsWith(targetPackage))
					.collect(Collectors.toMap(c -> c.getSimpleName(), c -> c));
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<>();
		}
	}
}
