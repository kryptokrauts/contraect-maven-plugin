package com.kryptokrauts.codegen;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
public class GeneratorConfiguration {

	@Default
	private long timeBetweenTransactionChecks = 1000l;

	@Default
	private int numberOfTrials = 60;
}
