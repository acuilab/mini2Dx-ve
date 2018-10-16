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
package org.mini2Dx.core.controller.xboxone;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;
import org.mini2Dx.core.controller.XboxOneController;
import org.mini2Dx.core.controller.button.XboxOneButton;
import org.mini2Dx.core.controller.deadzone.DeadZone;

/**
 * Windows bindings for Xbox One controller
 */
public class WindowsXboxOneController extends XboxOneController {
	public static final int BUTTON_A = 0;
	public static final int BUTTON_B = 1;
	public static final int BUTTON_X = 2;
	public static final int BUTTON_Y = 3;
	
	public static final int BUTTON_LEFT_SHOULDER = 4;
	public static final int BUTTON_RIGHT_SHOULDER = 5;
	
	public static final int BUTTON_VIEW = 6;
	public static final int BUTTON_MENU = 7;
	
	public static final int BUTTON_LEFT_STICK = 8;
	public static final int BUTTON_RIGHT_STICK = 9;
	
	//BUG: JInput does not detect home button
	public static final int BUTTON_HOME = -1;
	
	public static final int AXIS_LEFT_STICK_Y = 0;
	public static final int AXIS_LEFT_STICK_X = 1;
	
	public static final int AXIS_RIGHT_STICK_Y = 2;
	public static final int AXIS_RIGHT_STICK_X = 3;
	
	//BUG: JInput detects both triggers as same code
	public static final int AXIS_LEFT_TRIGGER = 4;
	public static final int AXIS_RIGHT_TRIGGER = 4;
	
	public static final int POV_DIRECTIONS = 0;
	
	private boolean up, down, left, right;
	
	public WindowsXboxOneController(Controller controller) {
		super(controller);
	}
	
	public WindowsXboxOneController(Controller controller, DeadZone leftStickDeadZone, DeadZone rightStickDeadZone) {
		super(controller, leftStickDeadZone, rightStickDeadZone);
	}
	
	@Override
	public void connected(Controller controller) {
	}

	@Override
	public void disconnected(Controller controller) {
		notifyDisconnected();
	}

	@Override
	public boolean buttonDown(Controller controller, int buttonCode) {
		switch(buttonCode) {
		case BUTTON_MENU:
			return notifyButtonDown(XboxOneButton.MENU);
		case BUTTON_VIEW:
			return notifyButtonDown(XboxOneButton.VIEW);
		case BUTTON_LEFT_STICK:
			return notifyButtonDown(XboxOneButton.LEFT_STICK);
		case BUTTON_RIGHT_STICK:
			return notifyButtonDown(XboxOneButton.RIGHT_STICK);
		case BUTTON_LEFT_SHOULDER:
			return notifyButtonDown(XboxOneButton.LEFT_SHOULDER);
		case BUTTON_RIGHT_SHOULDER:
			return notifyButtonDown(XboxOneButton.RIGHT_SHOULDER);
		case BUTTON_HOME:
			return notifyButtonDown(XboxOneButton.HOME);
		case BUTTON_A:
			return notifyButtonDown(XboxOneButton.A);
		case BUTTON_B:
			return notifyButtonDown(XboxOneButton.B);
		case BUTTON_X:
			return notifyButtonDown(XboxOneButton.X);
		case BUTTON_Y:
			return notifyButtonDown(XboxOneButton.Y);
		}
		return false;
	}

	@Override
	public boolean buttonUp(Controller controller, int buttonCode) {
		switch(buttonCode) {
		case BUTTON_MENU:
			return notifyButtonUp(XboxOneButton.MENU);
		case BUTTON_VIEW:
			return notifyButtonUp(XboxOneButton.VIEW);
		case BUTTON_LEFT_STICK:
			return notifyButtonUp(XboxOneButton.LEFT_STICK);
		case BUTTON_RIGHT_STICK:
			return notifyButtonUp(XboxOneButton.RIGHT_STICK);
		case BUTTON_LEFT_SHOULDER:
			return notifyButtonUp(XboxOneButton.LEFT_SHOULDER);
		case BUTTON_RIGHT_SHOULDER:
			return notifyButtonUp(XboxOneButton.RIGHT_SHOULDER);
		case BUTTON_HOME:
			return notifyButtonUp(XboxOneButton.HOME);
		case BUTTON_A:
			return notifyButtonUp(XboxOneButton.A);
		case BUTTON_B:
			return notifyButtonUp(XboxOneButton.B);
		case BUTTON_X:
			return notifyButtonUp(XboxOneButton.X);
		case BUTTON_Y:
			return notifyButtonUp(XboxOneButton.Y);
		}
		return false;
	}

	@Override
	public boolean axisMoved(Controller controller, int axisCode, float value) {
		switch(axisCode) {
		case AXIS_LEFT_STICK_X:
			return notifyLeftStickXMoved(value);
		case AXIS_LEFT_STICK_Y:
			return notifyLeftStickYMoved(value);
		case AXIS_RIGHT_STICK_X:
			return notifyRightStickXMoved(value);
		case AXIS_RIGHT_STICK_Y:
			return notifyRightStickYMoved(value);
		case AXIS_RIGHT_TRIGGER:
			return notifyRightTriggerMoved(value);
		}
		return false;
	}

	@Override
	public boolean povMoved(Controller controller, int povCode, PovDirection value) {
		switch(povCode) {
		case POV_DIRECTIONS:
			switch(value) {
			case center:
				if(up) {
					notifyButtonUp(XboxOneButton.UP);
					up = false;
				}
				if(down) {
					notifyButtonUp(XboxOneButton.DOWN);
					down = false;
				}
				if(left) {
					notifyButtonUp(XboxOneButton.LEFT);
					left = false;
				}
				if(right) {
					notifyButtonUp(XboxOneButton.RIGHT);
					right = false;
				}
				break;
			case east:
				if(up) {
					notifyButtonUp(XboxOneButton.UP);
					up = false;
				}
				if(down) {
					notifyButtonUp(XboxOneButton.DOWN);
					down = false;
				}
				if(left) {
					notifyButtonUp(XboxOneButton.LEFT);
					left = false;
				}
				if(!right) {
					right = true;
					notifyButtonDown(XboxOneButton.RIGHT);
				}
				break;
			case north:
				if(left) {
					notifyButtonUp(XboxOneButton.LEFT);
					left = false;
				}
				if(right) {
					notifyButtonUp(XboxOneButton.RIGHT);
					right = false;
				}
				if(down) {
					notifyButtonUp(XboxOneButton.DOWN);
					down = false;
				}
				if(!up) {
					up = true;
					notifyButtonDown(XboxOneButton.UP);
				}
				break;
			case northEast:
				if(left) {
					notifyButtonUp(XboxOneButton.LEFT);
					left = false;
				}
				if(down) {
					notifyButtonUp(XboxOneButton.DOWN);
					down = false;
				}
				if(!right) {
					notifyButtonDown(XboxOneButton.RIGHT);
					right = true;
				}
				if(!up) {
					up = true;
					notifyButtonDown(XboxOneButton.UP);
				}
				break;
			case northWest:
				if(right) {
					notifyButtonUp(XboxOneButton.RIGHT);
					right = false;
				}
				if(down) {
					notifyButtonUp(XboxOneButton.DOWN);
					down = false;
				}
				if(!left) {
					notifyButtonDown(XboxOneButton.LEFT);
					left = true;
				}
				if(!up) {
					up = true;
					notifyButtonDown(XboxOneButton.UP);
				}
				break;
			case south:
				if(left) {
					notifyButtonUp(XboxOneButton.LEFT);
					left = false;
				}
				if(right) {
					notifyButtonUp(XboxOneButton.RIGHT);
					right = false;
				}
				if(up) {
					notifyButtonUp(XboxOneButton.UP);
					up = false;
				}
				if(!down) {
					notifyButtonDown(XboxOneButton.DOWN);
					down = true;
				}
				break;
			case southEast:
				if(left) {
					notifyButtonUp(XboxOneButton.LEFT);
					left = false;
				}
				if(up) {
					notifyButtonUp(XboxOneButton.UP);
					up = false;
				}
				if(!right) {
					notifyButtonDown(XboxOneButton.RIGHT);
					right = true;
				}
				if(!down) {
					notifyButtonDown(XboxOneButton.DOWN);
					down = true;
				}
				break;
			case southWest:
				if(right) {
					notifyButtonUp(XboxOneButton.RIGHT);
					right = false;
				}
				if(up) {
					notifyButtonUp(XboxOneButton.UP);
					up = false;
				}
				if(!left) {
					notifyButtonDown(XboxOneButton.LEFT);
					left = true;
				}
				if(!down) {
					notifyButtonDown(XboxOneButton.DOWN);
					down = true;
				}
				break;
			case west:
				if(up) {
					notifyButtonUp(XboxOneButton.UP);
					up = false;
				}
				if(down) {
					notifyButtonUp(XboxOneButton.DOWN);
					down = false;
				}
				if(right) {
					notifyButtonUp(XboxOneButton.LEFT);
					right = false;
				}
				if(!left) {
					left = true;
					notifyButtonDown(XboxOneButton.LEFT);
				}
				break;
			default:
				break;
			}
			break;
		}
		return false;
	}

	@Override
	public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
		return false;
	}

	@Override
	public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
		return false;
	}

	@Override
	public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
		return false;
	}
}