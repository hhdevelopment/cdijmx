/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hhf.jmx.core;

import java.lang.management.ManagementFactory;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.management.MBeanServer;

/**
 *
 * @author François Achache
 */
public class MBeanServerProducer {

	@Produces
	@Default
	public MBeanServer getMBeanServer() {
		return ManagementFactory.getPlatformMBeanServer();
	}
}
