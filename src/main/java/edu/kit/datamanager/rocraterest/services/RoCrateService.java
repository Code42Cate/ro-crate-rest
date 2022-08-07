package edu.kit.datamanager.rocraterest.services;

public class RoCrateService {

  private final String id;

  public RoCrateService(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
}
