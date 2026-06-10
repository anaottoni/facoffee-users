package facoffe.users.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import facoffe.users.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByEmail(String email);
    Boolean existsByEmail(String email);
}
