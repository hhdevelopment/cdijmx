/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.cdijmx.core;

import fr.hhdev.cdijmx.annotations.JMXArgument;
import fr.hhdev.cdijmx.annotations.JMXDescription;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

/**
 *
 * @author François From http://weblogs.java.net/blog/emcmanus/archive/2005/07/adding_informat.html
 */
public class AnnotatedStandardMBean extends StandardMBean {

	private static final Map<String, Class<?>> primitiveClasses = new HashMap<String, Class<?>>();

	/**
	 * Instance where the MBean interface is implemented by another object.
	 */
	public <T> AnnotatedStandardMBean(T impl, Class<T> mbeanInterface) throws NotCompliantMBeanException {
		super(impl, mbeanInterface);
	}

	/**
	 * Instance where the MBean interface is implemented by this object.
	 */
	protected AnnotatedStandardMBean(Class<?> mbeanInterface) throws NotCompliantMBeanException {
		super(mbeanInterface);
	}

	static Class<?> classForName(String name, ClassLoader loader) throws ClassNotFoundException {
		Class<?> c = primitiveClasses.get(name);
		if (c == null) {
			c = Class.forName(name, false, loader);
		}
		return c;
	}

	static {
		Class<?>[] prims = {byte.class, short.class, int.class, long.class, float.class, double.class, char.class, boolean.class};
		for (Class<?> c : prims) {
			primitiveClasses.put(c.getName(), c);
		}
	}

	private static Method methodFor(Class<?> mbeanInterface, MBeanOperationInfo op) {
		final MBeanParameterInfo[] params = op.getSignature();
		final String[] paramTypes = new String[params.length];
		for (int i = 0; i < params.length; i++) {
			paramTypes[i] = params[i].getType();
		}
		return findMethod(mbeanInterface, op.getName(), paramTypes);
	}

	private static Method findMethod(Class<?> mbeanInterface, String name, String... paramTypes) {
		try {
			final ClassLoader loader = mbeanInterface.getClassLoader();
			final Class<?>[] paramClasses = new Class<?>[paramTypes.length];
			for (int i = 0; i < paramTypes.length; i++) {
				paramClasses[i] = classForName(paramTypes[i], loader);
			}
			return mbeanInterface.getMethod(name, paramClasses);
		} catch (RuntimeException e) {
			// avoid accidentally catching unexpected runtime exceptions
			throw e;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	protected String getDescription(MBeanOperationInfo op) {
		String descr = op.getDescription();
		Method m = methodFor(getMBeanInterface(), op);
		if (m != null) {
			JMXDescription d = m.getAnnotation(JMXDescription.class);
			if (d != null) {
				descr = d.value();
			}
		}
		return descr;
	}

	static <A extends Annotation> A getParameterAnnotation(Method m, int paramNo, Class<A> annot) {
		for (Annotation a : m.getParameterAnnotations()[paramNo]) {
			if (annot.isInstance(a)) {
				return annot.cast(a);
			}
		}
		return null;
	}

	@Override
	protected String getParameterName(MBeanOperationInfo op, MBeanParameterInfo param, int paramNo) {
		String name = param.getName();
		Method m = methodFor(getMBeanInterface(), op);
		if (m != null) {
			JMXArgument pname = getParameterAnnotation(m, paramNo, JMXArgument.class);
			if (pname != null) {
				name = pname.value();
			}
		}
		return name;
	}
}
