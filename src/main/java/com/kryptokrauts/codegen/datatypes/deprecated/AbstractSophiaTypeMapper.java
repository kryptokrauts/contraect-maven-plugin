package com.kryptokrauts.codegen.datatypes.deprecated;

import com.squareup.javapoet.CodeBlock;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public abstract class AbstractSophiaTypeMapper implements SophiaTypeMapper {

  protected Logger _logger = LoggerFactory.getLogger(this.getClass());

  @NonNull protected TypeResolverRefactored typeResolverInstance;

  /**
   * null safe to string conversion
   *
   * @param value
   * @return
   */
  protected String valueToString(Object value) {
    if (value == null) {
      return "";
    }
    return value.toString();
  }

  public String getUnsupportedMappingException(
      Object unsupportedTypeCaseString,
      String typeMapperClass,
      String typeMapperClassMethod,
      String additionalInfo) {
    return String.format(
        "This is an unforseen contract type definition case for mapper %s.%s - cannot map java type to given sophia type. Please create an issue on https://github.com/kryptokrauts/contraect-maven-plugin along with your contract code and the following strings\nTypeString:\n%s"
            + (additionalInfo != null ? "\nAdditional information: " + additionalInfo : ""),
        typeMapperClass,
        typeMapperClassMethod,
        unsupportedTypeCaseString);
  }

  @Override
  public CodeBlock getReturnStatement(Object resultToReturn) {
    return CodeBlock.builder().add("$L.toString()", resultToReturn).build();
  }
}
