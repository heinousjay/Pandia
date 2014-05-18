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
/**
 * <p>
 * Provides test facilities to drive in-browser testing of web apps
 * 
 * <p>
 * The main entry point to this API is {@link jj.webdriver.WebDriverRule}. An
 * instance of this rule will manage a {@link org.openqa.selenium.WebDriver} for
 * you, and provide a factory for page objects
 * 
 * <p>
 * Declare sets of interfaces descending from {@link jj.webdriver.Page}/{@link jj.webdriver.Panel}
 * that describe how to navigate your target site.
 * 
 * @author jason
 *
 */
package jj.webdriver;
