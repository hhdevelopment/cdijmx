/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.jmx;

import java.io.File;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author François Achache
 */
@RunWith(Arquillian.class)
public class JMXTest {

	@Resource(lookup = "java:app/AppName")
	private String appName;
	@Resource(lookup = "java:module/ModuleName")
	private String modName;
	@Inject
	private MBeanServer mbs;
	@Inject
	private JMXServicesMBean jMXServices;
	@Inject
	private JMXServices1MBean jMXServices1;

	@Deployment
	public static EnterpriseArchive createEarArchive() {
		File[] libs = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeDependencies().resolve().withTransitivity().asFile();
		EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
				  .addAsLibraries(libs)
				  .addAsModule(createJmxModuleArchive())
				  .addAsModule(createTestArchive());
		System.out.println(ear.toString(true));
		return ear;
	}

	/**
	 * logger est ajouté à l'ear en tant que librairie
	 *
	 * @return
	 */
	public static JavaArchive createJmxModuleArchive() {
		File beans = new File("src/main/resources/META-INF/beans.xml");
		JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "jmx.jar")
				  .addAsManifestResource(new FileAsset(beans), ArchivePaths.create("beans.xml"))
				  .addPackages(true, "hhf.jmx");
		System.out.println(jar.toString(true));
		return jar;
	}

	/**
	 * Les classes de tests sont ajoutées à l'ear comme module ejb, car la classe doit être managé
	 *
	 * @return
	 */
	public static JavaArchive createTestArchive() {
		File logback = new File("src/test/resources/logback-test.xml");
		File beans = new File("src/main/resources/META-INF/beans.xml");
		JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "test.jar")
				  .addAsResource(new FileAsset(logback), ArchivePaths.create("logback-test.xml"))
				  .addAsManifestResource(new FileAsset(beans), ArchivePaths.create("beans.xml"))
				  .addPackages(true, "test.jmx");
		System.out.println(jar.toString(true));
		return jar;
	}

	@Test
	public void testJMXService() {
		try {
			ObjectName name = new ObjectName(appName + ":type=" + JMXServicesMBean.class.getSimpleName());
			String value = jMXServices.getValue();
			String result = (String) mbs.getAttribute(name, "Value");
			Assert.assertEquals(value, result);
		} catch (Exception ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void testJMXService1() {
		try {
			ObjectName name = new ObjectName(appName + ":type=" + JMXServices1MBean.class.getSimpleName());
			String value = jMXServices1.getValue();
			String result = (String) mbs.getAttribute(name, "Value");
			Assert.assertEquals(value, result);
		} catch (Exception ex) {
			Assert.fail(ex.getMessage());
		}
	}

	private void showMBeanInfo(ObjectName name) throws InstanceNotFoundException, IntrospectionException, ReflectionException {
		MBeanInfo mBeanInfo = mbs.getMBeanInfo(name);
		for (MBeanAttributeInfo att : mBeanInfo.getAttributes()) {
			System.out.println(" = Attribute : " + att.getName());
		}
		for (MBeanOperationInfo op : mBeanInfo.getOperations()) {
			System.out.println(" = Operation : " + op.getName());
		}
	}
}
