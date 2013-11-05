/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.jmx.core;

import fr.hhdev.jmx.JMXManageable;
import fr.hhdev.logger.LoggerName;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.slf4j.Logger;

/**
 *
 * @author François Achache
 */
@Singleton
@Startup
public class JMXRegister {

	@Inject
	@Any
	private Instance<JMXManageable> jmxms;
	@Inject
	@LoggerName("JMX")
	private Logger logger;
	@Inject
	@Default
	private MBeanServer mbs;
	@Resource(lookup = "java:app/AppName")
	private String appName;

	@PostConstruct
	protected void postconstruct() {
		for (JMXManageable jmxm : jmxms) {
			try {
				record(jmxm);
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
	}

	public void record(JMXManageable jmxm) throws Exception {
		Object obj = jmxm;
		Class interfs[] = obj.getClass().getInterfaces();
		for (Class interf : interfs) {
			if (interf.getSimpleName().endsWith("MBean") || interf.getSimpleName().endsWith("MXBean")) {
				String mBeanName = appName + ":type=" + interf.getSimpleName();
				logger.info("Registration of " + mBeanName);
				try {
					ObjectName objectName = new ObjectName(mBeanName);
					Object annoted = new AnnotatedStandardMBean(obj, interf);
					if (!mbs.isRegistered(objectName)) {
						mbs.registerMBean(annoted, objectName);
					}
				} catch (Exception e) {
					throw new IllegalStateException("Problem during registration of MBean : " + mBeanName, e);
				}
			}
		}
	}

	@PreDestroy
	protected void predestroy() {
		for (JMXManageable jmxm : jmxms) {
			try {
				unregisterFromJMX(jmxm);
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
	}

	public void unregisterFromJMX(JMXManageable jmxm) {
		Object obj = jmxm;
		Class interfs[] = obj.getClass().getInterfaces();
		for (Class interf : interfs) {
			if (interf.getSimpleName().endsWith("MBean") || interf.getSimpleName().endsWith("MXBean")) {
				String mBeanName = appName + ":type=" + interf.getSimpleName();
				logger.info("Unregistration of " + mBeanName);
				try {
					ObjectName objectName = new ObjectName(mBeanName);
					if (mbs.isRegistered(objectName)) {
						mbs.unregisterMBean(objectName);
					}
				} catch (Exception e) {
					throw new IllegalStateException("Problem during unregistration of MBean : " + mBeanName, e);
				}
			}
		}
	}
}
