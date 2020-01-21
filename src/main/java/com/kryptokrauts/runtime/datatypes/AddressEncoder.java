package com.kryptokrauts.runtime.datatypes;

import com.kryptokrauts.codegen.datatypes.deprecated.AddressDatatypeGenerator;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

public class AddressEncoder extends AbstractDatatypeEncoder {

	public AddressEncoder(DatatypeEncodingHandler resolveInstance) {
		super(resolveInstance);
	}

	public boolean applies(TypeName type) {
		return TypeName.get(getAddressClass()).equals(type);
	}

	@Override
	public boolean applies(Object type) {
		return AddressDatatypeGenerator.ADDRESS_DATATYPE_NAME.equals(type);
	}

	public CodeBlock encodeValue(TypeName type, String variableName) {
		return CodeBlock.builder().add("$L.toString()", variableName).build();
	}

	@Override
	public CodeBlock mapToReturnValue(TypeName type, String variableName) {
		return CodeBlock.builder()
				.add("new $T($L.toString())", getAddressClass(), variableName)
				.build();
	}

	// retrieve all fields using getDeclaredFields
	private Class<?> addressClass;

	private Class<?> getAddressClass() {
		if (addressClass == null) {
			addressClass = this.resolveInstance
					.getCustomDatatype(
							AddressDatatypeGenerator.ADDRESS_DATATYPE_NAME)
					.load();
		}
		return addressClass;
	}

	@Override
	public TypeName getTypeName(Object type) {
		// return TypeName.get(addressClass);
		return ClassName.get("", AddressDatatypeGenerator.ADDRESS_DATATYPE_NAME)
				.box();
	}
}
