package com.kryptokrauts.codegen.datatypes.defaults;

import com.kryptokrauts.codegen.maven.ABIJsonConfiguration;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import io.vertx.core.json.JsonObject;
import java.util.LinkedList;
import java.util.List;

public interface CustomType {

  public static final String RECORD = "record";

  public static final String ADDRESS_TYPE = "address";

  public static final String BYTES_TYPE = "bytes";

  public static final String HASH_TYPE = "hash";

  public JsonObject getTypeDefinition(ABIJsonConfiguration abiJsonConfiguration);

  public CodeBlock encodeValueCodeblock(String MP_PARAM);

  public CodeBlock mapToReturnValueCodeblock(String MP_PARAM);

  public MethodSpec constructorMethod();

  public List<MethodSpec> methodList();

  public default List<FieldSpec> fieldList() {
    return new LinkedList<>();
  };
}
