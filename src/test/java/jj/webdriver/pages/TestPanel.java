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
package jj.webdriver.pages;

import jj.webdriver.By;
import jj.webdriver.Panel;

/**
 * @author jason
 *
 */
public interface TestPanel extends Panel {
	
	@By("panel-")
	TestPanel setSomeForm(TestModel model);
	
	TestPanel setSameForm(TestModel model);
	
	@By("submit")
	TestPage2 clickFormSubmit();
	
	//@By("panel-")
	//TestModel getSomeForm();
	
	//TestModel getSameForm();
	
	@By("panel-%s[%d]-")
	TestPanel setAnotherForm(TestModel model, String group, int index);
	// is this better?  honestly can't decide
	//TestPanel setAnotherForm(String group, int index, TestModel model);
	
	@By("submit-%s[%d]")
	TestPage2 clickFormSubmit(String group, int index);
	
	//@By("panel-%s%d-")
	//TestModel getSomeArrayedConstruct(String group, int index);
	
	@By("user")
	String readUser();
	
	@By("user-%d")
	String readUsers(int index);

}
