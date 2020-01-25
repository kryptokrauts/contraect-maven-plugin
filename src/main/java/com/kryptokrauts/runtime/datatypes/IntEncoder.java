package com.kryptokrauts.runtime.datatypes;

import java.math.BigInteger;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

public class IntEncoder extends AbstractDatatypeEncoder {

	public IntEncoder(DatatypeEncodingHandler resolveInstance) {
		super(resolveInstance);
	}

	public boolean applies(TypeName type) {
		return TypeName.get(BigInteger.class).equals(type);
	}

	@Override
	public boolean applies(Object type) {
		return "int".equals(type);
	}

	@Override
	public CodeBlock mapToReturnValue(TypeName type, String variableName) {
		return CodeBlock.builder()
				.add("new $T($L.toString())", BigInteger.class, variableName)
				.build();
	}

	@Override
	public TypeName getTypeName(Object type) {
		return TypeName.get(BigInteger.class);
	}
}
