package com.kryptokrauts.codegen.datatypes.deprecated;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomDatatypeGenerator {

  // @NonNull private CodegenConfiguration config;
  //
  // private Map<String, TypeSpec> alreadyGeneratedDatatypes = new
  // HashMap<>();
  //
  // public void generate(JsonObject datatypeDescription) throws
  // MojoExecutionException {
  // try {
  // String datatypeName =
  // datatypeDescription.getString(config.getAbiJSONTypesNameElement());
  // if (!StringUtils.isEmpty(datatypeName)) {
  //
  // TypeSpec customDatatypeSpec =
  // TypeSpec.classBuilder(datatypeName)
  // .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
  // .addMethod(toStringMethod())
  // .addMethod(setAddressMethod())
  // .addMethod(getAddressMethod())
  // .addMethod(constructor())
  // .addField(String.class, "address", Modifier.PRIVATE)
  // .build();
  //
  // alreadyGeneratedDatatypes.put(datatypeName, customDatatypeSpec);
  //
  // JavaFile javaFile =
  // JavaFile.builder(config.getDatatypePackage(),
  // customDatatypeSpec).build();
  //
  // Path path = Paths.get("", config.getTargetPath());
  //
  // javaFile.writeTo(path);
  // } else {
  // throw new MojoExecutionException(
  // String.format(
  // "Datatype description %s has no given datatypeName",
  // datatypeDescription.toString()));
  // }
  // } catch (Exception e) {
  // throw new MojoExecutionException("Error generating custom datatype", e);
  // }
  // }
  //
  // private MethodSpec constructor() {
  // MethodSpec constructor =
  // MethodSpec.constructorBuilder()
  // .addModifiers(Modifier.PUBLIC)
  // .addParameter(
  // ParameterSpec.builder(String.class, addressVariable,
  // Modifier.FINAL).build())
  // .addStatement("this.$L = $L", addressVariable, addressVariable)
  // .build();
  // return constructor;
  // }
  //
  // private MethodSpec getAddressMethod() {
  // MethodSpec method =
  // MethodSpec.methodBuilder("getAddress")
  // .addModifiers(Modifier.PUBLIC)
  // .returns(String.class)
  // .addStatement("return $L", addressVariable)
  // .build();
  // return method;
  // }
  //
  // private MethodSpec setAddressMethod() {
  // MethodSpec method =
  // MethodSpec.methodBuilder("setAddress")
  // .addModifiers(Modifier.PUBLIC)
  // .addParameter(
  // ParameterSpec.builder(String.class, addressVariable,
  // Modifier.FINAL).build())
  // .beginControlFlow(
  // "if($L == null || !$L.startsWith($T.ACCOUNT_PUBKEY))",
  // "address",
  // "address",
  // ApiIdentifiers.class)
  // .addStatement(
  // "throw new $T($T.format(\"Given address %s is not of type aeternity
  // public key\",$L))",
  // InvalidParameterException.class, String.class, addressVariable)
  // .endControlFlow()
  // .addStatement("this.$L = $L", addressVariable, addressVariable)
  // .build();
  //
  // return method;
  // }
  //
  // private static MethodSpec toStringMethod() {
  // MethodSpec method =
  // MethodSpec.methodBuilder("toString")
  // .addModifiers(Modifier.PUBLIC)
  // .addAnnotation(Override.class)
  // .returns(String.class)
  // .addStatement("return $L", addressVariable)
  // .build();
  // return method;
  // }
}
