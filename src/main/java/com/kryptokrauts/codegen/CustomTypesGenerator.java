package com.kryptokrauts.codegen;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import org.javatuples.Pair;
import org.javatuples.Quartet;

import com.google.common.collect.ImmutableMap;
import com.kryptokrauts.aeternity.sdk.exception.InvalidParameterException;
import com.kryptokrauts.aeternity.sdk.util.EncodingUtils;
import com.kryptokrauts.codegen.datatypes.DatatypeMappingHandler;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * this class generates type definition for custom types which are added as
 * static subclasses to the generated contract class
 *
 * @author mitch
 */
@RequiredArgsConstructor
public class CustomTypesGenerator {

	private static final String ADDRESS_TYPE = "address";

	// the default parameter name
	public static final String MP_PARAM = "p";

	// the encodeValue method name
	public static final String M_ENCODE_VALUE = "encodeValue";

	// the mapToReturnValue method name
	public static final String M_MAP_TO_RETURN_VALUE = "mapToReturnValue";

	// add the default address datatype
	public static final JsonObject ADDRESS_DATATYPE = new JsonObject()
			.put("name", ADDRESS_TYPE).put("typedef",
					new JsonObject().put("record",
							new JsonArray().add(
									new JsonObject().put("name", ADDRESS_TYPE)
											.put("type", "string"))));

	@NonNull
	DatatypeMappingHandler datatypeEncodingHandler;
	/** @TODO check if record before -> if not, don't create type */
	public List<TypeSpec> generateCustomTypes(JsonArray customTypes,
			Object state) {
		customTypes.add(ADDRESS_DATATYPE);
		if (state != null) {
			customTypes.add(new JsonObject().put("name", "state").put("typedef",
					state));
		}
		return customTypes.stream()
				.map(typeDefinition -> generateCustomType(
						JsonObject.mapFrom(typeDefinition)))
				.collect(Collectors.toList());
	}

	/** @TODO refactor json names */
	private TypeSpec generateCustomType(JsonObject typeDefinition) {
		List<Pair<String, TypeName>> fields = new LinkedList<>();
		Object fieldDefinition = typeDefinition.getValue("typedef");
		if (fieldDefinition instanceof JsonObject) {
			JsonArray record = typeDefinition.getJsonObject("typedef")
					.getJsonArray("record");
			// if record is defined, it is a custom type
			if (record != null) {
				fields = record.stream().map(definition -> {
					JsonObject oneField = JsonObject.mapFrom(definition);
					String name = oneField.getString("name");
					TypeName type = this.datatypeEncodingHandler
							.getTypeNameFromJSON(oneField.getValue("type"));
					return Pair.with(name, type);
				}).collect(Collectors.toList());
			}
		} else if (fieldDefinition instanceof String) {
			fields.add(Pair.with(fieldDefinition.toString(),
					TypeName.get(String.class)));
		} else {
			throw new RuntimeException(
					"Unforseen case of custom type definition "
							+ typeDefinition.encodePrettily());
		}
		String name = CodegenUtil
				.getUppercaseClassName(typeDefinition.getString("name"));

		return TypeSpec.classBuilder(name).addFields(buildFields(fields))
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.addMethods(generateCustomTypeConstructors(fields, name))
				.addMethods(generateCustomTypeGetters(fields))
				.addMethods(generateCustomTypeSetters(fields, name))
				.addMethod(generateEncodeValueMethod(fields, name))
				.addMethod(generateToString(fields))
				.addMethod(generateEquals(fields, name))
				.addMethod(generateMapToReturnValueMethod(name)).build();
	}

	private MethodSpec generateEncodeValueMethod(
			List<Pair<String, TypeName>> fields, String customTypeName) {
		CodeBlock encodeValueLogic = CodeBlock.builder().add(" \"{")
				.add(fields.stream().map(f -> f.getValue0() + "=\"+"
						+ this.datatypeEncodingHandler.encodeParameter(
								f.getValue1(), MP_PARAM + "." + f.getValue0())
						+ "+\"").collect(Collectors.joining(",")))
				.add("}\"").build();
		// if it is a predefined class use this encoding logic
		if (PREDEFINED_TYPES.get(customTypeName.toLowerCase()) != null) {
			encodeValueLogic = PREDEFINED_TYPES
					.get(customTypeName.toLowerCase()).getValue0();
		}

		return MethodSpec.methodBuilder(M_ENCODE_VALUE).addCode("return ")
				.addCode(encodeValueLogic).addCode(";")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.addParameter(this.getParameterSpec(customTypeName))
				.returns(TypeName.get(String.class)).build();
	}

	private MethodSpec generateMapToReturnValueMethod(String customTypeName) {
		CodeBlock returnValueLogic = CodeBlock.builder()
				.add("$T.mapFrom($L).mapTo($T.class)", JsonObject.class,
						MP_PARAM, this.getClassName(customTypeName))
				.build();
		// if it is a predefined class use this encoding logic
		if (PREDEFINED_TYPES.get(customTypeName.toLowerCase()) != null) {
			returnValueLogic = PREDEFINED_TYPES
					.get(customTypeName.toLowerCase()).getValue1();
		}

		return MethodSpec.methodBuilder(M_MAP_TO_RETURN_VALUE)
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.addParameter(TypeName.get(Object.class), MP_PARAM)
				.returns(this.getClassName(customTypeName)).addCode("return ")
				.addCode(returnValueLogic).addCode(";").build();
	}

	private List<MethodSpec> generateCustomTypeConstructors(
			List<Pair<String, TypeName>> fields, String customTypeName) {
		List<MethodSpec> constructors = new LinkedList<>();

		MethodSpec valueConstructor = MethodSpec.constructorBuilder()
				.addParameters(fields.stream()
						.map(v -> ParameterSpec
								.builder(v.getValue1(), v.getValue0()).build())
						.collect(Collectors.toList()))
				.addCode(generateConstructorInitializer(fields))
				.addModifiers(Modifier.PUBLIC).build();

		// if it is a predefined class add constructor logic
		if (PREDEFINED_TYPES.get(customTypeName.toLowerCase()) != null) {
			valueConstructor = PREDEFINED_TYPES
					.get(customTypeName.toLowerCase()).getValue2();
		}
		constructors.add(valueConstructor);
		// add default constructor if fields are defined
		if (fields.size() > 0) {
			constructors.add(MethodSpec.constructorBuilder()
					.addModifiers(Modifier.PUBLIC).build());
		}
		return constructors;
	}

	private CodeBlock generateConstructorInitializer(
			List<Pair<String, TypeName>> fields) {
		CodeBlock initializer = CodeBlock.builder().build();
		for (Pair<String, TypeName> pair : fields) {
			initializer = initializer.toBuilder().addStatement("this.$L = $L",
					pair.getValue0(), pair.getValue0()).build();
		}
		return initializer;
	}

	private MethodSpec generateToString(List<Pair<String, TypeName>> fields) {
		return MethodSpec.methodBuilder("toString")
				.returns(TypeName.get(String.class))
				.addModifiers(Modifier.PUBLIC)
				.addStatement("return " + ((fields != null && fields.size() > 0)
						? fields.stream()
								.map(f -> "\"" + f.getValue0() + "=\"+this."
										+ f.getValue0() + ".toString()")
								.collect(Collectors.joining("+\",\"+"))
						: "\"\""))
				.build();
	}

	private MethodSpec generateEquals(List<Pair<String, TypeName>> fields,
			String customTypeName) {
		String MP_OBJ = "obj";
		String MP_OTHER = "other";
		return MethodSpec.methodBuilder("equals").returns(TypeName.BOOLEAN)
				.addModifiers(Modifier.PUBLIC)
				.addParameter(TypeName.OBJECT, MP_OBJ)
				.addStatement("if(this == $L) return true", MP_OBJ)
				.addStatement("if($L == null) return false", MP_OBJ)
				.addStatement("if(getClass() != $L.getClass()) return false",
						MP_OBJ)
				.addStatement("$T other = ($T)$L", getClassName(customTypeName),
						getClassName(customTypeName), MP_OBJ)
				.addStatement((fields != null && fields.size() > 0)
						? fields.stream().map(f -> {
							if (f.getValue1().isPrimitive()) {
								return CodeBlock.builder()
										.addStatement("return if($L != $L.$L )",
												f.getValue0(), MP_OTHER,
												f.getValue0())
										.build().toString();
							} else
								return CodeBlock.builder()
										.beginControlFlow("if($L == null)",
												f.getValue0())
										.addStatement(
												"if($L.$L == null) return false",
												MP_OTHER, f.getValue0())
										.endControlFlow()
										.addStatement(
												"else if(!$L.equals($L.$L)) return false",
												f.getValue0(), MP_OTHER,
												f.getValue0())
										.build().toString();
						}).collect(Collectors.joining(" "))
						: "\"\"")
				.addStatement("return true").build();
	}

	private List<MethodSpec> generateCustomTypeGetters(
			List<Pair<String, TypeName>> fields) {
		return fields.stream()
				.map(field -> MethodSpec
						.methodBuilder("get"
								+ field.getValue0().substring(0, 1)
										.toUpperCase()
								+ field.getValue0().substring(1))
						.addModifiers(Modifier.PUBLIC)
						.returns(field.getValue1())
						.addStatement("return this.$L", field.getValue0())
						.build())
				.collect(Collectors.toList());
	}

	private List<MethodSpec> generateCustomTypeSetters(
			List<Pair<String, TypeName>> fields, String customTypeName) {

		if (PREDEFINED_TYPES.get(customTypeName.toLowerCase()) != null) {
			return PREDEFINED_TYPES.get(customTypeName.toLowerCase())
					.getValue3();
		}

		return fields.stream()
				.map(field -> MethodSpec
						.methodBuilder("set"
								+ field.getValue0().substring(0, 1)
										.toUpperCase()
								+ field.getValue0().substring(1))
						.addModifiers(Modifier.PUBLIC)
						.addStatement("this.$L = $L", field.getValue0(),
								field.getValue0())
						.addParameter(field.getValue1(), field.getValue0())
						.build())
				.collect(Collectors.toList());
	}

	private List<FieldSpec> buildFields(List<Pair<String, TypeName>> fields) {
		return fields.stream()
				.map(field -> FieldSpec.builder(field.getValue1(),
						field.getValue0(), Modifier.PRIVATE).build())
				.collect(Collectors.toList());
	}

	private ClassName getClassName(String customTypeName) {
		return ClassName.get("", customTypeName);
	}

	private ParameterSpec getParameterSpec(String customTypeName) {
		return ParameterSpec
				.builder(this.getClassName(customTypeName), MP_PARAM).build();
	}

	// class definition for default types
	// contains code for <param encoding, returnValue mapping, constructor,
	// setters>
	private static final CodeBlock ADDRESS_CHECK_CODEBLOCK = CodeBlock.builder()
			.beginControlFlow("if(!$T.isAddressValid($L))", EncodingUtils.class,
					ADDRESS_TYPE)
			.addStatement("throw new $T($T.format($S,$L))",
					InvalidParameterException.class, String.class,
					"Given address %s is not of type aeternity public key",
					ADDRESS_TYPE)
			.endControlFlow()
			.addStatement("this.$L=$L", ADDRESS_TYPE, ADDRESS_TYPE).build();

	public static final Map<String, Quartet<CodeBlock, CodeBlock, MethodSpec, List<MethodSpec>>> PREDEFINED_TYPES = ImmutableMap
			.of(ADDRESS_TYPE, Quartet.with(
					CodeBlock.builder()
							.add("$L.get" + CodegenUtil
									.getUppercaseClassName(ADDRESS_TYPE) + "()",
									MP_PARAM)
							.build(),
					CodeBlock.builder()
							.add("new $T($L.toString())",
									ClassName.get("",
											CodegenUtil.getUppercaseClassName(
													ADDRESS_TYPE)),
									MP_PARAM)
							.build(),
					MethodSpec.constructorBuilder()
							.addModifiers(Modifier.PUBLIC)
							.addParameter(ParameterSpec
									.builder(String.class, ADDRESS_TYPE)
									.build())
							.addCode(ADDRESS_CHECK_CODEBLOCK).build(),
					Arrays.asList(MethodSpec.methodBuilder("setAddress")
							.addModifiers(Modifier.PUBLIC)
							.addParameter(ParameterSpec
									.builder(String.class, ADDRESS_TYPE)
									.build())
							.addCode(ADDRESS_CHECK_CODEBLOCK).build())));
}
