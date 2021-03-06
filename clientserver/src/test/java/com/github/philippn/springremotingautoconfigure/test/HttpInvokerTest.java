/*
 * Copyright (C) 2015 Philipp Nanz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package com.github.philippn.springremotingautoconfigure.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.philippn.springremotingautoconfigure.test.service.PingService;
import com.github.philippn.springremotingautoconfigure.test.service.PingServiceWithMappingPath;

/**
 * @author Philipp Nanz
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes=HttpInvokerTestApplication.class)
@WebIntegrationTest("server.port:8080")
public class HttpInvokerTest {

	@Autowired
	@Qualifier("PingServiceProxy")
	private PingService pingServiceProxy;

	@Autowired
	@Qualifier("PingServiceWithMappingPathProxy")
	private PingServiceWithMappingPath pingServiceWithMappingPathProxy;

	@Test
	public void testDefaultMappingPath() {
		Assert.assertEquals("pong", pingServiceProxy.ping());
	}

	@Test
	public void testSpecifiedMappingPath() {
		Assert.assertEquals("pong", pingServiceWithMappingPathProxy.ping());
	}
}
