package com.elex.chatservice.view.allianceshare.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;

import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.elex.chatservice.controller.ChatServiceController;
import com.elex.chatservice.controller.JniController;
import com.elex.chatservice.controller.SwitchUtils;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.db.DBHelper;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.allianceshare.AllianceShareDetailActivity;
import com.elex.chatservice.view.allianceshare.AllianceShareListActivity;
import com.elex.chatservice.view.allianceshare.model.AllianceShareAuthority;
import com.elex.chatservice.view.allianceshare.model.AllianceShareComment;
import com.elex.chatservice.view.allianceshare.model.AllianceShareImageData;
import com.elex.chatservice.view.allianceshare.model.AllianceShareInfo;
import com.elex.chatservice.view.allianceshare.model.ImageItem;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;

public class AllianceShareManager
{
	public static final int						SHARE_NUM_LIMIT							= 9;
	// 首选项:临时图片
	public static final String					PREF_TEMP_IMAGES						= "pref_temp_images";
	public static final String					APPLICATION_NAME						= "clash_of_kings_aliianceshare";
	private Map<String, ImageItem>				selectedImage;
	public static final String					FIREBASE_BASE_URL						= "https://cokallianceshare.firebaseio.com/";
	public static final String					FIREBASE_BASE_NODE_ALLIANCE				= "timeline/alliance/";
	public static final String					FIREBASE_BASE_NOTICE					= "notice/user/";
	public static final String					COGNITO_POOL_ID							= "us-east-1:6230e174-850c-45c8-9e26-67ba0622cdf3";
	public static final String					BUCKET_NAME								= "cokallianceshare2";
	public static final String					AS3_SERVER_IMAGE_BASE_URL				= "http://cokallianceshare2.cdn.cok.chat/";
	public static final String					AS3_SERVER_IMAGE_BASE_THUMB_URL			= "http://cokallianceshare2thumb.cdn.cok.chat/thumb_";
	public static final String					ALLIANCE_DETAIL_FID						= "alliance_detail_fid";
	private static final int					ALLIANCE_SHARE_LOAD_COUNT				= 20;

	public static final int						TAKE_PICTURE							= 65500;

	public static final int						ALLIANCE_SHARE_MSG						= 1;
	public static final int						ALLIANCE_SHARE_IMAGE					= 2;
	public static final int						ALLIANCE_SHARE_VIDEO					= 3;
	public static final int						ALLIANCE_SHARE_LIKE						= 101;
	public static final int						ALLIANCE_SHARE_COMMENT					= 102;
 
	public static boolean						isAllianceShareEnable					= true;

	private Firebase							allianceShareFireBase					= null;
	private Firebase							allianceShareNoticeFireBase				= null;
	private static AllianceShareManager			mInstance								= null;
	private Query								query;
	private Query								noticeQuery;
	private List<AllianceShareInfo>				allianceShareData						= null;
	private List<AllianceShareComment>			allianceShareNotice						= null;
	private Map<String, AllianceShareInfo>		allianceShareDataMap					= null;
	private Map<String, AllianceShareComment>	allianceShareNoticeMap					= null;

	private ExecutorService						executorService							= null;
	private ChildEventListener					mAllianceShareDataChildEventListener	= null;
	private TransferUtility						transferUtility							= null;

	public static long							remoteNewestAllianceShareTime			= 0;
	public static long							localNewestAllianceShareTime			= 0;

	private AllianceShareManager()
	{
		selectedImage = new HashMap<String, ImageItem>();
		executorService = Executors.newFixedThreadPool(2);
		transferUtility = Util.getTransferUtility(ChatServiceController.hostActivity);
        
	}

	public Map<String, ImageItem> getSelectedImages()
	{
		return selectedImage;
	}

	public List<ImageItem> getSelctedImageArray()
	{
		List<ImageItem> array = new ArrayList<ImageItem>();
		if (selectedImage != null && selectedImage.size() > 0)
		{
			Collection<ImageItem> collection = selectedImage.values();
			for (ImageItem item : collection)
			{
				if (item != null)
					array.add(0, item);
			}
		}
		return array;
	}

	public boolean containsSelelctedImageKey(String key)
	{
		if (StringUtils.isEmpty(key) || selectedImage == null || selectedImage.size() <= 0)
			return false;
		return selectedImage.containsKey(key);
	}

	public void clearSelectedImages()
	{
		if (selectedImage != null)
			selectedImage.clear();
	}

	public List<AllianceShareInfo> getAllianceShareData()
	{
		return allianceShareData;
	}

	public boolean isAllianceShareDataExist()
	{
		if (allianceShareData == null || allianceShareData.size() <= 0)
			return false;
		return true;
	}

	public List<AllianceShareComment> getAllianceShareNoticeData()
	{
		return allianceShareNotice;
	}

	public int getAllianceShareNoticeNum()
	{
		if (allianceShareNotice != null)
			return allianceShareNotice.size();
		return 0;
	}

	public static AllianceShareManager getInstance()
	{
		if (mInstance == null)
		{
			synchronized (AllianceShareManager.class)
			{
				if (mInstance == null)
					mInstance = new AllianceShareManager();
			}
		}
		return mInstance;
	}

	public static AllianceShareListActivity getAllianceShareListActivity()
	{
		if (ChatServiceController.getCurrentActivity() != null
				&& ChatServiceController.getCurrentActivity() instanceof AllianceShareListActivity)
			return (AllianceShareListActivity) ChatServiceController.getCurrentActivity();
		return null;
	}

	public static AllianceShareInfo parseAllianceShareData(DataSnapshot data)
	{
		AllianceShareInfo info = null;
		if (data != null)
		{
			info = new AllianceShareInfo();
			if (data.hasChild("at"))
				info.setAt(data.child("at").getValue().toString());
			if (data.hasChild("authority"))
				info.setAuthority(data.child("authority").getValue(AllianceShareAuthority.class));
			if (data.hasChild("data"))
			{
				String dataJson = data.child("data").getValue().toString();
				if (StringUtils.isNotEmpty(dataJson))
				{
					try
					{
						List<AllianceShareImageData> imageDataList = JSON.parseArray(dataJson, AllianceShareImageData.class);
						info.setData(imageDataList);
					}
					catch (JSONException e)
					{
						e.printStackTrace();
					}
				}
			}
			if (data.hasChild("fid"))
				info.setFid(data.child("fid").getValue().toString());
			if (data.hasChild("id"))
				info.setId(data.child("id").getValue().toString());
			if (data.hasChild("msg"))
				info.setMsg(data.child("msg").getValue().toString());
			if (data.hasChild("sender"))
				info.setSender(data.child("sender").getValue().toString());
			if (data.hasChild("status"))
				info.setStatus(data.child("status").getValue(Long.class).intValue());
			if (data.hasChild("time"))
				info.setTime(data.child("time").getValue(Long.class).longValue());
			if (data.hasChild("type"))
				info.setType(data.child("type").getValue(Long.class).intValue());
			if (data.hasChild("comment") && data.child("comment").hasChildren())
			{
				List<AllianceShareComment> commentArray = new ArrayList<AllianceShareComment>();
				for (DataSnapshot commentData : data.child("comment").getChildren())
				{
					if (commentData != null)
					{
						commentArray.add(commentData.getValue(AllianceShareComment.class));
						Collections.sort(commentArray);
					}
				}
				info.setComment(commentArray);
			}

			if (data.hasChild("like") && data.child("like").hasChildren())
			{
				List<AllianceShareComment> likeArray = new ArrayList<AllianceShareComment>();
				for (DataSnapshot commentData : data.child("like").getChildren())
				{
					if (commentData != null)
						likeArray.add(commentData.getValue(AllianceShareComment.class));
				}
				info.setLike(likeArray);
			}
		}
		return info;
	}

	public static AllianceShareComment parseAllianceShareCommentData(DataSnapshot data)
	{
		AllianceShareComment comment = null;
		if (data != null)
		{
			comment = new AllianceShareComment();
			if (data.hasChild("at"))
				comment.setAt(data.child("at").getValue().toString());
			if (data.hasChild("authority"))
				comment.setAuthority(data.child("authority").getValue(AllianceShareAuthority.class));
			if (data.hasChild("fid"))
				comment.setFid(data.child("fid").getValue().toString());
			if (data.hasChild("id"))
				comment.setId(data.child("id").getValue().toString());
			if (data.hasChild("msg"))
				comment.setMsg(data.child("msg").getValue().toString());
			if (data.hasChild("sender"))
				comment.setSender(data.child("sender").getValue().toString());
			if (data.hasChild("status"))
				comment.setStatus(data.child("status").getValue(Long.class).intValue());
			if (data.hasChild("time"))
				comment.setTime(data.child("time").getValue(Long.class).longValue());
			if (data.hasChild("type"))
				comment.setType(data.child("type").getValue(Long.class).intValue());
		}
		return comment;
	}

	public void deleteAllianceShare(String id)
	{
		if (!isAllianceShareEnable || UserManager.getInstance().getCurrentUser() == null
				|| StringUtils.isEmpty(UserManager.getInstance().getCurrentUser().allianceId))
			return;
		String allianceFireBaseUrl = AllianceShareManager.FIREBASE_BASE_URL + AllianceShareManager.FIREBASE_BASE_NODE_ALLIANCE
				+ UserManager.getInstance().getCurrentUser().allianceId;
		allianceFireBaseUrl += id;
		Firebase fireBase = new Firebase(allianceFireBaseUrl);
		fireBase.setValue(null);
	}

	private void initAllianceShareHandler()
	{
		if (!isAllianceShareEnable || UserManager.getInstance().getCurrentUser() == null
				|| StringUtils.isEmpty(UserManager.getInstance().getCurrentUser().allianceId))
			return;
		String allianceFireBaseUrl = AllianceShareManager.FIREBASE_BASE_URL + AllianceShareManager.FIREBASE_BASE_NODE_ALLIANCE
				+ UserManager.getInstance().getCurrentUser().allianceId;
		System.out.println("initFireBase allianceFireBaseUrl:" + allianceFireBaseUrl);

        allianceShareFireBase = new Firebase(allianceFireBaseUrl);

		mAllianceShareDataChildEventListener = new ChildEventListener()
		{

			@Override
			public void onChildRemoved(DataSnapshot datasnapshot)
			{
				System.out.println("onChildRemoved");
				System.out.println("data key:" + datasnapshot.getKey());
				AllianceShareInfo shareInfo = AllianceShareManager.parseAllianceShareData(datasnapshot);
				if (shareInfo != null && StringUtils.isNotEmpty(shareInfo.getId()))
				{
					System.out.println("data time :" + shareInfo.getTime() + "   type:" + shareInfo.getType());
					removeData(shareInfo);
				}
				System.out.println("-------------------------");

			}

			@Override
			public void onChildMoved(DataSnapshot datasnapshot, String s)
			{

			}

			@Override
			public void onChildChanged(DataSnapshot datasnapshot, String s)
			{
				System.out.println("onChildChanged");
				System.out.println("data key:" + datasnapshot.getKey());
				AllianceShareInfo shareInfo = AllianceShareManager.parseAllianceShareData(datasnapshot);
				if (shareInfo != null && StringUtils.isNotEmpty(shareInfo.getId()))
				{
					System.out.println("data time :" + shareInfo.getTime() + "   type:" + shareInfo.getType());
					changeData(shareInfo);

				}
				System.out.println("-------------------------");
			}

			@Override
			public void onChildAdded(DataSnapshot datasnapshot, String s)
			{
				// System.out.println("initAllianceShareHandler onChildAdded");
				// System.out.println("initAllianceShareHandler data key:" + datasnapshot.getKey());
				AllianceShareInfo shareInfo = AllianceShareManager.parseAllianceShareData(datasnapshot);
				if (shareInfo != null)
				{
					// System.out.println("initAllianceShareHandler data time :" + shareInfo.getTime() + " type:" + shareInfo.getType() + " msg:" +
					// shareInfo.getMsg());
					addAllianceShare(shareInfo);
				}
				System.out.println("-------------------------");
			}

			@Override
			public void onCancelled(FirebaseError firebaseerror)
			{

			}
		};
	}

	private void initShareFireBase()
	{
		initAllianceShareHandler();
		if (allianceShareData == null)
			allianceShareData = new ArrayList<AllianceShareInfo>();
		else
			allianceShareData.clear();
		if (allianceShareDataMap == null)
			allianceShareDataMap = new HashMap<String, AllianceShareInfo>();
		else
			allianceShareDataMap.clear();
		AllianceShareListActivity.onAllianceShareDataChanged();
		if (allianceShareFireBase != null)
		{
			query = allianceShareFireBase.orderByChild("time").limitToLast(ALLIANCE_SHARE_LOAD_COUNT);
			if (mAllianceShareDataChildEventListener != null && query != null)
				query.addChildEventListener(mAllianceShareDataChildEventListener);
		}
	}

	public void goOnline()
	{
		System.out.println("goOnline");
        if (SwitchUtils.allianceShareFirebaseKeepSynEnable) {
            System.out.println("zp 联盟分享Firebase数据库同步开关是 打开状态！");
        }else {
            System.out.println("zp 联盟分享Firebase数据库同步开关是 关闭状态！");
        }
		Firebase.goOnline();
		if (allianceShareFireBase != null && SwitchUtils.allianceShareFirebaseKeepSynEnable)
			allianceShareFireBase.keepSynced(true);
		if (allianceShareNoticeFireBase != null && SwitchUtils.allianceShareFirebaseKeepSynEnable)
			allianceShareNoticeFireBase.keepSynced(true);
	}

	public void goOffline()
	{
		if (allianceShareFireBase != null)
			allianceShareFireBase.keepSynced(false);
		if (allianceShareNoticeFireBase != null)
			allianceShareNoticeFireBase.keepSynced(false);
		Firebase.goOffline();
	}


	private void addAllianceShare(AllianceShareInfo info)
	{
		if (info == null || StringUtils.isEmpty(info.getId()) || allianceShareData == null || allianceShareDataMap == null
				|| allianceShareDataMap.containsKey(info.getId()))
			return;
		allianceShareData.add(info);
		allianceShareDataMap.put(info.getId(), info);
		Collections.sort(allianceShareData);
		if (info.getTime() > remoteNewestAllianceShareTime)
			remoteNewestAllianceShareTime = info.getTime();
		AllianceShareListActivity.onAllianceShareDataChanged();
	}

	private void addAllianceShareNotice(AllianceShareComment comment)
	{
		if (comment == null || StringUtils.isEmpty(comment.getId()) || allianceShareNotice == null || allianceShareNoticeMap == null
				|| allianceShareNoticeMap.containsKey(comment.getId()))
			return;
		AllianceShareAuthority authority = comment.getAuthority();
		if (authority != null && StringUtils.isNotEmpty(authority.getAlliance()) && UserManager.getInstance().getCurrentUser() != null
				&& StringUtils.isNotEmpty(UserManager.getInstance().getCurrentUser().allianceId)
				&& authority.getAlliance().equals(UserManager.getInstance().getCurrentUser().allianceId))
		{
			allianceShareNotice.add(comment);
			allianceShareNoticeMap.put(comment.getId(), comment);
			Collections.sort(allianceShareNotice);
			JniController.getInstance().excuteJNIVoidMethod("postAllianceShareNoticeNum",
					new Object[] { Integer.valueOf(allianceShareNotice.size()) });
			if (ChatServiceController.getAllianceShareListActivity() != null)
				ChatServiceController.getAllianceShareListActivity().refreshAllianceShareNotice();
			if (ChatServiceController.getAllianceShareCommentListActivity() != null)
				ChatServiceController.getAllianceShareCommentListActivity().notifyDataChanged();

			if (allianceShareDataMap != null && !allianceShareDataMap.containsKey(comment.getFid()))
			{
				if (!isAllianceShareEnable || UserManager.getInstance().getCurrentUser() == null
						|| StringUtils.isEmpty(UserManager.getInstance().getCurrentUser().allianceId))
					return;
				String fireBaseUrl = AllianceShareManager.FIREBASE_BASE_URL + AllianceShareManager.FIREBASE_BASE_NODE_ALLIANCE
						+ UserManager.getInstance().getCurrentUser().allianceId + "/" + comment.getFid();
				// System.out.println("addAllianceShare from Notice fireBaseUrl:" + fireBaseUrl);
				Firebase fireBase = new Firebase(fireBaseUrl);
				if (mAllianceShareDataChildEventListener != null)
					fireBase.addChildEventListener(mAllianceShareDataChildEventListener);
			}
		}
	}

	public void loadMoreHistoryAllianceShare()
	{
		initAllianceShareHandler();
		if (allianceShareFireBase == null || allianceShareData == null || allianceShareData.size() <= 0)
		{
			if (ChatServiceController.getAllianceShareListActivity() != null
					&& ChatServiceController.getAllianceShareListActivity().adapter != null)
				ChatServiceController.getAllianceShareListActivity().onLoadMoreComplete();
			return;
		}
		long time = 0;
		for (int i = allianceShareData.size() - 1; i >= 0; i--)
		{
			AllianceShareInfo info = allianceShareData.get(i);
			if (info != null && info.getTime() > 0)
			{
				time = info.getTime();
				break;
			}
		}
		if (time == 0)
		{
			if (ChatServiceController.getAllianceShareListActivity() != null
					&& ChatServiceController.getAllianceShareListActivity().adapter != null)
				ChatServiceController.getAllianceShareListActivity().onLoadMoreComplete();
			return;
		}

		if (ChatServiceController.getAllianceShareListActivity() != null)
			ChatServiceController.getAllianceShareListActivity().createTimerTask();
		Query query = allianceShareFireBase.orderByChild("time").endAt(time).limitToLast(ALLIANCE_SHARE_LOAD_COUNT);
		query.addChildEventListener(new ChildEventListener()
		{

			@Override
			public void onChildRemoved(DataSnapshot datasnapshot)
			{
				System.out.println("onChildRemoved");
				System.out.println("data key:" + datasnapshot.getKey());
                
				AllianceShareInfo shareInfo = AllianceShareManager.parseAllianceShareData(datasnapshot);
				if (shareInfo != null && StringUtils.isNotEmpty(shareInfo.getId()))
				{
					System.out.println("data time :" + shareInfo.getTime() + "   type:" + shareInfo.getType());
					removeData(shareInfo);
				}
				System.out.println("-------------------------");

			}

			@Override
			public void onChildMoved(DataSnapshot datasnapshot, String s)
			{

			}

			@Override
			public void onChildChanged(DataSnapshot datasnapshot, String s)
			{
				System.out.println("onChildChanged");
				System.out.println("data key:" + datasnapshot.getKey());
				AllianceShareInfo shareInfo = AllianceShareManager.parseAllianceShareData(datasnapshot);
				if (shareInfo != null && StringUtils.isNotEmpty(shareInfo.getId()))
				{
					System.out.println("data time :" + shareInfo.getTime() + "   type:" + shareInfo.getType());
					changeData(shareInfo);

				}
				System.out.println("-------------------------");
			}

			@Override
			public void onChildAdded(DataSnapshot datasnapshot, String s)
			{
				System.out.println("onChildAdded");
				System.out.println("data key:" + datasnapshot.getKey());
				AllianceShareInfo shareInfo = AllianceShareManager.parseAllianceShareData(datasnapshot);
				if (shareInfo != null)
				{
					System.out.println("data time :" + shareInfo.getTime() + "   type:" + shareInfo.getType());
					addAllianceShare(shareInfo);
					if (ChatServiceController.getAllianceShareListActivity() != null
							&& ChatServiceController.getAllianceShareListActivity().adapter != null)
					{
						ChatServiceController.getAllianceShareListActivity().onLoadMoreComplete();
					}
				}
				System.out.println("-------------------------");
			}

			@Override
			public void onCancelled(FirebaseError firebaseerror)
			{

			}
		});
	}

	private void initShareNoticeFireBase()
	{
		allianceShareNotice = new ArrayList<AllianceShareComment>();
		allianceShareNoticeMap = new HashMap<String, AllianceShareComment>();
		AllianceShareListActivity.onAllianceShareDataChanged();
		if (!isAllianceShareEnable || StringUtils.isEmpty(UserManager.getInstance().getCurrentUserId()))
			return;

		String noticeFireBaseUrl = AllianceShareManager.FIREBASE_BASE_URL + AllianceShareManager.FIREBASE_BASE_NOTICE
				+ UserManager.getInstance().getCurrentUserId();

		allianceShareNoticeFireBase = new Firebase(noticeFireBaseUrl);
		if (allianceShareNoticeFireBase != null)
		{
			noticeQuery = allianceShareNoticeFireBase.orderByChild("time");
			if (noticeQuery != null)
			{
				noticeQuery.addChildEventListener(new ChildEventListener()
				{

					@Override
					public void onChildRemoved(DataSnapshot datasnapshot)
					{
						System.out.println("initShareNoticeFireBase onChildRemoved");
						System.out.println("data key:" + datasnapshot.getKey());
						AllianceShareComment shareComment = AllianceShareManager.parseAllianceShareCommentData(datasnapshot);
						if (shareComment != null && StringUtils.isNotEmpty(shareComment.getId()))
						{
							System.out.println("data time :" + shareComment.getTime() + "   type:" + shareComment.getType());
							removeNoticeData(shareComment);
						}
						System.out.println("-------------------------");

					}

					@Override
					public void onChildMoved(DataSnapshot datasnapshot, String s)
					{

					}

					@Override
					public void onChildChanged(DataSnapshot datasnapshot, String s)
					{
						System.out.println("initShareNoticeFireBase onChildChanged");
						System.out.println("data key:" + datasnapshot.getKey());
						AllianceShareComment shareComment = AllianceShareManager.parseAllianceShareCommentData(datasnapshot);
						if (shareComment != null && StringUtils.isNotEmpty(shareComment.getId()))
						{
							System.out.println("data time :" + shareComment.getTime() + "   type:" + shareComment.getType());
							changeNoticeData(shareComment);
						}
						System.out.println("-------------------------");
					}

					@Override
					public void onChildAdded(DataSnapshot datasnapshot, String s)
					{
						System.out.println("initShareNoticeFireBase onChildAdded");
						System.out.println("data key:" + datasnapshot.getKey());
						AllianceShareComment shareComment = AllianceShareManager.parseAllianceShareCommentData(datasnapshot);
						if (shareComment != null && StringUtils.isNotEmpty(shareComment.getId())
								&& (shareComment.isComment() && StringUtils.isNotEmpty(shareComment.getMsg()) || shareComment.isLike()))
						{
							System.out.println("data time :" + shareComment.getTime() + "   type:" + shareComment.getType());
							addAllianceShareNotice(shareComment);
						}
						System.out.println("-------------------------");
					}

					@Override
					public void onCancelled(FirebaseError firebaseerror)
					{
						// TODO Auto-generated method stub

					}
				});
			}
		}
	}

	public void initFireBase()
	{
		System.out.println("initFireBase goOnline");
		goOnline();
		initShareFireBase();
		initShareNoticeFireBase();
		if (ChatServiceController.getAllianceShareListActivity() != null)
			return;
		TimerTask task = new TimerTask()
		{

			@Override
			public void run()
			{
				if (ChatServiceController.getAllianceShareListActivity() != null || ChatServiceController.getAllianceShareDetailActivity() != null
						|| ChatServiceController.getAllianceShareCommentListActivity() != null)
					return;
				System.out.println("initFireBase goOffline");
				goOffline();
			}
		};
		Timer timer = new Timer();
		timer.schedule(task, 20000);
	}

	public void clearFireBaseData()
	{
		if (allianceShareData == null)
			allianceShareData = new ArrayList<AllianceShareInfo>();
		else
			allianceShareData.clear();
		if (allianceShareDataMap == null)
			allianceShareDataMap = new HashMap<String, AllianceShareInfo>();
		else
			allianceShareDataMap.clear();
		AllianceShareListActivity.onAllianceShareDataChanged();
	}

	private void changeNoticeData(AllianceShareComment comment)
	{
		if (comment == null || allianceShareNoticeMap == null || StringUtils.isEmpty(comment.getId())
				|| !allianceShareNoticeMap.containsKey(comment.getId()))
			return;
		allianceShareNoticeMap.put(comment.getId(), comment);
		for (int i = 0; i < allianceShareNotice.size(); i++)
		{
			AllianceShareComment allianceShareComment = allianceShareNotice.get(i);
			if (allianceShareComment != null && allianceShareComment.getId().equals(comment.getId()))
			{
				allianceShareNotice.set(i, comment);
				if (ChatServiceController.getAllianceShareListActivity() != null)
					ChatServiceController.getAllianceShareListActivity().refreshAllianceShareNotice();
				break;
			}
		}

	}

	private void changeData(AllianceShareInfo info)
	{
		if (info == null || allianceShareDataMap == null || StringUtils.isEmpty(info.getId())
				|| !allianceShareDataMap.containsKey(info.getId()))
			return;
		allianceShareDataMap.put(info.getId(), info);
		for (int i = 0; i < allianceShareData.size(); i++)
		{
			AllianceShareInfo allianceShareInfo = allianceShareData.get(i);
			if (allianceShareInfo != null && allianceShareInfo.getId().equals(info.getId()))
			{
				allianceShareData.set(i, info);
				AllianceShareListActivity.onAllianceShareDataChanged();
				AllianceShareDetailActivity.onAllianceShareDataChanged();
				break;
			}
		}
	}

	private void removeData(AllianceShareInfo info)
	{
		if (info == null || allianceShareDataMap == null || StringUtils.isEmpty(info.getId())
				|| !allianceShareDataMap.containsKey(info.getId()))
			return;
		for (int i = 0; i < allianceShareData.size(); i++)
		{
			AllianceShareInfo allianceShareInfo = allianceShareData.get(i);
			if (allianceShareInfo != null && allianceShareInfo.getId().equals(info.getId()))
			{
				allianceShareData.remove(i);
				AllianceShareListActivity.onAllianceShareDataChanged();
				AllianceShareDetailActivity.onAllianceShareDataChanged();
				break;
			}
		}
		allianceShareDataMap.remove(info.getId());
	}

	private void removeNoticeData(AllianceShareComment comment)
	{
		if (comment == null || allianceShareNoticeMap == null || StringUtils.isEmpty(comment.getId())
				|| !allianceShareNoticeMap.containsKey(comment.getId()))
			return;
		for (int i = 0; i < allianceShareNotice.size(); i++)
		{
			AllianceShareComment allianceShareComment = allianceShareNotice.get(i);
			if (allianceShareComment != null && allianceShareComment.getId().equals(comment.getId()))
			{
				allianceShareNotice.remove(i);
				if (ChatServiceController.getAllianceShareListActivity() != null)
					ChatServiceController.getAllianceShareListActivity().refreshAllianceShareNotice();
				break;
			}
		}
		if (allianceShareNoticeMap != null)
			allianceShareNoticeMap.remove(comment.getId());
	}

	public int getSelectedImageSize()
	{
		if (selectedImage != null)
			return selectedImage.size();
		return 0;
	}

	public boolean canAddImage()
	{
		return getSelectedImageSize() < SHARE_NUM_LIMIT;
	}

	public void removeSelectedImage(String key)
	{
		if (selectedImage != null && StringUtils.isNotEmpty(key) && selectedImage.containsKey(key))
			selectedImage.remove(key);
	}

	public void putSelectedImage(String key, ImageItem imageItem)
	{
		if (selectedImage == null || selectedImage.size() >= SHARE_NUM_LIMIT || StringUtils.isEmpty(key) || imageItem == null)
			return;
		selectedImage.put(key, imageItem);
	}

	public void clearNoticeData()
	{
		JniController.getInstance().excuteJNIVoidMethod("deleteAllianceShareNotice",
				new Object[] { AllianceShareManager.getInstance().getAllianceShareNoticeIds() });
		if (allianceShareNotice != null)
			allianceShareNotice.clear();
		JniController.getInstance().excuteJNIVoidMethod("postAllianceShareNoticeNum", new Object[] { Integer.valueOf(0) });
		AllianceShareListActivity.onAllianceShareDataChanged();
	}

	public String getAllianceShareNoticeIds()
	{
		String ids = "";
		if (allianceShareNotice != null && allianceShareNotice.size() > 0)
		{
			for (int i = 0; i < allianceShareNotice.size(); i++)
			{
				AllianceShareComment comment = allianceShareNotice.get(i);
				if (comment != null && StringUtils.isNotEmpty(comment.getId()))
				{
					if (StringUtils.isNotEmpty(ids))
						ids += ",";
					ids += comment.getId();
				}
			}
		}
		return ids;
	}

	public AllianceShareInfo getAllianceShareInfoById(String id)
	{
		if (StringUtils.isEmpty(id) || allianceShareDataMap == null || !allianceShareDataMap.containsKey(id))
			return null;
		return allianceShareDataMap.get(id);
	}

	public String getLocalAllianceShareImagePath(String fileName)
	{
		String path = DBHelper.getLocalDirectoryPath(ChatServiceController.hostActivity, "alliance_share_image") + fileName;
		return path;
	}

	public String getLocalAllianceShareCaptureImagePath(String fileName)
	{
		String path = DBHelper.getLocalDirectoryPath(ChatServiceController.hostActivity, "alliance_share_capture_image") + fileName;
		return path;
	}

	public String getLocalAllianceShareImageThumbPath(String fileName)
	{
		String path = DBHelper.getLocalDirectoryPath(ChatServiceController.hostActivity, "alliance_share_image/thumb") + fileName;
		return path;
	}

	public String getServerAllianceShareImagePath(String fileName)
	{
		String path = AS3_SERVER_IMAGE_BASE_URL + fileName;
		return path;
	}

	public String getServerAllianceShareImageThumbPath(String fileName)
	{
		String path = AS3_SERVER_IMAGE_BASE_THUMB_URL + fileName;
		return path;
	}

	public static int getImageWidthByImageNum(int num)
	{
		int screenWidth = ScaleUtil.getScreenWidth();
		int imageWidth = screenWidth / 5;
		if (imageWidth > 240)
			imageWidth = 240;
		if (num == 0)
			return 0;
		else if (num == 1)
			return imageWidth * 3;
		else if (num == 2 || num == 4)
			return (int) (imageWidth * 1.5);
		else
			return imageWidth;
	}

	public static int getGridColumNumByImageNum(int num)
	{
		if (num == 0)
			return 0;
		else if (num == 1)
			return 1;
		else if (num == 2 || num == 4)
			return 2;
		else
			return 3;
	}

	public int getGridRowNumByImageNum(int num)
	{
		if (num == 0)
			return 0;
		else if (num <= 3)
			return 1;
		else if (num <= 6)
			return 2;
		else
			return 3;
	}

	public void uploadImage(final File file, final String targetFileName)
	{
		if (executorService != null)
		{
			executorService.execute(new Runnable()
			{

				@Override
				public void run()
				{
					try
					{
						if(transferUtility!=null)
							transferUtility.upload(BUCKET_NAME, targetFileName, file);
					}
					catch (OutOfMemoryError e)
					{
						e.printStackTrace();
					}
				}
			});
		}
	}

	public boolean isAllianceShareInfoExist(String id)
	{
		if (allianceShareDataMap == null || !allianceShareDataMap.containsKey(id))
			return false;
		return true;
	}

	public static void adjustGridViewSize(GridView gridView, int imageNum)
	{
		if (gridView == null)
			return;
		int columWidth = AllianceShareManager.getImageWidthByImageNum(imageNum);
		int columNum = AllianceShareManager.getGridColumNumByImageNum(imageNum);
		int rowNum = AllianceShareManager.getInstance().getGridRowNumByImageNum(imageNum);

		if (columWidth == 0 || columNum == 0 || rowNum == 0)
		{
			gridView.setVisibility(View.GONE);
		}
		else
		{
			gridView.setColumnWidth(columWidth);
			gridView.setNumColumns(columNum);
			gridView.setVisibility(View.VISIBLE);
			LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) gridView.getLayoutParams();
			int width = columWidth * columNum + (columNum - 1) * ScaleUtil.dip2px(5);
			int height = columWidth * rowNum + (rowNum - 1) * ScaleUtil.dip2px(5);
			layoutParams.width = width;
			layoutParams.height = height;
			gridView.setLayoutParams(layoutParams);
		}

	}

}
