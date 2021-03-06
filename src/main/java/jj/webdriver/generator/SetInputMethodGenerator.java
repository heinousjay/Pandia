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
package jj.webdriver.generator;

import java.util.regex.Pattern;

import javax.inject.Singleton;

import javassist.CtMethod;
import jj.webdriver.panel.PanelMethodGenerator;

/**
 * @author jason
 *
 */
@Singleton
class SetInputMethodGenerator extends PanelMethodGenerator {
	
	private static final Pattern NAME = makeNamePattern("set");

	@Override
	protected boolean matches(CtMethod newMethod, CtMethod baseMethod) throws Exception {
		return NAME.matcher(newMethod.getName()).find() &&
			newMethod.getParameterTypes().length >= 1 &&
			newMethod.getParameterTypes()[0].getName().equals("java.lang.String") &&
			parametersMatchByAnnotation(1, newMethod, baseMethod) &&
			isStandardReturn(newMethod);
	}
	
	@Override
	protected int sliceAt() {
		return 1;
	}
	
	@Override
	protected void generate(CtMethod newMethod, CtMethod baseMethod, StringBuilder sb) throws Exception {
		sb.append("set(").append(LOCAL_BY).append(", $1);");
	}

}
