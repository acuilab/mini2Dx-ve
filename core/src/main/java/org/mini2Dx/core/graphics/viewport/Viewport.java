/**
 * Copyright (c) 2019 See AUTHORS file
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the mini2Dx nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mini2Dx.core.graphics.viewport;

import com.badlogic.gdx.math.Vector2;
import org.mini2Dx.core.geom.Rectangle;
import org.mini2Dx.core.graphics.Graphics;

/**
 * Base class for implementing Viewport strategies.
 */
public abstract class Viewport {
	private int x, y, width, height;
	private float scaleX, invScaleX;
	private float scaleY, invScaleY;

	private int previousWindowWidth, previousWindowHeight;
	private float previousScaleX, previousScaleY, previousTranslateX, previousTranslateY;
	private final Rectangle previousClip = new Rectangle();

	protected abstract void onResize(int windowWidth, int windowHeight);

	/**
	 * Configures the {@link Graphics} context for the viewport. Note: This will manipulate the scale, clip and translation of the {@link Graphics} context.
	 * @param g The {@link Graphics} instance to configure
	 */
	public void apply(Graphics g) {
		if(previousWindowWidth != g.getWindowWidth() || previousWindowHeight != g.getWindowHeight()) {
			onResize(g.getWindowWidth(), g.getWindowHeight());
			previousWindowWidth = g.getWindowWidth();
			previousWindowHeight = g.getWindowHeight();
		}

		previousScaleX = g.getScaleX();
		previousScaleY = g.getScaleY();
		previousTranslateX = g.getTranslationX();
		previousTranslateY = g.getTranslationY();
		g.peekClip(previousClip);

		g.setScale(scaleX, scaleY);
		g.setTranslation(-x, -y);
		g.setClip(0, 0, width, height);
	}

	/**
	 * Restores the {@link Graphics} to its state before {@link #apply(Graphics)} was invoked.
	 * @param g The {@link Graphics} instance
	 */
	public void unapply(Graphics g) {
		g.setClip(previousClip);
		g.setTranslation(previousTranslateX, previousTranslateY);
		g.setScale(previousScaleX, previousScaleY);
	}

	/**
	 * Converts the game world's coordinate to a pixel coordinate on screen
	 * @param result The {@link Vector2} to store the result in
	 * @param worldX The world x coordinate
	 * @param worldY The world y coordinate
	 */
	public void toScreenCoordinates(Vector2 result, float worldX, float worldY) {
		result.x = (worldX * scaleX) + x;
		result.y = (worldY * scaleY) + y;
	}

	/**
	 * Converts a pixel coordinate to a game world coordinate
	 * @param result The {@link Vector2} to store the result in
	 * @param screenX The pixel x coodinate
	 * @param screenY The pixel y coordinate
	 */
	public void toWorldCoordinates(Vector2 result, float screenX, float screenY) {
		result.x = (screenX - x) * invScaleX;
		result.y = (screenY - y) * invScaleY;
	}

	/**
	 * Converts a game world coordinate to a pixel coordinate on screen
	 * @param worldCoordinates The {@link Vector2} to convert. The result is stored in this {@link Vector2}.
	 */
	public void toScreenCoordinates(Vector2 worldCoordinates) {
		toScreenCoordinates(worldCoordinates, worldCoordinates.x, worldCoordinates.y);
	}

	/**
	 * Converts a pixel coordinate to a game world coordinate
	 * @param screenCoordinates The {@link Vector2} to convert. The result is stored in this {@link Vector2}.
	 */
	public void toWorldCoordinates(Vector2 screenCoordinates) {
		toWorldCoordinates(screenCoordinates, screenCoordinates.x, screenCoordinates.y);
	}

	protected void setBounds(int x, int y, int width, int height, float scaleX, float scaleY) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.scaleX = scaleX;
		this.invScaleX = 1f / scaleX;
		this.scaleY = scaleY;
		this.invScaleY = 1f / scaleY;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public float getScaleX() {
		return scaleX;
	}

	public float getInvScaleX() {
		return invScaleX;
	}

	public float getScaleY() {
		return scaleY;
	}

	public float getInvScaleY() {
		return invScaleY;
	}
}