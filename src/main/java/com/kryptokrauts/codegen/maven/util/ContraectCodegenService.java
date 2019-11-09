package com.kryptokrauts.codegen.maven.util;

@Deprecated
public class ContraectCodegenService {
	//
	// private ContraectGenerator contraectGenerator;
	//
	// @NonNull
	// private CodegenConfiguration config;
	//
	// public void generateContract(String aesFile) throws
	// MojoExecutionException {
	// String aesContent = readFile(aesFile);
	// ACIResult abiContent = this.getAeternityService().compiler
	// .blockingGenerateACI(aesContent, null, null);
	// if (abiContent.getEncodedAci() != null) {
	// generateContractFromContent(abiContent.getEncodedAci(), aesFile);
	// } else {
	// throw new MojoExecutionException(
	// "Cannot create ABI for contract " + aesFile);
	// }
	// }
	// private String readFile(String filePath) throws MojoExecutionException {
	// try {
	// return IOUtils.toString(Paths.get("", filePath).toUri(),
	// StandardCharsets.UTF_8.toString());
	// } catch (IOException e) {
	// throw new MojoExecutionException(String
	// .format("Cannot read contract from file %s", filePath), e);
	// }
	// }
	//
	// private void generateContractFromContent(Object contractContent,
	// String contractFile) throws MojoExecutionException {
	// String filename = Paths.get(contractFile).toFile().getName()
	// .split("\\.")[0];
	// log("Generating objects for contract + filename");
	// ContraectGenerator.generate(filename, contractContent,
	// config.getTargetPackage(), config.getTargetPath());
	// }
	//
	//
	//
	// private void log(String message) {
	// if (config.getLog() != null) {
	// config.getLog().info(message);
	// } else {
	// System.out.println(message);
	// }
	// }
}
