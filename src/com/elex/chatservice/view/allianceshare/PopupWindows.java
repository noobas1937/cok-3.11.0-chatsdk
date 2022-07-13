package com.elex.chatservice.view.allianceshare;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.elex.chatservice.R;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.controller.MenuController;
import com.elex.chatservice.controller.ServiceInterface;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.util.PermissionManager;
import com.elex.chatservice.view.AllianceShareActivity;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;
import com.elex.chatservice.view.allianceshare.model.AllianceShareComment;
import com.elex.chatservice.view.allianceshare.util.AllianceShareManager;
import com.elex.chatservice.view.allianceshare.util.IntentConstants;

public class PopupWindows extends PopupWindow
{

	private Activity				mActivity;
	private AllianceShareComment	comment;

	public PopupWindows(Activity activity)
	{
		mActivity = activity;
		View view = View.inflate(mActivity, R.layout.item_popupwindow, null);
		view.startAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.fade_in));
		LinearLayout ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);
		ll_popup.startAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.push_bottom_in));

		setWidth(LayoutParams.MATCH_PARENT);
		setHeight(LayoutParams.WRAP_CONTENT);
		setFocusable(true);
		setOutsideTouchable(true);
		setBackgroundDrawable(new ColorDrawable(0x00000000));
		setContentView(view);
		// showAtLocation(parent, Gravity.BOTTOM, 0, 0);
		update();

		Button bt1 = (Button) view.findViewById(R.id.item_popupwindows_camera);
		Button bt2 = (Button) view.findViewById(R.id.item_popupwindows_Photo);
		Button bt3 = (Button) view.findViewById(R.id.item_popupwindows_cancel);
		bt1.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_TAKE_PHOTO));
		bt1.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (!PermissionManager.getInstance().checkAllianceSharePermissions(mActivity))
					return;
//				takePhoto(); //换到Permission回调里。
				dismiss();
			}
		});

		bt2.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_UPLOAD_PHOTO));
		bt2.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				ServiceInterface.showImageBucketActivity(mActivity);
				dismiss();
			}
		});

		bt3.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CANCEL));
		bt3.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				dismiss();
			}
		});

		RelativeLayout alliancesharepopuplayout = (RelativeLayout) view.findViewById(R.id.alliancesharepopuplayout);
		alliancesharepopuplayout.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				dismiss();
			}
		});

	}

	public PopupWindows(MyActionBarActivity activity, final AllianceShareComment comment, final String allianceShareSender)
	{
		this.mActivity = activity;
		this.comment = comment;

		View view = View.inflate(mActivity, R.layout.coment_popupwindow, null);
		view.startAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.fade_in));
		LinearLayout ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);
		ll_popup.startAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.push_bottom_in));

		setWidth(LayoutParams.MATCH_PARENT);
		setHeight(LayoutParams.WRAP_CONTENT);
		setFocusable(true);
		setOutsideTouchable(true);
		setBackgroundDrawable(new ColorDrawable(0x00000000));
		setContentView(view);

		Button comment_del_btn = (Button) view.findViewById(R.id.comment_del_btn);
		Button item_popupwindows_cancel = (Button) view.findViewById(R.id.item_popupwindows_cancel);

		comment_del_btn.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_DELETE));
		comment_del_btn.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				MenuController.showAllianceShareCommentDeleteComfirm(LanguageManager.getLangByKey(LanguageKeys.TIP_DELETE_COMMENT), comment,
						allianceShareSender);
				dismiss();
			}
		});

		item_popupwindows_cancel.setText(LanguageManager.getLangByKey(LanguageKeys.BTN_CANCEL));
		item_popupwindows_cancel.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				dismiss();
			}
		});

		RelativeLayout commentpopuplayout = (RelativeLayout) view.findViewById(R.id.commentpopuplayout);
		commentpopuplayout.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				dismiss();
			}
		});
	}

	/*MM: 这一段统一到了AllianceSharePermission的回调里。
	public void takePhoto()
	{
		Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		String fileName = UserManager.getInstance().getCurrentUserId() + System.currentTimeMillis() + ".jpeg";
		String targetFileName = AllianceShareManager.getInstance().getLocalAllianceShareCaptureImagePath(fileName);
		File vFile = new File(targetFileName);
		AllianceShareActivity.currentPhotoPath = targetFileName;

		System.out.println("takePhoto path:" + AllianceShareActivity.currentPhotoPath);
		Uri cameraUri = Uri.fromFile(vFile);
		openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
		mActivity.startActivityForResult(openCameraIntent, AllianceShareManager.TAKE_PICTURE);
	}
	*/
}