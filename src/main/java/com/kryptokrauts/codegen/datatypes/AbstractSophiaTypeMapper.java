package com.kryptokrauts.codegen.datatypes;

public abstract class AbstractSophiaTypeMapper implements SophiaTypeMapper {

	protected String getType(Object type) {
		if (type == null) {
			return "";
		}
		return type.toString();
	}

}
