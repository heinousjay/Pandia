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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Singleton;

import jj.webdriver.finder.ImpatientWebElementFinder;
import jj.webdriver.generator.PanelMethodGeneratorsModule;
import jj.webdriver.panel.PanelBase;
import jj.webdriver.panel.PanelFactory;
import jj.webdriver.panel.URLBase.BaseURL;

import jj.webdriver.provider.JBrowserWebDriverProvider;
import org.apache.commons.codec.binary.Base64;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.ScreenshotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * <p>
 * Manages a WebDriver to connect to a configured browser instance,
 * producing Page/Panel instances to drive the browser for testing
 * purposes
 * 
 * <p>
 * Usage example:<pre class="brush:java">
 * 
 * public class SomeBrowserDrivenTest {
 * 
 * 	{@literal @}Rule
 * 	public WebDriverRule webDriverRule = new WebDriverRule()
 * 		.baseUrl(... defaults to "http://localhost:8080" ...)
 * 		.driverProvider(... required! ...);
 * 
 * 	{@literal @}Test
 * 	public void test() {
 * 		SomePage page = webDriverRule.get(SomePage.class);
 * 		// drive the page!
 * 	}
 * }
 * </pre>
 * 
 * @author jason
 *
 * @see Panel
 * @see Page
 *
 */
public class WebDriverRule implements TestRule {
	
	private static final String SEPARATOR = "*************************************************************************************";
	
	// TODO is it reasonable even having a default here?
	private String baseUrl = "http://localhost:8080";
	
	private Class<? extends WebDriverProvider> webDriverProvider = JBrowserWebDriverProvider.class;
	
	private Class<? extends WebElementFinder> webElementFinder = ImpatientWebElementFinder.class;
	
	private Class<? extends PanelBase> panelBaseClass = PanelBase.class;
	
	private Description currentDescription = null;
	
	private Path screenshotDir = Paths.get("build");
	
	private boolean screenshotOnError = true;
	
	private Logger logger = null;
	
	private Injector injector = null;
	
	private WebDriver webDriver = null;
	
	@Override
	public Statement apply(final Statement base, final Description description) {
		
		return new Statement() {
			
			@Override
			public void evaluate() throws Throwable {
				
				assert webDriverProvider != null : "you must supply a WebDriverProvider";
				
				currentDescription = description;
				
				logger = LoggerFactory.getLogger("test runner");
				
				injector = Guice.createInjector(
					new AbstractModule() {
						
						@Override
						protected void configure() {
							bind(new TypeLiteral<Class<? extends PanelBase>>() {}).toInstance(panelBaseClass);
							bind(String.class).annotatedWith(BaseURL.class).toInstance(baseUrl);
							bind(WebDriver.class).toProvider(webDriverProvider).in(Singleton.class);
							bind(WebElementFinder.class).to(webElementFinder);
							bind(Logger.class).toInstance(logger);
						}
					},
					new PanelMethodGeneratorsModule()
				);
				
				webDriver = injector.getInstance(WebDriver.class);
				
				try {
				
					logger.info(SEPARATOR);
					logger.info("beginning {}.{}", description.getClassName(), description.getMethodName());
					logger.info("using driver {}", webDriver);
					base.evaluate();
				
				} catch (Throwable t) {	
					
					logger.error("TEST ENDED IN ERROR", t);
					
					if (screenshotOnError && !saveScreenshotIfFound(t)) {
						takeScreenshot(makeScreenShotName("error-screenshot"));
					}
					
					throw t;
					
				} finally {
					
					logger.info(SEPARATOR + "\n");
					webDriver.quit();
					currentDescription = null;
					webDriver = null;
					injector = null;
					logger = null;
				}
			}
		};
	}
	
	private boolean saveScreenshotIfFound(Throwable t) {
		boolean hasScreenshot = false;
		if (t.getCause() instanceof ScreenshotException) {
			
			try {
				hasScreenshot = true;
				String screenshotBase64 = ((ScreenshotException)t.getCause()).getBase64EncodedScreenshot();
				
				byte[] screenshot = Base64.decodeBase64(screenshotBase64);
				
				Path screenshotFile = screenshotDir.resolve(makeScreenShotName("error-screenshot"));
				
				Files.write(screenshotFile, screenshot);
				logger.info("saved error state screenshot {}", screenshotFile);
			
			} catch (Exception ioe) {
				logger.error("couldn't save the error screenshot", ioe);
			}
		}
		
		return hasScreenshot;
	}
	
	private void assertUnstarted() {
		assert currentDescription == null : "rule configuration must be before test runs begin";
	}
	
	/**
	 * <p>
	 * Configure the base URL for the test run.  URLs are determined with simple
	 * concatenation - the URL configured for a Page interface is appended to
	 * the value configured here. Default is "http://localhost:8080"
	 *
	 * @param baseUrl The base URL to use
	 *
	 * @return the rule being configured
	 */
	public WebDriverRule baseUrl(final String baseUrl) {
		assertUnstarted();
		
		this.baseUrl = baseUrl;
		return this;
	}
	
	/**
	 * <p>
	 * Configure the class that provides the {@link WebDriver} implementation for the 
	 * test run.  Default is {@link JBrowserWebDriverProvider}.
	 * 
	 * <p>
	 * The provider will be bound as a singleton
	 *
	 * @param webDriverProvider the driver provider to configure
	 *
	 * @return the rule being configured
	 */
	public WebDriverRule driverProvider(Class<? extends WebDriverProvider> webDriverProvider) {
		assertUnstarted();
		assert webDriverProvider != null : "don't give me null!";
		
		this.webDriverProvider = webDriverProvider;
		return this;
	}
	
	public WebDriverRule webElementFinder(Class<? extends WebElementFinder> webElementFinder) {
		assertUnstarted();
		assert webElementFinder != null : "don't give me null!";
		
		this.webElementFinder = webElementFinder;
		return this;
	}
	
	public WebDriverRule panelBaseClass(Class<? extends PanelBase> panelBaseClass) {
		assertUnstarted();
		assert panelBaseClass != null : "don't give me null!";
		
		this.panelBaseClass = panelBaseClass;
		return this;
	}
	
	public WebDriverRule screenShotDir(Path screenshotDir) {
		assertUnstarted();
		assert screenshotDir != null : "don't give me null!";
		assert Files.isDirectory(screenshotDir) : "must be a directory!";
		
		this.screenshotDir = screenshotDir;
		return this;
	}
	
	/**
	 * <p>
	 * Takes a screenshot of the current state of the browser, if possible according to the
	 * current driver, and stores it in the current directory, which is dependent upon
	 * test invocation
	 *
	 * <p>
	 * Silently does nothing if the underlying driver does not support screenshots
	 *
	 * @throws IOException if unable to take or save the screenshot
	 */
	public void takeScreenshot() throws IOException {
		takeScreenshot(makeScreenShotName("screenshot"));
	}
	
	/**
	 * <p>
	 * Takes a screenshot of the current state of the browser, if possible according to the
	 * current driver, and stores it in the screenshot directory using the given name.
	 * 
	 * <p>
	 * If the file already exists, it is overwritten
	 *
	 * @param screenshotName the file name of the saved screenshot, resolved in the
	 * configured screenshot directory
	 *
	 * @throws IOException if unable to take or save the screenshot
	 *
	 * @see #screenShotDir(Path)
	 */
	public void takeScreenshot(String screenshotName) throws IOException {
		
		assert webDriver != null : "cannot take a screenshot outside of a test";
		
		if (webDriver instanceof TakesScreenshot) {
		
			byte[] screenshot = ((TakesScreenshot)webDriver).getScreenshotAs(OutputType.BYTES);
			Path restingPlace = screenshotDir.resolve(screenshotName);
			Files.write(restingPlace, screenshot);
			
			logger.info("saved {}", restingPlace);
		}
	}

	/**
	 * Helper method to make a screenshot name in the format:
	 * <pre>
	 * ${base}-${test class name}.${test method name}[${year}.${month}.${day}.${hour}.${minute}.${second}.${millisecond}].png
	 * </pre>
	 *
	 * @param base as above
	 *
	 * @return the result as above
	 */
	private String makeScreenShotName(String base) {
		
		Calendar now = Calendar.getInstance();
		
		return String.format("%s-%s.%s[%d.%d.%d.%d.%d.%d.%d].png",
			base,
			currentDescription.getClassName(),
			currentDescription.getMethodName(),
			now.get(Calendar.YEAR),
			now.get(Calendar.MONTH) + 1, // ANNOYING
			now.get(Calendar.DATE),
			now.get(Calendar.HOUR_OF_DAY),
			now.get(Calendar.MINUTE),
			now.get(Calendar.SECOND),
			now.get(Calendar.MILLISECOND)
		);
	}
	
	private String makeURL(String inputURL, Object...queryObjects) {
		
		List<Object> formatArgs = new ArrayList<>();
		QueryParams queryParams = null;
		if (queryObjects != null) {
			for (Object queryObject : queryObjects) {
				if (queryObject instanceof QueryParams) {
					queryParams = queryParams == null ? (QueryParams)queryObject : queryParams.and((QueryParams)queryObject);
				} else if (queryObject instanceof String) {
					try {
						formatArgs.add(URLEncoder.encode((String)queryObject, "UTF-8"));
					} catch (UnsupportedEncodingException e) { /* can't happen */ }
				} else if (queryObject instanceof Number) {
					formatArgs.add(queryObject);
				} else {
					logger.error("got a querystring argument that makes no sense, {}", queryObject);
				}
			}
		}
		String url = String.format(inputURL, formatArgs.toArray()); 
		
		if (queryParams != null) {
			url = url + (url.contains("?") ? "&" : "?") + queryParams;
		}
		
		return url;
	}
	
	/**
	 * <p>
	 * Directs the underlying WebDriver to make a request, using the URL defined on the
	 * given page interface combined with any query args, then returns a object implementing
	 * the interface that can be used to drive the browser.
	 * 
	 * <p>
	 * The page interface must have a {@link URL} annotation.  The value of this annotation is
	 * concatenated to the configured base URL in this rule, then the result is used as a format
	 * string.
	 * 
	 * <p>
	 * The queryArgs parameter can be a mix of String, Number, and QueryParam objects.  All
	 * String and Number arguments are passed to the String.format call in the order they appear.
	 * All QueryParam objects are rendered to the end of the query string for the URL.
	 * 
	 * <p>
	 * it's probably best to not mix the two styles.  this API is under... consideration.  So
	 * don't expect it to be stable.
	 *
	 * @param <T> the configuration interface type for the page
	 * @param pageInterface the configuration interface instance
	 * @param queryArgs the arguments to the format string created to use as a URL
	 *
	 * @return a live {@link Page} instance
	 * 
	 */
	public <T extends Page> T get(final Class<T> pageInterface, final Object...queryArgs) {
		
		assert webDriver != null : "cannot get a page outside of a test";
		
		assert pageInterface.getAnnotation(URL.class) != null : "page declarations must have a URL annotation";
		
		webDriver.get(makeURL(baseUrl + pageInterface.getAnnotation(URL.class).value(), queryArgs));
		
		return injector.getInstance(PanelFactory.class).create(pageInterface);
	}

	/**
	 * Configure the rule to take screenshots on error, or not. Default is true
	 *
	 * @param screenshotOnError the setting
	 *
	 * @return the rule being configured
	 */
	public WebDriverRule screenshotOnError(boolean screenshotOnError) {
		this.screenshotOnError = screenshotOnError;
		return this;
	}
}
