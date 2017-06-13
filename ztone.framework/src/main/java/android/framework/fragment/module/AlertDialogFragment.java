package android.framework.fragment.module;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.database.Cursor;
import android.framework.context.FrameActivity;
import android.graphics.drawable.Drawable;
import android.log.Log;
import android.os.Bundle;
import android.reflect.ClazzLoader;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListAdapter;

public class AlertDialogFragment extends DialogFragment {
	private static final String TAG = "AlertDialogFragment";
	private AlertDialog.Builder mAlertDialogBuilder;

	public AlertDialogFragment() {

	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = null;

		if (mAlertDialogBuilder != null) {
			dialog = mAlertDialogBuilder.create();
		} else {
			dialog = super.onCreateDialog(savedInstanceState);
		}

		return dialog;
	}

	@Override
	public void show(FragmentManager manager, String tag) {
		ClazzLoader.setFieldValue(DialogFragment.class, this, "mDismissed", false);
		ClazzLoader.setFieldValue(DialogFragment.class, this, "mShownByMe", true);

		if (manager != null) {
			manager.beginTransaction().add(this, tag).commitAllowingStateLoss();
		}
	}

	@Override
	public int show(FragmentTransaction transaction, String tag) {
		int backStackId = -1;

		ClazzLoader.setFieldValue(DialogFragment.class, this, "mDismissed", false);
		ClazzLoader.setFieldValue(DialogFragment.class, this, "mShownByMe", true);
		ClazzLoader.setFieldValue(DialogFragment.class, this, "mViewDestroyed", false);

		if (transaction != null) {
			backStackId = transaction.add(this, tag).commitAllowingStateLoss();

			ClazzLoader.setFieldValue(DialogFragment.class, this, "mBackStackId", backStackId);
		}

		return backStackId;
	}

	public AlertDialog.Builder getDialogBuilder() {

		return mAlertDialogBuilder;
	}

	private void apply(AlertDialog.Builder alertDialogBuilder) {
		mAlertDialogBuilder = alertDialogBuilder;
	}

	/**
	 * AlertDialogFragment构建器
	 * 
	 * @author handy
	 */
	public static class Builder {
		private FragmentActivity mFragmentActivity;
		private int mTheme;
		private int mStyle;

		private AlertDialog.Builder mAlertDialogBuilder;

		public Builder(FragmentActivity fragmentActivity) {
			this(fragmentActivity, DialogFragment.STYLE_NORMAL, 0);
		}

		public Builder(FragmentActivity fragmentActivity, int theme) {
			this(fragmentActivity, DialogFragment.STYLE_NORMAL, theme);
		}

		public Builder(FragmentActivity fragmentActivity, int style, int theme) {
			mFragmentActivity = fragmentActivity;

			mStyle = style;
			mTheme = theme;

			mAlertDialogBuilder = new AlertDialog.Builder(fragmentActivity);
		}

		public FragmentActivity getFragmentActivity() {

			return mFragmentActivity;
		}

		public Builder setTitle(int titleId) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setTitle(titleId);
			}

			return this;
		}

		public Builder setTitle(CharSequence title) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setTitle(title);
			}

			return this;
		}

		public Builder setCustomTitle(View customTitleView) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setCustomTitle(customTitleView);
			}

			return this;
		}

		public Builder setMessage(int messageId) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setMessage(messageId);
			}

			return this;
		}

		public Builder setMessage(CharSequence message) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setMessage(message);
			}

			return this;
		}

		public Builder setIcon(int iconId) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setIcon(iconId);
			}

			return this;
		}

		public Builder setIcon(Drawable icon) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setIcon(icon);
			}

			return this;
		}

		public Builder setIconAttribute(int attrId) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setIconAttribute(attrId);
			}

			return this;
		}

		public Builder setPositiveButton(int textId, OnClickListener listener) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setPositiveButton(textId, listener);
			}

			return this;
		}

		public Builder setPositiveButton(CharSequence text, OnClickListener listener) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setPositiveButton(text, listener);
			}

			return this;
		}

		public Builder setNegativeButton(int textId, OnClickListener listener) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setNegativeButton(textId, listener);
			}

			return this;
		}

		public Builder setNegativeButton(CharSequence text, OnClickListener listener) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setNegativeButton(text, listener);
			}

			return this;
		}

		public Builder setNeutralButton(int textId, OnClickListener listener) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setNeutralButton(textId, listener);
			}

			return this;
		}

		public Builder setNeutralButton(CharSequence text, OnClickListener listener) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setNeutralButton(text, listener);
			}

			return this;
		}

		public Builder setCancelable(boolean cancelable) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setCancelable(cancelable);
			}

			return this;
		}

		public Builder setOnCancelListener(OnCancelListener onCancelListener) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setOnCancelListener(onCancelListener);
			}

			return this;
		}

		public Builder setOnKeyListener(OnKeyListener onKeyListener) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setOnKeyListener(onKeyListener);
			}

			return this;
		}

		public Builder setItems(int itemsId, OnClickListener listener) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setItems(itemsId, listener);
			}

			return this;
		}

		public Builder setItems(CharSequence[] items, OnClickListener listener) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setItems(items, listener);
			}

			return this;
		}

		public Builder setAdapter(ListAdapter adapter, OnClickListener listener) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setAdapter(adapter, listener);
			}

			return this;
		}

		public Builder setCursor(Cursor cursor, OnClickListener listener, String labelColumn) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setCursor(cursor, listener, labelColumn);
			}

			return this;
		}

		public Builder setMultiChoiceItems(int itemsId, boolean[] checkedItems, OnMultiChoiceClickListener listener) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setMultiChoiceItems(itemsId, checkedItems, listener);
			}

			return this;
		}

		public Builder setMultiChoiceItems(CharSequence[] items, boolean[] checkedItems,
				OnMultiChoiceClickListener listener) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setMultiChoiceItems(items, checkedItems, listener);
			}

			return this;
		}

		public Builder setMultiChoiceItems(Cursor cursor, String isCheckedColumn, String labelColumn,
				OnMultiChoiceClickListener listener) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setMultiChoiceItems(cursor, isCheckedColumn, labelColumn, listener);
			}

			return this;
		}

		public Builder setSingleChoiceItems(int itemsId, int checkedItem, OnClickListener listener) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setSingleChoiceItems(itemsId, checkedItem, listener);
			}

			return this;
		}

		public Builder setSingleChoiceItems(Cursor cursor, int checkedItem, String labelColumn, OnClickListener listener) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setSingleChoiceItems(cursor, checkedItem, labelColumn, listener);
			}

			return this;
		}

		public Builder setSingleChoiceItems(CharSequence[] items, int checkedItem, OnClickListener listener) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setSingleChoiceItems(items, checkedItem, listener);
			}

			return this;
		}

		public Builder setSingleChoiceItems(ListAdapter adapter, int checkedItem, OnClickListener listener) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setSingleChoiceItems(adapter, checkedItem, listener);
			}

			return this;
		}

		public Builder setOnItemSelectedListener(OnItemSelectedListener listener) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setOnItemSelectedListener(listener);
			}

			return this;
		}

		public Builder setView(View view) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setView(view);
			}

			return this;
		}

		public Builder setInverseBackgroundForced(boolean useInverseBackground) {
			if (mAlertDialogBuilder != null) {
				mAlertDialogBuilder.setInverseBackgroundForced(useInverseBackground);
			}

			return this;
		}

		public AlertDialogFragment create() {
			final AlertDialogFragment alertDialogFragment = new AlertDialogFragment();
			alertDialogFragment.setStyle(mStyle, mTheme);
			alertDialogFragment.apply(mAlertDialogBuilder);

			return alertDialogFragment;
		}

		public AlertDialogFragment show(boolean isInLayout) {
			final AlertDialogFragment alertDialogFragment = create();

			if (mFragmentActivity != null) {
				FragmentManager fragmentManager = mFragmentActivity instanceof FrameActivity ? ((FrameActivity) mFragmentActivity)
						.iSupportFragmentManager() : mFragmentActivity.getSupportFragmentManager();
				if (fragmentManager != null) {
					try {
						alertDialogFragment.show(fragmentManager, TAG);
					} catch (Exception e) {
						Log.e(TAG, e);
					}
				}
			}

			return alertDialogFragment;
		}
	}
}
