package com.mydrakortv.beta.ui.activity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.leanback.widget.HorizontalGridView;
import androidx.leanback.widget.SearchOrbView;
import androidx.leanback.widget.VerticalGridView;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;

import com.mydrakortv.beta.NetworkInst;
import com.mydrakortv.beta.utils.PreferenceUtils;
import com.mydrakortv.beta.R;
import com.mydrakortv.beta.fragments.CountryFragment;
import com.mydrakortv.beta.fragments.CustomHeadersFragment;
import com.mydrakortv.beta.fragments.CustomRowsFragment;
import com.mydrakortv.beta.fragments.FavouriteFragment;
import com.mydrakortv.beta.fragments.GenreFragment;
import com.mydrakortv.beta.fragments.HomeFragment;
import com.mydrakortv.beta.fragments.MoviesFragment;
import com.mydrakortv.beta.fragments.MyAccountFragment;
import com.mydrakortv.beta.fragments.TvSeriesFragment;
import com.mydrakortv.beta.ui.CustomFrameLayout;
import com.mydrakortv.beta.ui.Utils;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;

public class LeanbackActivity extends FragmentActivity {
    public static final String INTENT_EXTRA_VIDEO = "intentExtraVideo";
    private CustomHeadersFragment headersFragment;
    private Fragment rowsFragment;
    private ImageView logoIv;
    private LinkedHashMap<Integer, Fragment> fragments;
    private SearchOrbView orbView;
    private boolean navigationDrawerOpen;
    private static final float NAVIGATION_DRAWER_SCALE_FACTOR = 0.9f;

    private CustomFrameLayout customFrameLayout;
    private boolean rowsContainerFocused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leanback);

        orbView = findViewById(R.id.custom_search_orb);
        logoIv = findViewById(R.id.logo);
        orbView.setOrbColor(getResources().getColor(R.color.colorPrimary));
        orbView.bringToFront();
        orbView.setOnOrbClickedListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
            }
        });

        //get subscription status and save to sharedPreference
        PreferenceUtils.updateSubscriptionStatus(this);

        fragments = new LinkedHashMap<>();

        int CATEGORIES_NUMBER = 5;
        for (int i = 0; i < CATEGORIES_NUMBER; i++) {
            if (i == 0) {
                HomeFragment fragment = new HomeFragment();

                fragments.put(i, fragment);
            } else if (i == 1) {
                MoviesFragment fragment = new MoviesFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("menu", i);
                fragment.setArguments(bundle);
                fragments.put(i, fragment);
            } else if (i == 2) {
                TvSeriesFragment fragment = new TvSeriesFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("menu", i);
                fragment.setArguments(bundle);
                fragments.put(i, fragment);
            } else if (i == 3) {
                FavouriteFragment fragment = new FavouriteFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("menu", i);
                fragment.setArguments(bundle);
                fragments.put(i, fragment);
            } else if (i == 4) {
                MyAccountFragment fragment = new MyAccountFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("menu", i);
                fragment.setArguments(bundle);
                fragments.put(i, fragment);
            }
        }


        headersFragment = new CustomHeadersFragment();
        rowsFragment = (HomeFragment) fragments.get(0);
        customFrameLayout = (CustomFrameLayout) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
        setupCustomFrameLayout();

        if (new NetworkInst(this).isNetworkAvailable()) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction
                    .replace(R.id.header_container, headersFragment, "CustomHeadersFragment")
                    .replace(R.id.rows_container, rowsFragment, "CustomRowsFragment");
            transaction.commit();
        } else {
            // show no internet page
            Intent intent = new Intent(this, ErrorActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public LinkedHashMap<Integer, Fragment> getFragments() {
        return fragments;
    }

    private void setupCustomFrameLayout() {
        customFrameLayout.setOnChildFocusListener(new CustomFrameLayout.OnChildFocusListener() {
            @Override
            public boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
                if (headersFragment.getView() != null && headersFragment.getView().requestFocus(direction, previouslyFocusedRect)) {
                    return true;
                }
                return rowsFragment.getView() != null && rowsFragment.getView().requestFocus(direction, previouslyFocusedRect);
            }

            @Override
            public void onRequestChildFocus(View child, View focused) {
                int childId = child.getId();
                if (childId == R.id.rows_container) {
                    rowsContainerFocused = true;
                    toggleHeadersFragment(false);

                } else if (childId == R.id.header_container) {
                    rowsContainerFocused = false;
                    toggleHeadersFragment(true);

                }
            }
        });

        customFrameLayout.setOnFocusSearchListener(new CustomFrameLayout.OnFocusSearchListener() {
            @Override
            public View onFocusSearch(View focused, int direction) {
                if (direction == View.FOCUS_LEFT) {
                    if (isVerticalScrolling() || navigationDrawerOpen) {
                        return focused;
                    }
                    return getVerticalGridView(headersFragment);
                } else if (direction == View.FOCUS_RIGHT) {
                    if (isVerticalScrolling() || !navigationDrawerOpen) {
                        return focused;
                    }
                    return getVerticalGridView(rowsFragment);
                } else if (focused == orbView && direction == View.FOCUS_DOWN) {
                    return navigationDrawerOpen ? getVerticalGridView(headersFragment) : getVerticalGridView(rowsFragment);
                } else if (focused != orbView && orbView.getVisibility() == View.VISIBLE && direction == View.FOCUS_UP) {

                    return orbView;
                } else {
                    return null;
                }
            }
        });
    }

    public VerticalGridView getVerticalGridView(Fragment fragment) {

        try {
            if (fragment instanceof TvSeriesFragment) {

                Class baseRowFragmentClass = getClassLoader().loadClass("androidx/leanback/app/BaseSupportFragment");
                Method getVerticalGridViewMethod = baseRowFragmentClass.getDeclaredMethod("getVerticalGridView");
                getVerticalGridViewMethod.setAccessible(true);
                VerticalGridView gridView = (VerticalGridView) getVerticalGridViewMethod.invoke(fragment, (Object[]) null);

                return gridView;
            } else if (fragment instanceof MoviesFragment) {

                Class baseRowFragmentClass = getClassLoader().loadClass("androidx/leanback/app/BaseSupportFragment");
                Method getVerticalGridViewMethod = baseRowFragmentClass.getDeclaredMethod("getVerticalGridView");
                getVerticalGridViewMethod.setAccessible(true);
                VerticalGridView gridView = (VerticalGridView) getVerticalGridViewMethod.invoke(fragment, (Object[]) null);
                return gridView;

            } else if (fragment instanceof FavouriteFragment) {

                Class baseRowFragmentClass = getClassLoader().loadClass("androidx/leanback/app/BaseSupportFragment");
                Method getVerticalGridViewMethod = baseRowFragmentClass.getDeclaredMethod("getVerticalGridView");
                getVerticalGridViewMethod.setAccessible(true);
                VerticalGridView gridView = (VerticalGridView) getVerticalGridViewMethod.invoke(fragment, (Object[]) null);
                return gridView;

            } else if (fragment instanceof GenreFragment) {

                Class baseRowFragmentClass = getClassLoader().loadClass("androidx/leanback/app/BaseSupportFragment");
                Method getVerticalGridViewMethod = baseRowFragmentClass.getDeclaredMethod("getVerticalGridView");
                getVerticalGridViewMethod.setAccessible(true);
                VerticalGridView gridView = (VerticalGridView) getVerticalGridViewMethod.invoke(fragment, (Object[]) null);
                return gridView;

            } else if (fragment instanceof CountryFragment) {
                Class baseRowFragmentClass = getClassLoader().loadClass("androidx/leanback/app/BaseSupportFragment");
                Method getVerticalGridViewMethod = baseRowFragmentClass.getDeclaredMethod("getVerticalGridView");
                getVerticalGridViewMethod.setAccessible(true);
                VerticalGridView gridView = (VerticalGridView) getVerticalGridViewMethod.invoke(fragment, (Object[]) null);
                return gridView;

            } else if (fragment instanceof MyAccountFragment) {
                Class baseRowFragmentClass = getClassLoader().loadClass("androidx/leanback/app/BaseSupportFragment");
                Method getVerticalGridViewMethod = baseRowFragmentClass.getDeclaredMethod("getVerticalGridView");
                getVerticalGridViewMethod.setAccessible(true);
                VerticalGridView gridView = (VerticalGridView) getVerticalGridViewMethod.invoke(fragment, (Object[]) null);
                return gridView;

            } else {
                Class baseRowFragmentClass = getClassLoader().loadClass("androidx/leanback/app/BaseRowSupportFragment");
                Method getVerticalGridViewMethod = baseRowFragmentClass.getDeclaredMethod("getVerticalGridView");
                getVerticalGridViewMethod.setAccessible(true);
                VerticalGridView gridView = (VerticalGridView) getVerticalGridViewMethod.invoke(fragment, (Object[]) null);

                return gridView;
            }


        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }


    public synchronized void toggleHeadersFragment(final boolean doOpen) {
        boolean condition = (doOpen != isNavigationDrawerOpen());
        if (condition) {
            final View headersContainer = (View) headersFragment.getView().getParent();
            final View rowsContainer = (View) rowsFragment.getView().getParent();

            final float delta = headersContainer.getWidth() * NAVIGATION_DRAWER_SCALE_FACTOR;

            // get current margin (a previous animation might have been interrupted)
            final int currentHeadersMargin = (((ViewGroup.MarginLayoutParams) headersContainer.getLayoutParams()).leftMargin);
            final int currentRowsMargin = (((ViewGroup.MarginLayoutParams) rowsContainer.getLayoutParams()).leftMargin);

            // calculate destination
            final int headersDestination = (doOpen ? 0 : (int) (0 - delta));
            final int rowsDestination = (doOpen ? (Utils.dpToPx(300, this)) : (int) (Utils.dpToPx(300, this) - delta));

            // calculate the delta (destination - current)
            final int headersDelta = headersDestination - currentHeadersMargin;
            final int rowsDelta = rowsDestination - currentRowsMargin;

            Animation animation = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    ViewGroup.MarginLayoutParams headersParams = (ViewGroup.MarginLayoutParams) headersContainer.getLayoutParams();
                    headersParams.leftMargin = (int) (currentHeadersMargin + headersDelta * interpolatedTime);
                    headersContainer.setLayoutParams(headersParams);

                    ViewGroup.MarginLayoutParams rowsParams = (ViewGroup.MarginLayoutParams) rowsContainer.getLayoutParams();
                    rowsParams.leftMargin = (int) (currentRowsMargin + rowsDelta * interpolatedTime);
                    rowsContainer.setLayoutParams(rowsParams);
                }
            };

            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    navigationDrawerOpen = doOpen;
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (!doOpen) {
                        if (rowsFragment instanceof CustomRowsFragment) {
                            ((CustomRowsFragment) rowsFragment).refresh();
                        }
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

            });

            animation.setDuration(200);
            ((View) rowsContainer.getParent()).startAnimation(animation);
        }
    }

    private boolean isVerticalScrolling() {
        try {
            // don't run transition
            return getVerticalGridView(headersFragment).getScrollState()
                    != HorizontalGridView.SCROLL_STATE_IDLE
                    || getVerticalGridView(rowsFragment).getScrollState()
                    != HorizontalGridView.SCROLL_STATE_IDLE;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    public synchronized boolean isNavigationDrawerOpen() {
        return navigationDrawerOpen;
    }

    public void updateCurrentRowsFragment(Fragment fragment) {
        rowsFragment = fragment;
    }


    @Override
    public void onBackPressed() {

        if (rowsContainerFocused) {
            toggleHeadersFragment(true);
            rowsContainerFocused = false;
        } else {
            super.onBackPressed();
        }

    }

    public void hideLogo() {
        logoIv.setVisibility(View.GONE);
    }

    public void showLogo() {
        //logoIv.setVisibility(View.VISIBLE);
    }

}

