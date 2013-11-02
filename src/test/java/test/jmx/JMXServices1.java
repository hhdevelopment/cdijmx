/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.jmx;

import hhf.jmx.JMXManageable;
import javax.ejb.Singleton;

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
