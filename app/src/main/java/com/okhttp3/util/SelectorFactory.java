package com.okhttp3.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

//选择器工厂：边框设置，字体设置
public class SelectorFactory {

    public static ShapeSelector newShapeSelector() {
        return new ShapeSelector();
    }

    public static final class ShapeSelector {

        @IntDef({GradientDrawable.RECTANGLE, GradientDrawable.OVAL,
                GradientDrawable.LINE, GradientDrawable.RING})
        private @interface Shape {}

        private int mShape;               //the shape of background
        private int mDefaultBgColor;      //default background color
        private int mDisabledBgColor;     //state_enabled = false
        private int mPressedBgColor;      //state_pressed = true
        private int mSelectedBgColor;     //state_selected = true
        private int mFocusedBgColor;      //state_focused = true
        private int mStrokeWidth;         //stroke width in pixel
        private int mDefaultStrokeColor;  //default stroke color
        private int mDisabledStrokeColor; //state_enabled = false
        private int mPressedStrokeColor;  //state_pressed = true
        private int mSelectedStrokeColor; //state_selected = true
        private int mFocusedStrokeColor;  //state_focused = true
        private int mCornerRadius;        //corner radius

        private boolean hasSetDisabledBgColor = false;
        private boolean hasSetPressedBgColor = false;
        private boolean hasSetSelectedBgColor = false;
        private boolean hasSetFocusedBgColor = false;

        private boolean hasSetDisabledStrokeColor = false;
        private boolean hasSetPressedStrokeColor = false;
        private boolean hasSetSelectedStrokeColor = false;
        private boolean hasSetFocusedStrokeColor = false;

        private ShapeSelector() {
            //initialize default values
            mShape = GradientDrawable.RECTANGLE;
            mDefaultBgColor = Color.TRANSPARENT;
            mDisabledBgColor = Color.TRANSPARENT;
            mPressedBgColor = Color.TRANSPARENT;
            mSelectedBgColor = Color.TRANSPARENT;
            mFocusedBgColor = Color.TRANSPARENT;
            mStrokeWidth = 0;
            mDefaultStrokeColor = Color.TRANSPARENT;
            mDisabledStrokeColor = Color.TRANSPARENT;
            mPressedStrokeColor = Color.TRANSPARENT;
            mSelectedStrokeColor = Color.TRANSPARENT;
            mFocusedStrokeColor = Color.TRANSPARENT;
            mCornerRadius = 0;
        }

        public ShapeSelector setShape(@Shape int shape) {
            mShape = shape;
            return this;
        }

        public ShapeSelector setDefaultBgColor(@ColorInt int color) {
            mDefaultBgColor = color;
            if (!hasSetDisabledBgColor)
                mDisabledBgColor = color;
            if (!hasSetPressedBgColor)
                mPressedBgColor = color;
            if (!hasSetSelectedBgColor)
                mSelectedBgColor = color;
            if (!hasSetFocusedBgColor)
                mFocusedBgColor = color;
            return this;
        }

        public ShapeSelector setDisabledBgColor(@ColorInt int color) {
            mDisabledBgColor = color;
            hasSetDisabledBgColor = true;
            return this;
        }

        public ShapeSelector setPressedBgColor(@ColorInt int color) {
            mPressedBgColor = color;
            hasSetPressedBgColor = true;
            return this;
        }

        public ShapeSelector setSelectedBgColor(@ColorInt int color) {
            mSelectedBgColor = color;
            hasSetSelectedBgColor = true;
            return this;
        }

        public ShapeSelector setFocusedBgColor(@ColorInt int color) {
            mFocusedBgColor = color;
            hasSetPressedBgColor = true;
            return this;
        }

        public ShapeSelector setStrokeWidth(int width) {
            mStrokeWidth = width;
            return this;
        }

        public ShapeSelector setDefaultStrokeColor(@ColorInt int color) {
            mDefaultStrokeColor = color;
            if (!hasSetDisabledStrokeColor)
                mDisabledStrokeColor = color;
            if (!hasSetPressedStrokeColor)
                mPressedStrokeColor = color;
            if (!hasSetSelectedStrokeColor)
                mSelectedStrokeColor = color;
            if (!hasSetFocusedStrokeColor)
                mFocusedStrokeColor = color;
            return this;
        }

        public ShapeSelector setDisabledStrokeColor(@ColorInt int color) {
            mDisabledStrokeColor = color;
            hasSetDisabledStrokeColor = true;
            return this;
        }

        public ShapeSelector setPressedStrokeColor(@ColorInt int color) {
            mPressedStrokeColor = color;
            hasSetPressedStrokeColor = true;
            return this;
        }

        public ShapeSelector setSelectedStrokeColor(@ColorInt int color) {
            mSelectedStrokeColor = color;
            hasSetSelectedStrokeColor = true;
            return this;
        }

        public ShapeSelector setFocusedStrokeColor(@ColorInt int color) {
            mFocusedStrokeColor = color;
            hasSetFocusedStrokeColor = true;
            return this;
        }

        public ShapeSelector setCornerRadius(int radius) {
            mCornerRadius = radius;
            return this;
        }

        public StateListDrawable create() {
            StateListDrawable selector = new StateListDrawable();

            //enabled = false
            if (hasSetDisabledBgColor || hasSetDisabledStrokeColor) {
                GradientDrawable disabledShape = getItemShape(mShape, mCornerRadius,
                        mDisabledBgColor, mStrokeWidth, mDisabledStrokeColor);
                selector.addState(new int[]{-android.R.attr.state_enabled}, disabledShape);
            }

            //pressed = true
            if (hasSetPressedBgColor || hasSetPressedStrokeColor) {
                GradientDrawable pressedShape = getItemShape(mShape, mCornerRadius,
                        mPressedBgColor, mStrokeWidth, mPressedStrokeColor);
                selector.addState(new int[]{android.R.attr.state_pressed}, pressedShape);
            }

            //selected = true
            if (hasSetSelectedBgColor || hasSetSelectedStrokeColor) {
                GradientDrawable selectedShape = getItemShape(mShape, mCornerRadius,
                        mSelectedBgColor, mStrokeWidth, mSelectedStrokeColor);
                selector.addState(new int[]{android.R.attr.state_selected}, selectedShape);
            }

            //focused = true
            if (hasSetFocusedBgColor || hasSetFocusedStrokeColor) {
                GradientDrawable focusedShape = getItemShape(mShape, mCornerRadius,
                        mFocusedBgColor, mStrokeWidth, mFocusedStrokeColor);
                selector.addState(new int[]{android.R.attr.state_focused}, focusedShape);
            }

            //default
            GradientDrawable defaultShape = getItemShape(mShape, mCornerRadius,
                    mDefaultBgColor, mStrokeWidth, mDefaultStrokeColor);
            selector.addState(new int[]{}, defaultShape);

            return selector;
        }

        public ShapeSelector bind(View view){
            if(view == null)
                return this;
            view.setBackground(create());
            return this;
        }

        private GradientDrawable getItemShape(int shape, int cornerRadius,
                                              int solidColor, int strokeWidth, int strokeColor) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(shape);
            drawable.setStroke(strokeWidth, strokeColor);
            drawable.setCornerRadius(cornerRadius);
            drawable.setColor(solidColor);
            return drawable;
        }
    }

    public static ColorSelector newColorSelector() {
        return new ColorSelector();
    }

    public static final class ColorSelector {

        private int mDefaultColor;
        private int mDisabledColor;
        private int mPressedColor;
        private int mSelectedColor;
        private int mFocusedColor;

        private boolean hasSetDisabledColor = false;
        private boolean hasSetPressedColor = false;
        private boolean hasSetSelectedColor = false;
        private boolean hasSetFocusedColor = false;

        private ColorSelector() {
            mDefaultColor = Color.BLACK;
            mDisabledColor = Color.GRAY;
            mPressedColor = Color.BLACK;
            mSelectedColor = Color.BLACK;
            mFocusedColor = Color.BLACK;
        }

        public ColorSelector setDefaultColor(@ColorInt int color) {
            mDefaultColor = color;
            if (!hasSetDisabledColor)
                mDisabledColor = color;
            if (!hasSetPressedColor)
                mPressedColor = color;
            if (!hasSetSelectedColor)
                mSelectedColor = color;
            if (!hasSetFocusedColor)
                mFocusedColor = color;
            return this;
        }

        public ColorSelector setDisabledColor(@ColorInt int color) {
            mDisabledColor = color;
            hasSetDisabledColor = true;
            return this;
        }

        public ColorSelector setPressedColor(@ColorInt int color) {
            mPressedColor = color;
            hasSetPressedColor = true;
            return this;
        }

        public ColorSelector setSelectedColor(@ColorInt int color) {
            mSelectedColor = color;
            hasSetSelectedColor = true;
            return this;
        }

        public ColorSelector setFocusedColor(@ColorInt int color) {
            mFocusedColor = color;
            hasSetFocusedColor = true;
            return this;
        }

        public ColorStateList create() {
            int[] colors = new int[]{
                    hasSetDisabledColor ? mDisabledColor : mDefaultColor,
                    hasSetPressedColor ? mPressedColor : mDefaultColor,
                    hasSetSelectedColor ? mSelectedColor : mDefaultColor,
                    hasSetFocusedColor ? mFocusedColor : mDefaultColor,
                    mDefaultColor
            };
            int[][] states = new int[5][];
            states[0] = new int[]{android.R.attr.state_enabled};
            states[1] = new int[]{android.R.attr.state_pressed};
            states[2] = new int[]{android.R.attr.state_selected};
            states[3] = new int[]{android.R.attr.state_focused};
            states[4] = new int[]{};
            return new ColorStateList(states, colors);
        }

        public ColorSelector bindHintTextColor(TextView view){
            if(view == null)
                return this;
            view.setHintTextColor(create());
            return this;
        }

        public ColorSelector bindTextColor(TextView view){
            if(view == null)
                return this;
            view.setTextColor(create());
            return this;
        }

    }

    public static GeneralSelector newGeneralSelector() {
        return new GeneralSelector();
    }

    public static final class GeneralSelector {

        private Drawable mDefaultDrawable;
        private Drawable mDisabledDrawable;
        private Drawable mPressedDrawable;
        private Drawable mSelectedDrawable;
        private Drawable mFocusedDrawable;

        private boolean hasSetDisabledDrawable = false;
        private boolean hasSetPressedDrawable = false;
        private boolean hasSetSelectedDrawable = false;
        private boolean hasSetFocusedDrawable = false;

        private GeneralSelector() {
            mDefaultDrawable = new ColorDrawable(Color.TRANSPARENT);
        }

        public GeneralSelector setDefaultDrawable(Drawable drawable) {
            mDefaultDrawable = drawable;
            if (!hasSetDisabledDrawable)
                mDisabledDrawable = drawable;
            if (!hasSetPressedDrawable)
                mPressedDrawable = drawable;
            if (!hasSetSelectedDrawable)
                mSelectedDrawable = drawable;
            if (!hasSetFocusedDrawable)
                mFocusedDrawable = drawable;
            return this;
        }

        public GeneralSelector setDisabledDrawable(Drawable drawable) {
            mDisabledDrawable = drawable;
            hasSetDisabledDrawable = true;
            return this;
        }

        public GeneralSelector setPressedDrawable(Drawable drawable) {
            mPressedDrawable = drawable;
            hasSetPressedDrawable = true;
            return this;
        }

        public GeneralSelector setSelectedDrawable(Drawable drawable) {
            mSelectedDrawable = drawable;
            hasSetSelectedDrawable = true;
            return this;
        }

        public GeneralSelector setFocusedDrawable(Drawable drawable) {
            mFocusedDrawable = drawable;
            hasSetFocusedDrawable = true;
            return this;
        }

        public StateListDrawable create() {
            StateListDrawable selector = new StateListDrawable();
            if (hasSetDisabledDrawable)
                selector.addState(new int[]{-android.R.attr.state_enabled}, mDisabledDrawable);
            if (hasSetPressedDrawable)
                selector.addState(new int[]{android.R.attr.state_pressed}, mPressedDrawable);
            if (hasSetSelectedDrawable)
                selector.addState(new int[]{android.R.attr.state_selected}, mSelectedDrawable);
            if (hasSetFocusedDrawable)
                selector.addState(new int[]{android.R.attr.state_focused}, mFocusedDrawable);
            selector.addState(new int[]{}, mDefaultDrawable);
            return selector;
        }

        public GeneralSelector bind(View view){
            if(view == null)
                return this;
            view.setBackground(create());
            return this;
        }

        //overload
        public GeneralSelector setDefaultDrawable(Context context, @DrawableRes int drawableRes) {
            return setDefaultDrawable(ContextCompat.getDrawable(context, drawableRes));
        }

        //overload
        public GeneralSelector setDisabledDrawable(Context context, @DrawableRes int drawableRes) {
            return setDisabledDrawable(ContextCompat.getDrawable(context, drawableRes));
        }

        //overload
        public GeneralSelector setPressedDrawable(Context context, @DrawableRes int drawableRes) {
            return setPressedDrawable(ContextCompat.getDrawable(context, drawableRes));
        }

        //overload
        public GeneralSelector setSelectedDrawable(Context context, @DrawableRes int drawableRes) {
            return setSelectedDrawable(ContextCompat.getDrawable(context, drawableRes));
        }

        //overload
        public GeneralSelector setFocusedDrawable(Context context, @DrawableRes int drawableRes) {
            return setFocusedDrawable(ContextCompat.getDrawable(context, drawableRes));
        }
    }
}
