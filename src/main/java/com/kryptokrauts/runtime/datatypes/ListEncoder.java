package com.kryptokrauts.runtime.datatypes;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.javatuples.Pair;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ListEncoder extends AbstractDatatypeEncoder {

	public ListEncoder(DatatypeEncodingHandler resolveInstance) {
		super(resolveInstance);
	}

	public boolean applies(TypeName type) {
		if (type instanceof ParameterizedTypeName) {
			return ((ParameterizedTypeName) type).rawType.simpleName()
					.equals(List.class.getSimpleName());
		}
		return TypeName.get(List.class).equals(type);
	}

	@Override
	public boolean applies(Object type) {
		return parseToJsonArray(type) != null;
	}

	public CodeBlock encodeValue(TypeName type, String variableName) {
		return generateListCodeblock(type, variableName, t -> {
			return this.resolveInstance.encodeParameter(t.getValue0(),
					t.getValue1());
		}).toBuilder()
				.add(").collect($T.toList()).toString()", Collectors.class)
				.build();
	}

	public CodeBlock mapToReturnValue(TypeName type, String variableName) {
		return generateListCodeblock(type, variableName, t -> {
			return this.resolveInstance.mapToReturnValue(t.getValue0(),
					t.getValue1());
		}).toBuilder().add(").collect($T.toList())", Collectors.class).build();
	}

	/**
	 * iterate over list an recursively resolve inner type
	 * 
	 * @param type
	 * @param variableName
	 * @param functionToCall
	 * @return
	 */
	private CodeBlock generateListCodeblock(TypeName type, String variableName,
			Function<Pair<TypeName, String>, CodeBlock> functionToCall) {
		String uniqueVariableName = this.getUniqueVariableName(variableName);
		TypeName innerType = ((ParameterizedTypeName) type).typeArguments
				.get(0);
		return CodeBlock.builder()
				.add("(($T)$L).stream().map($L->", listWildCard, variableName,
						uniqueVariableName)
				.add(functionToCall
						.apply(Pair.with(innerType, uniqueVariableName)))
				.build();
	}

	private TypeName listWildCard = ParameterizedTypeName.get(
			ClassName.get(List.class),
			WildcardTypeName.subtypeOf(Object.class));

	@Override
	public TypeName getTypeName(Object type) {
		return ParameterizedTypeName.get(ClassName.get(List.class),
				this.resolveInstance
						.getTypeName(parseToJsonArray(type).getValue(0)));
	}

	private JsonArray parseToJsonArray(Object type) {
		if (type != null) {
			if (type instanceof JsonObject) {
				Object list = JsonObject.mapFrom(type).getValue("list");
				if (list instanceof JsonArray) {
					return (JsonArray) list;
				}
			}
		}
		throw new RuntimeException(getUnsupportedMappingException(type,
				ListEncoder.class.getName(), "parseToJsonArray",
				"expected list definition but got " + type));
	}
}
