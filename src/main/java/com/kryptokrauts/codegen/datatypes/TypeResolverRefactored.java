package com.kryptokrauts.codegen.datatypes;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

/**
 * @TODO Allgemeine Methode für Fehlermeldung generieren mit "Darf nicht
 *       auftreten - Ticket stellen" -> Codestelle (Klasse, Methode), COntract
 *       code, Übergabeparameter
 * @author mitch
 */
public class TypeResolverRefactored {

	private List<SophiaTypeMapper> typeMapperList;

	private SophiaTypeMapper defaultTypeMapper;

	private void resolveType(Map parsedType) {
		MethodSpec loadListInteger = MethodSpec.methodBuilder("loadListInteger")
				.returns(ParameterizedTypeName.get(List.class, Integer.class))
				.addParameter(
						ParameterizedTypeName.get(List.class, Integer.class),
						"list")
				.addParameter(ParameterizedTypeName.get(
						ClassName.get(List.class),
						ParameterizedTypeName.get(List.class, Integer.class)),
						"t1")
				.build();
	}

	/*
	 * custom types müssen einen eigenen mapper definieren, der hier in die
	 * liste aufgenommen werden kann
	 */
	public TypeResolverRefactored() {
		this.typeMapperList = Arrays.asList(new IntMapper(this),
				new ListMapper(this), new MapMapper(this),
				new StringMapper(this), new BoolMapper(this),
				new CustomDatatypeMapper(this));
		this.defaultTypeMapper = new DefaultMapper(this);
	}

	/**
	 * resolve typeMapper to use for given sophia datatype string
	 * 
	 * @param typeString
	 *            with definition of sophia datatype
	 * @return typeMapper
	 */
	public SophiaTypeMapper getTypeMapper(Object typeString) {
		return typeMapperList.stream().filter(t -> t.applies(typeString))
				.findFirst().orElse(defaultTypeMapper);
	}

	/**
	 * maps aeternity type to java type
	 *
	 * @param classType
	 * @return
	 */
	private Type mapClass(Object classType) {
		return typeMapperList.stream().filter(t -> t.applies(classType))
				.findFirst().orElse(defaultTypeMapper).getJavaType();
	}

	/**
	 * resolve typename for given sophia datatype string
	 * 
	 * @param typeDefinition
	 * @return
	 */
	public TypeName getReturnType(Object typeString) {
		return getTypeMapper(typeString).getReturnType(typeString);
	}
}
