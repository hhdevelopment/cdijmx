/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.jmx;

import fr.hhdev.jmx.JMXManageable;
import javax.ejb.Singleton;

/**
 *
 * @author François Achache
 */
@Singleton
public class JMXServices implements JMXManageable, JMXServicesMBean {

	@Override
	public String getValue() {
		return "test";
	}
}
