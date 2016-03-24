package com.gmail.collinsmith70.unifi.unifi.layout;

import com.gmail.collinsmith70.unifi.unifi.Widget;

import java.util.Set;

public class HorizontalLayout extends LinearLayout {

  @Override
  public void requestLayout() {
    int childrenWidth = 0;
    final Set<Gravity> gravity = getGravity();
    final Direction direction = getDirection();
    for (Widget child : this) {

      if (gravity.contains(Gravity.CENTER_VERTICAL)) {
        child.translateVerticalCenter((getHeight() - getPaddingTop() - getPaddingBottom()) / 2);
      } else if (gravity.contains(Gravity.BOTTOM)) {
        child.translateBottom(getPaddingBottom());
      } else {
        child.translateTop(getHeight() - getPaddingTop());
      }

      childrenWidth += child.getWidth();

    }

    int offset = 0;
    for (Widget child : this) {
      switch (direction) {
        case START_TO_END:
          child.translateLeft(getPaddingLeft() + offset);
          break;
        case END_TO_START:
          child.translateRight(getPaddingLeft() + childrenWidth - offset);
          break;
        default:
      }

      offset += child.getWidth();

    }

    if (gravity.contains(Gravity.LEFT)) {
      return;
    }

    if (gravity.contains(Gravity.CENTER_HORIZONTAL)) {
      final int shift = getWidth() / 2 - childrenWidth / 2;
      for (Widget child : this) {
        child.translateLeft(child.getLeft() + shift);
      }
    } else if (gravity.contains(Gravity.RIGHT)) {
      final int shift = getWidth() - getPaddingLeft() + childrenWidth;
      for (Widget child : this) {
        child.translateRight(child.getRight() + shift);
      }
    }
  }

}
