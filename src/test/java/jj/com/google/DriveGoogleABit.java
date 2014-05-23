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
package jj.com.google;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import jj.webdriver.WebDriverRule;
import jj.webdriver.provider.PhantomJSWebDriverProvider;

import org.junit.Rule;
import org.junit.Test;

/**
 * @author jason
 *
 */
public class DriveGoogleABit {
	
	@Rule
	public WebDriverRule driver = new WebDriverRule()
		.driverProvider(PhantomJSWebDriverProvider.class)
		.baseUrl("https://google.com");

	@Test
	public void test() throws Exception {
		driver.get(Index.class).setQuery("selenium").clickSearch();
		
		driver.takeScreenshot();
		
	}
	
	@Test
	public void testURLSubstitution() throws Exception {
		
		assertThat(driver.get(Results.class, "selenium").readQuery(), is("selenium"));
	}

}
