package com.kryptokrauts.codegen;

import com.kryptokrauts.aeternity.sdk.service.aeternity.AeternityServiceConfiguration;
import com.kryptokrauts.aeternity.sdk.service.aeternity.AeternityServiceFactory;
import com.kryptokrauts.aeternity.sdk.service.aeternity.impl.AeternityService;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CodegenConfiguration {

  private AeternityService aeternityService;

  private String compilerBaseUrl;

  // @TODO - MAYBE ADD CONTRACT NAME TO PACKAGE?
  private String targetPackage;

  private String datatypePackage;

  private String targetPath;

  private int numTrials;

  private String initFunctionName;

  /*
   * JSON values for contract abi
   */

  private String abiJSONRootElement;

  private String abiJSONNameElement;

  private String abiJSONFunctionsElement;

  private String abiJSONFunctionsNameElement;

  private String abiJSONFunctionsReturnTypeElement;

  private String abiJSONFunctionArgumentElement;

  private String abiJSONFunctionArgumentTypeElement;

  private String abiJSONFunctionArgumentNameElement;

  private String abiJSONFunctionStatefulElement;

  private String abiJSONTypesElement;

  private String abiJSONTypesNameElement;

  private String abiJSONTypesTypedefElement;

  public AeternityService getAeternityService() {
    if (aeternityService == null) {
      VertxOptions options = new VertxOptions();
      options
          .getFileSystemOptions()
          .setFileCachingEnabled(false)
          .setClassPathResolvingEnabled(false);
      options.getMetricsOptions().setEnabled(false);
      Vertx vertxInstance = Vertx.vertx(options);
      aeternityService =
          new AeternityServiceFactory()
              .getService(
                  AeternityServiceConfiguration.configure()
                      .vertx(vertxInstance)
                      .compilerBaseUrl(this.compilerBaseUrl)
                      .compile());
    }
    return aeternityService;
  }
}
