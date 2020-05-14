package com.hesheng1024.spinner

import android.graphics.Rect
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.NonNull
import org.jetbrains.annotations.NotNull

/**
 *
 * @author hesheng1024
 * @date 2020/5/14 9:03
 */
internal class SpinnerAdapter<T> : BaseAdapter {

    private val mObjects: MutableList<T>

    private var mItemLayoutId = android.R.layout.simple_spinner_item
    private var mTvId = android.R.id.text1
    private var mTextColor: Int = -1
    private var mTextSize = -1f
    private var mPaddingRect = Rect()
    private var mGravity = Gravity.CENTER

    constructor() : this(ArrayList<T>())

    constructor(objects: List<T>) {
        mObjects = objects.toMutableList()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val tvItem: TextView
        val contentView: View
        if (convertView == null) {
            contentView = LayoutInflater.from(parent?.context).inflate(mItemLayoutId, parent, false)
            contentView.tag = ViewHolder(contentView)
            tvItem = if (contentView is TextView) {
                contentView
            } else {
                contentView.findViewById(mTvId)
            }
            if (tvItem == null) {
                throw RuntimeException(
                        "Failed to find view with ID " +
                                "${parent?.context?.resources?.getResourceName(mTvId)} in item layout"
                )
            }
            if (mTextSize != -1f) {
                tvItem.textSize = mTextSize
            }
            if (mTextColor != -1) {
                tvItem.setTextColor(mTextColor)
            }
            tvItem.gravity = mGravity
            tvItem.setPadding(
                    mPaddingRect.left,
                    mPaddingRect.top,
                    mPaddingRect.right,
                    mPaddingRect.bottom
            )
        } else {
            contentView = (convertView.tag as ViewHolder).itemView
            tvItem = if (contentView is TextView) {
                contentView
            } else {
                contentView.findViewById(mTvId)
            }
        }
        val item = getItem(position)
        if (item is CharSequence) {
            tvItem.text = item
        } else {
            tvItem.text = item.toString()
        }
        return contentView
    }

    override fun getItem(position: Int): T {
        return mObjects[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return mObjects.size
    }

    fun add(@NonNull item: T) {
        mObjects.add(item)
    }

    fun addAll(@NotNull items: List<T>) {
        mObjects.addAll(items)
    }

    fun remove(@NonNull position: Int) {
        mObjects.removeAt(position)
    }

    fun remove(@NonNull item: T) {
        mObjects.remove(item)
    }

    fun setData(data: List<T>) {
        mObjects.addAll(data)
    }

    fun getItems(): List<T> = mObjects

    /**
     * item layout not a TextView, but had a TextView
     *
     * @param itemLayoutId: item layout
     * @param tvId: TextView Id, use {@link View.findViewById()}
     */
    fun setItemLayoutId(@LayoutRes itemLayoutId: Int, @IdRes tvId: Int) {
        mItemLayoutId = itemLayoutId
        mTvId = tvId
    }

    /**
     * item layout only TextView
     *
     * @param itemLayoutId: item layout id
     */
    fun setItemLayoutId(@LayoutRes itemLayoutId: Int) {
        mItemLayoutId = itemLayoutId
    }

    fun setTextColor(@ColorInt color: Int) {
        mTextColor = color
    }

    fun setTextSize(@NonNull size: Float) {
        mTextSize = size
    }

    fun setTextPadding(left: Int, top: Int, right: Int, bottom: Int) {
        mPaddingRect.set(left, top, right, bottom)
    }

    fun setGravity(gravity: Int) {
        mGravity = gravity
    }

    private class ViewHolder(val itemView: View) {

    }
}