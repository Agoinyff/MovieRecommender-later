package com.rcd.movierecommender.backend.mapper;

import com.rcd.movierecommender.backend.entity.UserEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT id, username, password_hash AS passwordHash, role, status, created_at AS createdAt, last_login_at AS lastLoginAt FROM app_users WHERE username = #{username}")
    UserEntity findByUsername(@Param("username") String username);

    @Select("SELECT id, username, password_hash AS passwordHash, role, status, created_at AS createdAt, last_login_at AS lastLoginAt FROM app_users WHERE id = #{id}")
    UserEntity findById(@Param("id") Long id);

    @Select("SELECT GREATEST(COALESCE((SELECT MAX(userID) FROM movie_preferences), 9999999), COALESCE((SELECT MAX(id) FROM app_users), 9999999)) + 1")
    Long nextUserId();

    @Insert("INSERT INTO app_users (id, username, password_hash, role, status, created_at) VALUES (#{id}, #{username}, #{passwordHash}, #{role}, #{status}, #{createdAt})")
    int insert(UserEntity user);

    @Update("UPDATE app_users SET last_login_at = #{lastLoginAt} WHERE id = #{id}")
    int updateLastLogin(@Param("id") Long id, @Param("lastLoginAt") LocalDateTime lastLoginAt);

    @Select("SELECT COUNT(*) FROM app_users")
    long countUsers();

    @Select("SELECT id, username, password_hash AS passwordHash, role, status, created_at AS createdAt, last_login_at AS lastLoginAt FROM app_users ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}")
    List<UserEntity> findUsers(@Param("limit") int limit, @Param("offset") int offset);
}
