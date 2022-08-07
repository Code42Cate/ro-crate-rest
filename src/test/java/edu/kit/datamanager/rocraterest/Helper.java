package edu.kit.datamanager.rocraterest;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.reader.RoCrateReader;
import edu.kit.datamanager.ro_crate.reader.ZipReader;
import edu.kit.datamanager.rocraterest.storage.LocalStorageService;
import edu.kit.datamanager.rocraterest.storage.StorageService;

public class Helper {

  private static final StorageService storageService = new LocalStorageService();

  public static RoCrate getCrate(String crateId) {
    RoCrate crate = new RoCrateReader(new ZipReader()).readCrate(storageService.path(crateId));

    return crate;
  }
}
