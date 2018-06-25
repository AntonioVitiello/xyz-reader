package com.example.xyzreader.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.transition.TransitionInflater;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.event.DetailEvent;
import com.example.xyzreader.handler.BodyBuilder;
import com.example.xyzreader.handler.PaletteBuilder;
import com.example.xyzreader.handler.SubtitleBuilder;
import com.example.xyzreader.handler.WorkerHandler;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import timber.log.Timber;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_ITEM_ID = "item_id";
    private static final float PARALLAX_FACTOR = 1.25f;
    private static final String ARG_POSITION = "position";

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;
    private ObservableScrollView mScrollView;
    private DrawInsetsFrameLayout mDrawInsetsFrameLayout;
    private ColorDrawable mStatusBarColorDrawable;

    private int mTopInset;
    private View mPhotoContainerView;
    private ImageView mPhotoView;
    private int mScrollY;
    private boolean mIsCard = false;
    private int mStatusBarFullOpacityBottom;

    private TextView mTitleView;
    private TextView mBylineView;
    private TextView mBodyView;
    private LinearLayout mMetaBarView;
    private FloatingActionButton mFabView;
    private ProgressBar mProgressBar;
    private int mPosition;
    private Target mTargetPicasso;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId, int position) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        arguments.putInt(ARG_POSITION, position);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }
        if (getArguments().containsKey(ARG_POSITION)) {
            mPosition = getArguments().getInt(ARG_POSITION);
        }

        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(R.dimen.detail_card_top_margin);
        setHasOptionsMenu(true);

        mTargetPicasso = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mPhotoView.setImageBitmap(bitmap);
                //Set palette muted color in a separate thread
                int defaultColor = getResources().getColor(R.color.cardview_dark_background);
                WorkerHandler.handle(new PaletteBuilder(bitmap, defaultColor, worker -> {
                    PaletteBuilder result = (PaletteBuilder) worker;
                    mMetaBarView.setBackgroundColor(result.getMutedColor());
                    updateStatusBar();
                }));
            }

            @Override
            public void onBitmapFailed(Exception exc, Drawable errorDrawable) {
                Timber.e(exc, "While loading image, position = %d", mPosition);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                //do nothing
            }
        };
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        initViews();

        mStatusBarColorDrawable = new ColorDrawable(0);

        updateStatusBar();
        return mRootView;
    }

    private void initViews() {
        mTitleView = mRootView.findViewById(R.id.article_title);
        mBylineView = mRootView.findViewById(R.id.article_byline);
        mBylineView.setMovementMethod(new LinkMovementMethod());

        mPhotoView = mRootView.findViewById(R.id.photo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementEnterTransition(TransitionInflater.from(getActivity())
                    .inflateTransition(R.transition.change_image_trans));
            setEnterTransition(TransitionInflater.from(getActivity())
                    .inflateTransition(android.R.transition.fade));
        }

        mMetaBarView = mRootView.findViewById(R.id.meta_bar);
        mProgressBar = mRootView.findViewById(R.id.loading_pb);

        mFabView = mRootView.findViewById(R.id.share_fab);
        mFabView.setOnClickListener(view -> startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                .setType("text/plain")
                .setText("I'm reading: " + mTitleView.getText() + "\n" + mBylineView.getText())
                .getIntent(), getString(R.string.action_share))));

        mBodyView = mRootView.findViewById(R.id.article_body);
        mBodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        mDrawInsetsFrameLayout = mRootView.findViewById(R.id.draw_insets_frame_layout);
        mDrawInsetsFrameLayout.setOnInsetsCallback(insets -> mTopInset = insets.top);

        mPhotoContainerView = mRootView.findViewById(R.id.photo_container);
        mScrollView = mRootView.findViewById(R.id.scrollview);
        mScrollView.setCallbacks(() -> {
            mScrollY = mScrollView.getScrollY();
            getActivityCast().onUpButtonFloorChanged(mItemId, ArticleDetailFragment.this);
            mPhotoContainerView.setTranslationY((int) (mScrollY - mScrollY / PARALLAX_FACTOR));
            updateStatusBar();
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDetailEvent(DetailEvent event) {
        if (mPosition != event.getPosition()) {
            return;
        }
        mProgressBar.setVisibility(View.VISIBLE);
        Picasso.get()
                .load(event.getImageurl())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_broken_image)
                .into(mTargetPicasso);
        mTitleView.setText(event.getTitle());
        mBylineView.setText(event.getSubtitle());

        //Transform body and set TextView in a separate thread
        WorkerHandler.handle(new BodyBuilder(event.getBody(), worker -> {
            BodyBuilder result = (BodyBuilder) worker;
            Handler appender = new Handler();
            appender.postDelayed(() -> {
                mBodyView.append(result.getSpanned());
                mProgressBar.setVisibility(View.GONE);
            }, 300);
        }));

    }

    private void updateStatusBar() {
        int color = 0;
        if (mPhotoView != null && mTopInset != 0 && mScrollY > 0) {
            float f = progress(mScrollY,
                    mStatusBarFullOpacityBottom - mTopInset * 3,
                    mStatusBarFullOpacityBottom - mTopInset);
            color = Color.argb((int) (255 * f),
                    (int) (Color.red(mMutedColor) * 0.9),
                    (int) (Color.green(mMutedColor) * 0.9),
                    (int) (Color.blue(mMutedColor) * 0.9));
        }
        mStatusBarColorDrawable.setColor(color);
        mDrawInsetsFrameLayout.setInsetBackground(mStatusBarColorDrawable);
    }

    static float progress(float v, float min, float max) {
        return constrain((v - min) / (max - min), 0, 1);
    }

    static float constrain(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        if (mCursor != null && mPhotoView.getDrawable() == null) {
            //Animate
            mProgressBar.setVisibility(View.VISIBLE);
            mRootView.animate().setDuration(300).alpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().setDuration(300).alpha(1);

            //Set title
            mTitleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));

            //Set By line with date
            WorkerHandler.handle(new SubtitleBuilder(mCursor, worker -> {
                SubtitleBuilder result = (SubtitleBuilder) worker;
                mBylineView.setText(result.getSpanned());
            }));

            final String photoUrl = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
            Picasso.get()
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_broken_image)
                    .into(mTargetPicasso);

            //Transform body and set TextView in a separate thread
            WorkerHandler.handle(new BodyBuilder(mCursor, worker -> {
                BodyBuilder result = (BodyBuilder) worker;
                Handler appender = new Handler();
                appender.postDelayed(() -> {
                    mBodyView.append(result.getSpanned());
                    mProgressBar.setVisibility(View.GONE);
                }, 300);
            }));

        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Timber.e("Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> cursorLoader) {
        mCursor = null;
    }

    public int getUpButtonFloor() {
        if (mPhotoContainerView == null || mPhotoView.getHeight() == 0) {
            return Integer.MAX_VALUE;
        }

        // account for parallax
        return mIsCard
                ? (int) mPhotoContainerView.getTranslationY() + mPhotoView.getHeight() - mScrollY
                : mPhotoView.getHeight() - mScrollY;
    }

}
