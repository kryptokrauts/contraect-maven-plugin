package com.kryptokrauts.codegen.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kryptokrauts.codegen.datatypes.CustomDatatypeMapper;
import com.kryptokrauts.codegen.datatypes.TypeResolverRefactored;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

public class DatatypeResolverTest {

	@Test
	public void testTypeMappings() {
		Map<String, TypeName> typeNames = new HashMap<String, TypeName>();
		// simple types
		typeNames.put(intType, TypeName.get(BigInteger.class));
		typeNames.put(stringType, TypeName.get(String.class));
		typeNames.put(boolType, TypeName.get(Boolean.class));
		// complex types
		typeNames.put(listIntType,
				ParameterizedTypeName.get(List.class, BigInteger.class));
		typeNames.put(tuplePair, ParameterizedTypeName.get(Pair.class,
				BigInteger.class, String.class));
		typeNames.put(listOfPairWithAddress,
				ParameterizedTypeName.get(ClassName.get(List.class),
						ParameterizedTypeName.get(Pair.class,
								CustomDatatypeMapper.getCustomDatatypes()
										.get("Address").load(),
								BigInteger.class)));
		typeNames.put(mapType, ParameterizedTypeName.get(Map.class,
				BigInteger.class, Boolean.class));
		typeNames.put(listMapType,
				ParameterizedTypeName.get(ClassName.get(List.class),
						ParameterizedTypeName.get(Map.class, BigInteger.class,
								Boolean.class)));
		typeNames.put(listTupleTriplet,
				ParameterizedTypeName.get(ClassName.get(List.class),
						ParameterizedTypeName.get(Triplet.class,
								BigInteger.class, BigInteger.class,
								String.class)));
		typeNames.put(listListMapType,
				ParameterizedTypeName.get(ClassName.get(List.class),
						ParameterizedTypeName.get(ClassName.get(List.class),
								ParameterizedTypeName.get(Map.class,
										BigInteger.class, Boolean.class))));
		// test the mapping
		typeNames.forEach((typeDef, type) -> {
			try {
				assertEquals(type, getTypeName(typeDef));
			} catch (Exception e) {
				fail("Failed testing type mappings", e);
			}
		});
	}

	private TypeName getTypeName(String definition) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		Map<?, ?> parsed = mapper.readValue(definition, Map.class);
		return new TypeResolverRefactored().getReturnType(parsed.get("type"));
	}

	private static String listMapType = "{\"type\": {\r\n"
			+ "					\"list\": [\r\n"
			+ "						{\r\n"
			+ "							\"map\": [\r\n"
			+ "								\"int\",\r\n"
			+ "								\"bool\"\r\n"
			+ "							]\r\n" + "						}\r\n"
			+ "					]\r\n" + "				}}";

	private static String listListMapType = "{\"type\":{\r\n"
			+ "					\"list\": [\r\n"
			+ "						{\r\n"
			+ "							\"list\": [\r\n"
			+ "								{\r\n"
			+ "									\"map\": [\r\n"
			+ "										\"int\",\r\n"
			+ "										\"bool\"\r\n"
			+ "									]\r\n"
			+ "								}\r\n"
			+ "							]\r\n" + "						}\r\n"
			+ "					]\r\n" + "				}}";

	private static String mapType = "{\"type\": {\r\n"
			+ "							\"map\": [\r\n"
			+ "								\"int\",\r\n"
			+ "								\"bool\"\r\n"
			+ "							]\r\n" + "						}}";

	private static String intType = "{\"type\": \"int\"}";

	private static String stringType = "{\"type\": \"string\"}";

	private static String boolType = "{\"type\": \"bool\"}";

	private static String tuplePair = "{\"type\": {\r\n"
			+ "					\"tuple\": [\r\n"
			+ "						\"int\",\r\n"
			+ "						\"string\"\r\n" + "					]\r\n"
			+ "				}}";

	private static String listTupleTriplet = "{\"type\": {\r\n"
			+ "						\"list\": [\r\n"
			+ "						{\r\n"
			+ "							\"tuple\": [\r\n"
			+ "								\"int\",\r\n"
			+ "								\"int\",\r\n"
			+ "								\"string\"\r\n"
			+ "							]\r\n" + "						}\r\n"
			+ "					]" + "					}}";

	private static String listIntType = "{\"type\": {\r\n"
			+ "						\"list\": [\r\n"
			+ "							\"int\"\r\n"
			+ "						]\r\n" + "					}}";

	private static String listOfPairWithAddress = "{\"type\": {\r\n"
			+ "						\"list\": [\r\n"
			+ "							{\r\n"
			+ "								\"tuple\": [\r\n"
			+ "									\"address\",\r\n"
			+ "									\"int\"\r\n"
			+ "								]\r\n"
			+ "							}\r\n" + "						]\r\n"
			+ "					}}";
}
