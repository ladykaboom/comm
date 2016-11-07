package Messanger.Service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import Messanger.Model.User;
import Messanger.Repository.UserRepositoryImpl;

//FIXME not used yet
@Service
@Transactional
public class DataService {

	@Autowired
	private UserRepositoryImpl userRepository;
	
	@RequestMapping("/db")
    @ResponseBody
    public static String testMethod() {
		System.out.println("Testmethods starting...");
        StringBuilder response = new StringBuilder();
 
        User user = new User()
        		.withId(1)
        		.withName("Name1")
        		.withNick("Nick1")
        		.withNumber("1234")
        		.withStatus("Niedostepny");
        
        System.out.println("User = " + user);
        System.out.println("User id = " + user.getId());
//        System.out.println("Userrepository = " + userRepository);
//        userRepository.save(user);
// 
//        for(User i: userRepository.findAll()) {
//            response.append(i).append("<br>");
//        }
//        
//		System.out.println("Testmethods ended...");
        return response.toString();
    }

	public UserRepositoryImpl getUserRepository() {
		return userRepository;
	}

	public void setUserRepository(UserRepositoryImpl userRepository) {
		this.userRepository = userRepository;
	}
	
	
}
