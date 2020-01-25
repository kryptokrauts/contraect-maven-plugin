package com.kryptokrauts.codegen;

import javax.lang.model.element.Modifier;

import com.kryptokrauts.aeternity.sdk.constants.ApiIdentifiers;
import com.kryptokrauts.aeternity.sdk.exception.InvalidParameterException;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
/**
 * @deprecated
 * @author mitch
 */
// @RequiredArgsConstructor
public class AddressDatatypeGenerator {

	// @NonNull
	// private CodegenConfiguration config;

	public static final String ADDRESS_DATATYPE_NAME = "Address";

	private static final String ADDRESS_VARIABLE = "address";

	public TypeSpec generate() {
		// try {
		TypeSpec addressTypeSpec = TypeSpec.classBuilder(ADDRESS_DATATYPE_NAME)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addMethod(toStringMethod()).addMethod(setAddressMethod())
				.addMethod(getAddressMethod()).addMethod(constructor())
				.addField(String.class, ADDRESS_VARIABLE, Modifier.PRIVATE)
				.build();

		// JavaFile javaFile = JavaFile
		// .builder(config.getDatatypePackage(), addressTypeSpec)
		// .build();
		//
		// Path path = Paths.get("", config.getTargetPath());
		//
		// javaFile.writeTo(path);
		// } catch (Exception e) {
		// throw new MojoExecutionException(
		// "Error generating sophia address datatype", e);
		// }
		return addressTypeSpec;
	}

	private MethodSpec constructor() {
		MethodSpec constructor = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addParameter(ParameterSpec
						.builder(String.class, ADDRESS_VARIABLE, Modifier.FINAL)
						.build())
				.addStatement("this.$L = $L", ADDRESS_VARIABLE,
						ADDRESS_VARIABLE)
				.build();
		return constructor;
	}

	private MethodSpec getAddressMethod() {
		MethodSpec method = MethodSpec
				.methodBuilder("get"
						+ CodegenUtil.getUppercaseClassName(ADDRESS_VARIABLE))
				.addModifiers(Modifier.PUBLIC).returns(String.class)
				.addStatement("return $L", ADDRESS_VARIABLE).build();
		return method;
	}

	private MethodSpec setAddressMethod() {
		MethodSpec method = MethodSpec
				.methodBuilder("set"
						+ CodegenUtil.getUppercaseClassName(ADDRESS_VARIABLE))
				.addModifiers(Modifier.PUBLIC)
				.addParameter(ParameterSpec
						.builder(String.class, ADDRESS_VARIABLE, Modifier.FINAL)
						.build())
				.beginControlFlow(
						"if($L == null || !$L.startsWith($T.ACCOUNT_PUBKEY))",
						"address", "address", ApiIdentifiers.class)
				.addStatement(
						"throw new $T($T.format(\"Given address %s is not of type aeternity public key\",$L))",
						InvalidParameterException.class, String.class,
						ADDRESS_VARIABLE)
				.endControlFlow().addStatement("this.$L = $L", ADDRESS_VARIABLE,
						ADDRESS_VARIABLE)
				.build();

		return method;
	}

	private static MethodSpec toStringMethod() {
		MethodSpec method = MethodSpec.methodBuilder("toString")
				.addModifiers(Modifier.PUBLIC).addAnnotation(Override.class)
				.returns(String.class)
				.addStatement("return $L", ADDRESS_VARIABLE).build();
		return method;
	}
}
