package com.kryptokrauts.codegen.datatypes;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

public class CustomDatatypeMapper extends AbstractSophiaTypeMapper {

	private List<ClassInfo> customClasses;

	/** @TODO - extract package for genereated datatypes with config value */
	public CustomDatatypeMapper(TypeResolverRefactored typeResolverInstance) {
		super(typeResolverInstance);
		customClasses = getCustomDatatypes().values().stream()
				.collect(Collectors.toList());
	}

	public static Map<String, ClassInfo> getCustomDatatypes() {
		try {
			ImmutableSet<ClassInfo> cls = ClassPath
					.from(Thread.currentThread().getContextClassLoader())
					.getTopLevelClasses();
			return cls.stream()
					.filter(c -> c.getPackageName().startsWith(
							"com.kryptokrauts.contraect.generated.datatypes"))
					.collect(Collectors.toMap(c -> c.getSimpleName(), c -> c));
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<>();
		}
	}

	@Override
	public CodeBlock getReturnStatement(Object resultToReturn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type getJavaType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeName getReturnType(Object valueTypeString) {
		if (getAppliedClass(valueTypeString).isPresent()) {
			return TypeName.get(getAppliedClass(valueTypeString).get().load());
		}
		throw new RuntimeException(
				"Type definition applied for custom type but no class can be loaded - this seems to be an implementation bug, please create a ticket in github along with your contract code, the string CustomDatatypeMapper.getReturnType(Object) and this string: "
						+ valueTypeString);
	}

	@Override
	public boolean applies(Object type) {
		return getAppliedClass(type).isPresent();
	}

	/**
	 * iterate over loaded classes and check which might apply as type
	 *
	 * @param type
	 * @return
	 */
	private Optional<ClassInfo> getAppliedClass(Object type) {
		return customClasses.stream().filter(
				c -> type.toString().equalsIgnoreCase(c.getSimpleName()))
				.findFirst();
	}
}
