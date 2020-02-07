package com.kryptokrauts.codegen.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.kryptokrauts.aeternity.sdk.service.aeternity.AeternityServiceConfiguration;
import com.kryptokrauts.codegen.ContraectGenerator;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import java.io.File;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.javatuples.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DatatypeResolverTest extends BaseTest {

  private static String datatypeTestClassName = "SophiaTypes";

  private static Object datatypeTestContractInstance;

  @BeforeAll
  public static void setup() throws Exception {
    ContraectGenerator generator = new ContraectGenerator(config);
    generator.generate(
        new File("src/test/resources/contraects/" + datatypeTestClassName + ".aes")
            .getAbsolutePath());
    datatypeTestContractInstance =
        Class.forName(targetPackage + "." + datatypeTestClassName)
            .getConstructor(AeternityServiceConfiguration.class, String.class)
            .newInstance(aeternityServiceConfig, null);
    datatypeTestContractInstance
        .getClass()
        .getDeclaredMethod("deploy")
        .invoke(datatypeTestContractInstance);
  }

  @Test
  public void testTypeMappings() {

    Map<String, Object> typeResolvingMap = new HashMap<String, Object>();
    // simple types
    typeResolvingMap.put("testInt", TypeName.get(BigInteger.class));
    typeResolvingMap.put("testString", TypeName.get(String.class));
    typeResolvingMap.put("testBool", TypeName.get(Boolean.class));
    // complex types
    typeResolvingMap.put("testListString", ParameterizedTypeName.get(List.class, String.class));
    typeResolvingMap.put(
        "testTuple", ParameterizedTypeName.get(Pair.class, BigInteger.class, Boolean.class));
    // custom types
    typeResolvingMap.put("testAddress", targetPackage + "." + datatypeTestClassName + "$Address");
    typeResolvingMap.put(
        "testCompanyAddress", targetPackage + "." + datatypeTestClassName + "$CompanyAddress");
    typeResolvingMap.put("testEmployee", targetPackage + "." + datatypeTestClassName + "$Employee");
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
    typeResolvingMap.forEach(
        (functionName, type) -> {
          try {
            System.out.println("Testing mapping for function " + functionName);
            assertEquals(type.toString(), getReturnTypeName(functionName));
          } catch (Exception e) {
            fail("Failed testing type mapping for function " + functionName, e);
          }
        });
  }

  /** this makes an end to end test for the test contract - given value should be returned */
  @Test
  public void testInputOutput() throws Exception {
    Map<String, Object> ioTestMap = new HashMap<String, Object>();
    // simple types
    ioTestMap.put("testInt", BigInteger.valueOf(42));
    ioTestMap.put("testString", "aeternity");
    ioTestMap.put("testBool", Boolean.TRUE);
    // complex types
    ioTestMap.put("testListString", Arrays.asList("one", "of", "four", "strings"));
    ioTestMap.put("testTuple", new Pair<BigInteger, Boolean>(BigInteger.valueOf(42), true));
    ioTestMap.put("testOption", Optional.of(BigInteger.valueOf(42)));
    ioTestMap.put(
        "testOptionList",
        Optional.of(Arrays.asList(BigInteger.valueOf(42), BigInteger.valueOf(200))));
    // custom types
    // @todo equals method in generated types
    // ioTestMap.put("testAddress", getAddressInstance(
    // "ak_2gx9MEFxKvY9vMG5YnqnXWv1hCsX7rgnfvBLJS4aQurustR1rt"));

    // test the mapping
    ioTestMap.forEach(
        (functionName, args) -> {
          try {
            System.out.println("Testing input/output for function " + functionName);
            assertEquals(args, getMethod(functionName).invoke(datatypeTestContractInstance, args));
          } catch (Exception e) {
            fail("Failed testing type mapping for function " + functionName, e);
          }
        });
  }

  private String getReturnTypeName(String functionName) throws Exception {
    Method m = getMethod(functionName);
    if (m != null) {
      return m.getGenericReturnType().getTypeName();
    }
    return null;
  }

  private Method getMethod(String functionName) throws Exception {
    for (Method method :
        Class.forName(targetPackage + "." + datatypeTestClassName).getDeclaredMethods()) {
      if (method.getName().equals(functionName)) {
        return method;
      }
    }
    return null;
  }

  private Object getAddressInstance(String address) throws Exception {
    return Class.forName(targetPackage + "." + datatypeTestClassName + "$Address")
        .getConstructor(String.class)
        .newInstance(address);
  }
}
