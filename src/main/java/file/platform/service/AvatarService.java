package file.platform.service;

import java.util.List;

public interface AvatarService {

    String uploadDefaultAvatar(byte[] imageBytes, Integer recommendedGender);

    String getRandomAvatar(String gender, List<Long> excludeIds);

}



