package com.kryptokrauts.codegen.datatypes;

import java.lang.reflect.Type;

import com.squareup.javapoet.CodeBlock;

public class BoolMapper extends AbstractSophiaTypeMapper {

	@Override
	public CodeBlock getReturnStatement(String resultToReturn) {
		return CodeBlock.builder().addStatement("return $T.valueOf($S)",
				Boolean.class, resultToReturn).build();
	}

	@Override
	public Type getJavaType() {
		return Boolean.class;
	}

	@Override
	public boolean applies(Object type) {
		return "bool".equalsIgnoreCase(getType(type));
	}

}
