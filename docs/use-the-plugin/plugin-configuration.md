# Plugin configuration

## Parameters
Most of the configuration parameters are shipped with default values, which in almost all cases don't need to be explicitly set. The following list gives an overview over the configurable parameters of the plugin.

### Example
This is an example for the minimal required configuration that only provides the required parameters:
```
<plugin>
  <groupId>com.kryptokrauts</groupId>
  <artifactId>contraect-maven-plugin</artifactId>
  <version>2.0.0</version>
  <configuration>
    <codegen>
      <compilerBaseUrl>https://compiler.aepps.com</compilerBaseUrl>
      <directories>
        <directory>${project.basedir}/src/main/resources</directory>
      </directories>
    </codegen>
  </configuration>
  <executions>
	  <execution>
		  <goals>
			  <goal>generate-contraects</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```

### Codegen configuration block
The *\<codegen>* configuration block contains the general parameters for the plugin

| Parameter                                                                               | Description                                                               | Default                                          | Sample                                                          |
| --------------------------------------------------------------------------------------- | ------------------------------------------------------------------------- | ------------------------------------------------ | --------------------------------------------------------------- |
| initFunctionName                                                                        | Name of the deploy init function to call right after deploy               | init                                             |                                                                 |
| targetPackage                                                                           | Java package of the generated contraect classes                           | *com.kryptokrauts.contraect.generated*           |                                                                 |
| datatypePackage                                                                         | Java package of the generated datatypes                                   | com.*kryptokrauts.contraect.generated.datatypes* |                                                                 |
| targetPath                                                                              | Generated Java classes will be stored in this folder                      | *target/generated-sources/contraect*             |                                                                 |
| compilerBaseUrl (*)                                                                     | The base url of the sophia compiler                                       |                                                  | *https://compiler.aepps.com*                                    |
| numTrials                                                                               | Number of trials (with 1 sec delay) to wait for transaction to be mined   |
| -> relevant for stateful calls and the deploy method                                    | 60                                                                        |                                                  |
| directories (*)                                                                         | List of directories to be scanned for contract files                      |                                                  | *\<directory>${project.basedir}/src/main/resources\<directory>* |
| contractSuffix                                                                          | File suffix identifying a contract code file                              | aes                                              |                                                                 |
| resultAbortKey                                                                          | indicates the abort case of a function call, also applies for dryRun call | abort                                            |                                                                 |
| resultErrorKeyindicates the error case of a function call, also applies for dryRun call | error                                                                     |                                                  |
| resultRevertKey                                                                         | indicates an revert of a contract deploy                                  | revert                                           |                                                                 |

### ABI-JSON configuration block
The *\<abi-json>* configuration block defines the json key names which are used to parse the contract code. The listed elements are used to parse the contracts JSON ABI and further transform this into the resulting contract java class. Typically this part of the configuration can be completely ommitted unless the basic layout of the sophia contract ABI does not change.

| Parameter                    | Description                                      | Default   |
| ---------------------------- | ------------------------------------------------ | --------- |
| rootElement                  | key for contract root                            | contract  |
| eventElement                 | key for events                                   | events    |
| functionsElement             | key for contract functions                       | functions |
| contractNameElement          | key for contract name element                    | name      |
| payableElement               | key for contract payable flag                    | payable   |
| stateElement                 | key for contracts state definition               | state     |
| customTypeElement            | key for contracts custom types                   | type_defs |
| functionNameElement          | key for a custom functions name                  | name      |
| functionReturnTypeElement    | key for a custom functions return type           | returns   |
| functionArgumentsElement     | key for a custom functions arguments             | arguments |
| functionArgumentTypeElement  | key for a custom functions argument type         | type      |
| functionArgumentNameElement  | key for a custom functions argument name         | name      |
| functionStatefulElement      | key for a custom function flagging stateful type | stateful  |
| customTypeNameElement        | key for a custom types name                      | name      |
| customTypeTypedefElement     | key for a custom types typedefinition list       | typedef   |
| customTypeTypedefNameElement | key for one custom types typedefintion name      | name      |
| customTypeTypedefTypeElement | key for one custom types typedefinition type     | type      |