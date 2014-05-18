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

import javax.inject.Inject;
import javax.inject.Provider;

import org.openqa.selenium.WebDriver;

/**
 * <p>
 * Implementations of this interface can be used to configure the
 * WebDriverRule to change which driver is used for a set of test
 * runs
 * 
 * <p>
 * Implementations are welcome to use {@literal @}{@link Inject}
 * annotations to have dependencies injected.  The instance will
 * be constructed using <a href="https://code.google.com/p/google-guice/">Guice</a>
 * so if you can make use of that info to deal with injections,
 * BAM
 * 
 * @author jason
 *
 */
public interface WebDriverProvider extends Provider<WebDriver> {
	
}
