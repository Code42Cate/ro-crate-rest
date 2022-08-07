package edu.kit.datamanager.rocraterest.storage;

import java.io.File;
import java.io.InputStream;

public interface StorageService {

  void store(InputStream file, String id);

  void delete(String id);

  void add(InputStream file, String filename, String id);

  File get(String id);

  String path(String id);

}

