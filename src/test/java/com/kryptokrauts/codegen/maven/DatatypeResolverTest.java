package com.kryptokrauts.codegen.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.javatuples.Pair;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kryptokrauts.codegen.ContraectGenerator;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

public class DatatypeResolverTest extends BaseTest {

	private String datatypeTestClassName = "DatatypeTest";

	@Test
	public void testTypeMappings() throws MojoExecutionException {
		ContraectGenerator generator = new ContraectGenerator(config);
		generator.generate(new File("src/test/resources/contraects/"
				+ datatypeTestClassName + ".aes").getAbsolutePath());

		Map<String, Object> typeResolvingMap = new HashMap<String, Object>();
		// simple types
		typeResolvingMap.put("testInt", TypeName.get(BigInteger.class));
		typeResolvingMap.put("testString", TypeName.get(String.class));
		typeResolvingMap.put("testBool", TypeName.get(Boolean.class));
		// complex types
		typeResolvingMap.put("testStringList",
				ParameterizedTypeName.get(List.class, String.class));
		typeResolvingMap.put("testTuple", ParameterizedTypeName.get(Pair.class,
				BigInteger.class, Boolean.class));
		// custom types
		typeResolvingMap.put("testAddress",
				targetPackage + "." + datatypeTestClassName + "$Address");
		// typeNames.put(
		// listOfPairWithAddress,
		// ParameterizedTypeName.get(
		// ClassName.get(List.class),
		// ParameterizedTypeName.get(
		// Pair.class,
		// CustomDatatypeMapper.getCustomDatatypes().get("Address").load(),
		// BigInteger.class)));
		// typeResolvingMap.put(mapType, ParameterizedTypeName.get(Map.class,
		// BigInteger.class, Boolean.class));
		// typeResolvingMap.put(listMapType,
		// ParameterizedTypeName.get(ClassName.get(List.class),
		// ParameterizedTypeName.get(Map.class, BigInteger.class,
		// Boolean.class)));
		// typeResolvingMap.put(listTupleTriplet,
		// ParameterizedTypeName.get(ClassName.get(List.class),
		// ParameterizedTypeName.get(Triplet.class,
		// BigInteger.class, BigInteger.class,
		// String.class)));
		// typeResolvingMap.put(listListMapType,
		// ParameterizedTypeName.get(ClassName.get(List.class),
		// ParameterizedTypeName.get(ClassName.get(List.class),
		// ParameterizedTypeName.get(Map.class,
		// BigInteger.class, Boolean.class))));

		// test the mapping
		typeResolvingMap.forEach((functionName, type) -> {
			try {
				System.out.println(
						"Testing mapping for function " + functionName);
				assertEquals(type.toString(), getReturnTypeName(functionName));
			} catch (Exception e) {
				fail("Failed testing type mapping for function " + functionName,
						e);
			}
		});
	}

	// public void testUnsupportedTypeMappings() throws Exception {
	// Assertions.assertThrows(RuntimeException.class, () -> {
	// typeResolver.getReturnType(getTypeDefinitionString(tupleEleven));
	// });
	// }

	private String getReturnTypeName(String functionName) throws Exception {
		for (Method method : Class
				.forName(targetPackage + "." + datatypeTestClassName)
				.getDeclaredMethods()) {
			if (method.getName().equals(functionName)) {
				return method.getGenericReturnType().getTypeName();
			}
		}
		return null;
	}

	private Object getTypeDefinitionString(String typeDefinition) {
		ObjectMapper mapper = new ObjectMapper();
		Map<?, ?> parsed;
		try {
			parsed = mapper.readValue(typeDefinition, Map.class);
			return parsed.get("type");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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

	private static String tupleEleven = "{\"type\": {\r\n"
			+ "					\"tuple\": [\r\n"
			+ "						\"int\",\r\n"
			+ "						\"int\",\r\n"
			+ "						\"int\",\r\n"
			+ "						\"int\",\r\n"
			+ "						\"int\",\r\n"
			+ "						\"int\",\r\n"
			+ "						\"int\",\r\n"
			+ "						\"int\",\r\n"
			+ "						\"int\",\r\n"
			+ "						\"int\",\r\n"
			+ "						\"string\"\r\n" + "					]\r\n"
			+ "				}}";
}
