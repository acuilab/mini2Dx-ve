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

import java.util.LinkedList;
import java.util.Queue;

import org.mini2Dx.core.exception.MdxException;
import org.mini2Dx.core.serialization.annotation.ConstructorArg;
import org.mini2Dx.core.serialization.annotation.Field;
import org.mini2Dx.ui.layout.LayoutRuleset;
import org.mini2Dx.ui.render.ParentRenderNode;
import org.mini2Dx.ui.render.ProgressBarRenderNode;

/**
 * Implements a progress/loading bar
 */
public class ProgressBar extends UiElement {
	@Field(optional=true)
	private float min;
	@Field(optional=true)
	private float max;
	@Field(optional=true)
	private float value;
	@Field(optional=true)
	private String layout = LayoutRuleset.DEFAULT_RULESET;
	
	protected ProgressBarRenderNode renderNode;
	
	public ProgressBar() {
		super();
		max = 1f;
	}
	
	public ProgressBar(@ConstructorArg(clazz = String.class, name = "id") String id) {
		super(id);
		max = 1f;
	}
	
	@Override
	public void syncWithUpdate() {
		while (!effects.isEmpty()) {
			renderNode.applyEffect(effects.poll());
		}
		processUpdateDeferred();
	}

	@Override
	public void attach(ParentRenderNode<?, ?> parentRenderNode) {
		if (renderNode != null) {
			return;
		}
		renderNode = new ProgressBarRenderNode(parentRenderNode, this);
		parentRenderNode.addChild(renderNode);
	}

	@Override
	public void detach(ParentRenderNode<?, ?> parentRenderNode) {
		if (renderNode == null) {
			return;
		}
		parentRenderNode.removeChild(renderNode);
		renderNode = null;
	}

	@Override
	public void setVisibility(Visibility visibility) {
		if (this.visibility == visibility) {
			return;
		}
		this.visibility = visibility;

		if (renderNode == null) {
			return;
		}
		renderNode.setDirty(true);
	}

	@Override
	public void setStyleId(String styleId) {
		if (styleId == null) {
			return;
		}
		if (this.styleId.equals(styleId)) {
			return;
		}
		this.styleId = styleId;

		if (renderNode == null) {
			return;
		}
		renderNode.setDirty(true);
	}

	@Override
	public void setZIndex(int zIndex) {
		this.zIndex = zIndex;

		if (renderNode == null) {
			return;
		}
		renderNode.setDirty(true);
	}

	public String getLayout() {
		return layout;
	}

	public void setLayout(String layout) {
		if(layout == null) {
			return;
		}
		if (this.layout.equals(layout)) {
			return;
		}
		this.layout = layout;

		if (renderNode == null) {
			return;
		}
		renderNode.setDirty(true);
	}

	public float getMin() {
		return min;
	}

	public void setMin(float min) {
		if(min >= max) {
			throw new MdxException("Min must be less than max");
		}
		this.min = min;
		if(value < min) {
			value = min;
		}
		if(renderNode == null) {
			return;
		}
		renderNode.updateFillWidth();
	}

	public float getMax() {
		return max;
	}

	public void setMax(float max) {
		if(max <= min) {
			throw new MdxException("Max must be greater than min");
		}
		this.max = max;
		if(value > max) {
			value = max;
		}
		if(renderNode == null) {
			return;
		}
		renderNode.updateFillWidth();
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		if(value < min) {
			value = min;
		} else if(value > max) {
			value = max;
		}
		this.value = value;
		if(renderNode == null) {
			return;
		}
		renderNode.updateFillWidth();
	}

	@Override
	public float getX() {
		if(renderNode == null) {
			return Float.MIN_VALUE;
		}
		return renderNode.getOuterX();
	}

	@Override
	public float getY() {
		if(renderNode == null) {
			return Float.MIN_VALUE;
		}
		return renderNode.getOuterY();
	}

	@Override
	public float getWidth() {
		if(renderNode == null) {
			return -1f;
		}
		return renderNode.getOuterWidth();
	}

	@Override
	public float getHeight() {
		if(renderNode == null) {
			return -1f;
		}
		return renderNode.getOuterHeight();
	}
}
