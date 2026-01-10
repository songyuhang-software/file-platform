package file.platform.service;

import java.io.File;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public interface FileHostingService {

    String uploadImage(File file) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException;

    boolean deleteFile(String fileName);

}


