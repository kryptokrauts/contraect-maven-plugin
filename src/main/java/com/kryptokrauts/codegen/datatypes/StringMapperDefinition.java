package com.kryptokrauts.codegen.datatypes;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
@Deprecated
public class StringMapperDefinition extends BaseMapper {

	@Override
	public String getMapperTypeName() {
		return "StringMapper";
	}

	@Override
	public TypeName getReturnType() {
		return TypeName.get(String.class);
	}

	@Override
	public CodeBlock appliesCodeblock() {
		return CodeBlock.builder()
				.addStatement("return $S.equalsIgnoreCase($N($L))", "string",
						M_VALUE_TO_STRING, TypeMapperInterface.VAR_TYPE_STR)
				.build();
	}

	@Override
	public CodeBlock getTypeCodeblock() {
		return CodeBlock.builder().addStatement("return $T.get($T.class)",
				TypeName.class, String.class).build();
	}

	@Override
	public CodeBlock decodeResultCodeblock() {
		return CodeBlock.builder()
				.add("$L.toString()", TypeMapperInterface.VAR_TYPE_STR).build();
	}

	// return "string".equalsIgnoreCase(valueToString(typeString))
	// || TypeName.get(String.class).equals(typeString);

}
