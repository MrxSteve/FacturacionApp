package app.models.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UploadFileServiceImp implements IUploadFileService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String UPLOADS_FOLDER = "uploads";

    @Override
    public Resource load(String filename) throws MalformedURLException {
        Path pathFoto = getPath(filename);
        log.info("pathFoto: " + pathFoto);

        Resource recurso;
        try {
            recurso = new UrlResource(pathFoto.toUri());
            if (!recurso.exists() || !recurso.isReadable()) {
                throw new RuntimeException("Error: no se puede leer el archivo " + pathFoto.toString());
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error al cargar la imagen: " + filename, e);
        }

        return recurso;
    }

    @Override
    public String copy(MultipartFile file) throws IOException {
        String uniqueFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path rootPath = getPath(uniqueFilename);
        log.info("rootPath: " + rootPath);

        Files.copy(file.getInputStream(), rootPath);
        return uniqueFilename;
    }

    @Override
    public boolean delete(String filename) {
        Path rootPath = getPath(filename);
        File archivo = rootPath.toFile();

        if (archivo.exists() && archivo.canRead()) {
            if (archivo.delete()) {
                log.info("Archivo eliminado: " + filename);
                return true;
            }
        }
        return false;
    }

//    @Override
//    public void deleteAll() {
//        FileSystemUtils.deleteRecursively(Paths.get(UPLOADS_FOLDER).toFile());
//    }
//
//    @Override
//    public void init() throws IOException {
//        Files.createDirectories(Paths.get(UPLOADS_FOLDER));
//    }

    private Path getPath(String filename) {
        Path uploadsPath = Paths.get(UPLOADS_FOLDER);
        try {
            if (!Files.exists(uploadsPath)) {
                Files.createDirectories(uploadsPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de uploads", e);
        }
        return uploadsPath.resolve(filename).toAbsolutePath();
    }
}
