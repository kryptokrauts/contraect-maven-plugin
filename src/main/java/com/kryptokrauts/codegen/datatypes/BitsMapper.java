package com.kryptokrauts.codegen.datatypes;

import java.lang.reflect.Type;

import com.squareup.javapoet.CodeBlock;

public class BitsMapper extends AbstractSophiaTypeMapper {

	@Override
	public CodeBlock getReturnStatement(Object resultToReturn) {
		return CodeBlock.builder()
				.addStatement("return new $T($L)", Byte.class, resultToReturn)
				.build();
	}

	@Override
	public Type getJavaType() {
		return Byte.class;
	}

	@Override
	public boolean applies(Object type) {
		return "bool".equalsIgnoreCase(getType(type));
	}

}
