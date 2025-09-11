package org.convenient.repository;

import jakarta.transaction.Transactional;
import org.convenient.models.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, String> {

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO user(id, email, password, profile_pic, full_name) VALUES (:id, :email, :password, :profile_pic, :full_name)", nativeQuery = true)
    int registerNewUser(@Param("id") String id,
                        @Param("email") String email,
                        @Param("password") String password,
                        @Param("profile_pic") String profile_pic,
                        @Param("full_name") String full_name);

    @Query(value = "SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) FROM user", nativeQuery = true)
    Integer getMaxNumericId();

    @Query("SELECT COUNT(u) FROM User u WHERE u.email = :email")
    int countByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    User findByEmail(@Param("email") String email);


}
