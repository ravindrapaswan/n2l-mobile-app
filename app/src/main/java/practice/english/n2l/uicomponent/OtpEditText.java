package practice.english.n2l.uicomponent;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.Objects;

import practice.english.n2l.R;


public class OtpEditText extends com.google.android.material.textfield.TextInputEditText {
    public static final String XML_NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android";

    private OnClickListener mClickListener;

    private Paint mLinesPaint;
    private Paint mStrokePaint;
    private Paint mTextPaint;
    private Paint mHintPaint;

    private boolean mMaskInput;

    private int defStyleAttr = 0;
    private int mMaxLength = 6;
    private int mPrimaryColor;
    private int mSecondaryColor;
    private int mTextColor;
    private int mHintTextColor;

    private float mLineStrokeSelected = 2; //2dp by default
    private float mLineStroke = 1; //1dp by default
    private float mSpace = 8; //24 dp by default, space between the lines
    private float mNumChars = 6;

    private String mBoxStyle;
    private String mMaskCharacter = "*";
    private String mHintText = "";

    private final String ROUNDED_BOX = "rounded_box";
    private final String UNDERLINE = "underline";
    private final String SQUARE_BOX = "square_box";
    private final String ROUNDED_UNDERLINE = "rounded_underline";

    float[] textWidths;
    float[] hintWidth = new float[1];


    public OtpEditText(Context context) {
        super(context);
    }

    public OtpEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public OtpEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.defStyleAttr = defStyleAttr;
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {

        if (attrs != null) {
            getAttrsFromTypedArray(attrs);
        }

        mTextPaint = getPaint();
        mTextPaint.setColor(mTextColor);

        mHintPaint = new Paint(getPaint());
        mHintPaint.setColor(mHintTextColor);


        float multi = context.getResources().getDisplayMetrics().density;
        mLineStroke = multi * mLineStroke;
        mLineStrokeSelected = multi * mLineStrokeSelected;

        mLinesPaint = new Paint(getPaint());
        mLinesPaint.setStrokeWidth(mLineStroke);

        setBackgroundResource(0);
        mSpace = multi * mSpace; //convert to pixels for our density
        mNumChars = mMaxLength;

        //Disable copy paste
        super.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        });
        // When tapped, move cursor to end of text.
        super.setOnClickListener(v -> {
            setSelection(Objects.requireNonNull(getText()).length());
            if (mClickListener != null) {
                mClickListener.onClick(v);
            }
        });

    }

    private void getAttrsFromTypedArray(AttributeSet attributeSet) {
        final TypedArray a = getContext().obtainStyledAttributes(attributeSet, R.styleable.OtpEditText, defStyleAttr, 0);

        mMaxLength = attributeSet.getAttributeIntValue(XML_NAMESPACE_ANDROID, "maxLength", 6);
        mHintText = attributeSet.getAttributeValue(XML_NAMESPACE_ANDROID, "hint");
        mHintTextColor = attributeSet.getAttributeIntValue(XML_NAMESPACE_ANDROID, "textColorHint", ContextCompat.getColor(getContext(),R.color.light_gray));
        mPrimaryColor = a.getColor(R.styleable.OtpEditText_oev_primary_color, ContextCompat.getColor(getContext(),android.R.color.holo_red_dark));
        mSecondaryColor = a.getColor(R.styleable.OtpEditText_oev_secondary_color, ContextCompat.getColor(getContext(),R.color.colorPrimary));
        mTextColor = a.getColor(R.styleable.OtpEditText_oev_text_color, ContextCompat.getColor(getContext(),android.R.color.black));
        mBoxStyle = a.getString(R.styleable.OtpEditText_oev_box_style);
        mMaskInput = a.getBoolean(R.styleable.OtpEditText_oev_mask_input, false);
        if (a.getString(R.styleable.OtpEditText_oev_mask_character) != null) {
            mMaskCharacter = String.valueOf(a.getString(R.styleable.OtpEditText_oev_mask_character)).substring(0, 1);
        } else {
            mMaskCharacter = getContext().getString(R.string.mask_character);
        }

        if (mBoxStyle != null && !mBoxStyle.isEmpty()) {
            switch (mBoxStyle) {
                case UNDERLINE:
                case ROUNDED_UNDERLINE:
                    mStrokePaint = new Paint(getPaint());
                    mStrokePaint.setStrokeWidth(8);
                    mStrokePaint.setStyle(Paint.Style.FILL);
                    break;

                case SQUARE_BOX:
                case ROUNDED_BOX:
                    mStrokePaint = new Paint(getPaint());
                    mStrokePaint.setStrokeWidth(8);
                    mStrokePaint.setStyle(Paint.Style.STROKE);
                    break;
                default:
                    mStrokePaint = new Paint(getPaint());
                    mStrokePaint.setStrokeWidth(8);
                    mStrokePaint.setStyle(Paint.Style.FILL);
                    mBoxStyle = UNDERLINE;
            }
        } else {
            mStrokePaint = new Paint(getPaint());
            mStrokePaint.setStrokeWidth(8);
            mStrokePaint.setStyle(Paint.Style.FILL);

            mBoxStyle = UNDERLINE;
        }

        a.recycle();
    }

    @Nullable
    public String getOtpValue() {
        if (String.valueOf(getText()).length() != mMaxLength) {
            triggerErrorAnimation();
            return null;
        } else {
            return String.valueOf(getText());
        }
    }

    public int getMaxCharLength() {
        return (int)mNumChars;
    }


    public void triggerErrorAnimation() {
        this.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake));
    }

    @Override
    public void setCustomSelectionActionModeCallback(ActionMode.Callback actionModeCallback) {
        throw new RuntimeException("setCustomSelectionActionModeCallback() not supported.");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int availableWidth = getWidth() - getPaddingRight() - getPaddingLeft();
        float mCharSize;
        if (mSpace < 0) {
            mCharSize = (availableWidth / (mNumChars * 2 - 1));
        } else {
            mCharSize = (availableWidth - (mSpace * (mNumChars - 1))) / mNumChars;
        }

        //8dp by default, height of the text from our lines
        float mLineSpacing = (float) (getHeight() * .6);

        int startX = getPaddingLeft();
        int hintStartX = getPaddingLeft();
        int bottom = getHeight() - getPaddingBottom();
        int top = getPaddingTop();

        //Text Width
        Editable text = getText();


        assert text != null;
        int textLength = text.length();
        textWidths = new float[textLength];


        if (text.length() == 0 && mHintText != null && !mHintText.isEmpty()) {
            getPaint().getTextWidths("1", 0, 1, hintWidth);
            for (int i = 0; i < mNumChars && i < mHintText.length(); i++) {
                float middle = hintStartX + mCharSize / 2;
                canvas.drawText(mHintText, i, i + 1, middle - hintWidth[0] / 2, mLineSpacing, mHintPaint);

                if (mSpace < 0) {
                    hintStartX += (int) (mCharSize * 2);
                } else {
                    hintStartX += (int) (mCharSize + mSpace);
                }
            }
        }

        getPaint().getTextWidths(getText(), 0, textLength, textWidths);

        for (int i = 0; i < mNumChars; i++) {
            updateColorForLines(i <= textLength, i == textLength);

            switch (mBoxStyle) {
                case ROUNDED_UNDERLINE:
                    try {
                        canvas.drawRoundRect(startX, bottom * .95f, startX + mCharSize, bottom, 16, 16, mStrokePaint);
                    } catch (NoSuchMethodError err) {
                        canvas.drawRect(startX, bottom * .95f, startX + mCharSize, bottom, mStrokePaint);
                    }
                    break;
                case ROUNDED_BOX:
                    try {
                        canvas.drawRoundRect(startX, top, startX + mCharSize, bottom, 8, 8, mLinesPaint);
                        canvas.drawRoundRect(startX, top, startX + mCharSize, bottom, 8, 8, mStrokePaint);
                    } catch (NoSuchMethodError err) {
                        canvas.drawRect(startX, top, startX + mCharSize, bottom, mLinesPaint);
                        canvas.drawRect(startX, top, startX + mCharSize, bottom, mStrokePaint);
                    }
                    break;

                case UNDERLINE:
                    canvas.drawRect(startX, ((float) bottom * .95f), startX + mCharSize, bottom, mStrokePaint);
                    break;

                case SQUARE_BOX:
                    canvas.drawRect(startX, top, startX + mCharSize, bottom, mLinesPaint);
                    canvas.drawRect(startX, top, startX + mCharSize, bottom, mStrokePaint);
                    break;
            }
            if (getText().length() > i) {
                float middle = startX + mCharSize / 2;
                if (mMaskInput) {
                    canvas.drawText(getMaskText(), i, i + 1, middle - textWidths[0] / 2, mLineSpacing, mTextPaint);
                } else {
                    canvas.drawText(text, i, i + 1, middle - textWidths[0] / 2, mLineSpacing, mTextPaint);
                }
            }

            if (mSpace < 0) {
                startX += (int) (mCharSize * 2);
            } else {
                startX += (int) (mCharSize + mSpace);
            }

        }
    }

    private String getMaskText() {
        int length = String.valueOf(getText()).length();
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < length; i++) {
            out.append(mMaskCharacter);
        }
        return out.toString();
    }

    /**
     * @param next Is the current char the next character to be input?
     */
    private void updateColorForLines(boolean next, boolean current) {
        if (next) {
            mStrokePaint.setColor(mSecondaryColor);
            mLinesPaint.setColor(mSecondaryColor);
        } else {
            mStrokePaint.setColor(mSecondaryColor);
            mLinesPaint.setColor(ContextCompat.getColor(getContext(), android.R.color.white));
        }
        if (current) {
            mLinesPaint.setColor(ContextCompat.getColor(getContext(), android.R.color.white));
            mStrokePaint.setColor(mPrimaryColor);
        }
    }
}
