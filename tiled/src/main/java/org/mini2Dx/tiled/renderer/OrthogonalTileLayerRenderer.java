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
package org.mini2Dx.tiled.renderer;

import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntIntMap;
import org.mini2Dx.core.geom.Rectangle;
import org.mini2Dx.core.graphics.Graphics;
import org.mini2Dx.tiled.TileLayer;
import org.mini2Dx.tiled.TiledMap;
import org.mini2Dx.tiled.Tileset;

/**
 * Renders orthogonal {@link TileLayer}s
 */
public class OrthogonalTileLayerRenderer implements TileLayerRenderer {
	private TiledMapRenderArea mapClip, tmpClip;
	private SpriteCache layerCache;
	private IntIntMap layerCacheIds;

	private final boolean cacheLayers;
	private final TiledMap tiledMap;
	private final Rectangle graphicsClip = new Rectangle();

	public OrthogonalTileLayerRenderer(TiledMap tiledMap, boolean cacheLayers) {
		super();
		this.cacheLayers = cacheLayers;
		this.tiledMap = tiledMap;

		if (cacheLayers) {
			layerCache = new SpriteCache(5000, true);
			layerCacheIds = new IntIntMap();
		}
		mapClip = new TiledMapRenderArea();
		tmpClip = new TiledMapRenderArea();
	}

	@Override
	public void drawLayer(Graphics g, TileLayer layer, int renderX, int renderY, int startTileX, int startTileY,
			int widthInTiles, int heightInTiles) {
		int startTileRenderX = (startTileX * tiledMap.getTileWidth());
		int startTileRenderY = (startTileY * tiledMap.getTileHeight());

		int tileRenderX = MathUtils.round(renderX - startTileRenderX);
		int tileRenderY = MathUtils.round(renderY - startTileRenderY);

		Rectangle existingClip = g.removeClip();
		graphicsClip.set(startTileRenderX, startTileRenderY, widthInTiles * tiledMap.getTileWidth(),
				heightInTiles * tiledMap.getTileHeight());

		g.translate(-tileRenderX, -tileRenderY);

		if (existingClip != null) {
			if (existingClip.intersects(graphicsClip)) {
				g.setClip(existingClip.intersection(graphicsClip));
			} else {
				g.setClip(existingClip);
			}
		} else {
			g.setClip(graphicsClip);
		}

		if (cacheLayers) {
			renderCachedLayer(g, layer, tileRenderX, tileRenderY, startTileX, startTileY, widthInTiles, heightInTiles);
		} else {
			renderLayer(g, layer, tileRenderX, tileRenderY, startTileX, startTileY, widthInTiles, heightInTiles);
		}

		g.removeClip();
		g.translate(tileRenderX, tileRenderY);

		if (existingClip != null) {
			g.setClip(existingClip.getX(), existingClip.getY(), existingClip.getWidth(), existingClip.getHeight());
		}
	}

	private void renderCachedLayer(Graphics g, TileLayer layer, int renderX, int renderY, int startTileX,
			int startTileY, int widthInTiles, int heightInTiles) {
		tmpClip.set(startTileX, startTileY, widthInTiles, heightInTiles);
		if (!mapClip.equals(tmpClip)) {
			layerCache.clear();
			layerCacheIds.clear();
			mapClip.set(startTileX, startTileY, widthInTiles, heightInTiles);
		}
		if (!layerCacheIds.containsKey(layer.getIndex())) {
			renderLayerToCache(g, layer, renderX, renderY, startTileX, startTileY, widthInTiles, heightInTiles);
		}

		int cacheId = layerCacheIds.get(layer.getIndex(), -1);
		if(cacheId < 0) {
			return;
		}
		g.drawSpriteCache(layerCache, cacheId);
	}

	private void renderLayer(Graphics g, TileLayer layer, int renderX, int renderY, int startTileX, int startTileY,
			int widthInTiles, int heightInTiles) {
		for (int y = startTileY; y < startTileY + heightInTiles && y < layer.getHeight(); y++) {
			for (int x = startTileX; x < startTileX + widthInTiles && x < layer.getWidth(); x++) {
				int tileId = layer.getTileId(x, y);

				if (tileId < 1) {
					continue;
				}
				boolean flipHorizontally = layer.isFlippedHorizontally(x, y);
				boolean flipVertically = layer.isFlippedVertically(x, y);
				boolean flipDiagonally = layer.isFlippedDiagonally(x, y);

				int tileRenderX = x * tiledMap.getTileWidth();
				int tileRenderY = y * tiledMap.getTileHeight();

				if (tileRenderX + tiledMap.getTileWidth() < g.getTranslationX()) {
					continue;
				}
				if (tileRenderY + tiledMap.getTileHeight() < g.getTranslationY()) {
					continue;
				}
				if (tileRenderX > g.getTranslationX() + g.getViewportWidth()) {
					continue;
				}
				if (tileRenderY > g.getTranslationY() + g.getViewportHeight()) {
					continue;
				}

				for (int i = 0; i < tiledMap.getTilesets().size; i++) {
					Tileset tileset = tiledMap.getTilesets().get(i);
					if (tileset.contains(tileId)) {
						tileset.getTile(tileId).draw(g, tileRenderX, tileRenderY, flipHorizontally, flipVertically, flipDiagonally);
						break;
					}
				}
			}
		}
	}

	private void renderLayerToCache(Graphics g, TileLayer layer, int renderX, int renderY, int startTileX,
			int startTileY, int widthInTiles, int heightInTiles) {
		layerCache.beginCache();
		for (int y = startTileY; y < startTileY + heightInTiles && y < layer.getHeight(); y++) {
			for (int x = startTileX; x < startTileX + widthInTiles && x < layer.getWidth(); x++) {
				int tileId = layer.getTileId(x, y);

				if (tileId < 1) {
					continue;
				}
				
				boolean flipHorizontally = layer.isFlippedHorizontally(x, y);
				boolean flipVertically = layer.isFlippedVertically(x, y);
				boolean flipDiagonally = layer.isFlippedDiagonally(x, y);
				
				int tileRenderX = x * tiledMap.getTileWidth();
				int tileRenderY = y * tiledMap.getTileHeight();

				for (int i = 0; i < tiledMap.getTilesets().size; i++) {
					Tileset tileset = tiledMap.getTilesets().get(i);
					if (tileset.contains(tileId)) {
						layerCache.add(tileset.getTile(tileId).getTileRenderer().getCurrentTileImage(), tileRenderX,
								tileRenderY);
						break;
					}
				}
			}
		}
		layerCacheIds.put(layer.getIndex(), layerCache.endCache());
	}

	@Override
	public void dispose() {
		if (layerCache == null) {
			return;
		}
		layerCache.dispose();
	}

}
