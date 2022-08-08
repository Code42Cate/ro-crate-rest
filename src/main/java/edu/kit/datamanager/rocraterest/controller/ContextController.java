package edu.kit.datamanager.rocraterest.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import edu.kit.datamanager.ro_crate.RoCrate;

@RestController
public class ContextController {

  static class PairValuePayload {
    public String value;
  }

  @DeleteMapping("/crates/{crateId}/context/pairs/{key}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void deletePair(@PathVariable String crateId, @PathVariable String key, @RequestAttribute RoCrate crate) {

    crate.deleteValuePairFromContext(key);

  }

  @PutMapping("/crates/{crateId}/context/pairs/{key}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void addPair(@PathVariable String crateId, @PathVariable String key,
      @RequestBody PairValuePayload pairValuePayload,
      @RequestAttribute RoCrate crate) {

    crate = new RoCrate.RoCrateBuilder(crate).addValuePairToContext(key, pairValuePayload.value).build();

  }

  @PutMapping("/crates/{crateId}/context/urls/{url}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void addUrl(@PathVariable String crateId, @PathVariable String url, @RequestAttribute RoCrate crate) {

    crate = new RoCrate.RoCrateBuilder(crate).addUrlToContext(URLDecoder.decode(url, StandardCharsets.UTF_8)).build();

  }

  /*
   * @DeleteMapping("/crates/{crateId}/context/urls/{url}")
   * 
   * @ResponseStatus(code = HttpStatus.NO_CONTENT)
   * public void deleteUrl(@PathVariable String crateId, @PathVariable String
   * url, @RequestAttribute RoCrate crate) {
   * 
   * // TODO: Add to ro-crate-java
   * // crate = new
   * // RoCrate.RoCrateBuilder(crate).deleteUrlFromContext(URLDecoder.decode(url,
   * // StandardCharsets.UTF_8))
   * // .build();
   * 
   * }
   */
}
