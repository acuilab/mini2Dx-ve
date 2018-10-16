/**
 * Copyright (c) 2017 See AUTHORS file
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the mini2Dx nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mini2Dx.core.engine.geom;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import org.mini2Dx.core.engine.PositionChangeListener;
import org.mini2Dx.core.engine.Positionable;
import org.mini2Dx.core.engine.SizeChangeListener;
import org.mini2Dx.core.engine.Sizeable;
import org.mini2Dx.core.game.GameContainer;
import org.mini2Dx.core.geom.Circle;
import org.mini2Dx.core.geom.Shape;

import java.util.Objects;

/**
 * A {@link Circle} implementation of {@link CollisionShape} that does not
 * need to be updated/interpolated. More lightweight than using a
 * {@link CollisionCircle} for static collisions.
 */
public class StaticCollisionCircle extends Circle implements CollisionShape {
	private static final Vector2 TMP_SOURCE_VECTOR = new Vector2();
	private static final Vector2 TMP_TARGET_VECTOR = new Vector2();

	private final int id;
	
	private Array<PositionChangeListener> positionChangeListeners;
	private Array<SizeChangeListener> sizeChangeListeners;
	
	public StaticCollisionCircle(float radius) {
		this(CollisionIdSequence.nextId(), radius);
	}
	
	public StaticCollisionCircle(int id, float radius) {
		this(id, 0f, 0f, radius);
	}
	
	public StaticCollisionCircle(float centerX, float centerY, float radius) {
		this(CollisionIdSequence.nextId(), centerX, centerY, radius);
	}
	
	public StaticCollisionCircle(int id, float centerX, float centerY, float radius) {
		super(centerX, centerY, radius);
		this.id = id;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public int getRenderX() {
		return MathUtils.round(getX());
	}

	@Override
	public int getRenderY() {
		return MathUtils.round(getY());
	}

	@Override
	public float getDistanceTo(Positionable positionable) {
		return getDistanceTo(positionable.getX(), positionable.getY());
	}

	public void moveTowards(float x, float y, float speed) {
		TMP_SOURCE_VECTOR.set(getX(), getY());
		TMP_TARGET_VECTOR.set(x, y);
		Vector2 direction = TMP_TARGET_VECTOR.sub(TMP_SOURCE_VECTOR).nor();
		
		float xComponent = speed * MathUtils.cosDeg(direction.angle());
		float yComponent = speed * MathUtils.sinDeg(direction.angle());
		TMP_SOURCE_VECTOR.add(xComponent, yComponent);
		
		set(TMP_SOURCE_VECTOR.x, TMP_SOURCE_VECTOR.y);
	}

	@Override
	public void moveTowards(Positionable positionable, float speed) {
		moveTowards(positionable.getX(), positionable.getY(), speed);
	}
	
	@Override
	public void preUpdate() {
	}

	@Override
	public void update(GameContainer gc, float delta) {
	}

	@Override
	public void interpolate(GameContainer gc, float alpha) {
	}
	
	/**
	 * @see Positionable#addPostionChangeListener(PositionChangeListener)
	 */
	@Override
	public <T extends Positionable> void addPostionChangeListener(PositionChangeListener<T> listener) {
		if (positionChangeListeners == null) {
			positionChangeListeners = new Array<PositionChangeListener>(true,1);
		}
		positionChangeListeners.add(listener);
	}

	/**
	 * @see Positionable#removePositionChangeListener(PositionChangeListener)
	 */
	@Override
	public <T extends Positionable> void removePositionChangeListener(PositionChangeListener<T> listener) {
		if (positionChangeListeners == null) {
			return;
		}
		positionChangeListeners.removeValue(listener, false);
	}

	private void notifyPositionChangeListeners() {
		if (positionChangeListeners == null) {
			return;
		}
		for (int i = positionChangeListeners.size - 1; i >= 0; i--) {
			if (i >= positionChangeListeners.size) {
				i = positionChangeListeners.size - 1;
			}
			PositionChangeListener listener = positionChangeListeners.get(i);
			listener.positionChanged(this);
		}
	}

	@Override
	public <T extends Sizeable> void addSizeChangeListener(SizeChangeListener<T> listener) {
		if (sizeChangeListeners == null) {
			sizeChangeListeners = new Array<SizeChangeListener>(true,1);
		}
		sizeChangeListeners.add(listener);
	}

	@Override
	public <T extends Sizeable> void removeSizeChangeListener(SizeChangeListener<T> listener) {
		if (sizeChangeListeners == null) {
			return;
		}
		sizeChangeListeners.removeValue(listener, false);
	}

	private void notifySizeChangeListeners() {
		if (sizeChangeListeners == null) {
			return;
		}
		for (int i = sizeChangeListeners.size - 1; i >= 0; i--) {
			if (i >= sizeChangeListeners.size) {
				i = sizeChangeListeners.size - 1;
			}
			SizeChangeListener listener = sizeChangeListeners.get(i);
			listener.sizeChanged(this);
		}
	}

	@Override
	public void forceTo(float x, float y) {
		set(x, y);
	}
	
	@Override
	public void add(float x, float y) {
		super.add(x, y);
		notifyPositionChangeListeners();
	}
	
	@Override
	public void subtract(float x, float y) {
		super.subtract(x, y);
		notifyPositionChangeListeners();
	}

	@Override
	public void setX(float x) {
		if(x == getX()) {
			return;
		}
		super.setX(x);
		notifyPositionChangeListeners();
	}
	
	@Override
	public void setY(float y) {
		if(y == getY()) {
			return;
		}
		super.setY(y);
		notifyPositionChangeListeners();
	}
	
	@Override
	public void set(float x, float y) {
		if(x == getX() && y == getY()) {
			return;
		}
		super.set(x, y);
		notifyPositionChangeListeners();
	}
	
	@Override
	public void setRadius(float radius) {
		if(radius == getRadius()) {
			return;
		}
		super.setRadius(radius);
		notifySizeChangeListeners();
	}
	
	@Override
	public void setCenter(float x, float y) {
		if(x == getCenterX() && y == getCenterY()) {
			return;
		}
		super.setCenter(x, y);
		notifyPositionChangeListeners();
	}

	@Override
	public void setCenterX(float x) {
		if(x == getCenterX()) {
			return;
		}
		super.setCenterX(x);
		notifyPositionChangeListeners();
	}

	@Override
	public void setCenterY(float y) {
		if(y == getCenterY()) {
			return;
		}
		super.setCenterY(y);
		notifyPositionChangeListeners();
	}
	
	@Override
	public float getWidth() {
		return getRadius() * 2f;
	}

	@Override
	public float getHeight() {
		return getRadius() * 2f;
	}
	
	@Override
	public Shape getShape() {
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		StaticCollisionCircle that = (StaticCollisionCircle) o;
		return id == that.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), id);
	}
}
