# Changelog

## [v2.0.0](https://github.com/kryptokrauts/contraect-maven-plugin/releases/tag/v2.0.0)

This release introduces type mapping support for the [æternity naming system (AENS)](https://aeternity.com/protocol/AENS.html) and ChainTTL as well as improvements, better error handling and fixes for known issues. This release is improved and adapted to the [v3.0.0](https://github.com/kryptokrauts/aepp-sdk-java/releases/tag/v3.0.0) of the [aepp-sdk-java](https://github.com/kryptokrauts/aepp-sdk-java).

### Bugfixes
- [#39](https://github.com/kryptokrauts/contraect-maven-plugin/issues/39) an NPE can occur if waiting for the tx leads to "Tx not mined"
- [#63](https://github.com/kryptokrauts/contraect-maven-plugin/issues/63) handle explicit return type unit properly
- [#66](https://github.com/kryptokrauts/contraect-maven-plugin/issues/66) getNextNonce should return BigInteger.ONE if account doesn't exist

### New Features
- [#2](https://github.com/kryptokrauts/contraect-maven-plugin/issues/2) check contract bytecode for given contractId 
- [#62](https://github.com/kryptokrauts/contraect-maven-plugin/issues/62) introduced Chain.ttl type
- [#65](https://github.com/kryptokrauts/contraect-maven-plugin/issues/65) Support for AENS type

### General changes
- [#40](https://github.com/kryptokrauts/contraect-maven-plugin/issues/40) handle known internal errors from dryRun call
- [#49](https://github.com/kryptokrauts/contraect-maven-plugin/issues/49) better readable error message on errors within dryRun
- [#52](https://github.com/kryptokrauts/contraect-maven-plugin/issues/52) provided getter and setter for contract address
- [#58](https://github.com/kryptokrauts/contraect-maven-plugin/issues/58) remove fixed gas limit from dryRun and make configurable via maven config
- SDK documentation via MkDocs

## [v1.0.0](https://github.com/kryptokrauts/contraect-maven-plugin/releases/tag/v1.0.0)

This release provides full support of complex data and return types, type aliases and includes of other contracts and libraries. Also the result mapping of contract calls was improved and adapted to the [v2.2.1](https://github.com/kryptokrauts/aepp-sdk-java/releases/tag/v2.2.1) of the [aepp-sdk-java](https://github.com/kryptokrauts/aepp-sdk-java). A unified error handling was introduced, please refer to the [documentation](https://kryptokrauts.gitbook.io/contraect-maven-plugin/use-the-plugin/plugin-execution/error-codes).

### Enhancements
- [#1](https://github.com/kryptokrauts/contraect-maven-plugin/issues/1) support of complex datatypes
- [#4](https://github.com/kryptokrauts/contraect-maven-plugin/issues/4) support of custom types
- [#6](https://github.com/kryptokrauts/contraect-maven-plugin/issues/6) handle revert messages properly
- [#20](https://github.com/kryptokrauts/contraect-maven-plugin/issues/20) evaluate impact of including other contracts
- [#21](https://github.com/kryptokrauts/contraect-maven-plugin/issues/21) add support for type aliases
- [#22](https://github.com/kryptokrauts/contraect-maven-plugin/issues/22) handle option return types in a convenient way
- [#24](https://github.com/kryptokrauts/contraect-maven-plugin/issues/24) support void methods
- [#27](https://github.com/kryptokrauts/contraect-maven-plugin/issues/27) handle payable modifier (aettos as param in generated methods)

## [v0.9.1](https://github.com/kryptokrauts/contraect-maven-plugin/releases/tag/v0.9.1)

### Fixes
- [#16](https://github.com/kryptokrauts/contraect-maven-plugin/issues/16) invalid deploy-method of generated class when init-function has params

## [v0.9.0](https://github.com/kryptokrauts/contraect-maven-plugin/releases/tag/v0.9.0)

Initial release of the contraect-maven-plugin to interact with the smart contracts on the æternity blockchain.

### Initial functionalities
- automated generation of java classes for default æternity datatypes
- generation of java classes for sophia contracts
- generation of deploy method which transactionally deploys the contract on the æternity blockchain
- generated classes support existing contracts
   - contractId needs to be passed to constructor
- mapping of simple sophia types to java types