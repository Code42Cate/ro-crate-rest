package edu.kit.datamanager.ro_crate_rest.storage;

import java.io.File;
import java.io.InputStream;

import edu.kit.datamanager.ro_crate.RoCrate;

/*
 * Strategy for storing and retrieving RoCrates.
 */
public interface StorageStrategy {

  /**
   * Used for the initial storing of the crate.
   * 
   * @param zip The uploaded zip file to be saved as a crate
   * 
   * @return The id of the crate in a URL compatible format
   */
  String storeCrate(InputStream zip);

  /**
   * Save a RoCrate
   * 
   * @param id The id of the crate to be deleted
   */
  void storeCrate(String id, RoCrate crate);

  /**
   * Read a crate
   * 
   * @param id The id of the crate to be read
   */
  RoCrate getCrate(String id);

  /**
   * Get InputStream of crate zip file
   * 
   * @param id The id of the crate to be loaded
   * 
   * @return InputStream of crate zip file or null if crate does not exist
   */
  InputStream getCrateInputStream(String id);

  /**
   * Delete a crate
   * 
   * @param id The id of the crate to be deleted
   */
  void deleteCrate(String id);

  /**
   * Add file or directory to crate.
   * 
   * @param file Can either be a normal file or NullInputStream to add directory
   * 
   * @param filename Name of file or directory. Can be nested, missing parent
   * directories will be created
   */
  void addFile(String id, InputStream file, String filename);

  /**
   * Delete file or directory from crate.
   * 
   * @param filename Name of file or directory.
   */
  void deleteFile(String id, String filename);

  /**
   * Get file or directory from crate.
   * 
   * @param id The id of the crate to be loaded
   * 
   * @param filename Name of file or directory. Can be nested.
   * 
   * @return read-only file or directory
   */
  File getFile(String id, String filename);

  /**
   * Get all files as unorderd array from crate.
   * 
   * @param id The id of the crate to be loaded
   */
  File[] getFiles(String id);

}
