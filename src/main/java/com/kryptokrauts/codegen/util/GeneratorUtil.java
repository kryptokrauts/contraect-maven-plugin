package com.kryptokrauts.codegen.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kryptokrauts.aeternity.sdk.constants.BaseConstants;
import com.kryptokrauts.aeternity.sdk.service.aeternity.AeternityServiceConfiguration;
import com.kryptokrauts.aeternity.sdk.service.aeternity.impl.AeternityService;
import com.kryptokrauts.aeternity.sdk.service.compiler.domain.ACIResult;
import com.kryptokrauts.aeternity.sdk.service.info.domain.TransactionInfoResult;
import com.kryptokrauts.aeternity.sdk.service.info.domain.TransactionResult;
import com.kryptokrauts.aeternity.sdk.service.transaction.domain.DryRunAccountModel;
import com.kryptokrauts.aeternity.sdk.service.transaction.domain.DryRunRequest;
import com.kryptokrauts.aeternity.sdk.service.transaction.domain.DryRunTransactionResult;
import com.kryptokrauts.aeternity.sdk.service.transaction.domain.DryRunTransactionResults;
import com.kryptokrauts.aeternity.sdk.service.transaction.domain.PostTransactionResult;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.ContractCallTransactionModel;
import com.kryptokrauts.aeternity.sdk.service.transaction.type.model.ContractCreateTransactionModel;
import com.kryptokrauts.codegen.GeneratorConfiguration;

import lombok.AllArgsConstructor;

/**
 * @deprecated will be refactored into ContraectGenerator
 * @author mitch
 */
@AllArgsConstructor
public class GeneratorUtil {

	protected static final Logger _logger = LoggerFactory
			.getLogger(GeneratorUtil.class);

	private AeternityService aeternityService;

	/**
	 * @param generatorConfig
	 *            this must be passed later through maven plugin config
	 */
	private GeneratorConfiguration generatorConfiguration;

	private AeternityServiceConfiguration config;

	private String contractUrl;

	private String getContract() {
		final InputStream inputStream = Thread.currentThread()
				.getContextClassLoader().getResourceAsStream(contractUrl);
		try {
			return IOUtils.toString(inputStream,
					StandardCharsets.UTF_8.toString());
		} catch (IOException e) {
			throw new RuntimeException("Cannot read contract " + contractUrl);
		}
	}

	public String dryRunCall(String deployedContractId, String function,
			Object... params) {
		String callData = this.getCalldataForFunction(function, params);

		DryRunTransactionResults dryRunResults = aeternityService.transactions
				.blockingDryRunTransactions(DryRunRequest.builder().build()
						.account(DryRunAccountModel.builder()
								.publicKey(
										config.getBaseKeyPair().getPublicKey())
								.build())
						.transactionInputItem(ContractCallTransactionModel
								.builder().callData(callData)
								.gas(BigInteger.valueOf(1579000))
								.contractId(deployedContractId)
								.gasPrice(BigInteger.valueOf(
										BaseConstants.MINIMAL_GAS_PRICE))
								.amount(BigInteger.ZERO)
								.nonce(this.getNextNonce())
								.callerId(this.config.getBaseKeyPair()
										.getPublicKey())
								.ttl(BigInteger.ZERO)
								.virtualMachine(config.getTargetVM()).build()));

		if (dryRunResults.getResults() != null
				&& dryRunResults.getResults().size() > 0) {
			DryRunTransactionResult result = dryRunResults.getResults().get(0);
			if ("ok".equals(result.getResult())) {
				Object decodedValue = this.aeternityService.compiler
						.blockingDecodeCallResult(getContract(), function,
								result.getResult(),
								result.getContractCallObject()
										.getReturnValue());
				return decodedValue.toString();
			}
		}
		/**
		 * @TODO exception handling
		 */
		throw new RuntimeException("call of function " + function
				+ " with params " + params + " failed");
	}

	public String getCalldataForFunction(String function, Object... params) {
		List<String> arguments = null;
		if (params != null) {
			arguments = new LinkedList<String>();
			for (Object param : params) {
				if (param instanceof Map) {
					arguments.add(generateMapParam((Map) param));
				} else {
					arguments.add(param.toString());
				}
			}
		}
		return aeternityService.compiler.blockingEncodeCalldata(getContract(),
				function, arguments);
	}

	private String generateMapParam(Map params) {
		Set<String> recipientConditionSet = new HashSet<>();
		params.forEach(
				(k, v) -> recipientConditionSet.add("[" + k + "] = " + v));
		return "{" + recipientConditionSet.stream()
				.collect(Collectors.joining(", ")) + "}";
	}

	private BigInteger getNextNonce() {
		return aeternityService.accounts
				.blockingGetAccount(
						Optional.of(config.getBaseKeyPair().getPublicKey()))
				.getNonce().add(BigInteger.ONE);
	}

	public String deployContract(String callData) {
		String byteCode = aeternityService.compiler
				.blockingCompile(this.getContract(), null, null);

		ContractCreateTransactionModel contractCreate = ContractCreateTransactionModel
				.builder().amount(BigInteger.ZERO).callData(callData)
				.contractByteCode(byteCode).deposit(BigInteger.ZERO)
				.gas(BigInteger.valueOf(48000000l))
				.gasPrice(BigInteger.valueOf(BaseConstants.MINIMAL_GAS_PRICE))
				.nonce(this.getNextNonce())
				.ownerId(config.getBaseKeyPair().getPublicKey())
				.ttl(BigInteger.ZERO).virtualMachine(config.getTargetVM())
				.build();

		String unsignedTx = aeternityService.transactions
				.blockingCreateUnsignedTransaction(contractCreate);

		DryRunTransactionResults dryRunResults = aeternityService.transactions
				.blockingDryRunTransactions(
						DryRunRequest.builder().build()
								.account(DryRunAccountModel.builder()
										.publicKey(config.getBaseKeyPair()
												.getPublicKey())
										.build())
								.transactionInputItem(unsignedTx));
		DryRunTransactionResult dryRunResult = dryRunResults.getResults()
				.get(0);
		contractCreate = contractCreate.toBuilder()
				.gas(dryRunResult.getContractCallObject().getGasUsed())
				.gasPrice(dryRunResult.getContractCallObject().getGasPrice())
				.build();

		PostTransactionResult result = aeternityService.transactions
				.blockingPostTransaction(contractCreate);

		TransactionInfoResult txInfoObject = this
				.waitForTxInfoMined(result.getTxHash());

		// Object decodedValue =
		// this.aeternityService.compiler.blockingDecodeCallResult(getContract(),
		// "init",
		// dryRunResult.getContractCallObject().getReturnType(),
		// dryRunResult.getContractCallObject().getReturnValue());

		return txInfoObject.getCallInfo().getContractId();
	}

	public boolean contractExists(String deployedContractId) {
		if (deployedContractId != null
				&& deployedContractId.trim().length() > 0) {
			String byteCode = "";// this.aeternityService.info.blockingGetContractByteCode(deployedContractId);
			if (byteCode == null) {
				throw new RuntimeException("Given contract with pubkey "
						+ deployedContractId + " is not deloyed");
			}
			if (!byteCode.equals(aeternityService.compiler
					.blockingCompile(getContract(), null, null))) {
				throw new RuntimeException("Given contract with pubkey "
						+ deployedContractId
						+ " is not equal to given contract source defined in "
						+ contractUrl);
			}
		}
		return false;
	}

	public Map generateACIFromContract(String contract)
			throws JsonParseException, JsonMappingException, IOException {
		Path contractPath = Paths.get("", "src", "test", "resources",
				"contracts", "aes", contract + ".aes");
		String aesContent = new String(Files.readAllBytes(contractPath));
		ACIResult result = this.aeternityService.compiler
				.blockingGenerateACI(aesContent, null, null);
		if (result != null && result.getEncodedAci() != null) {
			ObjectMapper mapper = new ObjectMapper();
			String contractACI = result.getEncodedAci().toString();
			return mapper.readValue(contractACI, Map.class);
		}
		throw new RuntimeException(
				"Given contract " + contract + " cannot be transformed to ACI: "
						+ result.getAeAPIErrorMessage());
	}

	private TransactionInfoResult waitForTxInfoMined(String txHash) {
		this.waitForTxMined(txHash);
		return this.aeternityService.info
				.blockingGetTransactionInfoByHash(txHash);
	}

	private TransactionResult waitForTxMined(String txHash) {
		int blockHeight = -1;
		TransactionResult minedTx = null;
		int doneTrials = 1;
		while (blockHeight == -1 && doneTrials < this.generatorConfiguration
				.getNumberOfTrials()) {
			minedTx = aeternityService.info
					.blockingGetTransactionByHash(txHash);
			if (minedTx.getBlockHeight().intValue() > 1) {
				_logger.debug("Mined tx: " + minedTx);
				blockHeight = minedTx.getBlockHeight().intValue();
			} else {
				_logger.debug(String.format(
						"Transaction not mined yet, trying again in 1 second (%s of %s)...",
						doneTrials,
						this.generatorConfiguration.getNumberOfTrials()));
				try {
					Thread.sleep(this.generatorConfiguration
							.getTimeBetweenTransactionChecks());
				} catch (InterruptedException e) {
					throw new RuntimeException(String.format(
							"Waiting for transaction %s to be mined was interrupted: %s",
							txHash, e.getMessage()));
				}
				doneTrials++;
			}
		}
		if (blockHeight == -1) {
			throw new RuntimeException(String.format(
					"Transaction %s was not mined after %s trials, aborting",
					txHash, doneTrials));
		}
		return minedTx;
	}
}
