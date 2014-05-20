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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * @author jason
 *
 */
public class QueryParams {
	
	private static final class Pair {
		final String name;
		final String value;
		
		Pair(String name, String value) {
			try {
				this.name = URLEncoder.encode(name, "UTF-8");
				this.value = URLEncoder.encode(value, "UTF-8");
			} catch (UnsupportedEncodingException uee) {
				throw new AssertionError(uee); // cant happen
			} 
		}
	}
	
	public static QueryParams query(String name, String value) {
		QueryParams result = new QueryParams();
		
		return result.and(name, value);
	}
	
	private final ArrayList<Pair> pairs = new ArrayList<>();

	private QueryParams() {}

	public QueryParams and(String name, String value) {
		pairs.add(new Pair(name, value));
		return this;
	}

	public QueryParams and(QueryParams queryParams) {
		for (Pair pair : queryParams.pairs) {
			pairs.add(pair);
		}
		return this;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Pair pair : pairs) {
			sb.append(pair.name).append("=").append(pair.value).append("&");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
}
