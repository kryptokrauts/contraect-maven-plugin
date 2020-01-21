package com.kryptokrauts.codegen.datatypes;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

@Deprecated
public abstract class BaseMapper {

	protected static MethodSpec M_VALUE_TO_STRING = MethodSpec
			.methodBuilder("valueToString").addModifiers(Modifier.PRIVATE)
			.addParameter(Object.class, TypeMapperInterface.VAR_TYPE_STR)
			.returns(String.class)
			.addCode(
					CodeBlock.builder()
							.beginControlFlow("if($L == null)",
									TypeMapperInterface.VAR_TYPE_STR)
							.addStatement("return \"\"").endControlFlow()
							.addStatement("return $L.toString()",
									TypeMapperInterface.VAR_TYPE_STR)
							.build())
			.build();

	public abstract String getMapperTypeName();

	public abstract TypeName getReturnType();

	public abstract CodeBlock appliesCodeblock();

	public abstract CodeBlock getTypeCodeblock();

	public abstract CodeBlock decodeResultCodeblock();
}
