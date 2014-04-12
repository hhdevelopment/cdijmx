/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.jmx.core;

import fr.hhdev.jmx.JMXManageable;
import fr.hhdev.logger.LoggerName;
import java.lang.management.ManagementFactory;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
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

	private final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	@Resource(lookup = "java:app/AppName")
	private String appName;

	@PostConstruct
	protected void postconstruct() {
		for (JMXManageable jmxm : jmxms) {
			record(jmxm);
		}
	}

	@PreDestroy
	protected void predestroy() {
		for (JMXManageable jmxm : jmxms) {
			unrecord(jmxm);
		}
	}

	protected void record(JMXManageable jmxm) {
		Class interfs[] = jmxm.getClass().getInterfaces();
		for (Class interf : interfs) {
			if (isMXBean(interf)) {
				recordInterface(jmxm, interf);
			}
		}
	}

	private void recordInterface(Object obj, Class interf) {
		String mBeanName = appName + ":type=" + interf.getSimpleName();
		logger.info("Registration of " + mBeanName);
		try {
			ObjectName objectName = new ObjectName(mBeanName);
			if (!mbs.isRegistered(objectName)) {
				mbs.registerMBean(new AnnotatedStandardMBean(obj, interf), objectName);
			}
		} catch (InstanceAlreadyExistsException e) {
			logger.error("Problem during registration of MBean : " + mBeanName, e);
		} catch (MBeanRegistrationException e) {
			logger.error("Problem during registration of MBean : " + mBeanName, e);
		} catch (MalformedObjectNameException e) {
			logger.error("Problem during registration of MBean : " + mBeanName, e);
		} catch (NotCompliantMBeanException e) {
			logger.error("Problem during registration of MBean : " + mBeanName, e);
		}
	}

	protected void unrecord(JMXManageable jmxm) {
		Class interfs[] = jmxm.getClass().getInterfaces();
		for (Class interf : interfs) {
			if (isMXBean(interf)) {
				unrecordInterface(interf);
			}
		}
	}

	private void unrecordInterface(Class interf) {
		String mBeanName = appName + ":type=" + interf.getSimpleName();
		logger.info("Unregistration of " + mBeanName);
		try {
			ObjectName objectName = new ObjectName(mBeanName);
			if (mbs.isRegistered(objectName)) {
				mbs.unregisterMBean(objectName);
			}
		} catch (InstanceNotFoundException e) {
			logger.error("Problem during unregistration of MBean : " + mBeanName, e);
		} catch (MBeanRegistrationException e) {
			logger.error("Problem during unregistration of MBean : " + mBeanName, e);
		} catch (MalformedObjectNameException e) {
			logger.error("Problem during unregistration of MBean : " + mBeanName, e);
		}
	}
	
	/**
	 * Verifie que l'interface finit bien par MBean ou MXBean
	 * @param interf
	 * @return 
	 */
	private boolean isMXBean(Class interf) {
		return interf.getSimpleName().endsWith("MBean") || interf.getSimpleName().endsWith("MXBean");
	}
}
