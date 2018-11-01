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
package org.mini2Dx.core.controller;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.utils.Array;
import org.mini2Dx.core.controller.button.PS4Button;
import org.mini2Dx.core.controller.deadzone.DeadZone;
import org.mini2Dx.core.controller.deadzone.NoopDeadZone;
import org.mini2Dx.core.controller.deadzone.RadialDeadZone;
import org.mini2Dx.core.controller.ps4.PS4ControllerListener;

/**
 * Base class for PS4 controller mapping implementations
 */
public abstract class PS4Controller implements MdxController<PS4ControllerListener> {
	public static final String WINDOWS_ID = "Wireless Controller".toLowerCase();
	public static final String MAC_ID = "Sony Interactive Entertainment Wireless Controller".toLowerCase();
	
	private final Controller controller;
	private final Array<PS4ControllerListener> listeners = new Array<PS4ControllerListener>(true, 2);
	
    private DeadZone leftStickDeadZone, rightStickDeadZone;
	private DeadZone l2DeadZone, r2DeadZone;
	private boolean l2, r2;
    
	public PS4Controller(Controller controller) {
		this(controller, new NoopDeadZone(), new NoopDeadZone());
	}
	
	public PS4Controller(Controller controller, DeadZone leftStickDeadZone, DeadZone rightStickDeadZone) {
		this.controller = controller;
		this.leftStickDeadZone = leftStickDeadZone;
		this.rightStickDeadZone = rightStickDeadZone;
		this.l2DeadZone = new RadialDeadZone();
		this.r2DeadZone = new RadialDeadZone();
		controller.addListener(this);
	}
    
	protected boolean notifyDisconnected() {
		for(PS4ControllerListener listener : listeners) {
			listener.disconnected(this);
		}
		return false;
	}
	
	protected boolean notifyButtonDown(PS4Button button) {
		for(PS4ControllerListener listener : listeners) {
			if(listener.buttonDown(this, button)) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean notifyButtonUp(PS4Button button) {
		for(PS4ControllerListener listener : listeners) {
			if(listener.buttonUp(this, button)) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean notifyLeftStickXMoved(float value) {
		leftStickDeadZone.updateX(value);
		for(PS4ControllerListener listener : listeners) {
			if(listener.leftStickXMoved(this, leftStickDeadZone.getX())) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean notifyLeftStickYMoved(float value) {
		leftStickDeadZone.updateY(value);
		for(PS4ControllerListener listener : listeners) {
			if(listener.leftStickYMoved(this, leftStickDeadZone.getY())) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean notifyRightStickXMoved(float value) {
		rightStickDeadZone.updateX(value);
		for(PS4ControllerListener listener : listeners) {
			if(listener.rightStickXMoved(this, rightStickDeadZone.getX())) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean notifyRightStickYMoved(float value) {
		rightStickDeadZone.updateY(value);
		for(PS4ControllerListener listener : listeners) {
			if(listener.rightStickYMoved(this, rightStickDeadZone.getY())) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean notifyL2Moved(float value) {
		l2DeadZone.updateY(value);
		if(l2DeadZone.getY() >= 0.5f && !l2) {
			notifyButtonDown(PS4Button.L2);
			l2 = true;
		} else if(l2DeadZone.getY() < 0.5f && l2) {
			notifyButtonUp(PS4Button.L2);
			l2 = false;
		}
		for(PS4ControllerListener listener : listeners) {
			if(listener.l2Moved(this, value)) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean notifyR2Moved(float value) {
		r2DeadZone.updateY(value);
		if(r2DeadZone.getY() >= 0.5f && !r2) {
			notifyButtonDown(PS4Button.R2);
			r2 = true;
		} else if(r2DeadZone.getY() < 0.5f && r2) {
			notifyButtonUp(PS4Button.R2);
			r2 = false;
		}
		for(PS4ControllerListener listener : listeners) {
			if(listener.r2Moved(this, value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ControllerType getControllerType() {
		return ControllerType.PS4;
	}
	
	@Override
    public void addListener(int index, PS4ControllerListener listener) {
		listeners.insert(index, listener);
	}
	
	@Override
    public void addListener(PS4ControllerListener listener) {
    	listeners.add(listener);
    }
	
	@Override
    public void removeListener(int index) {
		listeners.removeIndex(index);
	}
    
    @Override
    public void removeListener(PS4ControllerListener listener) {
    	listeners.removeValue(listener, false);
    }
    
    @Override
    public PS4ControllerListener getListener(int index) {
    	return listeners.get(index);
    }
	
    @Override
	public int getTotalListeners() {
		return listeners.size;
	}
    
    @Override
	public void clearListeners() {
		listeners.clear();
	}

	public DeadZone getLeftStickDeadZone() {
		return leftStickDeadZone;
	}

	public void setLeftStickDeadZone(DeadZone leftStickDeadZone) {
		if(leftStickDeadZone == null) {
			leftStickDeadZone = new NoopDeadZone();
		}
		this.leftStickDeadZone = leftStickDeadZone;
	}

	public DeadZone getRightStickDeadZone() {
		return rightStickDeadZone;
	}

	public void setRightStickDeadZone(DeadZone rightStickDeadZone) {
		if(rightStickDeadZone == null) {
			rightStickDeadZone = new NoopDeadZone();
		}
		this.rightStickDeadZone = rightStickDeadZone;
	}

	public DeadZone getL2DeadZone() {
		return l2DeadZone;
	}

	public void setL2DeadZone(DeadZone l2DeadZone) {
		if(l2DeadZone == null) {
			l2DeadZone = new NoopDeadZone();
		}
		this.l2DeadZone = l2DeadZone;
	}

	public DeadZone getR2DeadZone() {
		return r2DeadZone;
	}

	public void setR2DeadZone(DeadZone r2DeadZone) {
		if(r2DeadZone == null) {
			r2DeadZone = new NoopDeadZone();
		}
		this.r2DeadZone = r2DeadZone;
	}
}
