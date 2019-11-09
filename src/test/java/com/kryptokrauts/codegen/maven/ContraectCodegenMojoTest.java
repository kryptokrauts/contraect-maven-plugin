package com.kryptokrauts.codegen.maven;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.kryptokrauts.codegen.CodegenConfiguration;
import com.kryptokrauts.codegen.ContraectGenerator;
import com.kryptokrauts.codegen.datatypes.AddressDatatypeGenerator;

public class ContraectCodegenMojoTest {

	private static final String targetPath = "target/generated/contracts/src/main/java";

	private static final String targetPackage = "com.kryptokrauts.contracts";

	private static CodegenConfiguration config;

	@BeforeAll
	public static void initConfig() {
		config = CodegenConfiguration.builder()
				.compilerBaseUrl("http://compiler.aelocal:3080")
				.targetPath(targetPath).targetPackage(targetPackage)
				.datatypePackage(targetPackage + ".datatypes").build();
	}

	@Test
	public void testCompileContracts() throws JsonParseException,
			JsonMappingException, IOException, MojoExecutionException {

		ContraectGenerator generator = new ContraectGenerator(config);

		generator.generate(
				new File("src/test/resources/contracts/aes/SophiaTypes.aes")
						.getAbsolutePath());
	}

	@Test
	public void generateDatatypes() throws MojoExecutionException {
		new AddressDatatypeGenerator(config).generate();
	}
}
