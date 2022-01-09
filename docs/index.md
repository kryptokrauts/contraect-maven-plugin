# Getting started

The **contræct-maven-plugin** is a maven/gradle plugin that simplifies interacting with smart contracts on the aeternity blockchain. It generates Java classes out of the sophia contract sourcecode which provide a convenient to deploy a contract on the chain or call a function.
It also wraps the input and output objects of those function calls - no parsing is necessary.

## Detail description

The plugin takes a contract written in sophia language as input and generates a Java class representing the contract and its methods. The following statements apply due to the code generation:

- creates a method to deploy the contract on the chain
- create method for every entrypoint to call entrypoints of a deployed contract
- methods will be executed using dryRun - which means it will not be committed and thus included in a block
- if a method is stateful - transaction additionally will be committed and thus included in a block. The method waits until the transaction is confirmed
- if a method is payable - an additional parameter is available to set the amount to deposit
- all input parameters are parsed to a typed Java classes (if possible),
also the result is parsed to a Java type (if possible)
- in case of errors during dryRun call or committing the transaction, exceptions are raised with details from the node
- initialization of an instance can be done using an existing contractId, thus the methods of an already deployed contract can be called

## System requirements

- System requirements of the [Java SDK](https://oss.sonatype.org/content/repositories/)

## Include dependencies

### contraect-maven-plugin

To add the contræct-maven-plugin to you build, simply add the following snippet to your *pom.xml*

```
<build>
	<plugins>
		<plugin>
			<groupId>com.kryptokrauts</groupId>
			<artifactId>contraect-maven-plugin</artifactId>
			<version>2.0.0</version>
			<configuration>
				...
			</configuration>
		</plugin>
	</plugins>
</build>
```
The content of the *\<configuration>* section is covered in the Plugin configuration

### aepp-sdk-java

Under the hood the plugin as well as the generated classes make use of the aepp-sdk-java to implement the interaction with the aeternity node. Therefore the dependency needs to be declared in the corresponding section - please make sure to always use the latest version of the SDK

```
<dependency>
	<groupId>com.kryptokrauts</groupId>
	<artifactId>aepp-sdk-java</artifactId>
	<version>3.0.0</version
</dependency>
```