package com.mathlit.backend.repository;

import com.mathlit.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByFirebaseUid(String firebaseUid);

    @Query("SELECT u FROM User u ORDER BY u.totalScore DESC LIMIT :limit")
    List<User> findTopByTotalScore(int limit);
}
