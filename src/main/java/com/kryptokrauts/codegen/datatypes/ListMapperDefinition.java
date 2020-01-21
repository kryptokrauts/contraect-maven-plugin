package com.kryptokrauts.codegen.datatypes;

import java.util.List;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
@Deprecated
public class ListMapperDefinition extends BaseMapper {

	@Override
	public String getMapperTypeName() {
		return "ListMapper";
	}

	@Override
	public TypeName getReturnType() {
		return TypeName.get(String.class);
	}

	@Override
	public CodeBlock appliesCodeblock() {
		return CodeBlock.builder()
				.addStatement("return $S.equalsIgnoreCase($N($L))", "list",
						M_VALUE_TO_STRING, TypeMapperInterface.VAR_TYPE_STR)
				.build();
	}

	@Override
	public CodeBlock getTypeCodeblock() {
		return CodeBlock.builder().addStatement("return $T.get($T.class)",
				TypeName.class, List.class).build();
	}

	@Override
	public CodeBlock decodeResultCodeblock() {
		return CodeBlock.builder()
				.add("$L.toString()", TypeMapperInterface.VAR_TYPE_STR).build();
	}

	// return "string".equalsIgnoreCase(valueToString(typeString))
	// || TypeName.get(String.class).equals(typeString);

}
