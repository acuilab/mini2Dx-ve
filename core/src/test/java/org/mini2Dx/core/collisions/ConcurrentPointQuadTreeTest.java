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
package org.mini2Dx.core.collisions;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import junit.framework.Assert;
import net.jodah.concurrentunit.Waiter;
import org.junit.Before;
import org.junit.Test;
import org.mini2Dx.core.engine.geom.CollisionPoint;
import org.mini2Dx.core.geom.LineSegment;
import org.mini2Dx.core.geom.Rectangle;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for {@link ConcurrentPointQuadTree}
 */
public class ConcurrentPointQuadTreeTest implements Runnable {
	private static final long CONCURRENCY_TEST_TIMEOUT = 10000L;
	private static final int CONCURRENCY_TEST_WATERMARK = 4;
	private static final float TREE_WIDTH = 128f;
	private static final float TREE_HEIGHT = 128f;

	private ConcurrentPointQuadTree<CollisionPoint> rootQuad;
	private CollisionPoint point1, point2, point3, point4;

	private final Waiter waiter = new Waiter();
	private AtomicInteger totalThreads = new AtomicInteger();
	private AtomicBoolean concurrencyExceptionOccurred = new AtomicBoolean(false);
	private AtomicInteger coordinateCursor = new AtomicInteger(0);
	private AtomicInteger collisionsFound = new AtomicInteger(0);
	private Queue<CollisionPoint> threadCollisions = new ConcurrentLinkedQueue<CollisionPoint>();
	
	@Before
	public void setup() {
		rootQuad = new ConcurrentPointQuadTree<CollisionPoint>(2, 0, 0, TREE_WIDTH, TREE_HEIGHT);
		point1 = new CollisionPoint(0, 0);
		point2 = new CollisionPoint(127, 0);
		point3 = new CollisionPoint(0, 127);
		point4 = new CollisionPoint(127, 127);
	}

	@Test
	public void testAdd() {
		Random random = new Random();
		for (int i = 0; i < 100; i++) {
			rootQuad.add(new CollisionPoint(random.nextInt(128), random.nextInt(128)));
			Assert.assertEquals(i + 1, rootQuad.getElements().size);
		}
	}

	@Test
	public void testAddAll() {
		Random random = new Random();
		Array<CollisionPoint> points = new Array<CollisionPoint>();
		for (int i = 0; i < 100; i++) {
			points.add(new CollisionPoint(random.nextInt(128), random.nextInt(128)));
		}
		rootQuad.addAll(points);
		Assert.assertEquals(points.size, rootQuad.getTotalElements());
	}

	@Test
	public void testRemove() {
		Random random = new Random();
		Array<CollisionPoint> collisionPoints = new Array<CollisionPoint>();
		for (int i = 0; i < 1000; i++) {
			collisionPoints.add(new CollisionPoint(random.nextInt(128), random.nextInt(128)));
		}

		for (int i = 0; i < collisionPoints.size; i++) {
			Assert.assertEquals(true, rootQuad.add(collisionPoints.get(i)));
			Assert.assertEquals(i + 1, rootQuad.getElements().size);
		}

		for (int i = collisionPoints.size - 1; i >= 0; i--) {
			Assert.assertEquals(i + 1, rootQuad.getElements().size);
			rootQuad.remove(collisionPoints.get(i));
			Assert.assertEquals(i, rootQuad.getElements().size);
		}
	}

	@Test
	public void testRemoveAll() {
		Random random = new Random();
		Array<CollisionPoint> points = new Array<CollisionPoint>();
		for (int i = 0; i < 100; i++) {
			points.add(new CollisionPoint(random.nextInt(128), random.nextInt(128)));
		}
		rootQuad.addAll(points);
		Assert.assertEquals(points.size, rootQuad.getTotalElements());
		rootQuad.removeAll(points);
		Assert.assertEquals(0, rootQuad.getTotalElements());
	}

	@Test
	public void testSubdivide() {
		rootQuad.add(point1);
		Assert.assertEquals(1, rootQuad.getElements().size);
		Assert.assertEquals(1, rootQuad.getTotalQuads());
		rootQuad.add(point2);
		Assert.assertEquals(2, rootQuad.getElements().size);
		Assert.assertEquals(1, rootQuad.getTotalQuads());
		rootQuad.add(point3);
		Assert.assertEquals(3, rootQuad.getElements().size);
		Assert.assertEquals(4, rootQuad.getTotalQuads());
		rootQuad.add(point4);
		Assert.assertEquals(4, rootQuad.getElements().size);
		Assert.assertEquals(4, rootQuad.getTotalQuads());
		rootQuad.add(new CollisionPoint(32, 32));
		Assert.assertEquals(5, rootQuad.getElements().size);
		Assert.assertEquals(4, rootQuad.getTotalQuads());
		rootQuad.add(new CollisionPoint(48, 48));
		Assert.assertEquals(6, rootQuad.getElements().size);
		Assert.assertEquals(7, rootQuad.getTotalQuads());
	}

	@Test
	public void testMergeRemoveSingleElements() {
		rootQuad = new ConcurrentPointQuadTree<CollisionPoint>(4, 3, 0, 0, 128, 128);
		rootQuad.add(point1);
		Assert.assertEquals(1, rootQuad.getTotalQuads());

		CollisionPoint point5 = new CollisionPoint(32, 32);

		for (int i = 0; i < 5; i++) {
			rootQuad.add(point2);
			rootQuad.add(point3);
			rootQuad.add(point4);
			rootQuad.add(point5);
			Assert.assertEquals(4, rootQuad.getTotalQuads());
			Assert.assertEquals(5, rootQuad.getTotalElements());
			rootQuad.remove(point4);
			rootQuad.remove(point3);
			rootQuad.remove(point2);
			Assert.assertEquals(1, rootQuad.getTotalQuads());
			Assert.assertEquals(2, rootQuad.getTotalElements());
			Assert.assertEquals(true, rootQuad.getElements().contains(point1, false));
			rootQuad.remove(point5);
		}
	}
	
	@Test
	public void testMergeRemoveAllElements() {
		rootQuad = new ConcurrentPointQuadTree<CollisionPoint>(4, 3, 0, 0, 128, 128);
		rootQuad.add(point1);
		Assert.assertEquals(1, rootQuad.getTotalQuads());

		CollisionPoint point5 = new CollisionPoint(32, 32);

		Array<CollisionPoint> points = new Array<CollisionPoint>();
		points.add(point2);
		points.add(point3);
		points.add(point4);

		for (int i = 0; i < 5; i++) {
			rootQuad.add(point2);
			rootQuad.add(point3);
			rootQuad.add(point4);
			rootQuad.add(point5);
			Assert.assertEquals(4, rootQuad.getTotalQuads());
			Assert.assertEquals(5, rootQuad.getTotalElements());
			rootQuad.removeAll(points);
			Assert.assertEquals(1, rootQuad.getTotalQuads());
			Assert.assertEquals(2, rootQuad.getTotalElements());
			Assert.assertEquals(true, rootQuad.getElements().contains(point1, false));
			rootQuad.remove(point5);
		}
	}

	@Test
	public void testGetTotalElements() {
		rootQuad.add(point1);
		Assert.assertEquals(1, rootQuad.getTotalElements());
		rootQuad.add(point2);
		Assert.assertEquals(2, rootQuad.getTotalElements());
		rootQuad.add(point3);
		Assert.assertEquals(3, rootQuad.getTotalElements());
		rootQuad.remove(point2);
		Assert.assertEquals(2, rootQuad.getTotalElements());
		rootQuad.add(point4);
		Assert.assertEquals(3, rootQuad.getTotalElements());
		rootQuad.add(point2);
		Assert.assertEquals(4, rootQuad.getTotalElements());
		rootQuad.add(new CollisionPoint(32, 32));
		Assert.assertEquals(5, rootQuad.getTotalElements());
		rootQuad.add(new CollisionPoint(48, 48));
		Assert.assertEquals(6, rootQuad.getTotalElements());
	}

	@Test
	public void testGetElementsWithinRegion() {
		rootQuad.add(point1);
		rootQuad.add(point2);
		rootQuad.add(point3);
		rootQuad.add(point4);

		Array<CollisionPoint> collisionPoints = rootQuad.getElementsWithinArea(new Rectangle(0, 0, 64, 64));
		Assert.assertEquals(1, collisionPoints.size);
		Assert.assertEquals(point1, collisionPoints.get(0));

		collisionPoints = rootQuad.getElementsWithinArea(new Rectangle(64, 0, 64, 64));
		Assert.assertEquals(1, collisionPoints.size);
		Assert.assertEquals(point2, collisionPoints.get(0));

		collisionPoints = rootQuad.getElementsWithinArea(new Rectangle(0, 64, 64, 64));
		Assert.assertEquals(1, collisionPoints.size);
		Assert.assertEquals(point3, collisionPoints.get(0));

		collisionPoints = rootQuad.getElementsWithinArea(new Rectangle(64, 64, 64, 64));
		Assert.assertEquals(1, collisionPoints.size);
		Assert.assertEquals(point4, collisionPoints.get(0));

		CollisionPoint collisionPoint5 = new CollisionPoint(32, 32);
		CollisionPoint collisionPoint6 = new CollisionPoint(48, 48);
		rootQuad.add(collisionPoint5);
		rootQuad.add(collisionPoint6);

		collisionPoints = rootQuad.getElementsWithinArea(new Rectangle(0, 0, 64, 64));
		Assert.assertEquals(3, collisionPoints.size);
		Assert.assertEquals(true, collisionPoints.contains(point1, false));
		Assert.assertEquals(true, collisionPoints.contains(collisionPoint5, false));
		Assert.assertEquals(true, collisionPoints.contains(collisionPoint6, false));
	}

	@Test
	public void testGetElementsIntersectingLineSegment() {
		rootQuad.add(point1);
		rootQuad.add(point2);
		rootQuad.add(point3);
		rootQuad.add(point4);

		Array<CollisionPoint> collisionPoints = rootQuad
				.getElementsIntersectingLineSegment(new LineSegment(0, 0, 128, 128));
		Assert.assertEquals(true, collisionPoints.contains(point1, false));
		Assert.assertEquals(false, collisionPoints.contains(point2, false));
		Assert.assertEquals(false, collisionPoints.contains(point3, false));
		Assert.assertEquals(true, collisionPoints.contains(point4, false));
	}

	@Test
	public void testMergingConcurrency() throws TimeoutException {
		rootQuad = new ConcurrentPointQuadTree<CollisionPoint>(CONCURRENCY_TEST_WATERMARK * 2,
				CONCURRENCY_TEST_WATERMARK, 0, 0, TREE_WIDTH, TREE_HEIGHT);

		for (int i = 0; i < MathUtils.round(TREE_WIDTH); i++) {
			createNextCollision();
		}

		int totalThreads = Runtime.getRuntime().availableProcessors();
		if (totalThreads % 2 == 1) {
			totalThreads++;
		}
		for (int i = 0; i < totalThreads; i++) {
			new Thread(this).start();
		}

		waiter.await(CONCURRENCY_TEST_TIMEOUT);

		System.out.println(rootQuad.getTotalMergeOperations() + " total merge operations, "
				+ collisionsFound.getAndIncrement() + " collisions found concurrently");
		Assert.assertEquals(true, rootQuad.getTotalMergeOperations() > 0);
		Assert.assertEquals(true, collisionsFound.get() > 0);
		Assert.assertEquals(false, concurrencyExceptionOccurred.get());
	}

	@Override
	public void run() {
		boolean readerThread = totalThreads.incrementAndGet() % 2 == 0;
		Array<CollisionPoint> collisions = new Array<CollisionPoint>();

		while (rootQuad.getTotalMergeOperations() < 10 || collisionsFound.get() == 0) {
			try {
				if (threadCollisions.isEmpty()) {
					int totalCollisions = MathUtils.random(1, MathUtils.round(TREE_WIDTH));
					for(int i = 0; i < totalCollisions; i++) {
						createNextCollision();
					}
				} else if (readerThread) {
					rootQuad.getElementsWithinArea(collisions,
							new Rectangle(MathUtils.random(TREE_WIDTH / 2f), MathUtils.random(TREE_HEIGHT / 2f),
									MathUtils.random(TREE_HEIGHT / 3f), MathUtils.random(TREE_HEIGHT / 3f)));
					collisionsFound.addAndGet(collisions.size);
					collisions.clear();
				} else {
					rootQuad.remove(threadCollisions.poll());
				}
			} catch (Exception e) {
				e.printStackTrace();
				concurrencyExceptionOccurred.set(true);
			}
		}
		waiter.resume();
	}

	private void createNextCollision() {
		int cursor = coordinateCursor.addAndGet(MathUtils.round(TREE_WIDTH / (CONCURRENCY_TEST_WATERMARK * 8)));
		float x = cursor % TREE_WIDTH;
		float y = (cursor / TREE_WIDTH) % TREE_HEIGHT;
		CollisionPoint nextCollision = new CollisionPoint(x, y);
		threadCollisions.offer(nextCollision);
		rootQuad.add(nextCollision);
	}
}
