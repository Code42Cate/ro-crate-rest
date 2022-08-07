package edu.kit.datamanager.rocraterest.interceptors;

import java.util.Map;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.reader.RoCrateReader;
import edu.kit.datamanager.ro_crate.reader.ZipReader;
import edu.kit.datamanager.ro_crate.writer.RoCrateWriter;
import edu.kit.datamanager.ro_crate.writer.ZipWriter;
import edu.kit.datamanager.rocraterest.storage.LocalStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CrateInterceptor implements HandlerInterceptor {

    private final LocalStorageService storageService = new LocalStorageService();

    private String parseCrateId(HttpServletRequest request) {
        Map<String, String> pathVariables = (Map<String, String>) request
                .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        return pathVariables.get("crateId");
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
            final Object handler) {

        String crateId = this.parseCrateId(request);
        if (crateId == null) {
            return true;
        }

        System.out.println(crateId);
        RoCrateReader roCrateFolderReader = new RoCrateReader(new ZipReader());
        RoCrate crate = roCrateFolderReader.readCrate(storageService.path(crateId));

        request.setAttribute("crate", crate);
        request.setAttribute("_crate", crate);

        return true;
    }

    @Override
    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response,
            final Object handler,
            final Exception ex) throws JsonMappingException, JsonProcessingException {

        String crateId = this.parseCrateId(request);
        if (crateId == null) {
            return;
        }

        RoCrate originalCrate = (RoCrate) request.getAttribute("_crate");
        RoCrate updatedCrate = (RoCrate) request.getAttribute("crate");

        JsonNode originalJson = new ObjectMapper().readTree((originalCrate).getJsonMetadata());
        JsonNode updatedJson = new ObjectMapper().readTree(updatedCrate.getJsonMetadata());

        if (!originalJson.equals(updatedJson)) {
            RoCrateWriter roCrateZipWriter = new RoCrateWriter(new ZipWriter());
            roCrateZipWriter.save(updatedCrate, this.storageService.path(crateId));
        }

    }

}
