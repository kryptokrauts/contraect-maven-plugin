package com.kryptokrauts.runtime.datatypes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class MapEncoder extends AbstractDatatypeEncoder {

	private static final String JSON_MAP_IDENTIFIER = "map";
	private static final String VAR_KEY = "k";
	private static final String VAR_VALUE = "v";
	private static final int KEY_POS = 0;
	private static final int VALUE_POS = 1;

	public MapEncoder(DatatypeEncodingHandler resolveInstance) {
		super(resolveInstance);
	}

	public boolean applies(TypeName type) {
		if (type instanceof ParameterizedTypeName) {
			return ((ParameterizedTypeName) type).rawType.simpleName()
					.equals(Map.class.getSimpleName());
		}
		return TypeName.get(Map.class).equals(type);
	}

	@Override
	public boolean applies(Object type) {
		return parseToJsonArray(type) != null;
	}

	public CodeBlock encodeValue(TypeName type, String variableName) {

		return CodeBlock.builder().add("\"{\"+")
				.add("$L.keySet().stream()", variableName)
				.add(".map($L -> \"[\"+$L+\"] = \"+$L)", VAR_KEY,
						this.resolveInstance.encodeParameter(
								getInnerType(type, KEY_POS), VAR_KEY),
						this.resolveInstance.encodeParameter(
								getInnerType(type, VALUE_POS),
								CodeBlock.builder()
										.add("$L.get($L)", variableName,
												VAR_KEY)
										.build().toString()))
				.add(".collect($T.joining(\",\"))", Collectors.class)
				.add("+\"}\"").build();
	}

	public CodeBlock mapToReturnValue(TypeName type, String variableName) {

		return CodeBlock.builder()
				.add("(($T)$L).stream().collect($T.toMap(", listWildCard,
						variableName, Collectors.class)
				.add("$L->$L", VAR_KEY,
						this.resolveInstance.mapToReturnValue(
								getInnerType(type, KEY_POS),
								getMapListReturnValueField(KEY_POS, VAR_KEY)))
				.add(",$L->$L", VAR_VALUE,
						this.resolveInstance.mapToReturnValue(
								getInnerType(type, VALUE_POS),
								getMapListReturnValueField(VALUE_POS,
										VAR_VALUE)))
				.add("))").build();
	}

	private String getMapListReturnValueField(int pos, String variableName) {
		return CodeBlock.builder()
				.add("(($T)$L).get(" + pos + ")", listWildCard, variableName)
				.build().toString();
	}

	@Override
	public TypeName getTypeName(Object type) {
		return ParameterizedTypeName.get(ClassName.get(Map.class),
				this.resolveInstance
						.getTypeName(parseToJsonArray(type).getValue(0)),
				this.resolveInstance
						.getTypeName(parseToJsonArray(type).getValue(1)));
	}

	private JsonArray parseToJsonArray(Object type) {
		if (type != null) {
			if (type instanceof JsonObject) {
				Object list = JsonObject.mapFrom(type)
						.getValue(JSON_MAP_IDENTIFIER);
				if (list instanceof JsonArray) {
					JsonArray mapArray = (JsonArray) list;
					if (mapArray.size() > 1) {
						return mapArray;
					}
				}
			}
		}
		return null;
		// throw new RuntimeException(getUnsupportedMappingException(type,
		// ListEncoder.class.getName(), "parseToJsonArray",
		// "expected list definition but got " + type));
	}

	private TypeName getInnerType(TypeName type, int pos) {
		return ((ParameterizedTypeName) type).typeArguments.get(pos);
	}

	private TypeName listWildCard = ParameterizedTypeName.get(
			ClassName.get(List.class),
			WildcardTypeName.subtypeOf(Object.class));
}
