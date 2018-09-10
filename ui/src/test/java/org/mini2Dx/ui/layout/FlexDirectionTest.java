/**
 * Copyright 2017 Thomas Cashman
 */
package org.mini2Dx.ui.layout;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.Array;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mini2Dx.ui.dummy.DummyParentRenderNode;
import org.mini2Dx.ui.dummy.DummyParentUiElement;
import org.mini2Dx.ui.dummy.DummyRenderNode;
import org.mini2Dx.ui.dummy.DummyUiElement;
import org.mini2Dx.ui.element.Visibility;
import org.mini2Dx.ui.render.RenderNode;
import org.mini2Dx.ui.render.UiContainerRenderTree;

import junit.framework.Assert;

/**
 * Unit tests for {@link FlexDirection}
 */
public class FlexDirectionTest {
	private static final int TOTAL_CHILDREN = 4;
	
	private Mockery mockery;
	private LayoutState layoutState;
	private UiContainerRenderTree renderTree;
	
	private final DummyParentUiElement parentElement = new DummyParentUiElement();
	private final List<DummyUiElement> children = new ArrayList<DummyUiElement>(1);
	private final Array<RenderNode<?, ?>> childrenRenderNodes = new Array<RenderNode<?, ?>>(1);
	
	private DummyParentRenderNode parentRenderNode;
	
	@Before
	public void setUp() {
		mockery = new Mockery();
		mockery.setImposteriser(ClassImposteriser.INSTANCE);
		
		layoutState = mockery.mock(LayoutState.class);
		renderTree = mockery.mock(UiContainerRenderTree.class);
		
		mockery.checking(new Expectations() {
			{
				atLeast(1).of(layoutState).getUiContainerRenderTree();
				will(returnValue(renderTree));
			}
		});
	}
	
	@After
	public void teardown() {
		mockery.assertIsSatisfied();
	}
	
	@Test
	public void testColumn() {
		setUpPreferredSizes(300f, 100f);
		
		FlexDirection.COLUMN.layout(layoutState, parentRenderNode, childrenRenderNodes);
		
		float relativeX = childrenRenderNodes.get(0).getRelativeX();
		float relativeY = childrenRenderNodes.get(0).getRelativeY();
		for(int i = 1; i < childrenRenderNodes.size; i++) {
			RenderNode<?, ?> nextNode = childrenRenderNodes.get(i);
			if(nextNode.getRelativeY() == relativeY) {
				Assert.assertEquals(true, nextNode.getRelativeX() > relativeX);
				relativeX = nextNode.getRelativeX();
			} else {
				Assert.assertEquals(0f, nextNode.getRelativeX());
				Assert.assertEquals(true, nextNode.getRelativeY() > relativeY);
				relativeX = nextNode.getRelativeX();
				relativeY = nextNode.getRelativeY();
			}
		}
	}
	
	@Test
	public void testColumnReverse() {
		setUpPreferredSizes(300f, 100f);
		
		FlexDirection.COLUMN_REVERSE.layout(layoutState, parentRenderNode, childrenRenderNodes);
		
		float relativeX = childrenRenderNodes.get(0).getRelativeX();
		float relativeY = childrenRenderNodes.get(0).getRelativeY();
		
		for(int i = 1; i < childrenRenderNodes.size; i++) {
			RenderNode<?, ?> nextNode = childrenRenderNodes.get(i);
			if(nextNode.getRelativeY() == relativeY) {
				Assert.assertEquals(true, relativeX > nextNode.getRelativeX());
				relativeX = nextNode.getRelativeX();
			} else {
				Assert.assertEquals(parentElement.getPreferredContentWidth() - nextNode.getPreferredContentWidth(), nextNode.getRelativeX());
				Assert.assertEquals(true, nextNode.getRelativeY() > relativeY);
				relativeX = nextNode.getRelativeX();
				relativeY = nextNode.getRelativeY();
			}
		}
	}
	
	private void setUpPreferredSizes(float parentWidth, float childSize) {
		parentElement.setPreferredContentWidth(parentWidth);
		parentElement.setVisibility(Visibility.VISIBLE);
		
		for(int i = 0; i < TOTAL_CHILDREN; i++) {
			DummyUiElement element = new DummyUiElement();
			element.setPreferredContentWidth(childSize);
			element.setPreferredContentHeight(childSize);
			element.setVisibility(Visibility.VISIBLE);
			parentElement.add(element);
			children.add(element);
		}
		parentRenderNode = new DummyParentRenderNode(null, parentElement);
		for(int i = 0; i < TOTAL_CHILDREN; i++) {
			DummyRenderNode renderNode = new DummyRenderNode(parentRenderNode, children.get(0));
			childrenRenderNodes.add(renderNode);
		}
	}
}
