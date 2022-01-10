# Roadmap

The following improvements and enhancements are planned for the next release.

### Consolidation with SDK
The generated classes will make use of the convienence methods performing contract create and contract calls included in the Java SDK.

### Sophia Types
To further consolidate logic with the Java SDK, Sophia Types will be moved to the SDK and reused instead of being generated within every class. This will help keeping the code clean and readable and avoid class clashes and confusion when working with multiple generated contracts in one project.

### Enhanced result object
Currently a contract call returns a pair of transaction hash and typed result. It is planned that every contract call returns a special return object (ContractTxResult) which holds more information about the contract call including the raw node result.