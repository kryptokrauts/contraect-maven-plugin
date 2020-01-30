package com.kryptokrauts.codegen.maven;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;

import com.kryptokrauts.codegen.ContraectGenerator;

public class ContraectCodegenMojoTest extends BaseTest {

	@Test
	public void testCompileContraects() throws MojoExecutionException {

		ContraectGenerator generator = new ContraectGenerator(config);
		generator.generate(
				new File("src/test/resources/contraects/DatatypeTest.aes")
						.getAbsolutePath());
		generator.generate(
				new File("src/test/resources/contraects/AENSNameUpdater.aes")
						.getAbsolutePath());
		// generator.generate(new
		// File("src/test/resources/contraects/SophiaTypes.aes").getAbsolutePath());
		// generator.generate(
		// new
		// File("src/test/resources/contraects/CryptoHamster.aes").getAbsolutePath());
	}
}
