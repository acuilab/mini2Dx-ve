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
package org.mini2Dx.ui.element;

import org.mini2Dx.core.serialization.annotation.ConstructorArg;
import org.mini2Dx.ui.layout.FlexDirection;
import org.mini2Dx.ui.render.DivRenderNode;
import org.mini2Dx.ui.render.ParentRenderNode;

/**
 * Utility {@link Div} class pre-set with flex layout "flex-column:xs-12c"
 */
public class FlexRow extends Div {
	/**
	 * Constructor. Generates a unique ID for this {@link FlexRow}
	 */
	public FlexRow() {
		this(null);
	}
	
	/**
	 * Constructor
	 * @param id The unique ID for this {@link FlexRow}
	 */
	public FlexRow(@ConstructorArg(clazz=String.class, name = "id") String id) {
		super(id);
		setFlexLayout("flex-column:xs-12c");
	}

	/**
	 * Sets the {@link FlexDirection} of this {@link FlexRow}
	 * @param flexDirection The flex direction
	 */
	public void setFlexDirection(FlexDirection flexDirection) {
		if(flexDirection == null) {
			return;
		}
		switch(flexDirection) {
		case COLUMN:
			setFlexLayout("flex-column:xs-12c");
			break;
		case COLUMN_REVERSE:
			setFlexLayout("flex-column-reverse:xs-12c");
			break;
		case ROW:
			setFlexLayout("flex-row:xs-12c");
			break;
		case ROW_REVERSE:
			setFlexLayout("flex-row-reverse:xs-12c");
			break;
		case CENTER:
			setFlexLayout("flex-center:xs-12c");
			break;
		}
	}
	
	@Override
	protected ParentRenderNode<?, ?> createRenderNode(ParentRenderNode<?, ?> parent) {
		return new DivRenderNode(parent, this);
	}
	
	/**
	 * Creates a {@link Visibility#VISIBLE} {@link FlexRow} containing the
	 * specified {@link UiElement}s
	 * 
	 * @param elements The {@link UiElement}s to add to the {@link FlexRow}
	 * @return A new {@link FlexRow} containing the {@link UiElement}s
	 */
	public static FlexRow withElements(UiElement ...elements) {
		return withElements(null, elements);
	}
	
	/**
	 * Creates a {@link Visibility#VISIBLE} {@link FlexRow} containing the
	 * specified {@link UiElement}s
	 * 
	 * @param rowId The unique ID of the {@link FlexRow}
	 * @param elements The {@link UiElement}s to add to the {@link FlexRow}
	 * @return A new {@link FlexRow} containing the {@link UiElement}s
	 */
	public static FlexRow withElements(String rowId, UiElement ...elements) {
		FlexRow result = new FlexRow(rowId);
		float maxY = 0f;
		for(int i = 0; i < elements.length; i++) {
			result.add(elements[i]);
			maxY = Math.max(maxY, elements[i].getY() + elements[i].getHeight());
		}
		result.setHeight(maxY);
		result.setVisibility(Visibility.VISIBLE);
		return result;
	}
}
