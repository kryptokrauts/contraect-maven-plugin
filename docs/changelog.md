# Changelog

## [v0.9.0](https://github.com/kryptokrauts/contraect-maven-plugin/releases/tag/v0.9.0)

Initial release of the contraect-maven-plugin to interact with the smart contracts on the æternity blockchain.

### Initial functionalities
- automated generation of java classes for default æternity datatypes
- generation of java classes for sophia contracts
- generation of deploy method which transactionally deploys the contract on the æternity blockchain
- generated classes support existing contracts
   - contractId needs to be passed to constructor
- mapping of simple sophia types to java types