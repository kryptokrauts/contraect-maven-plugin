package com.kryptokrauts.codegen.maven;

import com.kryptokrauts.codegen.ContraectGenerator;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

@Mojo(
    name = "generate-contraects",
    defaultPhase = LifecyclePhase.GENERATE_SOURCES,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ContraectCodegenMojo extends AbstractMojo {

  // the list of contract files to process
  private List<String> aesFiles = new LinkedList<String>();

  // the base configuration for the code generation
  @Parameter(required = true, alias = "codegen")
  private CodegenConfiguration config;

  /** describes the abi json tags which are parsed during contract generation */
  @Parameter(alias = "abi-json")
  private ABIJsonDescriptionConfiguration abiJsonDescription =
      new ABIJsonDescriptionConfiguration();

  // the generator class
  ContraectGenerator contraectGenerator;

  @Override
  public void execute() throws MojoExecutionException {
    logMessage("Starting kryptokrauts contraect generator");
    config.validate();
    gatherContractFiles();
    processContractFiles();
    logMessage("Execution of kryptokrauts contraect generator finished");
  }

  private void processContractFiles() throws MojoExecutionException {
    for (String aesFile : aesFiles) {
      contraectGenerator = new ContraectGenerator(config, abiJsonDescription);
      contraectGenerator.generate(aesFile);
    }
  }

  private void gatherContractFiles() {
    for (String currentDirectory : config.getDirectories()) {
      if (!StringUtils.isBlank(currentDirectory)) {
        aesFiles.addAll(scanFileset(config.getContractSuffix(), currentDirectory));
      }
    }
    logFiles(aesFiles);
  }

  private void logFiles(List<String> aesFiles) {
    if (aesFiles.size() > 0) {
      getLog().info(String.format("Found %d contracts for generation", aesFiles.size()));
      aesFiles.forEach(
          c -> {
            getLog().info(c);
          });
    }
  }

  private List<String> scanFileset(String suffix, String currentDirectory) {
    FileSetManager fileSetManager = new FileSetManager();
    FileSet fileset = new FileSet();
    fileset.setDirectory(currentDirectory);
    fileset.setIncludes(Arrays.asList("**/*." + suffix));
    return Arrays.asList(fileSetManager.getIncludedFiles(fileset)).stream()
        .map(file -> currentDirectory + "/" + file)
        .collect(Collectors.toList());
  }

  private void logMessage(String message) {
    getLog().info("------------------------------------------------------------------------");
    getLog().info(message);
    getLog().info("------------------------------------------------------------------------");
  }
}
