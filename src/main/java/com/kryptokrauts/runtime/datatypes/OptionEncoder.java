package com.kryptokrauts.runtime.datatypes;

import java.util.Optional;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * option is mapped to {@link Optional}
 * 
 * @author mitch
 */
public class OptionEncoder extends AbstractDatatypeEncoder {

	public static final String HAS_VALUE_STRING = "\"Some(\"+$L+\")\"";

	public static final String HAS_NO_VALUE = "\"None\"";

	private static final String OPTION_JSON_IDENTIFIER = "option";

	public OptionEncoder(DatatypeEncodingHandler resolveInstance) {
		super(resolveInstance);
	}

	public boolean applies(TypeName type) {
		return type instanceof ParameterizedTypeName
				&& ((ParameterizedTypeName) type).rawType
						.equals(ClassName.get(Optional.class));
	}

	@Override
	public boolean applies(Object type) {
		return getOptionInnerType(type) != null;
	}

	public CodeBlock encodeValue(TypeName type, String variableName) {
		return CodeBlock.builder().add(
				"$L.isPresent()?" + HAS_VALUE_STRING + ":" + HAS_NO_VALUE,
				variableName,
				this.resolveInstance.encodeParameter(
						this.getOptionInnerType(type), variableName + ".get()"))
				.build();
	}

	@Override
	public CodeBlock mapToReturnValue(TypeName type, String variableName) {
		return CodeBlock.builder().add("$T.of(($T)$L)", Optional.class,
				getOptionInnerType(type), variableName).build();
	}

	@Override
	public TypeName getTypeName(Object type) {
		return ParameterizedTypeName.get(ClassName.get(Optional.class),
				this.resolveInstance.getTypeName(getOptionInnerType(type)));
	}

	private TypeName getOptionInnerType(TypeName type) {
		return ((ParameterizedTypeName) type).typeArguments.get(0);
	}

	private Object getOptionInnerType(Object type) {
		if (type != null) {
			if (type instanceof JsonObject) {
				Object list = JsonObject.mapFrom(type)
						.getValue(OPTION_JSON_IDENTIFIER);
				if (list instanceof JsonArray) {
					JsonArray array = (JsonArray) list;
					if (array.size() > 0) {
						return array.getValue(0);
					}
				}
			}
		}
		return null;
		// throw new RuntimeException(getUnsupportedMappingException(type,
		// ListEncoder.class.getName(), "parseToJsonArray",
		// "expected list definition but got " + type));
	}
}
