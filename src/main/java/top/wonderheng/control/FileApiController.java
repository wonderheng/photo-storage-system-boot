package top.wonderheng.control;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.wonderheng.common.Binary;
import top.wonderheng.domain.SmallFile;
import top.wonderheng.service.FileService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @BelongsProject: photo-storage-system-boot
 * @BelongsPackage: top.wonderheng.control
 * @Author: WonderHeng
 * @CreateTime: 2018-12-27 20:05
 */
@RestController
@CrossOrigin(origins = "*",maxAge = 3306)
@RequestMapping(value = "/api")
public class FileApiController {

    @Autowired
    private FileService fileService;

    private Logger logger = LoggerFactory.getLogger(FileApiController.class);

    @RequestMapping(value = "delete", method = {RequestMethod.GET,RequestMethod.DELETE}, produces = {"application/json"})
    public Map<String, Object> delete(
            @RequestParam(value = "id") String id) {
        HashMap<String, Object> result = new HashMap<>();
        try {

            fileService.removeFile(id);
            result.put("code", 200);
            result.put("data", true);
            result.put("message", "");
        } catch (Exception e) {
            logger.error("Delete occur {} .", e.getMessage());
            result.put("data", false);
            result.put("message", "Delete failed");
        }
        return result;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Map<String, Object> query(@PathVariable(value = "id") String id) {
        HashMap<String, Object> result = new HashMap<>();
        Optional<SmallFile> optional = fileService.getFileById(id);
        if (optional.isPresent()) {
            SmallFile smallFile = optional.get();
            Map<String, Object> item = new HashMap<>();
            item.put("id", smallFile.getId());
            item.put("contentType", smallFile.getContentType());
            item.put("name", smallFile.getName());
            item.put("size", smallFile.getSize());
            item.put("md5", smallFile.getMd5());
            item.put("uploadDate", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(smallFile.getUploadDate()));
            result.put("code", 200);
            result.put("data", item);
        } else {
            result.put("message", "Query filed");
        }
        return result;
    }


    @RequestMapping(value = "query", method = RequestMethod.GET, produces = {"application/json"})
    public Map<String, Object> query(
            @RequestParam(value = "pageIndex", defaultValue = "1") int pageIndex,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        HashMap<String, Object> result = new HashMap<>();
        try {
            //http://localhost:80/api/query?pageIndex=xx&pageSize=xx
            List<SmallFile> smallFileList = fileService.listFilesByPage(
                    pageIndex, pageSize
            );
            System.out.println("-------" + smallFileList);
            List<Map<String, Object>> data = new ArrayList<>();
            for (SmallFile smallFile : smallFileList) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", smallFile.getId());
                item.put("contentType", smallFile.getContentType());
                item.put("name", smallFile.getName());
                item.put("size", smallFile.getSize());
                item.put("md5", smallFile.getMd5());
                item.put("uploadDate", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(smallFile.getUploadDate()));
                data.add(item);
            }
            result.put("code", 200);
            result.put("data", data);
        } catch (Exception e) {
            logger.error("Query by page occur {} .", e.getMessage());
            result.put("message", "Query filed");
        }
        return result;
    }


    @RequestMapping(value = "upload", method = RequestMethod.POST, produces = {"application/json"})
    public Map<String, Object> upload(HttpServletRequest req) {
        HashMap<String, Object> result = new HashMap<>();
        try {
            Part part = req.getPart("filename");
            SmallFile smallFile = new SmallFile();
            smallFile.setName(part.getSubmittedFileName());
            smallFile.setContentType(part.getContentType());
            try (InputStream is = part.getInputStream()) {
                smallFile.setContent(new Binary(IOUtils.toByteArray(is)));
            }
            smallFile = fileService.saveFile(smallFile);
            //http://localhost:80/view/file?id=xxxx
            result.put("code", 200);
            result.put("data", String.format("%s://39.107.67.167:%d/api/show/%s",
                    req.getScheme(),
                    req.getLocalPort(),
                    smallFile.getId()));

        } catch (Exception e) {
            logger.error("Upload occur {} .", e.getMessage());
            result.put("message", "Upload filed");
        }
        return result;
    }

    @RequestMapping(value = "/show/{id}", method = RequestMethod.GET)
    public void show(@PathVariable(value = "id") String id, HttpServletResponse response) {
        Optional<SmallFile> optional = fileService.getFileById(id);
        if (optional.isPresent()) {
            SmallFile smallFile = optional.get();
            byte[] content = smallFile.getContent().getData();
            try {
                response.setContentType(smallFile.getContentType());
                response.getOutputStream().write(content);
                response.flushBuffer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
