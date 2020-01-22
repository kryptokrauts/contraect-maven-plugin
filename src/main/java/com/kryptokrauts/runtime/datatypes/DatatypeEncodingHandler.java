package com.kryptokrauts.runtime.datatypes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

public class DatatypeEncodingHandler {

	protected String generatedTypesPackageName;

	protected String generatedContractClassName;

	private List<DatatypeEncoder> datatypeHandlers;

	public DatatypeEncodingHandler(String generatedTypesPackageName,
			String generatedContractClassName) {
		this.datatypeHandlers = Arrays.asList(new StringEncoder(this),
				new IntEncoder(this), new ListEncoder(this),
				new AddressEncoder(this), new CustomTypeEncoder(this));
		this.generatedTypesPackageName = generatedTypesPackageName;
		this.generatedContractClassName = generatedContractClassName;
	}

	public CodeBlock encodeParameter(TypeName type, String variableName) {
		return getHandler(type).encodeValue(type, variableName);
	}

	public CodeBlock mapToReturnValue(TypeName type, String variableName) {
		return getHandler(type).mapToReturnValue(type, variableName);
	}

	public TypeName getTypeName(Object type) {
		return getHandler(type).getTypeName(type);
	}

	private DatatypeEncoder getHandler(TypeName type) {
		return datatypeHandlers.stream().filter(t -> t.applies(type))
				.findFirst().orElse(new DefaultDatatypeEncoder(this));
	}

	private DatatypeEncoder getHandler(Object type) {
		return datatypeHandlers.stream().filter(t -> t.applies(type))
				.findFirst().orElse(new DefaultDatatypeEncoder(this));
	}

	protected ClassInfo getCustomDatatype(String datatypeName) {
		return getCustomDatatypes().get(datatypeName);
	}

	protected Map<String, ClassInfo> getCustomDatatypes() {
		try {
			ImmutableSet<ClassInfo> cls = ClassPath
					.from(Thread.currentThread().getContextClassLoader())
					.getTopLevelClasses();
			return cls.stream()
					.filter(c -> c.getPackageName()
							.startsWith(generatedTypesPackageName))
					.collect(Collectors.toMap(c -> c.getSimpleName(), c -> c));
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<>();
		}
	}
}
