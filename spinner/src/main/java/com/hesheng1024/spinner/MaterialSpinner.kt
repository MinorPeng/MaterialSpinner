package com.hesheng1024.spinner

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.annotation.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat


/**
 *
 * @author hesheng1024
 * @date 2020/5/11 17:38
 */
class MaterialSpinner : AppCompatTextView {

    private var onNothingSelectedListener: OnNothingSelectedListener? = null
    private var onItemSelectedListener: OnItemSelectedListener? = null

    private lateinit var mPopupWindow: ListPopupWindow
    private var mAdapter: SpinnerAdapter<*>? = null
    private var mArrowDrawable: Drawable? = null
    private var mHideArrow = false
    private var mArrowColor = 0

    private var mPopBgColor = Color.WHITE
    private var mPopBgResourceId = -1

    private var mItemLayoutId = -1
    private var mTvId = -1
    private var mItemTextColor = -1
    private var mItemTextSize = -1f
    private var mItemPaddingL = 0
    private var mItemPaddingT = 0
    private var mItemPaddingR = 0
    private var mItemPaddingB = 0
    private var mItemGravity = Gravity.CENTER

    private var mPopWindowMaxH = 0
    private var mPopWindowH = 0
    private var mArrowColorDisabled = 0

    private var mSelectedIndex = 0

    private var nothingSelected = false
    private var mHintStr: String = ""

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val ta: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.MaterialSpinner)
        val defaultColor = textColors.defaultColor
        val entries: Array<CharSequence>?
        try {
            mPopBgResourceId = ta.getResourceId(R.styleable.MaterialSpinner_popBackground, -1)
            if (mPopBgResourceId == -1) {
                mPopBgColor = ta.getColor(R.styleable.MaterialSpinner_popBackground, Color.WHITE)
            }

            mItemGravity = ItemTextGravity.formId(
                ta.getInt(R.styleable.MaterialSpinner_itemGravity, ItemTextGravity.CENTER.ordinal)
            )
            mItemTextColor = ta.getColor(R.styleable.MaterialSpinner_itemTextColor, defaultColor)
            mItemTextSize = ta.getDimension(R.styleable.MaterialSpinner_itemTextSize, -1f)
            mItemPaddingL = ta.getDimensionPixelSize(R.styleable.MaterialSpinner_itemPaddingL, 0)
            mItemPaddingT = ta.getDimensionPixelSize(R.styleable.MaterialSpinner_itemPaddingT, 0)
            mItemPaddingR = ta.getDimensionPixelSize(R.styleable.MaterialSpinner_itemPaddingR, 0)
            mItemPaddingB = ta.getDimensionPixelSize(R.styleable.MaterialSpinner_itemPaddingB, 0)

            entries = ta.getTextArray(R.styleable.MaterialSpinner_entries)
            mHintStr = ta.getString(R.styleable.MaterialSpinner_hint).toString()
            mArrowColor = ta.getColor(R.styleable.MaterialSpinner_arrowHint, defaultColor)
            mHideArrow = ta.getBoolean(R.styleable.MaterialSpinner_hideArrow, false)

            mPopWindowMaxH =
                ta.getDimensionPixelSize(R.styleable.MaterialSpinner_ms_dropdown_max_height, 0)
            mPopWindowH = ta.getLayoutDimension(
                R.styleable.MaterialSpinner_ms_dropdown_height,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            mArrowColorDisabled = lighter(mArrowColor, 0.8f)
        } finally {
            ta.recycle()
        }

        isSingleLine = true
        if (gravity == (Gravity.TOP or Gravity.START)) {
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
            if (paddingStart == 0 && paddingTop == 0 && paddingEnd == 0 && paddingBottom == 0) {
                val l = resources.getDimensionPixelSize(R.dimen.ms_text_padding_l)
                val t = resources.getDimensionPixelSize(R.dimen.ms_text_padding_t)
                val r = resources.getDimensionPixelSize(R.dimen.ms_text_padding_r)
                val b = resources.getDimensionPixelSize(R.dimen.ms_text_padding_b)
                setPadding(l, t, r, b)
            }
        }

        isClickable = true
        if (background == null) {
            setBackgroundResource(R.drawable.ms_selector)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isRtl(context)) {
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            textDirection = View.TEXT_DIRECTION_RTL
        }

        initArrow()
        initPopWindow()
        entries?.let {
            setItems(entries.toList())
        }
    }

    private fun initArrow() {
        // arrow
        if (!mHideArrow) {
            mArrowDrawable = ContextCompat.getDrawable(context, R.drawable.ms_arrow)?.mutate()!!
            mArrowDrawable?.setColorFilter(mArrowColor, PorterDuff.Mode.SRC_IN)
            val drawables = compoundDrawables
            if (isRtl(context)) {
                drawables[0] = mArrowDrawable
            } else {
                drawables[2] = mArrowDrawable
            }
            setCompoundDrawablesWithIntrinsicBounds(
                drawables[0],
                drawables[1],
                drawables[2],
                drawables[3]
            )
        }
    }

    private fun initPopWindow() {
        // PopupWindow
        mPopupWindow = ListPopupWindow(context)
        mPopupWindow.isModal = true
        mPopupWindow.setOnItemClickListener { parent, view, position, id ->
            mSelectedIndex = position
            nothingSelected = false
            val item = parent.adapter.getItem(position)
            text = item.toString()
            collapse()
            if (onItemSelectedListener != null) {
                onItemSelectedListener?.onItemSelected(this@MaterialSpinner, position, id, item)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPopupWindow.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ms_drawable
                )
            )
        } else {
            mPopupWindow.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ms__drop_down_shadow
                )
            )
        }
        if (mPopBgColor != Color.WHITE) { // default color is white
            mPopupWindow.background?.setColorFilter(mPopBgColor, PorterDuff.Mode.SRC_IN)
        } else if (mPopBgResourceId != -1) {
            mPopupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, mPopBgResourceId))
        }
        mPopupWindow.setOnDismissListener {
            if (nothingSelected && onNothingSelectedListener != null) {
                onNothingSelectedListener?.onNothingSelected(this@MaterialSpinner)
            }
            if (!mHideArrow) {
                animateArrow(false)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (mAdapter != null && mAdapter!!.count != 0) {
            val currentText = text
            var longestItem = currentText.toString()
            for (i in 0 until mAdapter!!.count) {
                val itemText: String = mAdapter!!.getItem(i).toString()
                if (itemText.length > longestItem.length) {
                    longestItem = itemText
                }
            }
            text = longestItem
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            text = currentText
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
        mPopupWindow.width = measuredWidth
        mPopupWindow.height = calculatePopupWindowHeight()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            if (isEnabled && isClickable) {
                if (!mPopupWindow.isShowing) {
                    expand()
                } else {
                    collapse()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("state", super.onSaveInstanceState())
        bundle.putInt("selected_index", mSelectedIndex)
        bundle.putBoolean("nothing_selected", nothingSelected)
        bundle.putBoolean("is_popup_showing", mPopupWindow.isShowing)
        collapse()
        return bundle
    }

    override fun onRestoreInstanceState(savedState: Parcelable?) {
        var savedState = savedState
        if (savedState is Bundle) {
            val bundle = savedState
            mSelectedIndex = bundle.getInt("selected_index")
            nothingSelected = bundle.getBoolean("nothing_selected")
            mAdapter?.let {
                text = if (nothingSelected && !TextUtils.isEmpty(mHintStr)) {
                    mHintStr
                } else {
                    it.getItem(mSelectedIndex).toString()
                }
            }
            if (bundle.getBoolean("is_popup_showing")) {
                // Post the show request into the looper to avoid bad token exception
                post { expand() }
            }
            savedState = bundle.getParcelable("state")
        }
        super.onRestoreInstanceState(savedState)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        mArrowDrawable?.setColorFilter(
            if (enabled) mArrowColor else mArrowColorDisabled,
            PorterDuff.Mode.SRC_IN
        )
    }

    /**
     * @return the selected item position
     */
    fun getSelectedIndex(): Int {
        return mSelectedIndex
    }

    /**
     * Set the default spinner item using its index
     *
     * @param position the item's position
     */
    fun setSelectedIndex(position: Int) {
        mAdapter?.let {
            if (position >= 0 && position <= it.count) {
                mSelectedIndex = position
                text = it.getItem(position).toString()
            } else {
                throw IllegalArgumentException("Position must be lower than adapter count!")
            }
        }
    }

    /**
     * Register a callback to be invoked when an item in the dropdown is selected.
     *
     * @param onItemSelectedListener The callback that will run
     */
    fun setOnItemSelectedListener(@Nullable onItemSelectedListener: OnItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener
    }

    /**
     * Register a callback to be invoked when the [PopupWindow] is shown but the user didn't select an item.
     *
     * @param onNothingSelectedListener the callback that will run
     */
    fun setOnNothingSelectedListener(@Nullable onNothingSelectedListener: OnNothingSelectedListener?) {
        this.onNothingSelectedListener = onNothingSelectedListener
    }

    /**
     * Set the dropdown items
     *
     * @param items A list of items
     * @param <T> The item type
    </T> */
    fun <T> setItems(vararg items: T) {
        setItems(listOf(items))
    }

    /**
     * Set the dropdown items
     *
     * @param items A list of items
     * @param <T> The item type
    </T> */
    fun <T> setItems(@NonNull items: List<T>) {
        mAdapter = SpinnerAdapter(items)
        mAdapter?.let {
            // it.setData(items)
            if (mItemLayoutId != -1 && mTvId != -1) {
                it.setItemLayoutId(mItemLayoutId, mTvId)
            } else if (mItemLayoutId != -1) {
                it.setItemLayoutId(mItemLayoutId)
            }
            it.setTextColor(mItemTextColor)
            it.setTextSize(mItemTextSize)
            it.setGravity(mItemGravity)
            if (mItemPaddingL == 0 && mItemPaddingT == 0 && mItemPaddingR == 0 && mItemPaddingB == 0) {
                mItemPaddingL = resources.getDimensionPixelSize(R.dimen.ms_item_padding_l)
                mItemPaddingT = resources.getDimensionPixelSize(R.dimen.ms_item_padding_t)
                mItemPaddingR = resources.getDimensionPixelSize(R.dimen.ms_item_padding_r)
                mItemPaddingB = resources.getDimensionPixelSize(R.dimen.ms_item_padding_b)
            }
            it.setTextPadding(mItemPaddingL, mItemPaddingT, mItemPaddingR, mItemPaddingB)
            setAdapterInternal()
        }
    }

    private fun setAdapterInternal() {
        mAdapter?.let {
            val shouldResetPopupHeight = false
            mPopupWindow.setAdapter(it)
            if (mSelectedIndex >= it.count) {
                mSelectedIndex = 0
            }
            text = if (it.count > 0) {
                if (nothingSelected && !TextUtils.isEmpty(mHintStr)) {
                    mHintStr
                } else {
                    it.getItem(mSelectedIndex).toString()
                }
            } else {
                ""
            }
            if (shouldResetPopupHeight) {
                mPopupWindow.height = calculatePopupWindowHeight()
            }
            requestLayout()
        }
    }

    /**
     * Get the list of items in the adapter
     *
     * @param <T> The item type
     * @return A list of items or `null` if no items are set.
    </T> */
    fun <T> getItems(): List<T>? {
        return mAdapter?.getItems() as List<T>?
    }

    /**
     * Show the dropdown menu
     */
    fun expand() {
        if (canShowPopup()) {
            if (!mHideArrow) {
                animateArrow(true)
            }
            nothingSelected = true
            mPopupWindow.anchorView = this
            mPopupWindow.show()
            val popPadding = resources.getDimensionPixelSize(R.dimen.ms_pop_window_padding)
            mPopupWindow.listView?.setPadding(popPadding, popPadding, popPadding, popPadding)
        }
    }

    /**
     * Closes the dropdown menu
     */
    fun collapse() {
        if (!mHideArrow) {
            animateArrow(false)
        }
        mPopupWindow.dismiss()
    }

    /**
     * Set the tint color for the dropdown arrow
     *
     * @param color the color value
     */
    fun setArrowColor(@ColorInt color: Int) {
        mArrowColor = color
        mArrowColorDisabled = lighter(mArrowColor, 0.8f)
        mArrowDrawable?.setColorFilter(mArrowColor, PorterDuff.Mode.SRC_IN)
    }

    /**
     * drop-down items layout id
     *
     * @param layoutId: Layout Res
     */
    fun setItemLayoutId(@LayoutRes layoutId: Int) {
        mItemLayoutId = layoutId
    }

    /**
     * drop-down items layout id and id for item text
     *
     * @param layoutId: Layout Res
     * @param tvId: id for TextView which show item text
     */
    fun setItemLayoutId(@LayoutRes layoutId: Int, @IdRes tvId: Int) {
        mItemLayoutId = layoutId
        mTvId = tvId
    }

    /**
     * item text color
     *
     * @param color:
     */
    fun setItemTextColor(@ColorInt color: Int) {
        mItemTextColor = color
    }

    /**
     * item text size
     *
     * @param size: pixel size, not dp or sp
     */
    fun setItemTextSize(size: Float) {
        mItemTextSize = size
    }

    /**
     * set padding of item text for every line
     *
     * @param l: padding left
     * @param t: padding top
     * @param r: padding right
     * @param b: padding bottom
     */
    fun setItemPadding(l: Int, t: Int, r: Int, b: Int) {
        mItemPaddingL = l
        mItemPaddingT = t
        mItemPaddingR = r
        mItemPaddingB = b
    }

    /**
     * set gravity of item text
     *
     * @param gravity: {@link ItemTextGravity}, also same of View.Gravity
     */
    fun setItemGravity(gravity: Int) {
        mItemGravity = gravity
    }

    private fun canShowPopup(): Boolean {
        val activity = getActivity()
        if (activity == null || activity.isFinishing) {
            return false
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            isLaidOut
        } else {
            width > 0 && height > 0
        }
    }

    private fun getActivity(): Activity? {
        var context: Context = context
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun animateArrow(shouldRotateUp: Boolean) {
        val start = if (shouldRotateUp) 0 else 10000
        val end = if (shouldRotateUp) 10000 else 0
        val animator = ObjectAnimator.ofInt(mArrowDrawable, "level", start, end)
        animator.start()
    }

    /**
     * Set the maximum height of the dropdown menu.
     *
     * @param height the height in pixels
     */
    fun setDropdownMaxHeight(height: Int) {
        mPopWindowMaxH = height
        mPopupWindow.height = calculatePopupWindowHeight()
    }

    /**
     * Set the height of the dropdown menu
     *
     * @param height the height in pixels
     */
    fun setDropdownHeight(height: Int) {
        mPopWindowH = height
        mPopupWindow.height = calculatePopupWindowHeight()
    }

    private fun calculatePopupWindowHeight(): Int {
        if (mAdapter == null) {
            return WindowManager.LayoutParams.WRAP_CONTENT
        }
        val itemHeight = resources.getDimension(R.dimen.ms_item_height)
        val listViewHeight: Float = mAdapter?.count!! * itemHeight
        if (mPopWindowMaxH > 0 && listViewHeight > mPopWindowMaxH) {
            return mPopWindowMaxH
        } else if (mPopWindowH != WindowManager.LayoutParams.MATCH_PARENT && mPopWindowH != WindowManager.LayoutParams.WRAP_CONTENT && mPopWindowH <= listViewHeight
        ) {
            return mPopWindowH
        } else if (listViewHeight == 0f && mAdapter?.count == 1) {
            return itemHeight.toInt()
        }
        return WindowManager.LayoutParams.WRAP_CONTENT
    }

    /**
     * Interface definition for a callback to be invoked when an item in this view has been selected.
     *
     * @param <T> Adapter item type
    </T> */
    interface OnItemSelectedListener {
        /**
         *
         * Callback method to be invoked when an item in this view has been selected. This callback is invoked only when
         * the newly selected position is different from the previously selected position or if there was no selected
         * item.
         *
         * @param view The [MaterialSpinner] view
         * @param position The position of the view in the adapter
         * @param id The row id of the item that is selected
         * @param item The selected item
         */
        fun onItemSelected(view: MaterialSpinner?, position: Int, id: Long, item: Any)
    }

    /**
     * Interface definition for a callback to be invoked when the dropdown is dismissed and no item was selected.
     */
    interface OnNothingSelectedListener {
        /**
         * Callback method to be invoked when the [PopupWindow] is dismissed and no item was selected.
         *
         * @param spinner the [MaterialSpinner]
         */
        fun onNothingSelected(spinner: MaterialSpinner?)
    }
}