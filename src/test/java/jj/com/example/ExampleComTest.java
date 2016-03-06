package jj.com.example;

import jj.webdriver.By;
import jj.webdriver.Page;
import jj.webdriver.URL;
import jj.webdriver.WebDriverRule;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Basic bootstrap test
 *
 * Created by jason on 3/6/16.
 */
public class ExampleComTest {

	@URL("/")
	public interface Index extends Page {

		// being very specific!
		@By(cssSelector = "div:first-child > h1:first-child")
		String readHeading();

		// not being very specific!
		@By(cssSelector = "a")
		String readMoreInformation();
	}

	@Rule
	public WebDriverRule driver = new WebDriverRule()
		.baseUrl("http://example.com");

	@Test
	public void loadTheHomePage() {
		Index index = driver.get(Index.class);
		assertThat(index.readHeading(), is("Example Domain"));
		assertThat(index.readMoreInformation(), is("More information..."));
	}
}
