package com.kryptokrauts.codegen.datatypes;

import java.lang.reflect.Type;

import com.squareup.javapoet.CodeBlock;

public class DefaultMapper extends AbstractSophiaTypeMapper {

	@Override
	public CodeBlock getReturnStatement(Object resultToReturn) {
		return CodeBlock.builder()
				.addStatement("return ($T)$L", Object.class, resultToReturn)
				.build();
	}

	@Override
	public Type getJavaType() {
		return Object.class;
	}

	@Override
	public boolean applies(Object type) {
		return false;
	}

}
