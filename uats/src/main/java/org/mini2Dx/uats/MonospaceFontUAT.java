package org.mini2Dx.uats;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.IntIntMap;
import org.mini2Dx.core.font.MonospaceFont;
import org.mini2Dx.core.game.GameContainer;
import org.mini2Dx.core.graphics.Graphics;
import org.mini2Dx.core.screen.BasicGameScreen;
import org.mini2Dx.core.screen.GameScreen;
import org.mini2Dx.core.screen.ScreenManager;
import org.mini2Dx.uats.util.ScreenIds;

public class MonospaceFontUAT extends BasicGameScreen implements MonospaceFont.FontRenderListener {
	private final int CHARACTER_WIDTH = 5;
	private final int LINE_HEIGHT = 12;

	private final AssetManager assetManager;
	private final MonospaceFont font;

	public MonospaceFontUAT(AssetManager assetManager) {
		super();
		this.assetManager = assetManager;

		final MonospaceFont.FontParameters parameters = new MonospaceFont.FontParameters();
		parameters.texturePath = "gnsh-bitmapfont.png";
		parameters.frameWidth = CHARACTER_WIDTH;
		parameters.frameHeight = LINE_HEIGHT;
		parameters.characterWidth = CHARACTER_WIDTH;
		parameters.lineHeight = LINE_HEIGHT;
		parameters.spacing = 1;
		parameters.overrideCharacterIndices = new IntIntMap();
		initCharacterIndices(parameters);

		font = new MonospaceFont(parameters);
	}

	@Override
	public void initialise(GameContainer gc) {

	}

	@Override
	public void update(GameContainer gc, ScreenManager<? extends GameScreen> screenManager, float delta) {
		if(!font.load(assetManager)) {
			return;
		}
	}

	@Override
	public void interpolate(GameContainer gc, float alpha) {

	}

	@Override
	public void render(GameContainer gc, Graphics g) {
		if(!font.load(assetManager)) {
			return;
		}

		font.setColor(Color.RED);

		float renderY = 4f;

		font.draw(g, "Left align\nno params", 4f, renderY);
		renderY += font.getLineHeight() * 4f;

		font.draw(g, "Left align\nwrap with line break", 4f, renderY, (CHARACTER_WIDTH * 10) + 9, Align.left, true);
		renderY += font.getLineHeight() * 4f;

		font.draw(g, "Left align wrap with params", 4f, renderY, (CHARACTER_WIDTH * 10) + 9, Align.left, true);
		renderY += font.getLineHeight() * 4f;

		font.draw(g, "Right align with params", 4f, renderY, (CHARACTER_WIDTH * 10) + 9, Align.right, true);
		renderY += font.getLineHeight() * 4f;

		font.draw(g, "Right align\nline break", 4f, renderY, -1f, Align.right, true);
		renderY += font.getLineHeight() * 4f;

		font.draw(g, "Center align with params", 4f, renderY, (CHARACTER_WIDTH * 10) + 9, Align.center, true);
		renderY += font.getLineHeight() * 4f;

		font.draw(g, "Center align\nline break", 4f, renderY, -1f, Align.center, true);
		renderY += font.getLineHeight() * 4f;
	}

	@Override
	public int getId() {
		return ScreenIds.getScreenId(MonospaceFontUAT.class);
	}

	@Override
	public boolean preRenderChar(Graphics g, char c, float charRenderX, float charRenderY, float charRenderWidth, float charRenderHeight) {

		return true;
	}

	@Override
	public void postRenderChar(Graphics g, char c, float charRenderX, float charRenderY, float charRenderWidth, float charRenderHeight) {
	}

	private void initCharacterIndices(final MonospaceFont.FontParameters parameters) {
		parameters.overrideCharacterIndices.put(' ', 0);
		parameters.overrideCharacterIndices.put('!', 1);
		parameters.overrideCharacterIndices.put('"', 2);
		parameters.overrideCharacterIndices.put('#', 3);
		parameters.overrideCharacterIndices.put('$', 4);
		parameters.overrideCharacterIndices.put('%', 5);
		parameters.overrideCharacterIndices.put('&', 6);
		parameters.overrideCharacterIndices.put('\'', 7);
		parameters.overrideCharacterIndices.put('(', 8);
		parameters.overrideCharacterIndices.put(')', 9);
		parameters.overrideCharacterIndices.put('*', 10);
		parameters.overrideCharacterIndices.put('+', 11);
		parameters.overrideCharacterIndices.put(',', 12);
		parameters.overrideCharacterIndices.put('-', 13);
		parameters.overrideCharacterIndices.put('.', 14);
		parameters.overrideCharacterIndices.put('/', 15);
		parameters.overrideCharacterIndices.put('0', 16);
		parameters.overrideCharacterIndices.put('1', 17);
		parameters.overrideCharacterIndices.put('2', 18);
		parameters.overrideCharacterIndices.put('3', 19);
		parameters.overrideCharacterIndices.put('4', 20);
		parameters.overrideCharacterIndices.put('5', 21);
		parameters.overrideCharacterIndices.put('6', 22);
		parameters.overrideCharacterIndices.put('7', 23);
		parameters.overrideCharacterIndices.put('8', 24);
		parameters.overrideCharacterIndices.put('9', 25);
		parameters.overrideCharacterIndices.put(':', 26);
		parameters.overrideCharacterIndices.put(';', 27);
		parameters.overrideCharacterIndices.put('<', 28);
		parameters.overrideCharacterIndices.put('=', 29);
		parameters.overrideCharacterIndices.put('>', 30);
		parameters.overrideCharacterIndices.put('?', 31);
		parameters.overrideCharacterIndices.put('@', 32);
		parameters.overrideCharacterIndices.put('A', 33);
		parameters.overrideCharacterIndices.put('B', 34);
		parameters.overrideCharacterIndices.put('C', 35);
		parameters.overrideCharacterIndices.put('D', 36);
		parameters.overrideCharacterIndices.put('E', 37);
		parameters.overrideCharacterIndices.put('F', 38);
		parameters.overrideCharacterIndices.put('G', 39);
		parameters.overrideCharacterIndices.put('H', 40);
		parameters.overrideCharacterIndices.put('I', 41);
		parameters.overrideCharacterIndices.put('J', 42);
		parameters.overrideCharacterIndices.put('K', 43);
		parameters.overrideCharacterIndices.put('L', 44);
		parameters.overrideCharacterIndices.put('M', 45);
		parameters.overrideCharacterIndices.put('N', 46);
		parameters.overrideCharacterIndices.put('O', 47);
		parameters.overrideCharacterIndices.put('P', 48);
		parameters.overrideCharacterIndices.put('Q', 49);
		parameters.overrideCharacterIndices.put('R', 50);
		parameters.overrideCharacterIndices.put('S', 51);
		parameters.overrideCharacterIndices.put('T', 52);
		parameters.overrideCharacterIndices.put('U', 53);
		parameters.overrideCharacterIndices.put('V', 54);
		parameters.overrideCharacterIndices.put('W', 55);
		parameters.overrideCharacterIndices.put('X', 56);
		parameters.overrideCharacterIndices.put('Y', 57);
		parameters.overrideCharacterIndices.put('Z', 58);
		parameters.overrideCharacterIndices.put('[', 59);
		parameters.overrideCharacterIndices.put('\\', 60);
		parameters.overrideCharacterIndices.put(']', 61);
		parameters.overrideCharacterIndices.put('^', 62);
		parameters.overrideCharacterIndices.put('_', 63);
		parameters.overrideCharacterIndices.put('`', 64);
		parameters.overrideCharacterIndices.put('a', 65);
		parameters.overrideCharacterIndices.put('b', 66);
		parameters.overrideCharacterIndices.put('c', 67);
		parameters.overrideCharacterIndices.put('d', 68);
		parameters.overrideCharacterIndices.put('e', 69);
		parameters.overrideCharacterIndices.put('f', 70);
		parameters.overrideCharacterIndices.put('g', 71);
		parameters.overrideCharacterIndices.put('h', 72);
		parameters.overrideCharacterIndices.put('i', 73);
		parameters.overrideCharacterIndices.put('j', 74);
		parameters.overrideCharacterIndices.put('k', 75);
		parameters.overrideCharacterIndices.put('l', 76);
		parameters.overrideCharacterIndices.put('m', 77);
		parameters.overrideCharacterIndices.put('n', 78);
		parameters.overrideCharacterIndices.put('o', 79);
		parameters.overrideCharacterIndices.put('p', 80);
		parameters.overrideCharacterIndices.put('q', 81);
		parameters.overrideCharacterIndices.put('r', 82);
		parameters.overrideCharacterIndices.put('s', 83);
		parameters.overrideCharacterIndices.put('t', 84);
		parameters.overrideCharacterIndices.put('u', 85);
		parameters.overrideCharacterIndices.put('v', 86);
		parameters.overrideCharacterIndices.put('w', 87);
		parameters.overrideCharacterIndices.put('x', 88);
		parameters.overrideCharacterIndices.put('y', 89);
		parameters.overrideCharacterIndices.put('z', 90);
		parameters.overrideCharacterIndices.put('{', 91);
		parameters.overrideCharacterIndices.put('|', 92);
		parameters.overrideCharacterIndices.put('}', 93);
		parameters.overrideCharacterIndices.put('~', 94);
	}
}
