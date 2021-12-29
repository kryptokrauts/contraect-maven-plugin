package com.kryptokrauts.codegen.datatypes.defaults;

import com.kryptokrauts.codegen.CodegenUtil;
import com.kryptokrauts.codegen.maven.ABIJsonConfiguration;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Map;

public class AccountPointeeType extends PointeeType {

  @Override
  public JsonObject getTypeDefinition(ABIJsonConfiguration abiJsonConfiguration) {
    return new JsonObject()
        .put(abiJsonConfiguration.getCustomTypeNameElement(), ACCOUNT_POINTEE_TYPE)
        .put(
            abiJsonConfiguration.getCustomTypeTypedefElement(),
            new JsonObject().put(RECORD, new JsonArray()))
        .put(abiJsonConfiguration.getCustomTypeExtendingClass(), POINTEE_TYPE);
  }

  @Override
  public CodeBlock mapToReturnValueCodeblock(String MP_PARAM) {
    return CodeBlock.builder()
        .add(
            "new $T($T.mapToReturnValue($L instanceof $T ? ((($T) (($T) $L).get(\"AENS.AccountPt\")).get(0)) : $L))",
            ClassName.get("", CodegenUtil.getUppercaseClassName(ACCOUNT_POINTEE_TYPE)),
            ClassName.get("", CodegenUtil.getUppercaseClassName(ADDRESS_TYPE)),
            MP_PARAM,
            Map.class,
            List.class,
            Map.class,
            MP_PARAM,
            MP_PARAM)
        .build();
  }
}
