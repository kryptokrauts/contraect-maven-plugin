package com.kryptokrauts.codegen.datatypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractSophiaTypeMapper implements SophiaTypeMapper {

	protected Logger _logger = LoggerFactory.getLogger(this.getClass());

	@NonNull
	protected TypeResolverRefactored typeResolverInstance;

	protected String getType(Object type) {
		if (type == null) {
			return "";
		}
		return type.toString();
	}

	// protected String encodeParametersOnCall(T obj) {
	// return obj.toString();
	// }

	/**
	 * necessary
	 *
	 * @return
	 */
	public TypeName getReturnType() {
		return ClassName.get(Object.class);
	}

	public String getUnsupportedMappingException(
			Object unsupportedTypeCaseString) {
		return String.format(
				"This is an unforseen contract type definition case - cannot map java type to given sophia type. Please create an issue on https://github.com/kryptokrauts/contraect-maven-plugin along with your contract code and this string: %s",
				unsupportedTypeCaseString);
	}
}
