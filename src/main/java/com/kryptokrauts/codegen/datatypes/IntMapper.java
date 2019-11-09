package com.kryptokrauts.codegen.datatypes;

import java.lang.reflect.Type;
import java.math.BigInteger;

import com.squareup.javapoet.CodeBlock;

public class IntMapper extends AbstractSophiaTypeMapper {

	@Override
	public CodeBlock getReturnStatement(String resultToReturn) {
		return CodeBlock.builder().addStatement("return new $T($S)",
				BigInteger.class, resultToReturn).build();
	}

	@Override
	public Type getJavaType() {
		return BigInteger.class;
	}

	@Override
	public boolean applies(Object type) {
		return "int".equalsIgnoreCase(getType(type));
	}

}
