package com.kryptokrauts.codegen.datatypes.deprecated;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.lang.model.element.Modifier;

import org.apache.maven.plugin.MojoExecutionException;

import com.kryptokrauts.aeternity.sdk.constants.ApiIdentifiers;
import com.kryptokrauts.aeternity.sdk.exception.InvalidParameterException;
import com.kryptokrauts.codegen.CodegenConfiguration;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AddressDatatypeGenerator {

	@NonNull
	private CodegenConfiguration config;

	public static final String ADDRESS_DATATYPE_NAME = "Address";

	private static final String addressVariable = "address";

	public void generate() throws MojoExecutionException {
		try {
			TypeSpec addressTypeSpec = TypeSpec
					.classBuilder(ADDRESS_DATATYPE_NAME)
					.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
					.addMethod(toStringMethod()).addMethod(setAddressMethod())
					.addMethod(getAddressMethod()).addMethod(constructor())
					.addField(String.class, "address", Modifier.PRIVATE)
					.build();

			JavaFile javaFile = JavaFile
					.builder(config.getDatatypePackage(), addressTypeSpec)
					.build();

			Path path = Paths.get("", config.getTargetPath());

			javaFile.writeTo(path);
		} catch (Exception e) {
			throw new MojoExecutionException(
					"Error generating sophia address datatype", e);
		}
	}

	private MethodSpec constructor() {
		MethodSpec constructor = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addParameter(ParameterSpec
						.builder(String.class, addressVariable, Modifier.FINAL)
						.build())
				.addStatement("this.$L = $L", addressVariable, addressVariable)
				.build();
		return constructor;
	}

	private MethodSpec getAddressMethod() {
		MethodSpec method = MethodSpec.methodBuilder("getAddress")
				.addModifiers(Modifier.PUBLIC).returns(String.class)
				.addStatement("return $L", addressVariable).build();
		return method;
	}

	private MethodSpec setAddressMethod() {
		MethodSpec method = MethodSpec.methodBuilder("setAddress")
				.addModifiers(Modifier.PUBLIC)
				.addParameter(ParameterSpec
						.builder(String.class, addressVariable, Modifier.FINAL)
						.build())
				.beginControlFlow(
						"if($L == null || !$L.startsWith($T.ACCOUNT_PUBKEY))",
						"address", "address", ApiIdentifiers.class)
				.addStatement(
						"throw new $T($T.format(\"Given address %s is not of type aeternity public key\",$L))",
						InvalidParameterException.class, String.class,
						addressVariable)
				.endControlFlow()
				.addStatement("this.$L = $L", addressVariable, addressVariable)
				.build();

		return method;
	}

	private static MethodSpec toStringMethod() {
		MethodSpec method = MethodSpec.methodBuilder("toString")
				.addModifiers(Modifier.PUBLIC).addAnnotation(Override.class)
				.returns(String.class)
				.addStatement("return $L", addressVariable).build();
		return method;
	}
}
