package com.kryptokrauts.codegen.datatypes.defaults;

import com.kryptokrauts.codegen.maven.ABIJsonConfiguration;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.vertx.core.json.JsonObject;
import java.util.LinkedList;
import java.util.List;

public interface CustomType {

  public static final String RECORD = "record";

  public static final String VARIANT = "variant";

  public static final String ADDRESS_TYPE = "address";

  public static final String BYTES_TYPE = "bytes";

  public static final String HASH_TYPE = "hash";

  public static final String SIGNATURE_TYPE = "signature";

  public static final String ORACLE_TYPE = "oracle";

  public static final String ORACLE_QUERY_TYPE = "oracle_query";

  public static final String CHAIN_TTL_TYPE = "ChainTTL";

  public static final String POINTEE_TYPE = "Pointee";

  public JsonObject getTypeDefinition(ABIJsonConfiguration abiJsonConfiguration);

  public CodeBlock encodeValueCodeblock(String MP_PARAM);

  public CodeBlock mapToReturnValueCodeblock(String MP_PARAM);

  public MethodSpec constructorMethod();

  public default boolean complexReturnTypeMethod() {
    return false;
  }

  public List<MethodSpec> methodList();

  public default List<FieldSpec> fieldList() {
    return new LinkedList<>();
  }
  ;

  public default List<TypeSpec> additionalInnerTypes() {
    return new LinkedList<>();
  }

  public default MethodSpec customToStringMethod() {
    return null;
  }
}
