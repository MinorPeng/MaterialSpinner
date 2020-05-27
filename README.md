# README

A material spinner for Kotlin base on [jaredrummler](https://github.com/jaredrummler)/**[MaterialSpinner](https://github.com/jaredrummler/MaterialSpinner)**.

add more custom attributes, such as item text size and text color. The show TextView is independent for drop-down TextView.



## Usage

use for by self !

 Available attributes in xml:

|        **name**        |      **type**       |                     **info**                     |
| :--------------------: | :-----------------: | :----------------------------------------------: |
|       arrowHint        | color or reference  |      sets the color on the drop-down arrow       |
|       hideArrow        |       boolean       |      set to true to hide the arrow drawable      |
|     popBackground      | color or reference  | set the background for the spinner and drop-down |
|     itemTextColor      |        color        |       set the drop-down items' text color        |
|      itemTextSize      |      dimension      |        set the drop-down items' text size        |
|      itemGravity       | start, center, end  |      set the drop-down items' text gravity       |
|      itemPaddingL      |      dimension      |   set the left padding of the drop-down items    |
|      itemPaddingT      |      dimension      |    set the toppadding of the drop-down items     |
|      itemPaddingR      |      dimension      |   set the right padding of the drop-down items   |
|      itemPaddingB      |      dimension      |  set the bottom padding of the drop-down items   |
|        entries         |      reference      |             set the drop-down items              |
|          hint          | string or reference |            set the hint when selected            |
| ms_dropdown_max_height |      dimension      |       set the max height of the drop-down        |
|   ms_dropdown_height   |      dimension      |         set the height of the drop-down          |

In code:

``` kotlin
fun getSelectedIndex(): Int

fun setSelectedIndex(position: Int)

fun setArrowColor(@ColorInt color: Int)

fun setItemLayoutId(@LayoutRes layoutId: Int)

fun setItemLayoutId(@LayoutRes layoutId: Int, @IdRes tvId: Int)

fun setItemTextColor(@ColorInt color: Int)

fun setItemTextSize(size: Float)

fun setItemPadding(l: Int, t: Int, r: Int, b: Int)

fun setItemGravity(gravity: Int)

fun <T> setItems(vararg items: T)

fun <T> setItems(@NonNull items: List<T>)
```

if you want set custom item layout, before you use `setItems()`.

## Thanks 

Thanks for **[jaredrummler](https://github.com/jaredrummler)**.