/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.cdijmx;

import fr.hhdev.cdijmx.JMXManageable;
import javax.inject.Singleton;

/**
 *
 * @author François Achache
 */
@Singleton
public class JMXServices1 implements JMXManageable, JMXServices1MBean {

	@Override
	public String getValue() {
		return "test1";
	}
}
