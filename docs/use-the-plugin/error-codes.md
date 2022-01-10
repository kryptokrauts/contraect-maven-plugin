#Error codes
During the contract class generation process, on of the following error codes might occur. In every case, a detailed message is displayed. 

If you face such an error, you're welcome to help improving the contraect-maven-plugin. Therefore, please create an issue on [contraect-maven-plugin](https://github.com/kryptokrauts/contraect-maven-plugin) along with your contract code and the arguments displayed in the error message.

#### 005 - FAIL_IMPORT_INCLUDES
Indicates an error concering a library or contract include
#### 010 - FAIL_CREATE_ABI
Indicates an error concering parsing the contract code to abi/json representation
#### 020 - FAIL_PARSE_ROOT
Indicates an error concering parsing the contract's json tree
#### 030 - FAIL_GENERATE_CONTRACT
Indicates a general error creating the Java class out of the contract
#### 100 - FAIL_READ_CONTRACT_FILE
Indicates an error reading the contract file
#### 200 - UNFORSEEN_MAPPING 
Indicates an unforseen or unimplemented case of type mapping
#### 300 - UNFORSEEN_CUSTOM_TYPEDEF
Indicates, that custom type definition cannot be parsed
