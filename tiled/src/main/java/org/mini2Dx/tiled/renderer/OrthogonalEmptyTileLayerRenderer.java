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
package org.mini2Dx.tiled.renderer;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import org.mini2Dx.core.graphics.Graphics;
import org.mini2Dx.tiled.Tile;
import org.mini2Dx.tiled.TileLayer;
import org.mini2Dx.tiled.TiledMap;
import org.mini2Dx.tiled.Tileset;

/**
 * Optimises CPU time (at cost of memory) for rendering orthogonal {@link TileLayer}s
 * where the layer consists mostly of empty tiles
 */
public class OrthogonalEmptyTileLayerRenderer implements TileLayerRenderer {
	private final TiledMap tiledMap;
	private final TileLayer layer;
	private final Array<TileRenderRef> tiles;

	public OrthogonalEmptyTileLayerRenderer(TiledMap tiledMap, TileLayer layer) {
		super();
		this.tiledMap = tiledMap;
		this.layer = layer;

		tiles = new Array<TileRenderRef>(layer.getTotalFilledTiles() + 1);

		for (int y = 0; y < layer.getHeight(); y++) {
			for (int x = 0; x < layer.getWidth(); x++) {
				int tileId = layer.getTileId(x, y);

				if (tileId < 1) {
					continue;
				}

				final TileRenderRef ref = new TileRenderRef();
				ref.x = x;
				ref.y = y;
				ref.tileId = tileId;
				tiles.add(ref);
			}
		}
	}

	@Override
	public void drawLayer(Graphics g, TileLayer layer, int renderX, int renderY,
	                      int startTileX, int startTileY, int widthInTiles, int heightInTiles) {
		int startTileRenderX = (startTileX * tiledMap.getTileWidth());
		int startTileRenderY = (startTileY * tiledMap.getTileHeight());

		renderX = MathUtils.round(renderX - startTileRenderX);
		renderY = MathUtils.round(renderY - startTileRenderY);

		for(TileRenderRef tileRef : tiles) {
			if(tileRef.x < startTileX) {
				continue;
			}
			if(tileRef.y < startTileY) {
				continue;
			}
			if(tileRef.x >= startTileX + widthInTiles) {
				continue;
			}
			if(tileRef.y >= startTileY + heightInTiles) {
				continue;
			}

			if(tileRef.tile == null) {
				for (int i = 0; i < tiledMap.getTilesets().size; i++) {
					Tileset tileset = tiledMap.getTilesets().get(i);
					if (tileset.contains(tileRef.tileId)) {
						tileRef.tile = tileset.getTile(tileRef.tileId);
						break;
					}
				}
			}
			if(tileRef.tile != null) {
				boolean flipHorizontally = layer.isFlippedHorizontally(tileRef.x, tileRef.y);
				boolean flipVertically = layer.isFlippedVertically(tileRef.x, tileRef.y);
				boolean flipDiagonally = layer.isFlippedDiagonally(tileRef.x, tileRef.y);

				int tileRenderX = renderX + (tileRef.x * tiledMap.getTileWidth());
				int tileRenderY = renderY + (tileRef.y * tiledMap.getTileHeight());

				tileRef.tile.draw(g, tileRenderX, tileRenderY, flipHorizontally, flipVertically, flipDiagonally);
			}
		}
	}

	@Override
	public void dispose() {
		tiles.clear();
	}

	public TiledMap getTiledMap() {
		return tiledMap;
	}

	public TileLayer getLayer() {
		return layer;
	}

	private class TileRenderRef {
		public int x, y;
		public int tileId;
		public Tile tile;
	}
}
