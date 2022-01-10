# Plugin execution
## Generate Java classes
The following mvn command can be used to explicitly generate the Java classes based on given contracts
```sh
mvn contraect:generate-contraects
```

After the plugin execution you should find the generated Java classes in the folder you specified through the configuration property targetPath.

Alternatively the default maven lifecycle phase generate-sources can be called - therefore the plugin configuration has to be as follows
```
<plugin>
  <groupId>com.kryptokrauts</groupId>
  <artifactId>contraect-maven-plugin</artifactId>
  <version>2.0.0</version>
  ...
  <executions>
	  <execution>
		  <goals>
			  <goal>generate-contraects</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```

## Add generated Java classes as sources
### Option 1
Add the folder containing the generated Java classes manually as source-folder within your IDE.

### Option 2
Generate classes directly into the regular source folder by defining *src/main/java* as **targetPath**.

**Note**: with this solution you will probably end up having the generated files committed to the repo.
### Option 3
Add the resources using the [build-helper-maven-plugin](https://www.mojohaus.org/build-helper-maven-plugin/index.html)
```
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>build-helper-maven-plugin</artifactId>
  <version>3.0.0</version>
  <executions>
    <execution>
      <phase>generate-sources</phase>
      <goals>
        <goal>add-source</goal>
      </goals>
      <configuration>
        <sources>
          <source>${project.build.directory}/generated-sources/contraect</source>
        </sources>
      </configuration>
    </execution>
  </executions>
</plugin>
```