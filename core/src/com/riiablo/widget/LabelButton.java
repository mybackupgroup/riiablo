package com.riiablo.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.riiablo.Riiablo;
import com.riiablo.graphics.PaletteIndexedBatch;

public class LabelButton extends Label {
  ClickListener clickListener;

  public LabelButton(int id, BitmapFont font) {
    super(id, font);
    init();
  }

  public LabelButton(int id, BitmapFont font, Color color) {
    super(id, font, color);
    init();
  }

  public LabelButton(String text, BitmapFont font) {
    super(text, font);
    init();
  }

  public LabelButton(BitmapFont font) {
    super(font);
    init();
  }

  private void init() {
    addListener(clickListener = new ClickListener());
  }

  @Override
  public void draw(PaletteIndexedBatch batch, float a) {
    setColor(clickListener.isOver() ? Riiablo.colors.blue : Riiablo.colors.white);
    super.draw(batch, a);
  }
}
