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
package org.mini2Dx.ui.render;

import org.mini2Dx.core.graphics.Graphics;
import org.mini2Dx.ui.element.ProgressBar;
import org.mini2Dx.ui.layout.LayoutState;
import org.mini2Dx.ui.layout.LayoutRuleset;
import org.mini2Dx.ui.style.ProgressBarStyleRule;

import com.badlogic.gdx.math.MathUtils;

/**
 * {@link RenderNode} implementation for {@link ProgressBar}
 */
public class ProgressBarRenderNode extends RenderNode<ProgressBar, ProgressBarStyleRule> {
	protected LayoutRuleset layoutRuleset;
	private float multiplier;
	private float fillWidth;

	public ProgressBarRenderNode(ParentRenderNode<?, ?> parent, ProgressBar element) {
		super(parent, element);
		layoutRuleset = LayoutRuleset.parse(element.getLayout());
		multiplier = element.getValue() / element.getMax();
	}

	@Override
	public void layout(LayoutState layoutState) {
		if (!layoutRuleset.equals(element.getLayout())) {
			layoutRuleset = LayoutRuleset.parse(element.getLayout());
		}
		super.layout(layoutState);
	}

	@Override
	protected void renderElement(Graphics g) {
		if (style.getBackground() != null) {
			style.getBackgroundRenderer().render(g, getInnerRenderX(), getInnerRenderY(), getInnerRenderWidth(),
					getInnerRenderHeight());
		}
		style.getFillRenderer().render(g, getContentRenderX(), getContentRenderY(), fillWidth,
				getContentRenderHeight());
	}

	@Override
	protected ProgressBarStyleRule determineStyleRule(LayoutState layoutState) {
		return layoutState.getTheme().getStyleRule(element, layoutState.getScreenSize());
	}

	@Override
	protected float determinePreferredContentWidth(LayoutState layoutState) {
		if (layoutRuleset.isHiddenByInputSource(layoutState)) {
			return 0f;
		}
		float layoutRuleResult = layoutRuleset.getPreferredElementWidth(layoutState);
		if (layoutRuleResult <= 0f) {
			hiddenByLayoutRule = true;
			return 0f;
		} else {
			hiddenByLayoutRule = false;
		}
		float result = layoutRuleResult - style.getPaddingLeft() - style.getPaddingRight() - style.getMarginLeft()
				- style.getMarginRight();
		fillWidth = MathUtils.round(result * multiplier);
		return result;
	}

	@Override
	protected float determinePreferredContentHeight(LayoutState layoutState) {
		if (preferredContentWidth <= 0f) {
			return 0f;
		}
		float preferredHeight = style.getMinHeight() - style.getPaddingTop()
				- style.getPaddingBottom() - style.getMarginTop() - style.getMarginBottom();

		float sizeRuleHeight = layoutRuleset.getPreferredElementHeight(layoutState) - style.getPaddingTop()
				- style.getPaddingBottom() - style.getMarginTop() - style.getMarginBottom();
		if (!layoutRuleset.getCurrentHeightRule().isAutoSize()) {
			preferredHeight = Math.max(preferredHeight, sizeRuleHeight);
		}
		return preferredHeight;
	}

	@Override
	protected float determineXOffset(LayoutState layoutState) {
		return layoutRuleset.getPreferredElementRelativeX(layoutState);
	}

	@Override
	protected float determineYOffset(LayoutState layoutState) {
		return layoutRuleset.getPreferredElementRelativeY(layoutState);
	}

	public void updateFillWidth() {
		multiplier = element.getValue() / element.getMax();
		fillWidth = MathUtils.round(getContentRenderWidth() * multiplier);
	}
}
