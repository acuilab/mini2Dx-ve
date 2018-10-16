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
package org.mini2Dx.core.assets;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import org.mini2Dx.core.exception.MdxException;

/**
 * A {@link FileHandleResolver} that checks for files using multiple
 * {@link FileHandleResolver}s in a priority order
 */
public class FallbackFileHandleResolver implements FileHandleResolver {
	private Array<FileHandleResolver> resolvers;

	/**
	 * Constructor
	 * 
	 * @param resolvers
	 *            The {@link FileHandleResolver}s in priority order. The first
	 *            resolver has the highest priority.
	 */
	public FallbackFileHandleResolver(FileHandleResolver... resolvers) {
		if (resolvers.length == 0) {
			throw new MdxException(FallbackFileHandleResolver.class.getSimpleName() + " requires at least 1 "
					+ FileHandleResolver.class.getSimpleName());
		}

		this.resolvers = new Array<FileHandleResolver>(true, resolvers.length, FileHandleResolver.class);
		for (int i = 0; i < resolvers.length; i++) {
			this.resolvers.add(resolvers[i]);
		}
	}

	@Override
	public FileHandle resolve(String fileName) {
		for (FileHandleResolver resolver : resolvers) {
			FileHandle result = resolver.resolve(fileName);
			if (result == null) {
				continue;
			}
			if (!result.exists()) {
				continue;
			}
			return result;
		}
		return null;
	}

}
