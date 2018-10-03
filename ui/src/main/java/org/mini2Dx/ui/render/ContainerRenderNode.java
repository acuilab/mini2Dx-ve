/**
 * Copyright (c) 2015 See AUTHORS file
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the mini2Dx nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mini2Dx.ui.render;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import com.badlogic.gdx.utils.IntMap;
import org.mini2Dx.core.controller.button.ControllerButton;
import org.mini2Dx.ui.element.Actionable;
import org.mini2Dx.ui.element.Div;
import org.mini2Dx.ui.element.Container;
import org.mini2Dx.ui.layout.LayoutState;
import org.mini2Dx.ui.navigation.ControllerHotKeyOperation;
import org.mini2Dx.ui.navigation.KeyboardHotKeyOperation;
import org.mini2Dx.ui.style.ContainerStyleRule;

/**
 * Base class for {@link Container} {@link RenderNode} implementations
 */
public class ContainerRenderNode extends ParentRenderNode<Div, ContainerStyleRule> implements NavigatableRenderNode {
	private final IntMap<String> keyboardHotkeys = new IntMap<String>();
	private final Map<String, String> controllerHotkeys = new HashMap<String, String>();
	private final Map<String, RenderNode<?, ?>> elementIdLookupCache = new HashMap<String, RenderNode<?, ?>>();
	
	public ContainerRenderNode(ParentRenderNode<?, ?> parent, Div div) {
		super(parent, div);
	}
	
	@Override
	public void layout(LayoutState layoutState) {
		if(isDirty()) {
			elementIdLookupCache.clear();
		}
		((Container) element).getNavigation().layout(layoutState.getScreenSize());
		super.layout(layoutState);
	}
	
	@Override
	public RenderNode<?, ?> getElementById(String id) {
		if (element.getId().equals(id)) {
			return this;
		}
		if(elementIdLookupCache.containsKey(id)) {
			return elementIdLookupCache.get(id);
		}
		for (RenderLayer layer : layers.values()) {
			RenderNode<?, ?> result = layer.getElementById(id);
			if (result != null) {
				elementIdLookupCache.put(id, result);
				return result;
			}
		}
		return null;
	}

	@Override
	public ActionableRenderNode hotkey(int keycode) {
		String id = keyboardHotkeys.get(keycode);
		if (id == null) {
			return null;
		}
		RenderNode<?, ?> renderNode = searchTreeForElementById(id);
		if (renderNode == null) {
			return null;
		}
		return (ActionableRenderNode) renderNode;
	}

	@Override
	public ActionableRenderNode hotkey(ControllerButton controllerButton) {
		String id = controllerHotkeys.get(controllerButton.getAbsoluteValue());
		if (id == null) {
			return null;
		}
		RenderNode<?, ?> renderNode = searchTreeForElementById(id);
		if (renderNode == null) {
			return null;
		}
		return (ActionableRenderNode) renderNode;
	}

	@Override
	public void syncHotkeys(Queue<ControllerHotKeyOperation> controllerHotKeyOperations,
							Queue<KeyboardHotKeyOperation> keyboardHotKeyOperations) {
		while (!controllerHotKeyOperations.isEmpty()) {
			ControllerHotKeyOperation hotKeyOperation = controllerHotKeyOperations.poll();
			if (hotKeyOperation.isMapOperation()) {
				controllerHotkeys.put(hotKeyOperation.getControllerButton().getAbsoluteValue(),
						hotKeyOperation.getActionable().getId());
			} else {
				if (hotKeyOperation.getControllerButton() == null) {
					controllerHotkeys.clear();
				} else {
					controllerHotkeys.remove(hotKeyOperation.getControllerButton().getAbsoluteValue());
				}
			}
		}
		while (!keyboardHotKeyOperations.isEmpty()) {
			KeyboardHotKeyOperation hotKeyOperation = keyboardHotKeyOperations.poll();
			if (hotKeyOperation.isMapOperation()) {
				keyboardHotkeys.put(hotKeyOperation.getKeycode(), hotKeyOperation.getActionable().getId());
			} else {
				if (hotKeyOperation.getKeycode() == Integer.MAX_VALUE) {
					keyboardHotkeys.clear();
				} else {
					keyboardHotkeys.remove(hotKeyOperation.getKeycode());
				}
			}
		}
	}

	@Override
	public ActionableRenderNode navigate(int keycode) {
		Actionable actionable = ((Container) element).getNavigation().navigate(keycode);
		if (actionable == null) {
			return null;
		}
		return (ActionableRenderNode) searchTreeForElementById(actionable.getId());
	}

	@Override
	protected ContainerStyleRule determineStyleRule(LayoutState layoutState) {
		return layoutState.getTheme().getStyleRule(((Container) element), layoutState.getScreenSize());
	}
}
