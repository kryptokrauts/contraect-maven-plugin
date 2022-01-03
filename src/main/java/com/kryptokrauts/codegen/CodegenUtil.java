package com.kryptokrauts.codegen;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.javatuples.Pair;

public class CodegenUtil {

  public static String GENERATED =
      String.format(
          "This contract class was generated by contraect-maven-plugin on %s\r\nThe documentation of the plugin can be found here\r\nhttps://kryptokrauts.github.io/contraect-maven-plugin\r\n\r\n",
          new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

  public static final String SUPPORT =
      "If you like this project we would appreciate your support.\r\nYou can find multiple ways to support us here\r\nhttps://kryptokrauts.com/support";

  public static final String KRYPTOKRAUTS =
      " _                     _        _                    _                            \r\n"
          + "| |                   | |      | |                  | |                           \r\n"
          + "| | ___ __ _   _ _ __ | |_ ___ | | ___ __ __ _ _   _| |_ ___   ___ ___  _ __ ___  \r\n"
          + "| |/ / '__| | | | '_ \\| __/ _ \\| |/ / '__/ _` | | | | __/ __| / __/ _ \\| '_ ` _ \\ \r\n"
          + "|   <| |  | |_| | |_) | || (_) |   <| | | (_| | |_| | |_\\__ \\| (_| (_) | | | | | |\r\n"
          + "|_|\\_\\_|   \\__, | .__/ \\__\\___/|_|\\_\\_|  \\__,_|\\__,_|\\__|___(_)___\\___/|_| |_| |_|\r\n"
          + "            __/ | |                                                               \r\n"
          + "           |___/|_|\r\n";

  public static final String LICENSE_HEADER =
      "\r\nISC License\r\n"
          + "\r\nCopyright (c) "
          + new SimpleDateFormat("yyyy").format(new Date())
          + "\r\n";

  public static final String LICENSE =
      "\r\nPermission to use, copy, modify, and/or distribute this software for any\r\n"
          + "purpose with or without fee is hereby granted, provided that the above\r\n"
          + "copyright notice and this permission notice appear in all copies.\r\n"
          + "\r\n"
          + "THE SOFTWARE IS PROVIDED \"AS IS\" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH\r\n"
          + "REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY\r\n"
          + "AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,\r\n"
          + "INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM\r\n"
          + "LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE\r\n"
          + "OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR\r\n"
          + "PERFORMANCE OF THIS SOFTWARE.\r\n";

  public static final List<String> DEFAULT_LIBRARIES =
      Arrays.asList(
          "List.aes",
          "ListInternal.aes",
          "Option.aes",
          "String.aes",
          "Func.aes",
          "Pair.aes",
          "Triple.aes",
          "BLS12_381.aes",
          "Frac.aes");

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
