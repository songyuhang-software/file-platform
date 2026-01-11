package file.platform.dao;

import file.platform.entity.DefaultAvatar;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DefaultAvatarMapper {

    void insert(@Param("defaultAvatar") DefaultAvatar defaultAvatar);

    DefaultAvatar selectById(@Param("id") Long id);

    DefaultAvatar selectByAvatarUrl(@Param("avatarUrl") String avatarUrl);

    List<DefaultAvatar> selectRandomByGenderAndExcludes(
            @Param("gender") Integer gender,
            @Param("excludeIds") List<Long> excludeIds);

    int countByGender(@Param("gender") Integer gender);

}