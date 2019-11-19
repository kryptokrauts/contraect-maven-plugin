# Changelog

## [v1.0.0](https://github.com/kryptokrauts/contraect-maven-plugin/releases/tag/v1.0.0)

Initial release contraect maven plugin to interact with the smart contracts on the æternity blockchain.

### Initial functionalities
- automated generation of java classes for default æternity datatypes
- generation of on java class per sophia contract
- generation of deploy method which transactionally deploys the contract on the æternity network
- generation supports existing contracts (contractId needs to be passed to constructor of generated classes)
- mapping of simple sophia types to java types