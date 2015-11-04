/******************************************************************************
 * Spine Runtimes Software License
 * Version 2.1
 * 
 * Copyright (c) 2013, Esoteric Software
 * All rights reserved.
 * 
 * You are granted a perpetual, non-exclusive, non-sublicensable and
 * non-transferable license to install, execute and perform the Spine Runtimes
 * Software (the "Software") solely for internal use. Without the written
 * permission of Esoteric Software (typically granted by licensing Spine), you
 * may not (a) modify, translate, adapt or otherwise create derivative works,
 * improvements of the Software or develop new applications using the Software
 * or (b) remove, delete, alter or obscure any trademarks or any copyright,
 * trademark, patent or other intellectual property or proprietary rights notices
 * on or in the Software, including any copy thereof. Redistributions in binary
 * or source form must include this license and terms.
 * 
 * THIS SOFTWARE IS PROVIDED BY ESOTERIC SOFTWARE "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL ESOTERIC SOFTARE BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *****************************************************************************/

package com.esotericsoftware.spine.superspineboy;

import static com.badlogic.gdx.math.Interpolation.*;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;
import static com.esotericsoftware.spine.superspineboy.Model.*;
import static com.esotericsoftware.spine.superspineboy.View.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.esotericsoftware.spine.SkeletonRendererDebug;

/** The user interface displayed on top of the game (menu, health bar, splash screens). */
class UI extends InputAdapter {
	static final Color gray = new Color(0.15f, 0.15f, 0.15f, 1);

	View view;
	Model model;

	ShapeRenderer shapes;
	SkeletonRendererDebug skeletonRendererDebug;
	Stage stage;
	Skin skin;
	Label fpsLabel, bindsLabel;
	TextButton debugButton, zoomButton, bgButton;
	TextButton speed200Button, speed150Button, speed100Button, speed33Button, speed15Button, speed3Button, pauseButton;
	Table splashTable;
	Image splashImage, splashTextImage;
	ProgressBar healthBar;
	TextButton fullscreenButton, restartButton, menuButton;
	Table menu;
	Vector2 temp = new Vector2();

	int windowWidth, windowHeight;
	float inputTimer;
	boolean hasSplash;

	UI (final View view) {
		this.view = view;
		this.model = view.model;

		shapes = new ShapeRenderer();

		skeletonRendererDebug = new SkeletonRendererDebug(shapes);
		skeletonRendererDebug.setScale(scale);
		// skeletonRendererDebug.setPremultipliedAlpha(true);

		stage = new Stage(new ScreenViewport());
		loadSkin();

		create();
		layout();
		events();

		showSplash(view.assets.titleRegion, view.assets.startRegion);
	}

	private void create () {
		speed200Button = speedButton(2f);
		speed150Button = speedButton(1.5f);
		speed100Button = speedButton(1);
		speed33Button = speedButton(0.33f);
		speed15Button = speedButton(0.15f);
		speed3Button = speedButton(0.03f);
		pauseButton = speedButton(0);
		pauseButton.setText("Pause");
		new ButtonGroup(speed200Button, speed150Button, speed100Button, speed33Button, speed15Button, speed3Button, pauseButton);
		speed100Button.setChecked(true);

		healthBar = new ProgressBar(0, Player.hpStart, 1, false, skin);
		healthBar.setAnimateDuration(0.3f);
		healthBar.setAnimateInterpolation(fade);
		fpsLabel = new Label("", skin);
		bindsLabel = new Label("", skin);
		debugButton = button("Debug", true);
		zoomButton = button("Zoom", true);
		bgButton = button("Background", true);
		bgButton.setChecked(true);

		fullscreenButton = button("Fullscreen", true);

		menuButton = button("Menu", true);
		menuButton.getColor().a = 0.3f;

		splashImage = new Image();
		splashImage.setScaling(Scaling.fit);

		splashTextImage = new Image();
		splashTextImage.addAction(forever(sequence(fadeOut(0.4f, pow2In), fadeIn(0.4f, pow2Out))));
	}

	private void layout () {
		Table buttons = new Table();
		buttons.defaults().uniformX().fillX();
		buttons.add(speed200Button).row();
		buttons.add(speed150Button).row();
		buttons.add(speed100Button).row();
		buttons.add(speed33Button).row();
		buttons.add(speed15Button).row();
		buttons.add(speed3Button).row();
		buttons.add(pauseButton).row();
		buttons.defaults().padTop(5);
		buttons.add(debugButton).row();
		buttons.add(zoomButton).row();
		buttons.add(bgButton).row();
		buttons.add(fullscreenButton).row();
		buttons.add(restartButton).row();

		menu = new Table(skin);
		menu.defaults().space(5);
		menu.add("FPS:");
		menu.add(fpsLabel).expandX().left().row();
		menu.add("Binds:");
		menu.add(bindsLabel).left().row();
		menu.add(buttons).colspan(2).left();
		menu.setVisible(false);

		Table root = new Table(skin);
		stage.addActor(root);
		root.top().left().pad(5).defaults().space(5);
		root.setFillParent(true);
		root.add(menuButton).fillX();
		root.add(healthBar).height(10).fillY().expandX().right().top().row();
		root.add(menu);

		splashTable = new Table(skin);
		splashTable.setFillParent(true);
		splashTable.add(splashImage).fillX().row();
		splashTable.add(splashTextImage);
		splashTable.setTouchable(Touchable.enabled);
	}

	private void events () {
		menuButton.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				menu.clearActions();
				menu.getColor().a = menu.isVisible() ? 1 : 0;
				if (menu.isVisible())
					menu.addAction(sequence(alpha(0, 0.5f, fade), hide()));
				else
					menu.addAction(sequence(show(), alpha(1, 0.5f, fade)));
				menuButton.getColor().a = menu.isVisible() ? 0.3f : 1;
			}
		});

		fullscreenButton.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				toggleFullscreen();
			}
		});

		restartButton = button("Restart", false);
		restartButton.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				model.controller.restart();
			}
		});

		splashTable.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (hasSplash && inputTimer < 0) {
					model.controller.restart();
					splashTable.clearActions();
					splashTable.getColor().a = 1;
					splashTable.addAction(sequence(fadeOut(1, fade), removeActor()));
					hasSplash = false;
					return true;
				}
				return false;
			}
		});
	}

	void loadSkin () {
		skin = new Skin();
		skin.add("default", new BitmapFont());

		Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		skin.add("white", new Texture(pixmap));

		TextButtonStyle textButtonStyle = new TextButtonStyle();
		textButtonStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
		textButtonStyle.down = skin.newDrawable("white", new Color(0x416ba1ff));
		textButtonStyle.over = skin.newDrawable("white", Color.GRAY);
		textButtonStyle.font = skin.getFont("default");
		skin.add("default", textButtonStyle);

		textButtonStyle = new TextButtonStyle(textButtonStyle);
		textButtonStyle.checked = skin.newDrawable("white", new Color(0x5287ccff));
		skin.add("toggle", textButtonStyle);

		LabelStyle labelStyle = new LabelStyle();
		labelStyle.font = skin.getFont("default");
		skin.add("default", labelStyle);

		ProgressBarStyle progressBarStyle = new ProgressBarStyle();
		progressBarStyle.background = skin.newDrawable("white", new Color(0.25f, 0.25f, 0.25f, 0.66f));
		progressBarStyle.background.setMinHeight(15);
		progressBarStyle.knobBefore = skin.newDrawable("white", Color.CLEAR);
		progressBarStyle.knobBefore.setMinHeight(15);
		progressBarStyle.knobAfter = skin.newDrawable("white", new Color(1, 0, 0, 0.66f));
		progressBarStyle.knobAfter.setMinHeight(15);
		skin.add("default-horizontal", progressBarStyle);
	}

	TextButton speedButton (final float speed) {
		final TextButton button = button((int)(speed * 100) + "%", true);
		button.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				if (button.isChecked()) model.timeScale = speed;
			}
		});
		return button;
	}

	TextButton button (String text, boolean toggle) {
		TextButton button = new TextButton(text, skin, toggle ? "toggle" : "default");
		button.pad(2, 12, 2, 12);
		return button;
	}

	void render () {
		float delta = Gdx.graphics.getDeltaTime();
		inputTimer -= delta;

		float zoom = zoomButton.isChecked() ? cameraZoom : 1;
		if (view.zoom != zoom) {
			if (view.zoom < zoom)
				view.zoom = Math.min(zoom, view.zoom + cameraZoomSpeed * delta);
			else
				view.zoom = Math.max(zoom, view.zoom - cameraZoomSpeed * delta);
			view.viewport.setMinWorldWidth(cameraMinWidth * view.zoom);
			view.viewport.setMinWorldHeight(cameraHeight * view.zoom);
			view.viewport.setMaxWorldWidth(cameraMaxWidth * view.zoom);
			view.viewport.setMaxWorldHeight(cameraHeight * view.zoom);
			view.viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		}

		if (!bgButton.isChecked()) {
			shapes.setTransformMatrix(view.batch.getTransformMatrix());
			shapes.setProjectionMatrix(view.batch.getProjectionMatrix());
			shapes.setColor(gray);
			shapes.begin(ShapeType.Filled);
			float w = view.viewport.getWorldWidth(), h = view.viewport.getWorldHeight();
			int x = (int)(view.camera.position.x - w / 2), y = (int)(view.camera.position.y - h / 2);
			for (Rectangle rect : model.getCollisionTiles(x, y, x + (int)(w + 0.5f), y + (int)(h + 0.5f))) {
				shapes.rect(rect.x, rect.y, rect.width, rect.height);
			}
			shapes.end();
		}

		healthBar.setValue(Player.hpStart - model.player.hp);

		SpriteCache spriteCache = view.mapRenderer.getSpriteCache();
		int renderCalls = view.batch.totalRenderCalls + spriteCache.totalRenderCalls;
		view.batch.totalRenderCalls = 0;
		spriteCache.totalRenderCalls = 0;
		fpsLabel.setText(Integer.toString(Gdx.graphics.getFramesPerSecond()));
		bindsLabel.setText(Integer.toString(renderCalls));

		if (!hasSplash && debugButton.isChecked()) {
			shapes.setTransformMatrix(view.batch.getTransformMatrix());
			shapes.setProjectionMatrix(view.batch.getProjectionMatrix());
			shapes.begin(ShapeType.Line);

			shapes.setColor(Color.GREEN);

			FloatArray bullets = model.bullets;
			for (int i = bullets.size - 5; i >= 0; i -= 5) {
				float x = bullets.get(i + 2);
				float y = bullets.get(i + 3);
				shapes.x(x, y, 10 * scale);
			}

			FloatArray hits = view.hits;
			for (int i = hits.size - 4; i >= 0; i -= 4) {
				float x = hits.get(i + 1);
				float y = hits.get(i + 2);
				shapes.x(x, y, 10 * scale);
			}

			for (Enemy enemy : model.enemies) {
				Rectangle rect = enemy.rect;
				shapes.rect(rect.x, rect.y, rect.width, rect.height);
			}

			Rectangle rect = model.player.rect;
			shapes.rect(rect.x, rect.y, rect.width, rect.height);

			shapes.end();

			skeletonRendererDebug.draw(model.player.view.skeleton);
			for (Enemy enemy : model.enemies) {
				skeletonRendererDebug.draw(enemy.view.skeleton);
			}
		}

		stage.act();
		stage.getViewport().apply(true);
		stage.draw();

		Batch batch = stage.getBatch();
		batch.setColor(Color.WHITE);
		batch.begin();
		Vector2 cursor = stage.screenToStageCoordinates(temp.set(Gdx.input.getX(), Gdx.input.getY()));
		TextureRegion crosshair = view.assets.crosshair;
		batch.draw(crosshair, cursor.x - crosshair.getRegionWidth() / 2, cursor.y - crosshair.getRegionHeight() / 2 + 2);
		batch.end();
	}

	void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	void toggleFullscreen () {
		if (Gdx.graphics.isFullscreen())
			Gdx.graphics.setDisplayMode(windowWidth, windowHeight, false);
		else {
			windowWidth = Gdx.graphics.getWidth();
			windowHeight = Gdx.graphics.getHeight();
			DisplayMode desktopDisplayMode = Gdx.graphics.getDesktopDisplayMode();
			Gdx.graphics.setDisplayMode(desktopDisplayMode.width, desktopDisplayMode.height, true);
		}
	}

	void showSplash (TextureRegion splash, TextureRegion text) {
		splashImage.setDrawable(new TextureRegionDrawable(splash));
		splashTextImage.setDrawable(new TextureRegionDrawable(text));
		stage.addActor(splashTable);
		splashTable.clearActions();
		splashTable.getColor().a = 0;
		splashTable.addAction(fadeIn(1));
		hasSplash = true;
	}

	public boolean keyDown (int keycode) {
		switch (keycode) {
		case Keys.NUM_6:
			speed200Button.toggle();
			return true;
		case Keys.NUM_5:
			speed150Button.toggle();
			return true;
		case Keys.NUM_4:
			speed100Button.toggle();
			return true;
		case Keys.NUM_3:
			speed33Button.toggle();
			return true;
		case Keys.NUM_2:
			speed15Button.toggle();
			return true;
		case Keys.NUM_1:
			speed3Button.toggle();
			return true;
		case Keys.P:
		case Keys.GRAVE:
			pauseButton.toggle();
			return true;
		case Keys.B:
			bgButton.toggle();
			return true;
		case Keys.Z:
			zoomButton.toggle();
			return true;
		case Keys.I:
			debugButton.toggle();
			return true;
		case Keys.T:
			view.assets.dispose();
			Texture.invalidateAllTextures(Gdx.app);
			view.assets = new Assets();
			return true;
		case Keys.ESCAPE:
			if (Gdx.graphics.isFullscreen())
				Gdx.graphics.setDisplayMode(windowWidth, windowHeight, false);
			else
				System.exit(0);
			return true;
		case Keys.ENTER:
			if (UIUtils.alt()) toggleFullscreen();
			return true;
		case Keys.O:
			showSplash(view.assets.titleRegion, view.assets.startRegion);
			return true;
		}
		return hasSplash;
	}

	public boolean keyUp (int keycode) {
		return hasSplash;
	}
}
