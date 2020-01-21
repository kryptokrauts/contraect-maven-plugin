package com.kryptokrauts.runtime.datatypes;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

public interface DatatypeEncoder {

	public boolean applies(TypeName type);

	/**
	 * @TEST
	 * @param type
	 * @return
	 */
	public boolean applies(Object type);

	public CodeBlock encodeValue(TypeName type, String variableName);

	public CodeBlock mapToReturnValue(TypeName type, String variableName);

	/**
	 * @TEST
	 * @param type
	 * @return
	 */
	public TypeName getTypeName(Object type);
}
