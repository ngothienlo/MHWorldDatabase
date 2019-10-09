package com.gatheringhallstudios.mhworlddatabase.components

import android.content.Context
import android.graphics.drawable.Animatable
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.gatheringhallstudios.mhworlddatabase.R
import com.gatheringhallstudios.mhworlddatabase.features.armor.list.compatSwitchVector
import com.gatheringhallstudios.mhworlddatabase.util.ConvertElevationToAlphaConvert
import kotlinx.android.synthetic.main.cell_expandable_cardview.view.*

class ExpandableCardView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {
    private var expandAnimationDuration = 300 //Should be shorter than the 180 of the arrow
    private var onExpand: () -> Unit = {}
    private var onContract: () -> Unit = {}
    private var cardElevation: Float = 0f
    private var headerLayout: Int = 0
    private var bodyLayout: Int = 0
    private var showRipple: Boolean = true
    private var onSwipeLeft: () -> Unit = {}
    private var onSwipeRight: () -> Unit = {}
    private var onClick: () -> Unit = {}

    private enum class cardState {
        EXPANDING,
        COLLAPSING
    }

    init {
        val inflater = getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.cell_expandable_cardview, this, true)
        card_arrow.setOnClickListener {
            toggle()
        }
        card_body.addOnLayoutChangeListener(object : OnLayoutChangeListener {
            override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                card_body.measure(MATCH_PARENT, WRAP_CONTENT)
                if (card_body.measuredHeight <= 0) {
                    card_arrow.visibility = View.INVISIBLE
                } else {
                    card_arrow.visibility = View.VISIBLE
                }
            }
        })

        if (attrs != null) {
            val attributes = context.obtainStyledAttributes(attrs, R.styleable.ExpandableCardView)
            cardElevation = attributes.getFloat(R.styleable.ExpandableCardView_cardViewElevation, 0f)
            showRipple = attributes.getBoolean(R.styleable.ExpandableCardView_clickable, true)
            headerLayout = attributes.getResourceId(R.styleable.ExpandableCardView_cardHeaderLayout, R.layout.view_base_header_expandable_cardview)
            bodyLayout = attributes.getResourceId(R.styleable.ExpandableCardView_cardBodyLayout, R.layout.view_base_body_expandable_cardview)

            if (Build.VERSION.SDK_INT < 21) {
                card_container.cardElevation = cardElevation
            } else {
                card_container.elevation = cardElevation
            }
            card_overlay.alpha = ConvertElevationToAlphaConvert(cardElevation.toInt())
            card_container.isClickable = showRipple
            card_container.isFocusable = showRipple
            setHeader(headerLayout)
            setBody(bodyLayout)
            attributes.recycle()
        }

        //Swipe/onclick handler
        card_container.setOnTouchListener(OnSwipeTouchListener(card_layout, left_icon_layout, right_icon_layout, context,
                onSwipeLeft, onSwipeRight, onClick))
    }

    fun setHeader(layout: Int) {
        val inflater = getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        card_header.removeAllViews()
        card_header.addView(inflater.inflate(layout, this, false))
    }

    fun setBody(layout: Int) {
        val inflater = getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        card_body.removeAllViews()
        card_body.addView(inflater.inflate(layout, this, false))
    }

    fun setOnClick(onClick: () -> Unit) {
        this.onClick = onClick
        //Update Swipe/onclick handler
        card_container.setOnTouchListener(OnSwipeTouchListener(card_layout, left_icon_layout, right_icon_layout, context,
                this.onSwipeLeft, this.onSwipeRight, this.onClick))
    }

    fun setOnSwipeLeft(onSwipeLeft: () -> Unit) {
        this.onSwipeLeft = onSwipeLeft
        //Update Swipe/onclick handler
        card_container.setOnTouchListener(OnSwipeTouchListener(card_layout, left_icon_layout, right_icon_layout, context,
                this.onSwipeLeft, this.onSwipeRight, this.onClick))
    }

    fun setOnSwipeRight(onSwipeRight: () -> Unit) {
        this.onSwipeRight = onSwipeRight
        //Update Swipe/onclick handler
        card_container.setOnTouchListener(OnSwipeTouchListener(card_layout, left_icon_layout, right_icon_layout, context,
                this.onSwipeLeft, this.onSwipeRight, this.onClick))
    }

    fun setOnExpand(onExpand: () -> Unit) {
        this.onExpand = onExpand
    }

    fun setOnContract(onContract: () -> Unit) {
        this.onContract = onContract
    }

    fun setCardElevation(cardElevation: Float) {
        card_container.cardElevation = cardElevation
        card_overlay.alpha = ConvertElevationToAlphaConvert(cardElevation.toInt())
    }

    fun toggle() {
        card_body.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        if (card_body.measuredHeight == 0) return

        val initialHeight = card_container.height
        val headerHeight = card_header.height
        card_layout.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        val targetHeight: Int = if (initialHeight == headerHeight) card_layout.measuredHeight else headerHeight
        if (targetHeight - initialHeight > 0) {
            animateViews(initialHeight,
                    targetHeight - initialHeight,
                    cardState.EXPANDING, card_container)
            onExpand()
        } else {
            animateViews(initialHeight,
                    initialHeight - targetHeight,
                    cardState.COLLAPSING, card_container)
            onContract()
        }
    }

    private fun animateViews(initialHeight: Int, distance: Int, animationType: cardState, cardView: View) {
        val expandAnimation = object : Animation() {
            var arrowStarted = false
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                cardView.layoutParams.height = if (animationType == cardState.EXPANDING)
                    (initialHeight + distance * interpolatedTime).toInt()
                else
                    (initialHeight - distance * interpolatedTime).toInt()

                cardView.requestLayout()
                if (!arrowStarted) {
                    arrowStarted = true
                    (cardView.card_arrow.drawable as Animatable).start()
                }
            }
        }

        expandAnimation.duration = expandAnimationDuration.toLong()
        cardView.startAnimation(expandAnimation)
        cardView.card_arrow.setImageResource(when (animationType) {
            cardState.EXPANDING -> compatSwitchVector(R.drawable.ic_expand_more_animated, R.drawable.ic_expand_more)
            cardState.COLLAPSING -> compatSwitchVector(R.drawable.ic_expand_less_animated, R.drawable.ic_expand_less)
        })
    }

    class OnSwipeTouchListener(val view: LinearLayout, val left_view: LinearLayout, val right_view: LinearLayout, val ctx: Context, val onSwipeLeft: () -> Unit, val onSwipeRight: () -> Unit, val onClick: () -> Unit) : OnTouchListener {
        var initialX = 0f
        var viewWidth = 0
        var dx = 0f
        var x = 0f

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = event.x
                    viewWidth = view.width
                }
                MotionEvent.ACTION_MOVE -> {
                    dx = event.x - initialX
                    if (dx > 0 && dx < 225) {
                        val layoutParams = left_view.layoutParams
                        layoutParams.width = dx.toInt()
                        left_view.layoutParams = layoutParams

                        val layoutParams2 = view.layoutParams as RelativeLayout.LayoutParams
                        layoutParams2.width = (viewWidth - dx).toInt()
                        layoutParams2.removeRule(RelativeLayout.ALIGN_PARENT_LEFT)
                        layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                        view.layoutParams = layoutParams2
                    } else if (dx < 0 && dx > -225) {
                        val layoutParams = right_view.layoutParams
                        layoutParams.width = -1 * dx.toInt()
                        right_view.layoutParams = layoutParams

                        val layoutParams2 = view.layoutParams as RelativeLayout.LayoutParams
                        layoutParams2.width = (viewWidth - dx).toInt()
                        layoutParams2.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                        layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                        view.layoutParams = layoutParams2
                    }
                }
                MotionEvent.ACTION_UP -> {
                    val layoutParams = left_view.layoutParams
                    layoutParams.width = 0
                    left_view.layoutParams = layoutParams

                    val layoutParams2 = view.layoutParams
                    layoutParams2.width = MATCH_PARENT
                    view.layoutParams = layoutParams2

                    val layoutParams3 = right_view.layoutParams
                    layoutParams3.width = 0
                    right_view.layoutParams = layoutParams3

                    if (dx > 175) {
                        onSwipeRight()
                    } else if (dx == 0f) {
                        onClick()
                    }

                    dx = 0f
                    initialX = 0f
                    x = 0f
                    viewWidth = 0
                }
                MotionEvent.ACTION_CANCEL -> {
                    val layoutParams = left_view.layoutParams
                    layoutParams.width = 0
                    left_view.layoutParams = layoutParams

                    val layoutParams2 = view.layoutParams
                    layoutParams2.width = MATCH_PARENT
                    view.layoutParams = layoutParams2

                    val layoutParams3 = right_view.layoutParams
                    layoutParams3.width = 0
                    right_view.layoutParams = layoutParams3

                    dx = 0f
                    initialX = 0f
                    x = 0f
                    viewWidth = 0
                }
            }
            return false
        }
    }
}

