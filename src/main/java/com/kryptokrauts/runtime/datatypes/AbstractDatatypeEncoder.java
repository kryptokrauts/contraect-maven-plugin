package com.kryptokrauts.runtime.datatypes;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author mitch
 */
@RequiredArgsConstructor
public abstract class AbstractDatatypeEncoder implements DatatypeEncoder {

	@NonNull
	protected DatatypeEncodingHandler resolveInstance;

	protected String getUniqueVariableName(String variableName) {
		return "_" + variableName;
	}

	protected String getUnsupportedMappingException(
			Object unsupportedTypeCaseString, String typeMapperClass,
			String typeMapperClassMethod, String additionalInfo) {
		return String.format(
				"This is an unforseen contract type definition case for mapper %s.%s - cannot map java type to given sophia type. Please create an issue on https://github.com/kryptokrauts/contraect-maven-plugin along with your contract code and the following strings\nTypeString:\n%s"
						+ (additionalInfo != null
								? "\nAdditional information: " + additionalInfo
								: ""),
				typeMapperClass, typeMapperClassMethod,
				unsupportedTypeCaseString);
	}
}
