/**
 * Copyright (c) 2016 See AUTHORS file
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the mini2Dx nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mini2Dx.ui.element;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mini2Dx.core.Mdx;
import org.mini2Dx.core.serialization.SerializationException;
import org.mini2Dx.desktop.serialization.DesktopXmlSerializer;

/**
 * Unit and integration tests for {@link Div}
 */
public class DivTest {
	@Before
	public void setUp() {
		Mdx.xml = new DesktopXmlSerializer();
	}

	@Test
	public void testSerialization() {
		Div div = new Div("div-1");
		div.setZIndex(22);
		div.add(new Label());
		div.add(new TextButton("button-1"));
		
		try {
			String xml = Mdx.xml.toXml(div);
			System.out.println(xml);
			Div result = Mdx.xml.fromXml(xml, Div.class);
			
			Assert.assertEquals(div.getId(), result.getId());
			Assert.assertEquals(div.getZIndex(), result.getZIndex());
			Assert.assertEquals(div.children.size, result.children.size);
			for(int i = 0; i < div.children.size; i++) {
				Assert.assertEquals(div.children.get(i).getId(), result.children.get(i).getId());
			}
		} catch (SerializationException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
}
