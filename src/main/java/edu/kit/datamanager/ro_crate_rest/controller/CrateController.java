package edu.kit.datamanager.ro_crate_rest.controller;

import jakarta.servlet.http.HttpServletRequest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import edu.kit.datamanager.ro_crate_rest.dto.RoCrateDto;
import edu.kit.datamanager.ro_crate_rest.storage.LocalStorageZipStrategy;
import edu.kit.datamanager.ro_crate_rest.storage.StorageClient;

@RestController
@RequestMapping("/crates")
public class CrateController {

    private final StorageClient storageClient = new StorageClient(new LocalStorageZipStrategy());

    @RequestMapping(value = "/{crateId}", produces = "application/zip",method = RequestMethod.GET)
    public ResponseEntity<Resource> get(@PathVariable String crateId) throws FileNotFoundException {

        InputStream zipStream = this.storageClient.get().getCrateInputStream(crateId);

        if (zipStream == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find crate");
        }

        return new ResponseEntity<>(new InputStreamResource(zipStream), HttpStatus.OK);
    }

    @DeleteMapping("/{crateId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String crateId) {
        this.storageClient.get().deleteCrate(crateId);
    }

    @PostMapping()
    public RoCrateDto create(@RequestParam("file") MultipartFile file) throws IOException {

        String crateId = this.storageClient.get().storeCrate(file.getInputStream());

        return new RoCrateDto(crateId);
    }

    @PostMapping("/{crateId}/**")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void update(
            HttpServletRequest request,
            @PathVariable String crateId,
            @RequestParam("file") Optional<MultipartFile> file)
            throws IOException {

        String path = request.getRequestURI().split("/crates/" + crateId + "/")[1];

        InputStream inputStream = file.isPresent() ? file.get().getInputStream() : InputStream.nullInputStream();

        this.storageClient.get().addFile(crateId, inputStream, path);

    }

    @DeleteMapping("/{crateId}/**")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String crateId, HttpServletRequest request) throws IOException {

        String path = request.getRequestURI().split("/crates/" + crateId + "/")[1];

        this.storageClient.get().deleteFile(crateId, path);

    }
}
