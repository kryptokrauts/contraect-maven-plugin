package com.kryptokrauts.codegen.datatypes.deprecated;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import javax.lang.model.element.Modifier;

import org.apache.maven.plugin.MojoExecutionException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kryptokrauts.codegen.CodegenConfiguration;
import com.kryptokrauts.codegen.datatypes.TypeMapperGenerator;
import com.kryptokrauts.codegen.maven.ABIJsonDescription;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class TypeResolverUser {
	private static final String targetPath = "target/generated-sources/contraect";

	private static final String targetPackage = "com.kryptokrauts.contraect.generated";

	public static void main(String[] args) throws MojoExecutionException {
		ABIJsonDescription abiJsonDescription = new ABIJsonDescription();
		CodegenConfiguration config = CodegenConfiguration.builder()
				.targetPath(targetPath).targetPackage(targetPackage)
				.datatypePackage(targetPackage + ".datatypes").numTrials(60)
				.initFunctionName(abiJsonDescription.getInitFunctionName())
				.abiJSONFunctionArgumentElement(
						abiJsonDescription.getAbiJSONFunctionArgumentElement())
				.abiJSONFunctionArgumentNameElement(abiJsonDescription
						.getAbiJSONFunctionArgumentNameElement())
				.abiJSONFunctionArgumentTypeElement(abiJsonDescription
						.getAbiJSONFunctionArgumentTypeElement())
				.abiJSONFunctionsElement(
						abiJsonDescription.getAbiJSONFunctionsElement())
				.abiJSONFunctionsNameElement(
						abiJsonDescription.getAbiJSONFunctionsNameElement())
				.abiJSONFunctionsReturnTypeElement(abiJsonDescription
						.getAbiJSONFunctionsReturnTypeElement())
				.abiJSONFunctionStatefulElement(
						abiJsonDescription.getAbiJSONFunctionStatefulElement())
				.abiJSONNameElement(abiJsonDescription.getAbiJSONNameElement())
				.abiJSONRootElement(abiJsonDescription.getAbiJSONRootElement())
				.build();
		TypeSpec ts = new TypeMapperGenerator(config).generate();

		System.out.println(ts.name);
	}

	public void test() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		Map parsed = mapper.readValue(sophiaTypes, Map.class);
		System.out.println(parsed.get("type"));
		// Map parsed2 = (Map) parsed.get("type");
		// Object key = parsed2.keySet().toArray()[0];
		// Object value = parsed2.get(key);
		// System.out.println("parsed2: " + parsed2);
		// System.out.println("key:" + key);
		// System.out.println("value: " + value);
		// System.out.println(value instanceof List<?>);

		TypeName result = new TypeResolverRefactored()
				.getReturnType(parsed.get("type"));

		MethodSpec testIt = MethodSpec.methodBuilder("testIt").returns(result)
				.build();

		TypeSpec contractTypeSpec = TypeSpec.classBuilder("TestClass")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL).addMethod(testIt)
				.build();

		JavaFile javaFile = JavaFile
				.builder("com.kryptokrauts.generated.test", contractTypeSpec)
				.build();

		Path path = Paths.get("", "target/generated-sources/test");

		javaFile.writeTo(path);

		// TypeName resolved = getTypeMapper(key).getReturnType(value);
		// System.out.println(resolved);
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

	private static String justInt = "{\"type\": \"int\"}";

	private static String justTuple = "{\"type\": {\r\n"
			+ "					\"tuple\": [\r\n"
			+ "						\"int\",\r\n"
			+ "						\"int\",\r\n"
			+ "						\"string\"\r\n" + "					]\r\n"
			+ "				}}";

	private static String listTuple = "{\"type\": {\r\n"
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

	private static String sophiaTypes = "{\"type\": {\r\n"
			+ "						\"list\": [\r\n"
			+ "							{\r\n"
			+ "								\"tuple\": [\r\n"
			+ "									\"address\",\r\n"
			+ "									\"int\"\r\n"
			+ "								]\r\n"
			+ "							}\r\n" + "						]\r\n"
			+ "					}}";
}
