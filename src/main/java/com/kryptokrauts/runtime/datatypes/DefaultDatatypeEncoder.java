package com.kryptokrauts.runtime.datatypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

public class DefaultDatatypeEncoder extends AbstractDatatypeEncoder {

	private static Logger log = LoggerFactory
			.getLogger(DefaultDatatypeEncoder.class);

	public DefaultDatatypeEncoder(DatatypeEncodingHandler resolveInstance) {
		super(resolveInstance);
	}

	public boolean applies(TypeName type) {
		return false;
	}

	@Override
	public boolean applies(Object type) {
		return false;
	}

	@Override
	public CodeBlock encodeValue(TypeName type, String variableName) {
		log.warn("Fallback to " + DefaultDatatypeEncoder.class.getSimpleName()
				+ ".encodeValue for type " + type);
		return CodeBlock.builder().add("$L.toString()", variableName).build();
	}
	@Override
	public CodeBlock mapToReturnValue(TypeName type, String variableName) {
		log.warn("Fallback to " + DefaultDatatypeEncoder.class.getSimpleName()
				+ ".mapToReturnValue for type " + type);
		return CodeBlock.builder().add("$L", variableName).build();
	}

	@Override
	public TypeName getTypeName(Object type) {
		return TypeName.get(Object.class);
	}

}
