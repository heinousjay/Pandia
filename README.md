# Pandia

## A declarative page object factory for selenium.

[![Build Status](https://travis-ci.org/heinousjay/Pandia.png?branch=master)](https://travis-ci.org/heinousjay/Pandia)


### What is it?

Pandia provides a simple interface-based DSL to drive your web pages, and a Junit test rule for managing
web driver resources during your tests.  The rule will automatically implement your interfaces according to
some simple guidelines.

### How does it work?

#### Driver management

A Pandia WebDriverRule manages a simple 1:1:1 relationship.  One rule provides one test method with one
managed WebDriver instance. Several rules can be instantiated in the test class to provide, for example,
several browsers at the same time, of a mix of types if needed.

A WebDriverRule is configured fluently on creation with a WebDriverProvider, which is responsible for
creating and configuring the WebDriver to be used. Basic implementations are provided for PhantomJS and
Firefox, with more coming.  There are some other configurations, such as a base URL, a WebElementFinder
which strategizes actually locating the WebElement from the WebDriver instance, and whether to attempt
screenshots on errors.

Inside a test method, there are (currently) two primary interactions available. The get method accepts a
page class and returns an instance of that class, backed by a driver pointing to the resolved URL (more
on that later). The test method can then interact with the page via that object, performing normal actions
with a fluent API designed to read like a script of steps. Also, the rule can take screenshots, if the 
current driver supports it.

#### Page objects

Pandia page objects start as panels.  Panels are collections of manageable interactions with some piece of a
web page, for instance a header menu, or a form. The interactions are defined using a
set of conventions and a handful of annotations.  For instance:

```java
public interface Menu extends Panel {

  @By(id = "menu-item-home")
  Home clickHome();
}
```
The By annotation corresponds to the underlying By in the WebDriver.  The return type of the method defines
that the action is expected to result in the Home page object.  The 'click' prefix targets a specific
method generator in the system, which sends a click to the intended element, just like you'd expect.

Panels can be built up into arbitrary hierarchies.  One of the simplest method generation patterns is to
retrieve a panel, which is simply:
```java
@URL("/")
public interface Home extends Page {
  Menu menu();
}
```
If the return type is a Panel, and the name doesn't match any other patterns, it simply creates and returns
the object against the current driver.

Descending from Page is for top-level objects that represent a unit of navigation.  The base URL configured
on the WebDriverRule is suffixed with the value of the URL attribute of the page object when the get method
is called on the rule.

### Where can I get it?
Well... for now, right here.  But it'll be in Maven Central soon.

### Where is it going?

1. better handling of URL parameters
2. better example code
3. cleaner code gen facilities for better extensibility
4. extraction of the gradle plugin for PhantomJS setup
5. publication to maven central


### Why the name?

Pandia is the the deification of the moon and daughter of the goddess Selene, so it's sort of related
to selenium.  I really just wanted a nicer name than "WebTest" and this seems like a winner.