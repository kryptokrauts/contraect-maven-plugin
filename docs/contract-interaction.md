# Contract interaction

## Local node example
The generated Java classes hide all the operations needed to interact with smart contracts on the aeternity blockchain. The only thing you need to do is create an instance of the generated class and provide an ***AeternityServiceConfiguration*** within the constructor of the generated class.

Without this configuration we wouldn't know to which compiler and to which node we should perform the necessary requests.

In this example we use the CryptoHamster sample from the [contræcts app](https://studio.aepps.com/) which we want to execute on our local æternity node
```java
KeyPairService keyPairService = new KeyPairServiceFactory().getService();
KeyPair keyPair = keyPairService.recoverKeyPair(<privateKey>);

AeternityServiceConfiguration config = AeternityServiceConfiguration
				.configure()
				.baseUrl("http://localhost")
                .compilerBaseUrl("http://localhost:3080")
                .mdwBaseUrl("http://localhost:4000")
				.network(Network.DEVNET)
                .keyPair(keyPair)
				.compile();

// get an instance of the generated contract object				
CryptoHamster cryptoHamsterInstance = new CryptoHamster(config, null);

// deploy the contract on the local node
Pair<String, String> deployment = cryptoHamsterInstance.deploy();
String txHash = deployment.getValue0();
String contractId = deployment.getValue1();
log.info("Deployed contract id - {} ", contractId );

// call a function of the contract
log.info("Call create hamster {}", cryptoHamsterInstance.createHamster("kryptokrauts"));
log.info("Call nameExists {}", cryptoHamsterInstance.nameExists("kryptokrauts"));
log.info("Call getHamsterDNA {}", cryptoHamsterInstance.getHamsterDNA("kryptokrauts"));
```

## Further examples

The [contræct-showcase-maven](https://github.com/kryptokrauts/contraect-showcase-maven) repository showcases how to use the contræct-maven-plugin to easily interact with smart contracts in Java. One of the examples is a MultiSig contract to be used with the Generalized Accounts feature.