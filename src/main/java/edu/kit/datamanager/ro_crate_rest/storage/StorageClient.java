package edu.kit.datamanager.ro_crate_rest.storage;

public class StorageClient {

  final private StorageStrategy strategy;

  public StorageClient(StorageStrategy strategy) {
    this.strategy = strategy;
  }

  public StorageStrategy get() {
    return this.strategy;
  }

}