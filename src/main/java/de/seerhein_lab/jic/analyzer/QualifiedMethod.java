package de.seerhein_lab.jic.analyzer;

import java.util.HashSet;
import java.util.Set;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import de.seerhein_lab.jic.DetailedClass;

public final class QualifiedMethod {
	private final JavaClass clazz;
	private final Method method;
	private final Set<QualifiedMethod> callingMethods = new HashSet<QualifiedMethod>();

	public QualifiedMethod(JavaClass clazz, Method method) {
		if (clazz == null || method == null) {
			throw new NullPointerException("params must not be null");
		}

		this.clazz = clazz;
		this.method = method;
	}

	public JavaClass getJavaClass() {
		return clazz;
	}

	public Method getMethod() {
		return method;
	}

	public Set<QualifiedMethod> getCallingMethods() {
		return callingMethods;
	}

	public void addCallingMethod(QualifiedMethod method) {
		this.callingMethods.add(method);
	}

	public Set<QualifiedMethod> getCallingMethodsWithInstantiation(DetailedClass clazz) {
		Set<QualifiedMethod> intersection = new HashSet<QualifiedMethod>(clazz.getInstantiations());
		Set<QualifiedMethod> result = new HashSet<QualifiedMethod>();
		for (QualifiedMethod method : callingMethods) {
			if (intersection.contains(method))
				result.add(method);
			else
				result.addAll(method.getCallingMethodsWithInstantiation(clazz));
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return clazz.getClassName() + "." + method.getName();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof QualifiedMethod))
			return false;

		QualifiedMethod other = (QualifiedMethod) obj;

		if (!clazz.equals(other.clazz))
			return false;

		return method.equals(other.method);
	}

}
