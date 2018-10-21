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

import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mini2Dx.core.Mdx;
import org.mini2Dx.desktop.serialization.DesktopXmlSerializer;

import junit.framework.Assert;
import org.mini2Dx.ui.render.UiContainerRenderTree;

/**
 * Unit and integration tests for {@link TabView}
 */
public class TabViewTest {
	private final Mockery mockery = new Mockery();

	private UiContainerRenderTree uiContainerRenderTree;

	@Before
	public void setUp() {
		mockery.setImposteriser(ClassImposteriser.INSTANCE);

		uiContainerRenderTree = mockery.mock(UiContainerRenderTree.class);

		Mdx.xml = new DesktopXmlSerializer();
	}

	@After
	public void teardown() {
		mockery.assertIsSatisfied();
	}

	@Test
	public void testSerialization() {
		TabView tabView = new TabView("tabView-3");

		Tab tab1 = new Tab("tab-1");
		tab1.setTitle("Tab 1");
		tab1.add(new Label("label-1"));
		tabView.add(tab1);

		Tab tab2 = new Tab("tab-2");
		tab2.setTitle("Tab 2");
		tab2.setIconPath("textures/icon.png");
		tab2.add(new Label("label-2"));
		tab2.add(new TextButton("textButton-1"));
		tabView.add(tab2);

		try {
			String xml = Mdx.xml.toXml(tabView);
			System.out.println(xml);
			TabView result = Mdx.xml.fromXml(xml, TabView.class);
			Assert.assertEquals(tabView.getId(), result.getId());
			Assert.assertEquals(tabView.getTotalTabs(), result.getTotalTabs());

			for (int i = 0; i < tabView.getTotalTabs(); i++) {
				Tab expectedTab = tabView.getTab(i);
				Tab resultTab = tabView.getTab(i);
				Assert.assertEquals(expectedTab.getId(), resultTab.getId());
				Assert.assertEquals(expectedTab.getTitle(), resultTab.getTitle());
				Assert.assertEquals(expectedTab.getIconPath(), resultTab.getIconPath());
				Assert.assertEquals(expectedTab.children.size, resultTab.children.size);
				for(int j = 0; j < expectedTab.children.size; j++) {
					Assert.assertEquals(expectedTab.children.get(j).getId(), resultTab.children.get(j).getId());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
}
