package file.platform.service;

import file.platform.entity.DefaultAvatar;

import java.util.List;

public interface AvatarService {

    String uploadDefaultAvatar(byte[] imageBytes, Integer recommendedGender);

    DefaultAvatar getRandomAvatar(String gender, List<Long> excludeIds);

    List<DefaultAvatar> getAllAvatars();

}




