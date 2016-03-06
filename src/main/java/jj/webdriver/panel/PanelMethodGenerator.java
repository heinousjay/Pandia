/*
 *    Copyright 2012 Jason Miller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jj.webdriver.panel;

import java.util.Arrays;
import java.util.regex.Pattern;

import javassist.CtClass;
import javassist.CtMethod;
import jj.webdriver.By;
import jj.webdriver.Page;
import jj.webdriver.Panel;

/**
 * Base helper for generating page object methods. Works in terms of a generated
 * method implementation, a method from an interface driving the configuration,
 * and the code generated to create the implementation.  Needs to be ByteBuddy!
 * 
 * @author jason
 *
 */
public abstract class PanelMethodGenerator {
	
	/**
	 * The default name for the local variable for the rendered {@link org.openqa.selenium.By}
	 */
	protected static final String LOCAL_BY = "localBy";

	/**
	 * Convenience to compile a {@link Pattern} for a method name that matches
	 * the given argument followed by one of an uppercase letter, a digit, or a '$'
	 *
	 * @param name the method name
	 *
	 * @return the compiled {@link Pattern}
	 */
	protected static final Pattern makeNamePattern(String name) {
		return Pattern.compile("^" + name + "[\\p{javaUpperCase}\\d_\\$]");
	}

	/**
	 * determine if the given method can be generated. immediately thereafter,
	 * {@link #generateMethod(CtMethod, CtMethod)} will be called
	 *
	 * @param newMethod
	 * the method implementation being generated
	 * @param baseMethod
	 * the method declaration from the configuration interface
	 *
	 * @return true if the method can be generated
	 *
	 * @throws Exception if anything goes wrong
	 */
	protected abstract boolean matches(CtMethod newMethod, CtMethod baseMethod) throws Exception;
	
	/**
	 * <p>
	 * Override to provide custom generation that fits into the template provided by the supplied
	 * implementation of {@link #generateMethod(CtMethod, CtMethod)}
	 * 
	 * <p>
	 * This method does nothing by default
	 *
	 * @param newMethod
	 * the method implementation being generated
	 * @param baseMethod
	 * the method declaration from the configuration interface
	 * @param sb
	 * the {@link StringBuilder} accumulating the implementation
	 *
	 * @throws Exception if anything goes wrong
	 */
	protected void generate(CtMethod newMethod, CtMethod baseMethod, StringBuilder sb) throws Exception {
		// does nothing by default, since you can override generateMethod and do it all yourself
	}
	
	/**
	 * Called to produce a method body if the pattern matches.  The method can be overriden if
	 * necessary.
	 * 
	 * <p>
	 * The default implementation of this method takes the following steps:
	 * <ol>
	 * <li>process the By annotation, slicing args using the result of {@link #sliceAt()}
	 * <li>call {@link #generate(CtMethod, CtMethod, StringBuilder)}
	 * <li>call {@link #generateReturn(CtMethod, CtMethod, StringBuilder)}
	 * <li>call {@link #setBody(CtMethod, StringBuilder)}
	 * </ol>
	 *
	 * @param newMethod
	 * the method implementation being generated
	 * @param baseMethod
	 * the method declaration from the configuration interface
	 *
	 * @throws Exception if anything goes wrong
	 */
	protected void generateMethod(CtMethod newMethod, CtMethod baseMethod) throws Exception {
		StringBuilder sb = new StringBuilder("{");
		processBy((By)baseMethod.getAnnotation(By.class), sliceAt(), sb);
		generate(newMethod, baseMethod, sb);
		generateReturn(newMethod, baseMethod, sb);
		sb.append("}");
		
		setBody(newMethod, sb);
	}
	
	/**
	 * <p>
	 * determines if the given method is annotated with a valid {@link By}
	 * 
	 * <p>
	 * This method throws an AssertionError if an invalid annotation is present.
	 *
	 * @param baseMethod
	 * the method declaration from the configuration interface
	 *
	 * @return true if the method has a {@link By} annotation
	 *
	 * @throws Exception if anything goes wrong
	 */
	protected boolean hasBy(CtMethod baseMethod) throws Exception {
		// compares to null, but is actually relying on an exception being thrown
		return baseMethod.hasAnnotation(By.class) && new ByReader((By)baseMethod.getAnnotation(By.class)) != null;
	}
	
	private static final String PAGE_CLASS_NAME = Page.class.getName();
	private static final String PANEL_CLASS_NAME = Panel.class.getName();
	
	private boolean hasInterface(CtClass type, String nameToCheck) throws Exception {
		boolean result = false;
		for (CtClass iface : type.getInterfaces()) {
			if (nameToCheck.equals(iface.getName()) || hasInterface(iface, nameToCheck)) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	/**
	 * <p>
	 * Determines if the given method has a "standard" return value, which is either void, or
	 * an interface descended from {@link Panel}
	 *
	 * @param newMethod
	 * the method declaration from the configuration interface
	 *
	 * @return true if the method has a "standard" return value
	 *
	 * @throws Exception if anything goes wrong
	 */
	protected final boolean isStandardReturn(CtMethod newMethod) throws Exception {
		
		CtClass returnType = newMethod.getReturnType();
		return returnType.getName().equals("void") || isPanel(returnType);
	}
	
	/**
	 * <p>
	 * Determines if the given type is an interface descending from {@link Panel}
	 *
	 * @param type the interesting type
	 *
	 * @return true if the type descends from {@link Panel}
	 *
	 * @throws Exception if anything goes wrong
	 */
	protected final boolean isPanel(CtClass type) throws Exception {
		return type.isInterface() && hasInterface(type, PANEL_CLASS_NAME);
	}
	
	/**
	 * <p>
	 * Determines if the given type is an interface descending from {@link Page}
	 *
	 * @param type the interesting type
	 *
	 * @return true if the type descends from {@link Page}
	 *
	 * @throws Exception if anything goes wrong
	 */
	protected final boolean isPage(CtClass type) throws Exception {
		return type.isInterface() && hasInterface(type, PAGE_CLASS_NAME);
	}
	
	/**
	 * <p>
	 * in {@link #generateMethod(CtMethod, CtMethod)}, determines the slice index for
	 * arguments to the underlying implementation. arguments after the slice are used
	 * as format args against the ultimate value from the By attribute.
	 *
	 * @return the slice index out of the arguments array
	 */
	protected int sliceAt() {
		return 0;
	}
	
	/**
	 * <p>
	 * Sets the body of the method with the contents of the supplied StringBuffer.  Call
	 * this instead of doing that directly because this will debug log at some point
	 *
	 * @param newMethod
	 * the method implementation being generated
	 * @param sb
	 * the {@link StringBuilder} that holds the implemenation
	 *
	 * @throws Exception if anything goes wrong
	 */
	protected void setBody(CtMethod newMethod, StringBuilder sb) throws Exception {
		// log it! but don't have the right loggers yet
		newMethod.setBody(sb.toString());
	}
	
	/**
	 * <p>
	 * called from {@link #generateMethod(CtMethod, CtMethod)} to create a return statement.
	 * 
	 * <p>
	 * The default implementation generates a "standard" return, which is either void, the
	 * containing page object, or a new instance of some other page object.
	 * 
	 * <p>
	 * this method asserts that the method being generated has a standard return
	 * as defined by {@link #isStandardReturn(CtMethod)}
	 *
	 * @param newMethod
	 * the method implementation being generated
	 * @param baseMethod
	 * the method declaration from the configuration interface
	 * @param sb
	 * the {@link StringBuilder} accumulating the implementation
	 *
	 * @throws Exception if anything goes wrong
	 * 
	 */
	protected void generateReturn(CtMethod newMethod, CtMethod baseMethod, StringBuilder sb) throws Exception {

		assert isStandardReturn(newMethod) : "can only generate standard returns for methods declared with a standard return!";
		
		CtClass newClass = newMethod.getDeclaringClass();
		CtClass returnType = newMethod.getReturnType();

		
		if (newClass.getInterfaces()[0] == returnType) {
			sb.append("return this;");
		} else if (isPanel(returnType)) {
			sb.append("return makePanel(").append(returnType.getName()).append(".class);");
		} else if (isPage(returnType)) {
			sb.append("return navigateTo(").append(returnType.getName()).append(".class);");
		}
	}
	
	/**
	 * process the {@link By} annotation into a local variable named by {@link #LOCAL_BY},
	 * using {@link #processBy(By, String, int, StringBuilder)}
	 *
	 * @param by the annotation being processed
	 * @param sliceArgs the index at which to slice format args
	 * @param sb the implementation accumulator
	 */
	protected final void processBy(By by, int sliceArgs, StringBuilder sb) {
		processBy(by, LOCAL_BY, sliceArgs, sb);
	}
	
	/**
	 * processes the {@link By} annotation, if present, into a {@link org.openqa.selenium.By} store in
	 * the named local variable.  This involves the following steps:
	 * 
	 * <ol>
	 * <li>slice the method arguments at the index provided by sliceArgs, using {@link Arrays#copyOfRange(Object[], int, int)}
	 * <li>create a {@link org.openqa.selenium.By} using the appropriate type according to {@link ByReader}.
	 *     if needed, this includes using {@link String#format(String, Object...)} on the annotated value,
	 *     and resolving the {@link ByStack}.
	 * </ol>
	 *
	 * @param by the annotation being processed
	 * @param varName the name of the local variable for the by instance
	 * @param sliceArgs the index at which to slice format args
	 * @param sb the implementation accumulator
	 */
	protected final void processBy(By by, String varName, int sliceArgs, StringBuilder sb) {
		
		if (by != null) {
			ByReader br = new ByReader(by);
			
			// args might get sliced for an implementation
			if (sliceArgs > -1) {
				sb.append("Object[] slicedArgs = java.util.Arrays.copyOfRange($args, ").append(sliceArgs).append(", $args.length);");
			}
			
			sb.append("org.openqa.selenium.By ").append(varName).append(" = org.openqa.selenium.By.");
			sb.append(br.type()).append("(");

			if (br.needsResolution()) {
				sb.append("byStack.resolve(");
			}
			
			if (sliceArgs > -1) {
				sb.append("String.format(");
			}
			
			sb.append("\"").append(br.value()).append("\"");
			
			if (sliceArgs > -1) {
				sb.append(", slicedArgs)");
			}
			
			if (br.needsResolution()) {
				sb.append(")");
			}
			
			sb.append(");");
		}
	}
	
	/**
	 * Helper to determine if a String is empty.
	 *
	 * @param string to check
	 * @return true if empty
	 */
	protected final boolean empty(String string) {
		return string == null || string.isEmpty();
	}

	/**
	 * <p>
	 * A match helper to determine if the declared arguments sliced at the given slice index are
	 * valid for use as format arguments with the value of the {@link By} annotation.
	 * 
	 * <p>
	 * This method only accepts <code>String</code> and <code>int</code> arguments for simplicity, although
	 * the actual format call may allow other arguments
	 *
	 * @param sliceAt
	 * the index at which to slice the format args
	 * @param newMethod
	 * the method implementation being generated
	 * @param baseMethod
	 * the method declaration from the configuration interface
	 *
	 * @return true if the parameters match the supplied value
	 *
	 * @throws Exception if anything goes wrong
	 */
	protected final boolean parametersMatchByAnnotation(int sliceAt, CtMethod newMethod, CtMethod baseMethod) throws Exception {
		
		By by = (By)baseMethod.getAnnotation(By.class);
		
		int length = baseMethod.getParameterTypes().length;
		CtClass[] params = Arrays.copyOfRange(baseMethod.getParameterTypes(), sliceAt, length);
		Object[] args = new Object[params.length];
		

		boolean result = args.length == 0 || (by != null && args.length > 0);
		
		int index = 0;
		for (CtClass type : params) {
			if ("java.lang.String".equals(type.getName())) {
				args[index++] = " ";
			} else if ("int".equals(type.getName())) {
				args[index++] = 0;
			} else {
				System.out.println("unknown type! " + type.getName());
				result = false;
			}
		}
		
		result = result && (by == null || new ByReader(by).validateValueAsFormatterFor(args));
		
		return result;
	}
}
