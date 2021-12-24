//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.serenegiant.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.serenegiant.utils.BuildCheck;
import com.serenegiant.uvccamera.R;

public class MessageDialogFragmentV4 extends DialogFragment {
	private static final String TAG = MessageDialogFragmentV4.class.getSimpleName();
	private MessageDialogFragmentV4.MessageDialogListener mDialogListener;

	public static MessageDialogFragmentV4 showDialog(FragmentActivity parent, int requestCode, int id_title, int id_message, String[] permissions) {
		MessageDialogFragmentV4 dialog = newInstance(requestCode, id_title, id_message, permissions);
		dialog.show(parent.getSupportFragmentManager(), TAG);
		return dialog;
	}

	public static MessageDialogFragmentV4 showDialog(Fragment parent, int requestCode, int id_title, int id_message, String[] permissions) {
		MessageDialogFragmentV4 dialog = newInstance(requestCode, id_title, id_message, permissions);
		dialog.setTargetFragment(parent, parent.getId());
		dialog.show(parent.getFragmentManager(), TAG);
		return dialog;
	}

	public static MessageDialogFragmentV4 newInstance(int requestCode, int id_title, int id_message, String[] permissions) {
		MessageDialogFragmentV4 fragment = new MessageDialogFragmentV4();
		Bundle args = new Bundle();
		args.putInt("requestCode", requestCode);
		args.putInt("title", id_title);
		args.putInt("message", id_message);
		args.putStringArray("permissions", permissions != null ? permissions : new String[0]);
		fragment.setArguments(args);
		return fragment;
	}

	public MessageDialogFragmentV4() {
	}

	@SuppressLint({"NewApi"})
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof MessageDialogFragmentV4.MessageDialogListener) {
			this.mDialogListener = (MessageDialogFragmentV4.MessageDialogListener)context;
		}

		Fragment target;
		if (this.mDialogListener == null) {
			target = this.getTargetFragment();
			if (target instanceof MessageDialogFragmentV4.MessageDialogListener) {
				this.mDialogListener = (MessageDialogFragmentV4.MessageDialogListener)target;
			}
		}

		if (this.mDialogListener == null && BuildCheck.isAndroid4_2()) {
			target = this.getParentFragment();
			if (target instanceof MessageDialogFragmentV4.MessageDialogListener) {
				this.mDialogListener = (MessageDialogFragmentV4.MessageDialogListener)target;
			}
		}

		if (this.mDialogListener == null) {
			throw new ClassCastException(context.toString());
		}
	}

	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = savedInstanceState != null ? savedInstanceState : this.getArguments();
		final int requestCode = this.getArguments().getInt("requestCode");
		int id_title = this.getArguments().getInt("title");
		int id_message = this.getArguments().getInt("message");
		final String[] permissions = args.getStringArray("permissions");
		return (new Builder(this.getActivity())).setIcon(R.drawable.ic_notification ).setTitle(id_title).setMessage(id_message).setPositiveButton(R.string.camera, new OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				try {
					MessageDialogFragmentV4.this.mDialogListener.onMessageDialogResult(MessageDialogFragmentV4.this, requestCode, permissions, true);
				} catch (Exception var4) {
					Log.w(MessageDialogFragmentV4.TAG, var4);
				}

			}
		}).setNegativeButton(R.string.camera, new OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				try {
					MessageDialogFragmentV4.this.mDialogListener.onMessageDialogResult(MessageDialogFragmentV4.this, requestCode, permissions, false);
				} catch (Exception var4) {
					Log.w(MessageDialogFragmentV4.TAG, var4);
				}

			}
		}).create();
	}

	public interface MessageDialogListener {
		void onMessageDialogResult(MessageDialogFragmentV4 var1, int var2, String[] var3, boolean var4);
	}
}
