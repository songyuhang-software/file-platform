package file.platform.service;

import com.upyun.UpException;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public interface FileHostingService {

    String uploadImage(File file) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException;

    boolean deleteFile(String fileName) throws UpException, IOException;

    String getDomain();

}


