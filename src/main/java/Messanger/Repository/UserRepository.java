package Messanger.Repository;

import org.springframework.data.repository.CrudRepository;

import Messanger.Model.User;

public interface UserRepository extends CrudRepository<User, Long> {

}
