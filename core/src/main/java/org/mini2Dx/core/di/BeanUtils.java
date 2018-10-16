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
package org.mini2Dx.core.di;

import org.mini2Dx.core.exception.MdxException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Bean utility functions
 */
public class BeanUtils {
	/**
	 * Creates a deep copy of an {@link Object}
	 * @param bean The {@link Object} to copy
	 * @return A new instance of {@link Object} with all its properties copied
	 * @throws MdxException Thrown if an exception occurs during the copy
	 */
	public Object cloneBean(Object bean) throws MdxException {
		try {
			Class<?> currentClass = bean.getClass();
			Object result = currentClass.newInstance();
			
			while (!currentClass.equals(Object.class)) {
				for (Field field : currentClass.getDeclaredFields()) {
					field.setAccessible(true);
					if(Modifier.isFinal(field.getModifiers())) {
						continue;
					}
					Object value = field.get(bean);
					field.set(result, value);
				}
				currentClass = currentClass.getSuperclass();
			}
			return result;
		} catch (IllegalAccessException e) {
			throw new MdxException(e.getMessage(), e);
		} catch (InstantiationException e) {
			throw new MdxException(e.getMessage(), e);
		}
	}
}
