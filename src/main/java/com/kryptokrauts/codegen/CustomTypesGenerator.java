package com.kryptokrauts.codegen;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import org.javatuples.Pair;

import com.kryptokrauts.runtime.datatypes.DatatypeEncodingHandler;
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

	public static final String M_ENCODE_VALUE = "encodeValue";

	public static final String M_MAP_TO_RETURN_VALUE = "mapToReturnValue";

	@NonNull
	DatatypeEncodingHandler datatypeEncodingHandler;
	/**
	 * @TODO check if record before -> if not, create not type
	 */
	public List<TypeSpec> generateCustomTypes(JsonArray customTypes) {
		return customTypes.stream()
				.map(typeDefinition -> generateCustomType(
						JsonObject.mapFrom(typeDefinition)))
				.collect(Collectors.toList());
	}

	/**
	 * @TODO refactor json names
	 */
	private TypeSpec generateCustomType(JsonObject typeDefinition) {
		List<Pair<String, TypeName>> fields = new LinkedList<>();

		JsonArray record = typeDefinition.getJsonObject("typedef")
				.getJsonArray("record");
		// if record is defined, it is a custom type
		if (record != null) {
			fields = record.stream().map(definition -> {
				JsonObject oneField = JsonObject.mapFrom(definition);
				String name = oneField.getString("name");
				TypeName type = this.datatypeEncodingHandler
						.getTypeName(oneField.getValue("type"));
				return Pair.with(name, type);
			}).collect(Collectors.toList());
		}
		String name = CodegenUtil
				.getUppercaseClassName(typeDefinition.getString("name"));

		return TypeSpec.classBuilder(name).addFields(buildFields(fields))
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.addMethods(generateCustomTypeConstructors(fields))
				.addMethods(generateCustomTypeGetters(fields))
				.addMethods(generateCustomTypeSetters(fields))
				.addMethod(generateEncodeValueMethod(fields, name)).build();
	}

	private MethodSpec generateEncodeValueMethod(
			List<Pair<String, TypeName>> fields, String customTypeName) {
		final String MP_PARAM = "p";
		return MethodSpec.methodBuilder(M_ENCODE_VALUE).addCode("return \"{")
				.addCode(fields.stream().map(f -> f.getValue0() + "=\"+"
						+ this.datatypeEncodingHandler.encodeParameter(
								f.getValue1(), MP_PARAM + "." + f.getValue0())
						+ "+\"").collect(Collectors.joining(",")))
				.addCode("}\";").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.addParameter(ClassName.get("", customTypeName), MP_PARAM)
				.returns(TypeName.get(String.class)).build();
	}

	private MethodSpec generateMapToReturnValueMethod(
			List<Pair<String, TypeName>> fields, String customTypeName) {
		return MethodSpec.methodBuilder(M_MAP_TO_RETURN_VALUE)
				.addModifiers(Modifier.PUBLIC).addCode("").build();
	}

	private List<MethodSpec> generateCustomTypeConstructors(
			List<Pair<String, TypeName>> fields) {
		List<MethodSpec> constructors = new LinkedList<>();
		constructors.add(MethodSpec.constructorBuilder()
				.addParameters(fields.stream()
						.map(v -> ParameterSpec
								.builder(v.getValue1(), v.getValue0()).build())
						.collect(Collectors.toList()))
				.addCode(generateConstructorInitializer(fields))
				.addModifiers(Modifier.PUBLIC).build());
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
			List<Pair<String, TypeName>> fields) {
		return fields.stream()
				.map(field -> MethodSpec
						.methodBuilder("get"
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
}
