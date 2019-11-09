package com.kryptokrauts.codegen;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;

import com.kryptokrauts.aeternity.sdk.service.aeternity.AeternityServiceConfiguration;
import com.kryptokrauts.aeternity.sdk.service.aeternity.AeternityServiceFactory;
import com.kryptokrauts.aeternity.sdk.service.aeternity.impl.AeternityService;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder
@Getter
public class CodegenConfiguration {

	private AeternityService aeternityService;

	private String compilerBaseUrl;

	// @TODO - MAYBE ADD CONTRACT NAME TO PACKAGE?
	private String targetPackage;

	private String datatypePackage;

	private String targetPath;

	/*
	 * JSON values for contract abi
	 */
	@Default
	private String abiJSONRootElement = "contract";

	@Default
	private String abiJSONNameElement = "name";

	@Default
	private String abiJSONFunctionsElement = "functions";

	@Default
	private String abiJSONFunctionsNameElement = "name";

	@Default
	private String abiJSONFunctionsReturnTypeElement = "returns";

	public AeternityService getAeternityService() {
		if (aeternityService == null) {
			VertxOptions options = new VertxOptions();
			options.getFileSystemOptions().setFileCachingEnabled(false)
					.setClassPathResolvingEnabled(false);
			options.getMetricsOptions().setEnabled(false);
			Vertx vertxInstance = Vertx.vertx(options);
			aeternityService = new AeternityServiceFactory()
					.getService(AeternityServiceConfiguration.configure()
							.vertx(vertxInstance)
							.compilerBaseUrl(this.compilerBaseUrl).compile());
		}
		return aeternityService;
	}

	public void setCompilerBaseUrl(String compilerBaseUrl)
			throws MojoExecutionException {
		try {
			URL url = new URL(compilerBaseUrl);
			url.toURI();
		} catch (MalformedURLException | URISyntaxException e) {
			throw new MojoExecutionException(String.format(
					"Given compilerBaseUrl %s is not a valid parameter",
					compilerBaseUrl));
		}
	}
}
