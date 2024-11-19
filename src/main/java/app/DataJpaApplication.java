package app;

import app.models.service.IUploadFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Si queremos eliminar todas las imagenes al cargar la aplicacion descomentamos las siguientes lineas
// y tambien descomentamos las lineas que estan en UploadFileServiceImp y IUploadFileService

@SpringBootApplication
public class DataJpaApplication { //implements CommandLineRunner

//    @Autowired
//    private IUploadFileService uploadFileService;

    public static void main(String[] args) {
        SpringApplication.run(DataJpaApplication.class, args);
    }

//    @Override
//    public void run(String... args) throws Exception {
//        uploadFileService.deleteAll();
//        uploadFileService.init();
//    }

}
