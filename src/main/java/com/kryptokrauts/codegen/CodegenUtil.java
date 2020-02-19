package com.kryptokrauts.codegen;

import java.util.List;
import java.util.stream.Collectors;
import org.javatuples.Pair;

public class CodegenUtil {

  private static final String CREATE_GITHUB_ISSUE =
      "Please create an issue on https://github.com/kryptokrauts/contraect-maven-plugin along with your contract code and the following arguments";

  public static final String getBaseErrorMessage(
      CmpErrorCode errorCode, String errorMessage, List<Pair<?, ?>> arguments) {
    return String.format(
        "[CMP_ERR_%s]\n%s\n%s\n%s",
        errorCode.getErrorCode(),
        errorMessage,
        CREATE_GITHUB_ISSUE,
        arguments.stream()
            .map(p -> new String(p.getValue0() + ": " + p.getValue1()))
            .collect(Collectors.joining("\n")));
  }

  public static String getUppercaseClassName(String name) {
    if (name != null && name.length() > 0) {
      return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
    throw new RuntimeException("Classname " + name + " is invalid - needs be at least one char");
  }
}
