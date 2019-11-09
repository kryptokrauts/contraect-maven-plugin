package com.kryptokrauts.codegen.datatypes;

import java.lang.reflect.Type;

import com.squareup.javapoet.CodeBlock;

public class BytesMapper extends AbstractSophiaTypeMapper {

	@Override
	public CodeBlock getReturnStatement(String resultToReturn) {
		// @TODO split result into list
		return CodeBlock.builder()
				.addStatement("return new $T($L)", Byte[].class, resultToReturn)
				.build();
	}

	@Override
	public Type getJavaType() {
		return Byte[].class;
	}

	@Override
	public boolean applies(Object type) {
		return getType(type).startsWith("bytes");
	}

}
