package file.platform.service;

import java.io.File;

public interface FileHostingService {

    String uploadImage(File file);

    boolean deleteFile(String fileName);

}


