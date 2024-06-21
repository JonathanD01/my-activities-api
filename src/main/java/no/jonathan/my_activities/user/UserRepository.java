package no.jonathan.my_activities.user;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByEmail(String username);

    boolean existsByEmail(String username);
}
