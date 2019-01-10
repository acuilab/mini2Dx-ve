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
package org.mini2Dx.desktop.playerdata;

import java.io.*;
import java.nio.file.Paths;
import java.util.Arrays;

import org.mini2Dx.core.Mdx;
import org.mini2Dx.core.playerdata.PlayerData;
import org.mini2Dx.core.playerdata.PlayerDataException;
import org.mini2Dx.core.serialization.GameDataSerializable;
import org.mini2Dx.core.serialization.SerializationException;
import org.mini2Dx.natives.OsInformation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * Desktop implementation of {@link PlayerData}
 */
public class DesktopPlayerData implements PlayerData {
	private final String saveDirectory;

	public DesktopPlayerData(String gameIdentifier) {
		saveDirectory = getSaveDirectoryForGame(gameIdentifier);
	}

	public String getSaveDirectoryForGame(String gameIdentifier) {
		switch (OsInformation.getOs()) {
		case WINDOWS:
			return Paths
					.get(Gdx.files.getExternalStoragePath(), "AppData",
							"Roaming", gameIdentifier).toAbsolutePath()
					.toString();
		case MAC:
			return Paths
					.get(Gdx.files.getExternalStoragePath(), "Library",
							"Application Support", gameIdentifier)
					.toAbsolutePath().toString();
		case UNIX:
			if (gameIdentifier.contains(".")) {
				gameIdentifier = gameIdentifier.substring(gameIdentifier
						.indexOf('.') + 1);
			}
			return Paths
					.get(Gdx.files.getExternalStoragePath(),
							"." + gameIdentifier).toAbsolutePath().toString();
		default:
			return Paths.get(Gdx.files.getLocalStoragePath()).toAbsolutePath()
					.toString();
		}
	}

	@Override
	public <T> T readXml(Class<T> clazz, String... filepath)
			throws PlayerDataException {
		if (filepath.length == 0) {
			throw new PlayerDataException("No file path specified");
		}
		try {
			final InputStream inputStream = resolve(filepath).read();
			final T result = Mdx.xml.fromXml(new InputStreamReader(inputStream), clazz);
			inputStream.close();
			return result;
		} catch (SerializationException e) {
			throw new PlayerDataException(e);
		} catch (IOException e) {
			throw new PlayerDataException(e);
		}
	}

	@Override
	public <T> void writeXml(T object, String... filepath)
			throws PlayerDataException {
		if (filepath.length == 0) {
			throw new PlayerDataException("No file path specified");
		}
		try {
			ensureDirectoryExistsForFile(filepath);
			StringWriter writer = new StringWriter();
			Mdx.xml.toXml(object, writer);

			FileHandle file = resolve(filepath);
			FileHandle tmpFile = resolveTmp(filepath);
			tmpFile.writeString(writer.toString(), false);
			writer.flush();
			writer.close();

			if(file.file().exists()) {
				file.file().delete();
			}
			tmpFile.file().renameTo(file.file());
		} catch (SerializationException e) {
			throw new PlayerDataException(e);
		} catch (IOException e) {
			throw new PlayerDataException(e);
		}
	}

	@Override
	public <T> T readJson(Class<T> clazz, String... filepath)
			throws PlayerDataException {
		if (filepath.length == 0) {
			throw new PlayerDataException("No file path specified");
		}
		try {
			return Mdx.json.fromJson(resolve(filepath).readString(), clazz);
		} catch (SerializationException e) {
			throw new PlayerDataException(e);
		}

	}

	@Override
	public <T> void writeJson(T object, String... filepath)
			throws PlayerDataException {
		if (filepath.length == 0) {
			throw new PlayerDataException("No file path specified");
		}
		try {
			ensureDirectoryExistsForFile(filepath);
			FileHandle file = resolve(filepath);
			FileHandle tmpFile = resolveTmp(filepath);
			tmpFile.writeString(Mdx.json.toJson(object), false);
			if(file.file().exists()) {
				file.file().delete();
			}
			tmpFile.file().renameTo(file.file());
		} catch (SerializationException e) {
			throw new PlayerDataException(e);
		}
	}

	@Override
	public String readString(String... filepath) throws PlayerDataException {
		if (filepath.length == 0) {
			throw new PlayerDataException("No file path specified");
		}
		try {
			FileHandle file = resolve(filepath);
			return file.readString();
		} catch (Exception e) {
			throw new PlayerDataException(e);
		}
	}

	@Override
	public void writeString(String content, String... filepath)
			throws PlayerDataException {
		if (filepath.length == 0) {
			throw new PlayerDataException("No file path specified");
		}
		try {
			ensureDirectoryExistsForFile(filepath);
			FileHandle file = resolve(filepath);
			FileHandle tmpFile = resolveTmp(filepath);
			tmpFile.writeString(content, false);
			if(file.file().exists()) {
				file.file().delete();
			}
			tmpFile.file().renameTo(file.file());
		} catch (Exception e) {
			throw new PlayerDataException(e);
		}
	}

	@Override
	public <T extends GameDataSerializable> void readBytes(T result, String... filepath)
			throws PlayerDataException {
		if (filepath.length == 0) {
			throw new PlayerDataException("No file path specified");
		}
		try {
			FileHandle file = resolve(filepath);
			final InputStream inputStream = file.read();
			result.readData(new DataInputStream(inputStream));
			inputStream.close();
		} catch (Exception e) {
			throw new PlayerDataException(e);
		}
	}

	@Override
	public <T extends GameDataSerializable> void writeBytes(T obj, String... filepath)
			throws PlayerDataException {
		if (filepath.length == 0) {
			throw new PlayerDataException("No file path specified");
		}
		try {
			ensureDirectoryExistsForFile(filepath);
			FileHandle file = resolve(filepath);
			FileHandle tmpFile = resolveTmp(filepath);
			final OutputStream outputStream = tmpFile.write(false);
			obj.writeData(new DataOutputStream(outputStream));
			outputStream.flush();
			outputStream.close();
			if(file.file().exists()) {
				file.file().delete();
			}
			tmpFile.file().renameTo(file.file());
		} catch (Exception e) {
			throw new PlayerDataException(e);
		}
	}

	@Override
	public boolean delete(String... path) throws PlayerDataException {
		if (path.length == 0) {
			throw new PlayerDataException("No file path specified");
		}
		FileHandle file = resolve(path);
		if(file.isDirectory()) {
			return file.deleteDirectory();
		}
		return file.delete();
	}

	@Override
	public boolean hasFile(String... filepath) throws PlayerDataException {
		if (filepath.length == 0) {
			throw new PlayerDataException("No file path specified");
		}
		FileHandle file = resolve(filepath);
		if (file.exists()) {
			return !file.isDirectory();
		}
		return false;
	}

	@Override
	public boolean hasDirectory(String... path) throws PlayerDataException {
		if (path.length == 0) {
			throw new PlayerDataException("No path specified");
		}
		FileHandle directoryHandle = resolve(path);
		if (directoryHandle.exists()) {
			return directoryHandle.isDirectory();
		}
		return false;
	}

	@Override
	public void createDirectory(String... path) throws PlayerDataException {
		if (path.length == 0) {
			throw new PlayerDataException("No path specified");
		}
		FileHandle directory = resolve(path);
		if (directory.exists()) {
			return;
		}
		ensureDataDirectoryExists();
		directory.mkdirs();
	}

	@Override
	public void wipe() throws PlayerDataException {
		FileHandle directory = Gdx.files.absolute(saveDirectory);
		if (!directory.exists()) {
			return;
		}
		directory.emptyDirectory();
		directory.deleteDirectory();
	}

	private void ensureDataDirectoryExists() {
		FileHandle directory = Gdx.files.absolute(saveDirectory);
		if (directory.exists()) {
			return;
		}
		directory.mkdirs();
	}

	private void ensureDirectoryExistsForFile(String... filepath) {
		ensureDataDirectoryExists();

		FileHandle file = resolve(filepath);
		if (file.exists()) {
			return;
		}
		FileHandle parent = file.parent();
		if (parent.exists()) {
			return;
		}
		parent.mkdirs();
	}

	@Override
	public FileHandle getFileHandle(String... filepath) {
		return resolve(filepath);
	}

	private FileHandle resolve(String[] filepath) {
		return Gdx.files.absolute(Paths.get(saveDirectory, filepath).toString());
	}

	private FileHandle resolveTmp(String[] filepath) {
		final String [] tmpFilepath = Arrays.copyOf(filepath, filepath.length);
		tmpFilepath[tmpFilepath.length - 1] = tmpFilepath[tmpFilepath.length - 1] + ".tmp";
		return Gdx.files.absolute(Paths.get(saveDirectory, tmpFilepath).toString());
	}

	public String getSaveDirectory() {
		return saveDirectory;
	}
}