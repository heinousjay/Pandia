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
package jj.webdriver;

import java.util.regex.Pattern;

import javax.inject.Singleton;

import javassist.CtField;
import javassist.CtMethod;

/**
 * @author jason
 *
 */
@Singleton
public class SetModelMethodGenerator extends PanelMethodGenerator {
	
	private static final Pattern NAME = makeNamePattern("set");

	@Override
	protected boolean matches(CtMethod newMethod, CtMethod baseMethod) throws Exception {
		return NAME.matcher(newMethod.getName()).find() &&
			isStandardReturn(newMethod) &&
			newMethod.getParameterTypes().length == 1 &&
			newMethod.getParameterTypes()[0].getAnnotation(Model.class) != null;
	}

	@Override
	protected void generate(CtMethod newMethod, CtMethod baseMethod) throws Exception {
		StringBuilder sb = new StringBuilder("{");
		
		for (CtField field : newMethod.getParameterTypes()[0].getFields()) {
			sb.append("set(org.openqa.selenium.By.id(\"").append(field.getName()).append("\"), $1.").append(field.getName()).append(");");
		}
		
		sb.append("java.lang.System.out.println(($1).getClass());");
		generateStandardReturn(newMethod, sb);
		sb.append("}");
		
		newMethod.setBody(sb.toString());
	}
}
