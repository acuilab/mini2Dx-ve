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
package org.mini2Dx.ui.style;

import org.mini2Dx.core.serialization.annotation.Field;
import org.mini2Dx.core.util.ColorUtils;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

/**
 * A font for user interfaces
 */
public class UiFont {

	@Field
	private String path;
	@Field(optional=true)
	private String borderColor;
	@Field(optional=true)
	private int borderWidth;
	@Field
	private int fontSize;
	
	private Color fontBorderColor;
	private BitmapFont bitmapFont;
	
	public void prepareAssets(UiTheme theme, FileHandleResolver fileHandleResolver) {
		if(theme.isHeadless()) {
			return;
		}
		
		final FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(fileHandleResolver.resolve(path));
		if(borderColor != null) {
			fontBorderColor = ColorUtils.rgbToColor(borderColor);
		}
		FreeTypeFontParameter fontParameter = new  FreeTypeFontParameter();
		fontParameter.size = fontSize;
		fontParameter.flip = true;
		if(borderWidth > 0) {
			fontParameter.borderWidth = borderWidth;
			fontParameter.borderColor = fontBorderColor;
		}
		bitmapFont = fontGenerator.generateFont(fontParameter);
	}
	
	public void dispose() {
		bitmapFont.dispose();
	}

	public BitmapFont getBitmapFont() {
		return bitmapFont;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getBorderWidth() {
		return borderWidth;
	}

	public void setBorderWidth(int borderWidth) {
		this.borderWidth = borderWidth;
	}

	public Color getFontBorderColor() {
		return fontBorderColor;
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	private String getFontParameterKey(FreeTypeFontParameter parameter) {
		StringBuilder result = new StringBuilder();
		result.append(parameter.characters);
		result.append(parameter.borderGamma);
		result.append(parameter.borderStraight);
		result.append(parameter.borderWidth);
		result.append(parameter.flip);
		result.append(parameter.gamma);
		result.append(parameter.genMipMaps);
		result.append(parameter.incremental);
		result.append(parameter.kerning);
		result.append(parameter.mono);
		result.append(parameter.renderCount);
		result.append(parameter.shadowOffsetX);
		result.append(parameter.shadowOffsetY);
		result.append(parameter.size);
		result.append(parameter.spaceX);
		result.append(parameter.spaceY);
		result.append(parameter.borderColor == null ? "null" : parameter.borderColor.toFloatBits());
		result.append(parameter.color == null ? "null" : parameter.color.toFloatBits());
		result.append(parameter.magFilter == null ? "null" : parameter.magFilter.getGLEnum());
		result.append(parameter.minFilter == null ? "null" : parameter.minFilter.getGLEnum());
		result.append(parameter.packer == null ? "null" : parameter.packer.hashCode());
		result.append(parameter.shadowColor == null ? "null" : parameter.shadowColor.toFloatBits());
		return result.toString();
	}
}
