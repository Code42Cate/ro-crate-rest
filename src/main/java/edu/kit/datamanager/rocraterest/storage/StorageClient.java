package edu.kit.datamanager.rocraterest.storage;

public class StorageClient {

  final private StorageStrategy strategy;

  public StorageClient(StorageStrategy strategy) {
    this.strategy = strategy;
  }

  public StorageStrategy get() {
    return this.strategy;
  }

}