package edu.kit.datamanager.rocraterest.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;

import org.springframework.web.multipart.MultipartFile;

public class LocalStorageService implements StorageService {

  private static final String UPLOAD_DIR = "rocrate-storage";

  public LocalStorageService() {
    init();
  }

  private void init() {
    // Create upload directory if it doesn't exist
    if (!Files.isDirectory(Paths.get(UPLOAD_DIR))) {
      try {
        Files.createDirectory(Paths.get(UPLOAD_DIR));
      } catch (IOException e) {
        System.out.println("Could not create upload directory. Ignoring.");
      }
    }
  }

  @Override
  public void store(InputStream inputStream, String id) {
    try {
      Files.copy(inputStream, Paths.get(UPLOAD_DIR, id));
    } catch (IOException e) {
      System.out.println("Could not upload file. Ignoring.");
    }
  }

  @Override
  public void delete(String id) {
    try {
      Files.delete(Paths.get(UPLOAD_DIR, id));
    } catch (IOException e) {
      System.out.println("Could not delete file. Ignoring.");
    }
  }

  @Override
  public void add(InputStream inputStream, String filename, String id) {
    try {
      ZipParameters parameters = new ZipParameters();
      parameters.setFileNameInZip(filename);
      new ZipFile(Paths.get(UPLOAD_DIR, id).toString()).addStream(inputStream, parameters);
    } catch (IOException e) {
      System.out.println("Could not upload file. Ignoring.");
    }
  }

  @Override
  public File get(String id) {
    return new File(Paths.get(UPLOAD_DIR, id).toString());
  }

  @Override
  public String path(String id) {
    return Paths.get(UPLOAD_DIR, id).toString();
  }


  @Override
  public Boolean exists(String id) {
    return Files.exists(Paths.get(UPLOAD_DIR, id));
  }
}
