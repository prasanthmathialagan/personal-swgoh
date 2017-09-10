package org.prasanth.swgoh;


import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Hello world!
 */
public class App {
	public static void main(String[] args) throws Exception {
		ApplicationContext appContext =
				new ClassPathXmlApplicationContext("BeanLocations.xml");

		Controller controller = (Controller) appContext.getBean("controller");
		controller.reconcileUsers();
	}
}
