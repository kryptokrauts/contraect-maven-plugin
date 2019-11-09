package com.kryptokrauts.codegen.datatypes;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import com.squareup.javapoet.CodeBlock;

import io.vertx.core.json.JsonObject;

/**
 * @TODO support Lists, Maps, Tuples, Nesting using a recursive approach
 * @author mitch
 */
public class ListMapper extends AbstractSophiaTypeMapper {

	@Override
	public CodeBlock getReturnStatement(String resultToReturn) {
		// @TODO split result into list
		return CodeBlock.builder()
				.addStatement("return $T.of($L)", Arrays.class, resultToReturn)
				.build();
	}

	@Override
	public Type getJavaType() {
		return List.class;
	}

	@Override
	public boolean applies(Object type) {
		try {
			return JsonObject.mapFrom(type).containsKey("list");
		} catch (Exception e) {
			// ignore because if it is not a json we do not need to go on
			// further
		}
		return false;
	}

}
