package edu.kit.datamanager.rocraterest.interceptors;

import java.util.Map;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

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

        return true;
    }

    @Override
    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response,
            final Object handler,
            final Exception ex) {

        String crateId = this.parseCrateId(request);
        if (crateId == null) {
            return;
        }

        RoCrate crate = (RoCrate) request.getAttribute("crate");

        // TODO: Only save when crate has been changed
        RoCrateWriter roCrateZipWriter = new RoCrateWriter(new ZipWriter());
        roCrateZipWriter.save(crate, this.storageService.path(crateId));

    }

}
