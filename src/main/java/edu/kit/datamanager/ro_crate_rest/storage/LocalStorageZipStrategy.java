package edu.kit.datamanager.ro_crate_rest.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.reader.RoCrateReader;
import edu.kit.datamanager.ro_crate.reader.ZipReader;
import edu.kit.datamanager.ro_crate.writer.RoCrateWriter;
import edu.kit.datamanager.ro_crate.writer.ZipWriter;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;

public class LocalStorageZipStrategy implements StorageStrategy {

  private final String UPLOAD_DIR = "rocrate-storage";

  public LocalStorageZipStrategy() {

    if (!Files.isDirectory(Paths.get(UPLOAD_DIR))) {
      try {
        Files.createDirectory(Paths.get(UPLOAD_DIR));
      } catch (IOException e) {
        System.out.println("Could not create upload directory. Ignoring.");
      }
    }
  }

  private Path getPath(String id) {
    return Paths.get(UPLOAD_DIR, id);
  }

  @Override
  public String storeCrate(InputStream zip) {

    String crateId = UUID.randomUUID().toString();

    try {
      Files.copy(zip, this.getPath(crateId));
    } catch (IOException e) {
      System.out.println("Could not upload file. Ignoring.");
    }

    return crateId;
  }

  @Override
  public void storeCrate(String id, RoCrate crate) {
    new RoCrateWriter(new ZipWriter()).save(crate, this.getPath(id).toString());
  }

  @Override
  public RoCrate getCrate(String id) {

    if (!Files.exists(this.getPath(id))) {
      return null;
    }

    return new RoCrateReader(new ZipReader()).readCrate(this.getPath(id).toString());
  }

  @Override
  public InputStream getCrateInputStream(String id) {

    try {
      return new FileInputStream(this.getPath(id).toFile());
    } catch (Exception e) {
      return null;
    }

  }

  @Override
  public void deleteCrate(String id) {

    try {
      Files.delete(this.getPath(id));
    } catch (IOException e) {
      System.out.println("Could not delete crate. Ignoring.");
    }

  }

  @Override
  public void addFile(String id, InputStream file, String filename) {

    try {
      ZipFile zf = new ZipFile(this.getPath(id).toString());
      ZipParameters parameters = new ZipParameters();
      parameters.setFileNameInZip(filename);
      zf.addStream(file, parameters);
    } catch (IOException e) {
      System.out.println("Could not delete file. Ignoring.");
    }

  }

  @Override
  public void deleteFile(String id, String filename) {

    try {
      ZipFile zf = new ZipFile(this.getPath(id).toString());
      zf.removeFile(filename);
    } catch (IOException e) {
      System.out.println("Could not delete file. Ignoring.");
    }

  }

  // TODO
  @Override
  public File getFile(String id, String filename) {
    return null;
  }

  // TODO
  @Override
  public File[] getFiles(String id) {
    return null;
  }

}
