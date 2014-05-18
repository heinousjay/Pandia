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

/**
 * @author jason
 *
 */
public class QueryParams {
	
	public static QueryParams query(String name, String value) {
		QueryParams result = new QueryParams();
		
		return result.add(name, value);
	}

	/**
	 * 
	 */
	private QueryParams() {
		
	}

	public QueryParams add(String name, String value) {
		
		return this;
	}
	
	public QueryParams add(QueryParams queryParams) {
		
		return this;
	}
}
