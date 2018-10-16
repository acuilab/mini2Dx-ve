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
package org.mini2Dx.core.di.bean;

import org.mini2Dx.core.Mdx;
import org.mini2Dx.core.exception.MdxException;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

/**
 * An implementation of {@link Bean} based on the
 * <a href="http://en.wikipedia.org/wiki/Prototype_pattern">prototype pattern</a>
 */
public class PrototypeBean extends Bean implements Runnable {
	private static final int MAXIMUM_PREPARED_PROTOTYPES = 3;

	private Object bean;
	private BlockingQueue<Object> prototypes;
	private ExecutorService executorService;

	public PrototypeBean(Object bean, ExecutorService executorService) {
		this.bean = bean;
		this.executorService = executorService;
		prototypes = new ArrayBlockingQueue<Object>(MAXIMUM_PREPARED_PROTOTYPES);
	}

	@Override
	public Object getInstance() {
		Object result = null;
		try {
			result = prototypes.take();
			executorService.submit(this);
		} catch (InterruptedException e) {
			e.printStackTrace();
			result = null;
		}
		return result;
	}

	public static Object duplicate(Object object)
			throws MdxException {
		return Mdx.di.beanUtils().cloneBean(object);
	}

	@Override
	public void run() {
		try {
			while (prototypes.size() < MAXIMUM_PREPARED_PROTOTYPES) {
				prototypes.offer(PrototypeBean.duplicate(bean));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
