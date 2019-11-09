package com.kryptokrauts.codegen.datatypes;

import java.lang.reflect.Type;

import com.squareup.javapoet.CodeBlock;

public class StringMapper extends AbstractSophiaTypeMapper {

	@Override
	public CodeBlock getReturnStatement(String resultToReturn) {
		return CodeBlock.builder().addStatement("return $S", resultToReturn)
				.build();
	}

	@Override
	public Type getJavaType() {
		return String.class;
	}

	@Override
	public boolean applies(Object type) {
		return "string".equalsIgnoreCase(getType(type));
	}

}
