package Messanger.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import Messanger.Service.DataService;

//FIXME not used yet
@Controller
public class DataController {

	@Autowired
	private DataService dataService;

	@RequestMapping(value = "/db", method = RequestMethod.GET)
	public @ResponseBody String getInvoices() {
		return dataService.testMethod();
	}

}
