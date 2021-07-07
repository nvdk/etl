package com.linkedpipes.etl.storage.web.servlet;

import com.linkedpipes.etl.storage.template.TemplateFacade;
import com.linkedpipes.etl.storage.template.plugin.PluginService;
import com.linkedpipes.plugin.loader.PluginJarFile;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@RestController
@RequestMapping(value = "/jars")
public class JarServlet {

    private final TemplateFacade templateFacade;

    @Autowired
    public JarServlet(TemplateFacade templateFacade) {
        this.templateFacade = templateFacade;
    }

    @RequestMapping(value = "/file", method = RequestMethod.GET)
    @ResponseBody
    public void getJarFile(
            @RequestParam(name = "iri") String iri,
            HttpServletResponse response)
            throws IOException, MissingResource {
        PluginJarFile plugin = templateFacade.getPluginJar(iri);
        if (plugin == null) {
            throw new MissingResource("Missing jar file: {}", iri);
        }
        response.setStatus(HttpStatus.SC_OK);
        response.setHeader("Content-Type", "application/octet-stream");
        try (OutputStream stream = response.getOutputStream()) {
            FileUtils.copyFile(plugin.getFile(), stream);
        }
    }

}
