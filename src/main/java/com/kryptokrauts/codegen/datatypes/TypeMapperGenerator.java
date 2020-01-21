package com.kryptokrauts.codegen.datatypes;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import com.kryptokrauts.codegen.CodegenConfiguration;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
@Deprecated
// can be removed later because we do not need to build a class - this will be
// called within the ContraectGenerator
@RequiredArgsConstructor
public class TypeMapperGenerator {

	// can be removed later because we do not need to build a class - this code
	// will
	// be called within the ContraectGenerator
	@NonNull
	private CodegenConfiguration config;

	private List<BaseMapper> mapperTypes = Arrays
			.asList(new StringMapperDefinition(), new ListMapperDefinition());

	public TypeSpec generate(BaseMapper mapper) {
		TypeSpec typeMapperImplSpec = TypeSpec
				.classBuilder(mapper.getMapperTypeName())
				.addModifiers(Modifier.PRIVATE)
				.addSuperinterface(ParameterizedTypeName.get(
						TypeMapperInterface.getClassName(),
						mapper.getReturnType()))
				.addMethod(MethodSpec
						.methodBuilder(
								TypeMapperInterface.MAPPER_METHOD_APPLIES)
						.addModifiers(Modifier.PUBLIC)
						.addParameter(ParameterSpec
								.builder(Object.class,
										TypeMapperInterface.VAR_TYPE_STR)
								.build())
						.addCode(mapper.appliesCodeblock())
						.returns(TypeName.BOOLEAN).build())
				.addMethod(MethodSpec
						.methodBuilder(
								TypeMapperInterface.MAPPER_METHOD_GET_TYPE)
						.addModifiers(Modifier.PUBLIC)
						.addParameter(ParameterSpec
								.builder(Object.class,
										TypeMapperInterface.VAR_TYPE_STR)
								.build())
						.addCode(mapper.getTypeCodeblock())
						.returns(TypeName.class).build())
				.addMethod(MethodSpec
						.methodBuilder(
								TypeMapperInterface.MAPPER_METHOD_DECODE_RESULT)
						.addModifiers(Modifier.PUBLIC)
						.returns(mapper.getReturnType())
						.addParameter(ParameterSpec
								.builder(Object.class,
										TypeMapperInterface.VAR_TYPE_STR)
								.build())
						.addCode(CodeBlock.builder().add("return ")
								.add(mapper.decodeResultCodeblock()).add(";")
								.build())
						.build())
				.build();

		return typeMapperImplSpec;
	}

	// For testing purpose, take method and list to contractgenerator later
	public TypeSpec generate() {
		String MP_MAPPER_LIST = "mapperList";
		String GCPM_GET_MAPPER = "getMapper";

		try {
			TypeSpec interfaceSpec = TypeSpec
					.classBuilder("TypeMapperInnerClassHolder")
					.addType(TypeMapperInterface.generate())
					.addField(FieldSpec.builder(
							ParameterizedTypeName.get(ClassName.get(List.class),
									ParameterizedTypeName.get(
											TypeMapperInterface.getClassName(),
											WildcardTypeName
													.subtypeOf(Object.class))),
							MP_MAPPER_LIST, Modifier.PRIVATE)
							.initializer(CodeBlock.builder()
									.add("$T.asList(", Arrays.class)
									.add(CodeBlock.join(mapperTypes.stream()
											.map(m -> CodeBlock.builder().add(
													"new $T()",
													ClassName.get("", m
															.getMapperTypeName()))
													.build())
											.collect(Collectors.toList()), ","))
									.add(")").build())
							.build())
					.addMethod(MethodSpec.methodBuilder(GCPM_GET_MAPPER)
							.addModifiers(Modifier.PRIVATE)
							.returns(ParameterizedTypeName.get(
									TypeMapperInterface.getClassName(),
									WildcardTypeName.subtypeOf(Object.class)))
							.addCode(CodeBlock.builder().addStatement(
									"return $L.stream().filter(mapper->mapper.$L($L)).findFirst().orElseThrow(() -> new $T($S))",
									MP_MAPPER_LIST,
									TypeMapperInterface.MAPPER_METHOD_APPLIES,
									TypeMapperInterface.VAR_TYPE_STR,
									RuntimeException.class,
									"No suitable mapper found").build())
							.addParameter(ParameterSpec
									.builder(Object.class,
											TypeMapperInterface.VAR_TYPE_STR)
									.build())
							.build())
					.addMethod(MethodSpec.methodBuilder("getReturnType")
							.addModifiers(Modifier.PUBLIC)
							.returns(TypeName.get(TypeName.class))
							.addCode(CodeBlock.builder().addStatement(
									"return this.$N($L).$L($L)",
									GCPM_GET_MAPPER,
									TypeMapperInterface.VAR_TYPE_STR,
									TypeMapperInterface.MAPPER_METHOD_GET_TYPE,
									TypeMapperInterface.VAR_TYPE_STR).build())
							.addParameter(ParameterSpec
									.builder(Object.class,
											TypeMapperInterface.VAR_TYPE_STR)
									.build())
							.build())
					.addTypes(
							mapperTypes.stream().map(mapper -> generate(mapper))
									.collect(Collectors.toList()))
					.addMethod(BaseMapper.M_VALUE_TO_STRING).build();

			JavaFile javaFile = JavaFile
					.builder(config.getDatatypePackage(), interfaceSpec)
					.build();

			Path path = Paths.get("", config.getTargetPath());

			javaFile.writeTo(path);

			return interfaceSpec;
		} catch (Exception e) {
			throw new RuntimeException(
					"Error generating sophia type mapper interface", e);
		}
	}
}
