package com.kryptokrauts.runtime.datatypes;

import com.kryptokrauts.codegen.CodegenUtil;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import io.vertx.core.json.JsonObject;

public class CustomTypeEncoder extends AbstractDatatypeEncoder {

	public CustomTypeEncoder(DatatypeEncodingHandler resolveInstance) {
		super(resolveInstance);
	}

	public boolean applies(TypeName type) {
		return type.equals(getTypeName(type.toString()));
	}

	@Override
	public boolean applies(Object type) {
		return getCustomTypeName(type) != null;
	}

	public CodeBlock encodeValue(TypeName type, String variableName) {
		return CodeBlock.builder().add("$T.encodeValue($L)", type, variableName)
				.build();
	}

	@Override
	public CodeBlock mapToReturnValue(TypeName type, String variableName) {
		return CodeBlock.builder().add("$T.mapFrom($L).mapTo($T.class)",
				JsonObject.class, variableName, type).build();
	}

	@Override
	public TypeName getTypeName(Object type) {
		return ClassName.get(this.resolveInstance.generatedContractClassName,
				getCustomTypeName(type));
	}

	private String getCustomTypeName(Object type) {
		if (type != null && type.toString().toLowerCase()
				.startsWith(this.resolveInstance.generatedContractClassName
						.toLowerCase())) {
			return CodegenUtil.getUppercaseClassName(type.toString()
					.substring(type.toString().indexOf(".") + 1));
		}
		return null;
	}
}
