package com.siyee.superagentweb.impl;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebView;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.siyee.superagentweb.R;
import com.siyee.superagentweb.abs.AbsAgentWebUIController;
import com.siyee.superagentweb.abs.Callback;
import com.siyee.superagentweb.utils.AgentWebUtils;
import com.siyee.superagentweb.utils.LogUtils;
import com.siyee.superagentweb.widget.WebParentLayout;


/**
 * @author hackycy
 */
public class DefaultUIController extends AbsAgentWebUIController {

	private AlertDialog mAlertDialog;
	private AlertDialog mConfirmDialog;
	private JsPromptResult mJsPromptResult = null;
	private JsResult mJsResult = null;
	private AlertDialog mPromptDialog = null;
	private Activity mActivity;
	private WebParentLayout mWebParentLayout;
	private AlertDialog mAskOpenOtherAppDialog = null;
	private ProgressDialog mProgressDialog;
	private Resources mResources = null;

	private static final String TAG = DefaultUIController.class.getSimpleName();

	@Override
	public void onJsAlert(WebView view, String url, String message) {
		LogUtils.i(TAG, "onJsAlert");
		Activity mActivity;
		if ((mActivity = this.mActivity) == null || mActivity.isFinishing()) {
			return;
		}
		if (mActivity.isDestroyed()) {
			return;
		}
		String host = null;
		try {
			host = Uri.parse(url).getHost();
		} catch (Exception ignore) {}
		mAlertDialog = new AlertDialog.Builder(mActivity)
				.setMessage(message)
				.setTitle(TextUtils.isEmpty(host) ? "Unkonwn" : host)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.create();
		mAlertDialog.show();
	}

	@Override
	public void onOpenPagePrompt(WebView view, String url, final Callback<Integer> callback) {
		LogUtils.i(TAG, "onOpenPagePrompt");
		Activity mActivity;
		if ((mActivity = this.mActivity) == null || mActivity.isFinishing()) {
			return;
		}
		if (mActivity.isDestroyed()) {
			return;
		}
		if (mAskOpenOtherAppDialog == null) {
			mAskOpenOtherAppDialog = new AlertDialog
					.Builder(mActivity)
					.setMessage(mResources.getString(R.string.agentweb_leave_app_and_go_other_page,
							AgentWebUtils.getApplicationName(mActivity)))
					.setTitle(mResources.getString(R.string.agentweb_tips))
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (callback != null) {
								callback.handleValue(-1);
							}
						}
					})
					.setPositiveButton(mResources.getString(R.string.agentweb_leave), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (callback != null) {
								callback.handleValue(1);
							}
						}
					})
					.create();
		}
		mAskOpenOtherAppDialog.show();
	}

	@Override
	public void onJsConfirm(WebView view, String url, String message, JsResult jsResult) {
		LogUtils.i(TAG, "activity:" + mActivity.hashCode() + "  ");
		Activity mActivity = this.mActivity;
		if (mActivity == null || mActivity.isFinishing()) {
			toCancelJsresult(jsResult);
			return;
		}
		if (mActivity.isDestroyed()) {
			toCancelJsresult(jsResult);
			return;
		}

		if (mConfirmDialog == null) {
			mConfirmDialog = new AlertDialog.Builder(mActivity)
					.setMessage(message)
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							toDismissDialog(mConfirmDialog);
							toCancelJsresult(mJsResult);
						}
					})//
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							toDismissDialog(mConfirmDialog);
							if (mJsResult != null) {
								mJsResult.confirm();
							}

						}
					})
					.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							dialog.dismiss();
							toCancelJsresult(mJsResult);
						}
					})
					.create();

		}
		mConfirmDialog.setMessage(message);
		this.mJsResult = jsResult;
		mConfirmDialog.show();
	}

	@Override
	public void onSelectItemsPrompt(WebView view, String url, final String[] ways, final Callback<Integer> callback) {
		Activity mActivity;
		if ((mActivity = this.mActivity) == null || mActivity.isFinishing()) {
			return;
		}
		if (mActivity.isDestroyed()) {
			return;
		}
		mAlertDialog = new AlertDialog.Builder(mActivity)
				.setItems(ways, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						LogUtils.i(TAG, "which:" + which);
						if (callback != null) {
							callback.handleValue(which);
						}
					}
				}).setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						dialog.dismiss();
						if (callback != null) {
							callback.handleValue(-1);
						}
					}
				}).create();
		mAlertDialog.show();
	}

	@Override
	public void onForceDownloadAlert(String url, final Callback<Integer> callback) {
		Activity mActivity;
		if ((mActivity = this.mActivity) == null || mActivity.isFinishing()) {
			return;
		}
		if (mActivity.isDestroyed()) {
			return;
		}
		mAlertDialog = new AlertDialog.Builder(mActivity)
				.setTitle(mResources.getString(R.string.agentweb_tips))
				.setMessage(mResources.getString(R.string.agentweb_performdownload))
				.setNegativeButton(mResources.getString(R.string.agentweb_download), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (dialog != null) {
							dialog.dismiss();
						}
						if (callback != null) {
							callback.handleValue(1);
						}
					}
				})//
				.setPositiveButton(mResources.getString(R.string.agentweb_cancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (dialog != null) {
							dialog.dismiss();
						}
					}
				}).create();
		mAlertDialog.show();
	}

	@Override
	public void onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult jsPromptResult) {
		Activity mActivity = this.mActivity;
		if (mActivity == null || mActivity.isFinishing()) {
			jsPromptResult.cancel();
			return;
		}
		if (mActivity.isDestroyed()) {
			jsPromptResult.cancel();
			return;
		}
		if (mPromptDialog == null) {
			final EditText et = new EditText(mActivity);
			et.setText(defaultValue);
			mPromptDialog = new AlertDialog.Builder(mActivity)
					.setView(et)
					.setTitle(message)
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							toDismissDialog(mPromptDialog);
							toCancelJsresult(mJsPromptResult);
						}
					})//
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							toDismissDialog(mPromptDialog);

							if (mJsPromptResult != null) {
								mJsPromptResult.confirm(et.getText().toString());
							}

						}
					})
					.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							dialog.dismiss();
							toCancelJsresult(mJsPromptResult);
						}
					})
					.create();
		}
		this.mJsPromptResult = jsPromptResult;
		mPromptDialog.show();
	}

	@Override
	public void onMainFrameError(WebView view, int errorCode, String description, String failingUrl) {
		LogUtils.i(TAG, "mWebParentLayout onMainFrameError:" + mWebParentLayout);
		if (mWebParentLayout != null) {
			mWebParentLayout.showPageMainFrameError();
		}
	}

	@Override
	public void onShowMainFrame() {
		if (mWebParentLayout != null) {
			mWebParentLayout.hideErrorLayout();
		}
	}

	@Override
	public void onLoading(String msg) {
		Activity mActivity;
		if ((mActivity = this.mActivity) == null || mActivity.isFinishing()) {
			return;
		}
		if (mActivity.isDestroyed()) {
			return;
		}
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(mActivity);
		}
		mProgressDialog.setCancelable(false);
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.setMessage(msg);
		mProgressDialog.show();
	}

	@Override
	public void onCancelLoading() {
		Activity mActivity;
		if ((mActivity = this.mActivity) == null || mActivity.isFinishing()) {
			return;
		}
		if (mActivity.isDestroyed()) {
			return;
		}
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
		mProgressDialog = null;
	}

	@Override
	public void onShowMessage(String message, String from) {
//		if (!TextUtils.isEmpty(from) && from.contains("performDownload")) {
//			return;
//		}
		AgentWebUtils.toastShowShort(mActivity.getApplicationContext(), message);
	}

	@Override
	public void onPermissionsDeny(String[] permissions, String action) {
		AgentWebUtils.toastShowShort(mActivity.getApplicationContext(), mResources.getString(R.string.agentweb_permission_deny_tips));
	}

	private void toCancelJsresult(JsResult result) {
		if (result != null) {
			result.cancel();
		}
	}

	@Override
	protected void bindSupportWebParent(WebParentLayout webParentLayout, Activity activity) {
		this.mActivity = activity;
		this.mWebParentLayout = webParentLayout;
		mResources = this.mActivity.getResources();
	}

}
