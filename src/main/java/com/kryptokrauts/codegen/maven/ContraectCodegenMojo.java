package com.kryptokrauts.codegen.maven;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

import com.kryptokrauts.codegen.CodegenConfiguration;
import com.kryptokrauts.codegen.ContraectGenerator;
import com.kryptokrauts.codegen.datatypes.AddressDatatypeGenerator;

@Mojo(name = "generate-contraects", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ContraectCodegenMojo extends AbstractMojo {

	/**
	 * The package for generated contracts
	 */
	@Parameter(defaultValue = "com.kryptokrauts.contracts")
	private String targetPackage;

	/**
	 * The package for autogenerated sophia datatypes
	 */
	@Parameter(defaultValue = "com.kryptokrauts.contracts.datatypes")
	private String datatypePackage;

	/**
	 * The path where generated contracts are created
	 */
	@Parameter(defaultValue = "target/generated/contracts")
	private String targetPath;

	/**
	 * url to sophia compiler, necessary to get abi from source file
	 */
	@Parameter(required = true)
	private String compilerBaseUrl;

	/**
	 * number of trials to wait for a transaction to be mined (stateful calls,
	 * deploy)
	 */
	@Parameter(defaultValue = "60")
	private int numTrials;

	/**
	 * the directories to be scanned for contracts
	 */
	@Parameter(required = true)
	private String[] directories;

	/**
	 * file suffix identifying contract code files
	 */
	@Parameter(defaultValue = "aes")
	private String contractSuffix;

	/**
	 * describes the abi json tags which are parsed during contract generation
	 */
	@Parameter
	private ABIJsonDescription abiJsonDescription;

	// the list of contract files to process
	List<String> aesFiles = new LinkedList<String>();

	// the base configuration for the code generation
	CodegenConfiguration config;

	// the generator class
	ContraectGenerator contraectGenerator;

	@Override
	public void execute() throws MojoExecutionException {
		logMessage("starting kryptokrauts contraect generator");
		if (abiJsonDescription == null) {
			abiJsonDescription = new ABIJsonDescription();
		}
		config = CodegenConfiguration.builder().compilerBaseUrl(compilerBaseUrl)
				.targetPackage(targetPackage).targetPath(targetPath)
				.datatypePackage(datatypePackage).numTrials(numTrials)
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
				.build();

		generateDefaultDatatypes();
		gatherContractFiles();
		processContractFiles();
	}

	private void processContractFiles() throws MojoExecutionException {
		for (String aesFile : aesFiles) {
			contraectGenerator = new ContraectGenerator(config);
			contraectGenerator.generate(aesFile);
		}
	}

	private void gatherContractFiles() {
		for (String currentDirectory : directories) {
			if (!StringUtils.isBlank(currentDirectory)) {
				aesFiles.addAll(scanFileset(contractSuffix, currentDirectory));
			}
		}
		logFiles(aesFiles);

	}

	private void generateDefaultDatatypes() throws MojoExecutionException {
		new AddressDatatypeGenerator(config).generate();
	}

	private void logFiles(List<String> aesFiles) {
		if (aesFiles.size() > 0) {
			getLog().debug(String.format("Found %d contracts for generation",
					aesFiles.size()));
			aesFiles.forEach(c -> {
				getLog().debug(c);
			});
		}
	}

	private List<String> scanFileset(String suffix, String currentDirectory) {
		FileSetManager fileSetManager = new FileSetManager();
		FileSet fileset = new FileSet();
		fileset.setDirectory(currentDirectory);
		fileset.setIncludes(Arrays.asList("**/*." + suffix));
		return Arrays.asList(fileSetManager.getIncludedFiles(fileset)).stream()
				.map(file -> currentDirectory + "/" + file)
				.collect(Collectors.toList());
	}

	private void logMessage(String message) {
		getLog().info(
				"------------------------------------------------------------------------");
		getLog().info(message);
		getLog().info(
				"------------------------------------------------------------------------");
	}
}
