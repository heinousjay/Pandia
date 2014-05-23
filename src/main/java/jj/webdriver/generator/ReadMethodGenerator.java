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
class ReadMethodGenerator extends PanelMethodGenerator {
	
	private static final Pattern NAME = makeNamePattern("read");
	
	@Override
	protected boolean matches(CtMethod newMethod, CtMethod baseMethod) throws Exception {
		return hasBy(baseMethod) &&
			NAME.matcher(newMethod.getName()).find() &&
			parametersMatchByAnnotation(0, newMethod, baseMethod) &&
			newMethod.getReturnType().getName().equals("java.lang.String");
	}
	
	@Override
	protected void generateReturn(CtMethod newMethod, CtMethod baseMethod, StringBuilder sb) throws Exception {
		sb.append("return read(").append(LOCAL_BY).append(");");
	}
	
}
