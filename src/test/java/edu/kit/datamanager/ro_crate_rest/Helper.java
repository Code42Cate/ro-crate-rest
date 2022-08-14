package edu.kit.datamanager.ro_crate_rest;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate_rest.storage.LocalStorageZipStrategy;
import edu.kit.datamanager.ro_crate_rest.storage.StorageClient;

public class Helper {

  private static final StorageClient storageClient = new StorageClient(new LocalStorageZipStrategy());

  public static RoCrate getCrate(String crateId) {
    return storageClient.get().getCrate(crateId);

  }
}
