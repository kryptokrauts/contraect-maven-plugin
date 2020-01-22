package com.kryptokrauts.codegen;

public class CodegenUtil {

	public static String getUppercaseClassName(String name) {
		if (name != null && name.length() > 0) {
			return name.substring(0, 1).toUpperCase() + name.substring(1);
		}
		throw new RuntimeException("Classname " + name
				+ " is invalid - needs be at least one char");
	}
}
