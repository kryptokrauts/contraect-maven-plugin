package com.kryptokrauts.codegen.datatypes;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
@Deprecated
public abstract class TypeMapperInterface extends BaseMapper {

	public static final String VAR_TYPE_STR = "typeString";

	public static final String MAPPER_METHOD_APPLIES = "applies";

	public static final String MAPPER_METHOD_GET_TYPE = "getType";

	public static final String MAPPER_METHOD_DECODE_RESULT = "decodeResult";

	public static String TYPE_MAPPER_INTERFACE_NAME = "SophiaTypeMapper";

	public static TypeVariableName TYPE_MAPPER_GENERIC_VARIABLE = TypeVariableName
			.get("JavaType");

	public static TypeSpec generate() {
		TypeSpec interfaceSpec = TypeSpec
				.interfaceBuilder(TYPE_MAPPER_INTERFACE_NAME)
				.addModifiers(Modifier.PRIVATE)
				.addTypeVariable(TYPE_MAPPER_GENERIC_VARIABLE)
				.addMethod(MethodSpec.methodBuilder(MAPPER_METHOD_APPLIES)
						.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
						.addParameter(ParameterSpec
								.builder(Object.class, VAR_TYPE_STR).build())
						.returns(TypeName.BOOLEAN)
						.addJavadoc(
								"check if typeMapper can be used for the given sophia typeString")
						.build())
				.addMethod(MethodSpec.methodBuilder(MAPPER_METHOD_GET_TYPE)
						.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
						.addParameter(ParameterSpec
								.builder(Object.class, VAR_TYPE_STR).build())
						.returns(TypeName.class)
						.addJavadoc(
								"returns the type for the given sophia typeString")
						.build())
				.addMethod(MethodSpec.methodBuilder(MAPPER_METHOD_DECODE_RESULT)
						.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
						.returns(TYPE_MAPPER_GENERIC_VARIABLE)
						.addParameter(ParameterSpec
								.builder(Object.class, VAR_TYPE_STR).build())
						.addJavadoc(
								"get the return statement for function call - parse result back to corresponding java type")
						.build())
				.build();

		return interfaceSpec;
	}

	public static ClassName getClassName() {
		return ClassName.get("", TYPE_MAPPER_INTERFACE_NAME);
	}
}
