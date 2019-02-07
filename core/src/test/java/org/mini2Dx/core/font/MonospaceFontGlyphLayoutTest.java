package org.mini2Dx.core.font;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MonospaceFontGlyphLayoutTest {
	private static final int FONT_FRAME_WIDTH = 32;
	private static final int FONT_FRAME_HEIGHT = 32;
	private static final int FONT_CHARACTER_WIDTH = 16;
	private static final int FONT_LINE_HEIGHT = 24;
	private static final int FONT_SPACING = 1;

	private final Mockery mockery = new Mockery();

	private final MonospaceFont.FontParameters fontParameters = new MonospaceFont.FontParameters();
	private MonospaceFont monospaceFont;
	private MonospaceFontGlyphLayout glyphLayout;

	@Before
	public void setUp() {
		fontParameters.frameWidth = FONT_FRAME_WIDTH;
		fontParameters.frameHeight = FONT_FRAME_HEIGHT;
		fontParameters.characterWidth = FONT_CHARACTER_WIDTH;
		fontParameters.lineHeight = FONT_LINE_HEIGHT;

		monospaceFont = new MonospaceFont(fontParameters);
		glyphLayout = (MonospaceFontGlyphLayout) monospaceFont.newGlyphLayout();
	}

	@After
	public void teardown() {
		mockery.assertIsSatisfied();
	}

	@Test
	public void testCalculateMaxCharactersBeforeWrap() {
		final int estimate = 5;
		final float targetWidth = (5 * FONT_CHARACTER_WIDTH) + (4 * FONT_SPACING);

		Assert.assertEquals(3, glyphLayout.calculateMaxCharactersBeforeWrap("abc\ndef", 0, estimate, targetWidth));
		Assert.assertEquals(3, glyphLayout.calculateMaxCharactersBeforeWrap("abc def", 0, estimate, targetWidth));
		Assert.assertEquals(3, glyphLayout.calculateMaxCharactersBeforeWrap("abc def ghi", 4, estimate, targetWidth));
		Assert.assertEquals(5, glyphLayout.calculateMaxCharactersBeforeWrap("abcdef", 0, estimate, targetWidth));
		Assert.assertEquals(2, glyphLayout.calculateMaxCharactersBeforeWrap("ab\ncdef", 0, estimate, targetWidth));
		Assert.assertEquals(2, glyphLayout.calculateMaxCharactersBeforeWrap("abc def", 5, estimate, targetWidth));
		Assert.assertEquals(0, glyphLayout.calculateMaxCharactersBeforeWrap("\n", 0, estimate, targetWidth));
		Assert.assertEquals(0, glyphLayout.calculateMaxCharactersBeforeWrap("", 0, estimate, targetWidth));
		Assert.assertEquals(0, glyphLayout.calculateMaxCharactersBeforeWrap("a", 0, estimate, FONT_CHARACTER_WIDTH - 1f));
	}

	@Test
	public void testLayoutLeftAlignLineBreak() {
		final String str = "abc\ndef";

		glyphLayout.setText(str);

		float expectedX = 0f;
		float expectedY = 0f;
		for(int i = 0; i < str.length(); i++) {
			if(str.charAt(i) == '\n') {
				expectedX = 0f;
				expectedY += FONT_LINE_HEIGHT;
				continue;
			}
			Assert.assertEquals(expectedX, glyphLayout.getGlyphs().get(i).x, 0.01f);
			Assert.assertEquals(expectedY, glyphLayout.getGlyphs().get(i).y, 0.01f);
			Assert.assertEquals(str.charAt(i), glyphLayout.getGlyphs().get(i).glyphChar);

			expectedX += FONT_CHARACTER_WIDTH + FONT_SPACING;
		}

		Assert.assertEquals((FONT_CHARACTER_WIDTH * 3f) + (FONT_SPACING * 2f), glyphLayout.getWidth(), 0.01f);
		Assert.assertEquals(FONT_LINE_HEIGHT * 2f, glyphLayout.getHeight(), 0.01f);
	}

	@Test
	public void testLayoutLeftAlignWrap() {
		final String str = "abc def";

		glyphLayout.setText(str, Color.BLUE, (FONT_CHARACTER_WIDTH + FONT_SPACING) * 5f, Align.left, true);

		float expectedX = 0f;
		float expectedY = 0f;
		for(int i = 0; i < str.length(); i++) {
			if(i == 3) {
				expectedX = 0f;
				expectedY += FONT_LINE_HEIGHT;
				continue;
			}
			Assert.assertEquals(expectedX, glyphLayout.getGlyphs().get(i).x, 0.01f);
			Assert.assertEquals(expectedY, glyphLayout.getGlyphs().get(i).y, 0.01f);
			Assert.assertEquals(str.charAt(i), glyphLayout.getGlyphs().get(i).glyphChar);

			expectedX += FONT_CHARACTER_WIDTH + FONT_SPACING;
		}

		Assert.assertEquals((FONT_CHARACTER_WIDTH * 3f) + (FONT_SPACING * 2f), glyphLayout.getWidth(), 0.01f);
		Assert.assertEquals(FONT_LINE_HEIGHT * 2f, glyphLayout.getHeight(), 0.01f);
	}

	@Test
	public void testLayoutLeftAlignTooSmall() {
		final String str = "ab";

		glyphLayout.setText(str, Color.BLUE, FONT_CHARACTER_WIDTH - 1f, Align.left, true);

		Assert.assertEquals(FONT_CHARACTER_WIDTH, glyphLayout.getWidth(), 0.01f);
		Assert.assertEquals(FONT_LINE_HEIGHT * 2f, glyphLayout.getHeight(), 0.01f);
	}

	@Test
	public void testLayoutRightAlignLineBreak() {
		final String str = "abc\ndef";

		glyphLayout.setText(str, Color.BLUE, -1f, Align.right, true);

		final float lineWidth = (3 * FONT_CHARACTER_WIDTH) + (FONT_SPACING * 3);
		float expectedX = 0f;
		float expectedY = 0f;
		for(int i = 0; i < str.length(); i++) {
			if(str.charAt(i) == '\n') {
				expectedX = 0f;
				expectedY += FONT_LINE_HEIGHT;
				continue;
			}

			Assert.assertEquals(expectedX, glyphLayout.getGlyphs().get(i).x, 0.01f);
			Assert.assertEquals(expectedY, glyphLayout.getGlyphs().get(i).y, 0.01f);
			Assert.assertEquals(str.charAt(i), glyphLayout.getGlyphs().get(i).glyphChar);

			expectedX += FONT_CHARACTER_WIDTH + FONT_SPACING;
			if(str.charAt(i) == '\n') {
				expectedX = 0f;
				expectedY += FONT_LINE_HEIGHT;
			}
		}

		Assert.assertEquals((FONT_CHARACTER_WIDTH * 3f) + (FONT_SPACING * 2f), glyphLayout.getWidth(), 0.01f);
		Assert.assertEquals(FONT_LINE_HEIGHT * 2f, glyphLayout.getHeight(), 0.01f);
	}

	@Test
	public void testLayoutRightAlignWrap() {
		final String str = "abc def";
		final float offset = 2f;
		final float targetWidth = (FONT_CHARACTER_WIDTH * 5f) + (FONT_SPACING * 4f) + offset;

		glyphLayout.setText(str, Color.BLUE, targetWidth, Align.right, true);

		float expectedX = targetWidth - (FONT_CHARACTER_WIDTH * 3f) - (FONT_SPACING * 2f);
		float expectedY = 0f;
		for(int i = 0; i < str.length(); i++) {
			if(i == 3) {
				expectedX = targetWidth - (FONT_CHARACTER_WIDTH * 3f) - (FONT_SPACING * 2f);
				expectedY += FONT_LINE_HEIGHT;
				continue;
			}
			Assert.assertEquals(expectedX, glyphLayout.getGlyphs().get(i).x, 0.01f);
			Assert.assertEquals(expectedY, glyphLayout.getGlyphs().get(i).y, 0.01f);
			Assert.assertEquals(str.charAt(i), glyphLayout.getGlyphs().get(i).glyphChar);

			expectedX += FONT_CHARACTER_WIDTH + FONT_SPACING;
		}

		Assert.assertEquals(targetWidth, glyphLayout.getWidth(), 0.01f);
		Assert.assertEquals(FONT_LINE_HEIGHT * 2f, glyphLayout.getHeight(), 0.01f);
	}

	@Test
	public void testLayoutRightAlignTooSmall() {
		final String str = "ab";

		glyphLayout.setText(str, Color.BLUE, FONT_CHARACTER_WIDTH - 1f, Align.right, true);

		Assert.assertEquals(FONT_CHARACTER_WIDTH - 1f, glyphLayout.getWidth(), 0.01f);
		Assert.assertEquals(FONT_LINE_HEIGHT * 2f, glyphLayout.getHeight(), 0.01f);
	}

	@Test
	public void testLayoutCenterAlignLineBreak() {
		final String str = "abc\nde";
		final float lineWidth = (FONT_CHARACTER_WIDTH * 3f) + (FONT_SPACING * 2f);

		glyphLayout.setText(str, Color.BLUE, -1f, Align.center, true);

		float expectedX = 0f;
		float expectedY = 0f;

		for(int i = 0; i < str.length(); i++) {
			if(str.charAt(i) == '\n') {
				expectedX = MathUtils.round((lineWidth * 0.5f) - (((FONT_CHARACTER_WIDTH * 2f) + FONT_SPACING) * 0.5f));
				expectedY += FONT_LINE_HEIGHT;
				continue;
			}

			Assert.assertEquals(expectedX, glyphLayout.getGlyphs().get(i).x, 0.01f);
			Assert.assertEquals(expectedY, glyphLayout.getGlyphs().get(i).y, 0.01f);
			Assert.assertEquals(str.charAt(i), glyphLayout.getGlyphs().get(i).glyphChar);

			expectedX += FONT_CHARACTER_WIDTH + FONT_SPACING;
		}

		Assert.assertEquals((FONT_CHARACTER_WIDTH * 3f) + (FONT_SPACING * 2f), glyphLayout.getWidth(), 0.01f);
		Assert.assertEquals(FONT_LINE_HEIGHT * 2f, glyphLayout.getHeight(), 0.01f);
	}

	@Test
	public void testLayoutCenterAlignWrap() {
		final String str = "abc de";
		final float lineWidth = (FONT_CHARACTER_WIDTH * 5f) + (FONT_SPACING * 4f);

		glyphLayout.setText(str, Color.BLUE, lineWidth, Align.center, true);

		float expectedX = MathUtils.round((lineWidth * 0.5f) - (((FONT_CHARACTER_WIDTH * 3f) + (FONT_SPACING * 2f)) * 0.5f));
		float expectedY = 0f;

		for(int i = 0; i < str.length(); i++) {
			if(i == 3) {
				expectedX = MathUtils.round((lineWidth * 0.5f) - (((FONT_CHARACTER_WIDTH * 2f) + FONT_SPACING) * 0.5f));
				expectedY += FONT_LINE_HEIGHT;
				continue;
			}
			Assert.assertEquals(expectedX, glyphLayout.getGlyphs().get(i).x, 0.01f);
			Assert.assertEquals(expectedY, glyphLayout.getGlyphs().get(i).y, 0.01f);
			Assert.assertEquals(str.charAt(i), glyphLayout.getGlyphs().get(i).glyphChar);

			expectedX += FONT_CHARACTER_WIDTH + FONT_SPACING;
		}

		Assert.assertEquals(lineWidth, glyphLayout.getWidth(), 0.01f);
		Assert.assertEquals(FONT_LINE_HEIGHT * 2f, glyphLayout.getHeight(), 0.01f);
	}

	@Test
	public void testLayoutCenterAlignTooSmall() {
		final String str = "ab";

		glyphLayout.setText(str, Color.BLUE, FONT_CHARACTER_WIDTH - 1f, Align.center, true);

		Assert.assertEquals(FONT_CHARACTER_WIDTH - 1f, glyphLayout.getWidth(), 0.01f);
		Assert.assertEquals(FONT_LINE_HEIGHT * 2f, glyphLayout.getHeight(), 0.01f);
	}
}
