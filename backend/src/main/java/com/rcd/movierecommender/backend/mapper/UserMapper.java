package com.rcd.movierecommender.backend.mapper;

import com.rcd.movierecommender.backend.entity.UserEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("SELECT id, username, password_hash AS passwordHash, display_name AS displayName, role, created_at AS createdAt, updated_at AS updatedAt FROM app_user WHERE username = #{username}")
    UserEntity findByUsername(@Param("username") String username);

    @Select("SELECT id, username, password_hash AS passwordHash, display_name AS displayName, role, created_at AS createdAt, updated_at AS updatedAt FROM app_user WHERE id = #{id}")
    UserEntity findById(@Param("id") Long id);

    @Insert("INSERT INTO app_user (username, password_hash, display_name, role, created_at, updated_at) VALUES (#{username}, #{passwordHash}, #{displayName}, #{role}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserEntity user);
}
