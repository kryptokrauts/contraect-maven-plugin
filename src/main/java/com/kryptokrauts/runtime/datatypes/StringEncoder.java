package com.kryptokrauts.runtime.datatypes;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

public class StringEncoder extends AbstractDatatypeEncoder {

	public StringEncoder(DatatypeEncodingHandler resolveInstance) {
		super(resolveInstance);
	}

	public boolean applies(TypeName type) {
		return TypeName.get(String.class).equals(type);
	}

	@Override
	public boolean applies(Object type) {
		return "string".equals(type);
	}

	public CodeBlock encodeValue(TypeName type, String variableName) {
		return CodeBlock.builder().add("$S+$L+$S", "\"", variableName, "\"")
				.build();
	}

	@Override
	public CodeBlock mapToReturnValue(TypeName type, String variableName) {
		return CodeBlock.builder().add("$L.toString()", variableName).build();
	}

	@Override
	public TypeName getTypeName(Object type) {
		return TypeName.get(String.class);
	}
}
