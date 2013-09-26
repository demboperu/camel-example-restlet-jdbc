/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.test.router;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.restlet.RestletConstants;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.commons.logging.Log;
import org.json.simple.JSONValue;
import org.restlet.Request;
import org.restlet.util.Series;

public class TestRouteBuilder extends RouteBuilder {

	@Override
	public void configure() {
		from("restlet:/user?restletMethod=POST")
				.setBody(
						simple("insert into user(firstName, lastName) values('${header.firstName}','${header.lastName}'); CALL IDENTITY();"))
				.to("jdbc:dataSource")
				.setBody(simple("select * from user ORDER BY id desc LIMIT 1"))
				.to("jdbc:dataSource");

		from("restlet:/user/{userId}?restletMethods=GET,PUT,DELETE")
				.choice()
				.when(simple("${header.CamelHttpMethod} == 'GET'"))
				.setBody(
						simple("select * from user where id = ${header.userId}"))
				.when(simple("${header.CamelHttpMethod} == 'PUT'"))
				.setBody(
						simple("update user set firstName='${header.firstName}', lastName='${header.lastName}' where id = ${header.userId}"))
				.when(simple("${header.CamelHttpMethod} == 'DELETE'"))
				.setBody(simple("delete from user where id = ${header.userId}"))
				.otherwise().stop().end().to("jdbc:dataSource");

		from("restlet:/users")
				.setBody(simple("select id, firstname, lastName from user"))
				.to("jdbc:dataSource").log("body ${body}")
				.process(new Processor() {
					public void process(Exchange exchange) throws Exception {

						List payload = exchange.getIn().getBody(List.class);
//						HashMap<String, String> map = new HashMap<String, String>();
//						map.put("hola", "holxax");
						StringWriter out = new StringWriter();
						JSONValue.writeJSONString(payload, out);
						String jsonText = out.toString();
						// payload = payload.replace("", "");
						// Log
						// do something with the payload and/or exchange here
						// exchange.getIn().setHeader("Access-Control-Allow-Origin",
						// "*");
						// exchange.getIn().setHeader("xx",simple("x"));
						exchange.getIn().setBody(jsonText);

						// exchange.getIn().setHeader(CxfConstants.CAMEL_CXF_RS_USING_HTTP_API,
						// Boolean.FALSE);
						//
						// exchange.getIn().setHeader("Access-Control-Allow-Methods",
						// "DELETE PUT");
						// exchange.getIn().setHeader("Access-Control-Allow-Credentials",
						// "false");
						// exchange.getIn().setHeader(org.apache.camel.component.http.HttpProducer.HTTP_URI,
						// "*");
						// org.apache.camel.component.http.HttpProducer.HTTP_URI
						Request request = exchange.getIn()
								.getHeader(RestletConstants.RESTLET_REQUEST,
										Request.class);
						Map<String, Object> attrs = request.getAttributes();
						Series headers = (Series) attrs
								.get("org.restlet.http.headers");
						headers.add("Access-Control-Allow-Origin", "*");

					}
				});
	}
}
