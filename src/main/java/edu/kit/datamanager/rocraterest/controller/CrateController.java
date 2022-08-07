package edu.kit.datamanager.rocraterest.controller;

import edu.kit.datamanager.rocraterest.services.RoCrateService;
import edu.kit.datamanager.rocraterest.storage.LocalStorageService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class CrateController {

  private final LocalStorageService storageService = new LocalStorageService();

  @RequestMapping(value = "/crates/{crateId}", produces = "application/zip")
  public ResponseEntity<Resource> get(@PathVariable String crateId) throws FileNotFoundException {
    File zip = storageService.get(crateId);

    Resource resource = new InputStreamResource(new FileInputStream(zip));

    return new ResponseEntity<>(resource, HttpStatus.OK);
  }

  @DeleteMapping("/crates/{crateId}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void delete(@PathVariable String crateId) {
    storageService.delete(crateId);
  }

  @PostMapping("/crates")
  public RoCrateService create(@RequestParam("file") MultipartFile file) throws IOException {

    String crateId = UUID.randomUUID().toString();

    this.storageService.store(file.getInputStream(), crateId);

    return new RoCrateService(crateId);
  }

  @PostMapping("/crates/{crateId}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void update(@PathVariable String crateId, @RequestParam("file") MultipartFile file) throws IOException {
    // Add file to directory
    this.storageService.add(file.getInputStream(), file.getOriginalFilename(), crateId);
  }
}
