package com.kryptokrauts.codegen.datatypes;

import java.lang.reflect.Type;

import com.squareup.javapoet.CodeBlock;

public interface SophiaTypeMapper {

	public CodeBlock getReturnStatement(Object resultToReturn);

	public Type getJavaType();

	public boolean applies(Object type);

}
