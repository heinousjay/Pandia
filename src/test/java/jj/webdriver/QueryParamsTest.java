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

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static jj.webdriver.QueryParams.query;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class QueryParamsTest {

	@Test
	public void test() {
		assertThat(query("name1", "value1").toString(), is("name1=value1"));
		
		assertThat(
			query("i have spaces", "and so do i").and("name2", "value2").toString(),
			is("i+have+spaces=and+so+do+i&name2=value2")
		);
		
		assertThat(
			query("something","nothing").and(query("other thing", "everything")).toString(),
			is("something=nothing&other+thing=everything")
		);
	}

}
