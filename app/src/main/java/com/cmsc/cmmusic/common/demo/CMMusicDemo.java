package com.cmsc.cmmusic.common.demo;

import java.io.File;
import java.net.URLEncoder;
import java.util.Hashtable;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.cmsc.cmmusic.common.CMMusicCallback;
import com.cmsc.cmmusic.common.CPManagerInterface;
import com.cmsc.cmmusic.common.DigitalAlbumManagerInterface;
import com.cmsc.cmmusic.common.ExclusiveManagerInterface;
import com.cmsc.cmmusic.common.FullSongManagerInterface;
import com.cmsc.cmmusic.common.Logger;
import com.cmsc.cmmusic.common.MusicQueryInterface;
import com.cmsc.cmmusic.common.OnlineListenerMusicInterface;
import com.cmsc.cmmusic.common.RingbackManagerInterface;
import com.cmsc.cmmusic.common.SongRecommendManagerInterface;
import com.cmsc.cmmusic.common.UserManagerInterface;
import com.cmsc.cmmusic.common.VibrateRingManagerInterface;
import com.cmsc.cmmusic.common.data.AlbumListRsp;
import com.cmsc.cmmusic.common.data.ChartListRsp;
import com.cmsc.cmmusic.common.data.CrbtListRsp;
import com.cmsc.cmmusic.common.data.CrbtOpenCheckRsp;
import com.cmsc.cmmusic.common.data.CrbtPrelistenRsp;
import com.cmsc.cmmusic.common.data.DownloadResult;
import com.cmsc.cmmusic.common.data.MusicInfoResult;
import com.cmsc.cmmusic.common.data.MusicListRsp;
import com.cmsc.cmmusic.common.data.OrderResult;
import com.cmsc.cmmusic.common.data.OwnRingRsp;
import com.cmsc.cmmusic.common.data.QueryResult;
import com.cmsc.cmmusic.common.data.Result;
import com.cmsc.cmmusic.common.data.SingerInfoRsp;
import com.cmsc.cmmusic.common.data.SongRecommendResult;
import com.cmsc.cmmusic.common.data.StreamRsp;
import com.cmsc.cmmusic.common.data.TagListRsp;
import com.cmsc.cmmusic.init.InitCmmInterface;
import com.cmsc.cmmusic.init.SmsLoginInfoRsp;

public class CMMusicDemo extends Activity implements OnClickListener {
	private final static String LOG_TAG = "CMMusicDemo";

	private ProgressDialog dialog;

	private long requestTime;

	private UIHandler mUIHandler = new UIHandler();

	private class UIHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			long responseTime = System.currentTimeMillis() - requestTime;

			switch (msg.what) {
				case 0:
					if (msg.obj == null) {
						hideProgressBar();
						Toast.makeText(CMMusicDemo.this, "结果 = null",
								Toast.LENGTH_SHORT).show();
						return;
					}
					new AlertDialog.Builder(CMMusicDemo.this).setTitle("结果")
															 .setMessage((msg.obj).toString())
															 .setPositiveButton("确认", null).show();
					break;
				case 1:
					if (msg.obj == null) {
						hideProgressBar();
						Toast.makeText(CMMusicDemo.this, "结果 = null",
								Toast.LENGTH_SHORT).show();
						return;
					}

					showDownloadAlertDialog((OrderResult) msg.obj, "结果");

					break;
			}

			hideProgressBar();
			if (null != dialog) {
				dialog.dismiss();
			}

		}
	}

	private void showDownloadAlertDialog(final OrderResult downloadResult,
										 String title) {
		if (null != downloadResult) {
			new AlertDialog.Builder(CMMusicDemo.this)
					.setTitle(title)
					.setMessage(downloadResult.toString())
					.setPositiveButton("确认", null)
					.setNeutralButton("下载",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
													int which) {
									if (null != downloadResult.getDownUrl()
											&& 0 < downloadResult.getDownUrl()
																 .length()) {
										Uri uri = Uri.parse(downloadResult
												.getDownUrl());
										Intent it = new Intent(
												Intent.ACTION_VIEW, uri);
										startActivity(it);
									}
								}
							}).show();
		} else {
			Toast.makeText(CMMusicDemo.this, "结果 = null", Toast.LENGTH_SHORT)
				 .show();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cmmusic_demo);

		InitCmmInterface.initSDK(this);

		final TextView logText = (TextView) this.findViewById(R.id.logText);
		Button getLogButton = (Button) this.findViewById(R.id.get_log);
		getLogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				logText.setText(Logger.getPreferencesLog(CMMusicDemo.this));
			}
		});

		Button cleanLogButton = (Button) this.findViewById(R.id.clean_log);
		cleanLogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Logger.cleanPreferencesLog(CMMusicDemo.this);
				logText.setText("");
			}
		});

		Button initButton = (Button) this.findViewById(R.id.initButton);
		initButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// if (!InitCmmInterface.initCheck(CMMusicDemo.this)) {
				dialog = ProgressDialog.show(CMMusicDemo.this, null, "请稍候……",
						true, false);
				requestTime = System.currentTimeMillis();
				new Thread(new T1()).start();
				// } else {
				// new
				// AlertDialog.Builder(CMMusicDemo.this).setTitle("init").setMessage("已初始化过")
				// .setPositiveButton("确认", null).show();
				// }
			}
		});

		// CP专属包月
		Button cpButton = (Button) this.findViewById(R.id.cp);
		cpButton.setOnClickListener(new OnClickListener() {
			String[] strs = new String[] { "CP按次全曲下载", "CP按次振铃下载", "CP专属全曲下载",
					"CP专属振铃下载", "查询CP专属包月订购关系", "CP专属包月订购", "CP专属包月退订" };

			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(CMMusicDemo.this).setTitle("CP专属包月类")
														 .setItems(strs, new DialogInterface.OnClickListener() {
															 public void onClick(DialogInterface dialog,
																				 int which) {
																 switch (which) {
																	 case 0:
																		 showParameterDialog("musicId",
																				 new ParameterCallback() {
																					 @Override
																					 public void callback(
																							 final String musicId) {
																						 Log.i("TAG", "musicId = "
																								 + musicId);
																						 CPManagerInterface
																								 .getCPFullSongTimeDownloadUrl(
																										 CMMusicDemo.this,
																										 musicId,
																										 new CMMusicCallback<OrderResult>() {
																											 @Override
																											 public void operationResult(
																													 final OrderResult downloadResult) {
																												 showDownloadAlertDialog(
																														 downloadResult,
																														 "getCPFullSongDownloadUrlByNet");

																												 Log.d(LOG_TAG,
																														 "vRing Download result is "
																																 + downloadResult);
																											 }
																										 });
																					 }
																				 });
																		 break;
																	 case 1:
																		 showParameterDialog("musicId",
																				 new ParameterCallback() {
																					 @Override
																					 public void callback(
																							 final String musicId) {
																						 Log.i("TAG", "musicId = "
																								 + musicId);
																						 CPManagerInterface
																								 .queryCPVibrateRingTimeDownloadUrl(
																										 CMMusicDemo.this,
																										 musicId,
																										 new CMMusicCallback<OrderResult>() {
																											 @Override
																											 public void operationResult(
																													 final OrderResult downloadResult) {
																												 showDownloadAlertDialog(
																														 downloadResult,
																														 "queryCPVibrateRingDownloadUrl");

																												 Log.d(LOG_TAG,
																														 "vRing Download result is "
																																 + downloadResult);
																											 }
																										 });
																					 }
																				 });
																		 break;

																	 case 2:
																		 showParameterDialog(
																				 new String[] { "serviceId",
																						 "musicId", "codeRate" },
																				 new ParameterCallbacks() {
																					 @Override
																					 public void callback(
																							 final String[] parameters) {
																						 Log.i("TAG",
																								 "parameters = "
																										 + parameters);
																						 showProgressBar("数据加载中...");
																						 CPManagerInterface
																								 .queryCPFullSongDownloadUrl(
																										 CMMusicDemo.this,
																										 parameters[0],
																										 parameters[1],
																										 parameters[2],
																										 new CMMusicCallback<OrderResult>() {

																											 @Override
																											 public void operationResult(
																													 final OrderResult downloadResult) {
																												 mUIHandler
																														 .obtainMessage(
																																 1,
																																 downloadResult)
																														 .sendToTarget();
																											 }

																										 });
																					 }
																				 });

																		 break;

																	 case 3:
																		 showParameterDialog(
																				 new String[] { "serviceId",
																						 "musicId", "codeRate" },
																				 new ParameterCallbacks() {
																					 @Override
																					 public void callback(
																							 final String[] parameters) {
																						 Log.i("TAG",
																								 "parameters = "
																										 + parameters);
																						 showProgressBar("数据加载中...");

																						 CPManagerInterface
																								 .queryCPVibrateRingDownloadUrl(
																										 CMMusicDemo.this,
																										 parameters[0],
																										 parameters[1],
																										 parameters[2],
																										 new CMMusicCallback<OrderResult>() {

																											 @Override
																											 public void operationResult(
																													 final OrderResult downloadResult) {
																												 mUIHandler
																														 .obtainMessage(
																																 1,
																																 downloadResult)
																														 .sendToTarget();
																											 }

																										 });
																					 }
																				 });
																		 break;

																	 case 4:
																		 showParameterDialog("serviceId",
																				 new ParameterCallback() {
																					 @Override
																					 public void callback(
																							 final String serviceId) {
																						 Log.i("TAG", "serviceId = "
																								 + serviceId);
																						 showProgressBar("数据加载中...");
																						 new Thread() {
																							 @Override
																							 public void run() {
																								 QueryResult t = CPManagerInterface
																										 .queryCPMonth(
																												 CMMusicDemo.this,
																												 serviceId);
																								 mUIHandler
																										 .obtainMessage(
																												 0,
																												 t)
																										 .sendToTarget();
																							 }
																						 }.start();
																					 }
																				 });
																		 break;

																	 case 5:
																		 showParameterDialog(new String[] {
																						 "serviceId", "definedseq" },
																				 new ParameterCallbacks() {
																					 @Override
																					 public void callback(
																							 final String[] parameters) {
																						 Log.i("TAG", "serviceId = "
																								 + parameters[0]);
																						 CPManagerInterface
																								 .openCPMonth(
																										 CMMusicDemo.this,
																										 parameters[0],
																										 parameters[1],
																										 new CMMusicCallback<OrderResult>() {
																											 @Override
																											 public void operationResult(
																													 OrderResult result) {
																												 if (null != result) {
																													 new AlertDialog.Builder(
																															 CMMusicDemo.this)
																															 .setTitle(
																																	 "openCpMonth")
																															 .setMessage(
																																	 result.toString())
																															 .setPositiveButton(
																																	 "确认",
																																	 null)
																															 .show();
																												 }

																												 Log.d(LOG_TAG,
																														 "ret is "
																																 + result);
																											 }
																										 });
																					 }
																				 });
																		 break;

																	 case 6:
																		 showParameterDialog("serviceId",
																				 new ParameterCallback() {
																					 @Override
																					 public void callback(
																							 final String serviceId) {
																						 Log.i("TAG", "serviceId = "
																								 + serviceId);
																						 showProgressBar("数据加载中...");
																						 new Thread() {
																							 @Override
																							 public void run() {
																								 Result t = CPManagerInterface
																										 .cancelCPMonth(
																												 CMMusicDemo.this,
																												 serviceId);
																								 mUIHandler
																										 .obtainMessage(
																												 0,
																												 t)
																										 .sendToTarget();
																							 }
																						 }.start();
																					 }
																				 });
																		 break;
																 }
															 }
														 }).create().show();
			}
		});

		Button digitalAlbumButton = (Button) this
				.findViewById(R.id.digitalAlbum);
		digitalAlbumButton.setOnClickListener(new OnClickListener() {
			String[] strs = new String[] { "数字专辑订购", "数字专辑赠送", "", "",
					"获取数字专辑信息", "数字专辑订购（无界面）", "数字专辑赠送（无界面）", "", "" };

			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(CMMusicDemo.this).setTitle("数字专辑类")
														 .setItems(strs, new DialogInterface.OnClickListener() {
															 public void onClick(DialogInterface dialog,
																				 int which) {
																 switch (which) {
																	 case 0:
																		 showParameterDialog("albumId",
																				 new ParameterCallback() {
																					 @Override
																					 public void callback(
																							 final String parameter) {
																						 Log.i("TAG",
																								 "parameters = "
																										 + parameter);
																						 DigitalAlbumManagerInterface
																								 .orderDigitalAlbum(
																										 CMMusicDemo.this,
																										 parameter,
																										 new CMMusicCallback<OrderResult>() {
																											 @Override
																											 public void operationResult(
																													 OrderResult result) {
																												 if (null != result) {
																													 new AlertDialog.Builder(
																															 CMMusicDemo.this)
																															 .setTitle(
																																	 "orderDigitalAlbum")
																															 .setMessage(
																																	 result.toString())
																															 .setPositiveButton(
																																	 "确认",
																																	 null)
																															 .show();
																												 }
																											 }
																										 });
																					 }
																				 });
																		 break;
																	 case 1:
																		 showParameterDialog("albumId",
																				 new ParameterCallback() {
																					 @Override
																					 public void callback(
																							 final String parameter) {
																						 Log.i("TAG",
																								 "parameters = "
																										 + parameter);
																						 DigitalAlbumManagerInterface
																								 .giveDigitalAlbum(
																										 CMMusicDemo.this,
																										 parameter,
																										 new CMMusicCallback<OrderResult>() {
																											 @Override
																											 public void operationResult(
																													 OrderResult result) {
																												 if (null != result) {
																													 new AlertDialog.Builder(
																															 CMMusicDemo.this)
																															 .setTitle(
																																	 "giveDigitalAlbum")
																															 .setMessage(
																																	 result.toString())
																															 .setPositiveButton(
																																	 "确认",
																																	 null)
																															 .show();
																												 }
																											 }
																										 });
																					 }
																				 });
																		 break;
																	 case 2:
																		 // showParameterDialog("albumId",
																		 // new ParameterCallback() {
																		 // @Override
																		 // public void callback(
																		 // final String parameter) {
																		 // Log.i("TAG",
																		 // "parameters = "
																		 // + parameter);
																		 // DigitalAlbumManagerInterface
																		 // .orderDigitalAlbumByOpenMember(
																		 // CMMusicDemo.this,
																		 // parameter,
																		 // new CMMusicCallback<OrderResult>() {
																		 // @Override
																		 // public void operationResult(
																		 // OrderResult result) {
																		 // if (null != result) {
																		 // new AlertDialog.Builder(
																		 // CMMusicDemo.this)
																		 // .setTitle(
																		 // "orderDigitalAlbumByOpenMember")
																		 // .setMessage(
																		 // result.toString())
																		 // .setPositiveButton(
																		 // "确认",
																		 // null)
																		 // .show();
																		 // }
																		 // }
																		 // });
																		 // }
																		 // });
																		 break;
																	 case 3:
																		 // showParameterDialog("albumId",
																		 // new ParameterCallback() {
																		 // @Override
																		 // public void callback(
																		 // final String parameter) {
																		 // Log.i("TAG",
																		 // "parameters = "
																		 // + parameter);
																		 // DigitalAlbumManagerInterface
																		 // .orderDigitalAlbumByOpenRingBack(
																		 // CMMusicDemo.this,
																		 // parameter,
																		 // new CMMusicCallback<OrderResult>() {
																		 // @Override
																		 // public void operationResult(
																		 // OrderResult result) {
																		 // if (null != result) {
																		 // new AlertDialog.Builder(
																		 // CMMusicDemo.this)
																		 // .setTitle(
																		 // "orderDigitalAlbumByOpenRingBack")
																		 // .setMessage(
																		 // result.toString())
																		 // .setPositiveButton(
																		 // "确认",
																		 // null)
																		 // .show();
																		 // }
																		 // }
																		 // });
																		 // }
																		 // });
																		 break;

																	 case 4:
																		 showParameterDialog("albumId",
																				 new ParameterCallback() {
																					 @Override
																					 public void callback(
																							 final String albumId) {
																						 Log.i("TAG", "albumId = "
																								 + albumId);
																						 showProgressBar("数据加载中...");
																						 new Thread() {
																							 @Override
																							 public void run() {
																								 String t = DigitalAlbumManagerInterface
																										 .getDigitalAlbumInfo(
																												 CMMusicDemo.this,
																												 albumId);

																								 mUIHandler
																										 .obtainMessage(
																												 0,
																												 t)
																										 .sendToTarget();
																							 }
																						 }.start();
																					 }
																				 });
																		 break;

																	 case 5:
																		 showParameterDialog(new String[] {
																						 "albumId", "bizCode", "price",
																						 "memberType", "hold2" },
																				 new ParameterCallbacks() {
																					 @Override
																					 public void callback(
																							 String[] parameters) {
																						 Log.i("TAG", "albumId = "
																								 + parameters[0]);
																						 DigitalAlbumManagerInterface
																								 .bugDigitalAlbum(
																										 CMMusicDemo.this,
																										 parameters[0],
																										 parameters[1],
																										 parameters[2],
																										 parameters[3],
																										 parameters[4],
																										 new CMMusicCallback<OrderResult>() {
																											 @Override
																											 public void operationResult(
																													 OrderResult result) {
																												 mUIHandler
																														 .obtainMessage(
																																 0,
																																 result)
																														 .sendToTarget();

																												 Log.d(LOG_TAG,
																														 "ret is "
																																 + result);
																											 }
																										 });
																					 }
																				 });
																		 break;

																	 case 6:
																		 showParameterDialog(new String[] {
																						 "albumId", "phoneNum", "bizCode",
																						 "price", "memberType", "hold2" },
																				 new ParameterCallbacks() {
																					 @Override
																					 public void callback(
																							 String[] parameters) {
																						 Log.i("TAG", "albumId = "
																								 + parameters[0]);
																						 DigitalAlbumManagerInterface
																								 .giveDigitalAlbum(
																										 CMMusicDemo.this,
																										 parameters[0],
																										 parameters[1],
																										 parameters[2],
																										 parameters[3],
																										 parameters[4],
																										 parameters[5],
																										 new CMMusicCallback<OrderResult>() {
																											 @Override
																											 public void operationResult(
																													 OrderResult result) {
																												 mUIHandler
																														 .obtainMessage(
																																 0,
																																 result)
																														 .sendToTarget();

																												 Log.d(LOG_TAG,
																														 "ret is "
																																 + result);
																											 }
																										 });
																					 }
																				 });
																		 break;
																	 case 7:
																		 // showParameterDialog(
																		 // new String[] { "albumId" },
																		 // new ParameterCallbacks() {
																		 // @Override
																		 // public void callback(
																		 // String[] parameters) {
																		 // Log.i("TAG", "albumId = "
																		 // + parameters[0]);
																		 // DigitalAlbumManagerInterface
																		 // .orderDigitalAlbumByOpenMember(
																		 // CMMusicDemo.this,
																		 // parameters[0],
																		 //
																		 // new CMMusicCallback<OrderResult>() {
																		 // @Override
																		 // public void operationResult(
																		 // OrderResult result) {
																		 // if (null != result) {
																		 // new AlertDialog.Builder(
																		 // CMMusicDemo.this)
																		 // .setTitle(
																		 // "orderDigitalAlbumByOpenMember")
																		 // .setMessage(
																		 // result.toString())
																		 // .setPositiveButton(
																		 // "确认",
																		 // null)
																		 // .show();
																		 // }
																		 //
																		 // Log.d(LOG_TAG,
																		 // "ret is "
																		 // + result);
																		 // }
																		 // });
																		 // }
																		 // });
																		 break;

																	 case 8:
																		 // showParameterDialog(
																		 // new String[] { "albumId" },
																		 // new ParameterCallbacks() {
																		 // @Override
																		 // public void callback(
																		 // String[] parameters) {
																		 // Log.i("TAG", "musicId = "
																		 // + parameters[0]);
																		 // DigitalAlbumManagerInterface
																		 // .orderDigitalAlbumOpenRingBack(
																		 // CMMusicDemo.this,
																		 // parameters[0],
																		 //
																		 // new CMMusicCallback<OrderResult>() {
																		 // @Override
																		 // public void operationResult(
																		 // OrderResult result) {
																		 // if (null != result) {
																		 // new AlertDialog.Builder(
																		 // CMMusicDemo.this)
																		 // .setTitle(
																		 // "orderDigitalAlbumOpenRingBack")
																		 // .setMessage(
																		 // result.toString())
																		 // .setPositiveButton(
																		 // "确认",
																		 // null)
																		 // .show();
																		 // }
																		 //
																		 // Log.d(LOG_TAG,
																		 // "ret is "
																		 // + result);
																		 // }
																		 // });
																		 // }
																		 // });
																		 break;
																 }
															 }
														 }).create().show();
			}
		});

		// 个性化彩铃
		Button ownRingback = (Button) this.findViewById(R.id.ownRingback);
		ownRingback.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RingbackManagerInterface.buyOwnRingback(CMMusicDemo.this,
						new CMMusicCallback<Result>() {
							@Override
							public void operationResult(Result downloadResult) {
								if (null != downloadResult) {
									new AlertDialog.Builder(CMMusicDemo.this)
											.setTitle("buyOwnRingbackByNet")
											.setMessage(
													downloadResult.toString())
											.setPositiveButton("确认", null)
											.show();
								}

								Log.d(LOG_TAG, "vRing Download result is "
										+ downloadResult);
							}
						});
			}
		});

		// 个性化彩铃包月
		Button ownRingMonthBtn = (Button) this
				.findViewById(R.id.own_ring_month);
		ownRingMonthBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RingbackManagerInterface.ownRingMonth(CMMusicDemo.this,
						new CMMusicCallback<Result>() {

							@Override
							public void operationResult(Result result) {

								if (null != result) {
									new AlertDialog.Builder(CMMusicDemo.this)
											.setTitle("OwnRingMonth")
											.setMessage(result.toString())
											.setPositiveButton("确认", null)
											.show();
								}

								Log.i(LOG_TAG, "OwnRingMonth is " + result);
							}
						});
			}
		});

		// 非彩铃用户一键订购个性化彩铃包月
		Button keyOrderOwnRingMonthBtn = (Button) this
				.findViewById(R.id.key_order_own_ring_month);
		keyOrderOwnRingMonthBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RingbackManagerInterface.keyOrderOwnRingMonth(CMMusicDemo.this,
						new CMMusicCallback<Result>() {

							@Override
							public void operationResult(Result result) {

								if (null != result) {
									new AlertDialog.Builder(CMMusicDemo.this)
											.setTitle("keyOrderOwnRingMonth")
											.setMessage(result.toString())
											.setPositiveButton("确认", null)
											.show();
								}

								Log.i(LOG_TAG, "keyOrderOwnRingMonth is "
										+ result);
							}
						});
			}
		});

		// 赠送全曲
		Button giveFullSongByNet = (Button) this
				.findViewById(R.id.giveFullSongByNet);
		giveFullSongByNet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showParameterDialog(new String[] { "receivemdn", "musicId",
								"bizCode", "bizType", "price", "memberType", "hold2" },
						new ParameterCallbacks() {
							@Override
							public void callback(final String[] parameters) {
								Log.i("TAG", "parameters = " + parameters);
								showProgressBar("数据加载中...");

								FullSongManagerInterface.giveFullSong(
										CMMusicDemo.this, parameters[0],
										parameters[1], parameters[2],
										parameters[3], parameters[4],
										parameters[5], parameters[6],
										new CMMusicCallback<OrderResult>() {
											@Override
											public void operationResult(
													OrderResult ret) {
												mUIHandler
														.obtainMessage(0, ret)
														.sendToTarget();
											}
										});

							}
						});
			}
		});

		// 赠送振铃
		Button giveVibrateRingByNet = (Button) this
				.findViewById(R.id.giveVibrateRingByNet);
		giveVibrateRingByNet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showParameterDialog(new String[] { "receivemdn", "musicId",
								"bizCode", "bizType", "price", "memberType", "hold2" },
						new ParameterCallbacks() {
							@Override
							public void callback(final String[] parameters) {
								Log.i("TAG", "parameters = " + parameters);
								showProgressBar("数据加载中...");

								VibrateRingManagerInterface.giveVibrateRing(
										CMMusicDemo.this, parameters[0],
										parameters[1], parameters[2],
										parameters[3], parameters[4],
										parameters[5], parameters[6],
										new CMMusicCallback<OrderResult>() {
											@Override
											public void operationResult(
													OrderResult ret) {
												mUIHandler
														.obtainMessage(0, ret)
														.sendToTarget();
											}
										});

							}
						});
			}
		});

		// 短信验证码登录
		Button login = (Button) this.findViewById(R.id.login);
		login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i("LOG_KeyOrder2", "-------1");
				UserManagerInterface.smsAuthLogin(CMMusicDemo.this,
						new CMMusicCallback<Result>() {
							@Override
							public void operationResult(Result result) {
								if (null != result) {
									new AlertDialog.Builder(CMMusicDemo.this)
											.setTitle("login")
											.setMessage(result.toString())
											.setPositiveButton("确认", null)
											.show();
								}

								Log.d(LOG_TAG, "ret is " + result);
							}
						});
			}
		});

		// 索要彩铃
		Button crbtAskfor = (Button) this.findViewById(R.id.crbtAskfor);
		crbtAskfor.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showParameterDialog(new String[] { "receivemdn", "musicId",
						"validCode" }, new ParameterCallbacks() {
					@Override
					public void callback(final String[] parameters) {
						Log.i("TAG", "parameters = " + parameters);
						showProgressBar("数据加载中...");
						new Thread() {
							@Override
							public void run() {
								Result t = RingbackManagerInterface
										.getCrbtAskFor(CMMusicDemo.this,
												parameters[0], parameters[1],
												parameters[2]);
								mUIHandler.obtainMessage(0, t).sendToTarget();
							}
						}.start();
					}
				});
			}
		});

		// 短信登录验证
		Button smsAuthLoginValidate = (Button) this
				.findViewById(R.id.smsAuthLoginValidate);
		smsAuthLoginValidate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showProgressBar("数据加载中...");
				new Thread() {
					@Override
					public void run() {
						super.run();

						SmsLoginInfoRsp result = UserManagerInterface
								.smsAuthLoginValidate(CMMusicDemo.this);

						mUIHandler.obtainMessage(0, result).sendToTarget();
					}
				}.start();
			}
		});

		// 根据手机号查询是否开通彩铃
		Button crbtOpenCheck = (Button) this.findViewById(R.id.crbtOpenCheck);
		crbtOpenCheck.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showParameterDialog("手机号", new ParameterCallback() {
					@Override
					public void callback(final String phoneNum) {
						Log.i("TAG", "phoneNum = " + phoneNum);
						showProgressBar("数据加载中...");
						new Thread() {
							@Override
							public void run() {
								super.run();

								CrbtOpenCheckRsp result = RingbackManagerInterface
										.crbtOpenCheck(CMMusicDemo.this,
												phoneNum);

								mUIHandler.obtainMessage(0, result)
										  .sendToTarget();

							}
						}.start();
					}
				});
			}
		});

		Button btnDel = (Button) this.findViewById(R.id.deletesong);
		btnDel.setOnClickListener(this);

		Button btnFull = (Button) this.findViewById(R.id.fullsong);
		btnFull.setOnClickListener(this);

		// 赠送彩铃
		Button giveRingback = (Button) this.findViewById(R.id.giveRingback);
		giveRingback.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showParameterDialog("musicId", new ParameterCallback() {
					@Override
					public void callback(final String musicId) {
						Log.i("TAG", "musicId = " + musicId);
						RingbackManagerInterface.giveRingBack(CMMusicDemo.this,
								musicId, new CMMusicCallback<OrderResult>() {
									@Override
									public void operationResult(
											OrderResult result) {
										if (null != result) {
											new AlertDialog.Builder(
													CMMusicDemo.this)
													.setTitle("giveRingBack")
													.setMessage(
															result.toString())
													.setPositiveButton("确认",
															null).show();
										}

										Log.d(LOG_TAG, "ret is " + result);
									}
								});
					}
				});
			}
		});

		// 开通彩铃
		Button openRingback = (Button) this.findViewById(R.id.openRingback);
		openRingback.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RingbackManagerInterface.openRingback(CMMusicDemo.this,
						new CMMusicCallback<Result>() {
							@Override
							public void operationResult(Result result) {
								if (null != result) {
									new AlertDialog.Builder(CMMusicDemo.this)
											.setTitle("openRingback")
											.setMessage(result.toString())
											.setPositiveButton("确认", null)
											.show();
								}

								Log.d(LOG_TAG, "ret is " + result);
							}
						});
			}
		});

		// 开通彩铃（无界面）
		// Button openRingbackByC = (Button) this
		// .findViewById(R.id.openRingbackByC);
		// openRingbackByC.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// new Thread() {
		// @Override
		// public void run() {
		// Result t = RingbackManagerInterface
		// .openRingback(CMMusicDemo.this);
		//
		// mUIHandler.obtainMessage(0, t).sendToTarget();
		// }
		// }.start();
		// }
		// });

		// 歌曲查詢类
		Button musicQuery = (Button) this.findViewById(R.id.musicQuery);
		musicQuery.setOnClickListener(new OnClickListener() {
			String[] strs = new String[] { "获取榜单信息", "获取榜单音乐信息", "获取专辑信息",
					"获取专辑音乐信息", "获取歌手音乐信息", "获取标签信息", "获取标签音乐信息", "关键字搜索歌曲",
					"歌曲ID查询歌曲信息", "歌曲ID查询专辑信息", "查询歌手信息" };

			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(CMMusicDemo.this).setTitle("歌曲查询类")
														 .setItems(strs, new DialogInterface.OnClickListener() {
															 public void onClick(DialogInterface dialog,
																				 int which) {
																 switch (which) {
																	 case 0:
																		 showProgressBar("数据加载中...");
																		 new Thread() {
																			 @Override
																			 public void run() {
																				 super.run();

																				 ChartListRsp c = MusicQueryInterface
																						 .getChartInfo(
																								 CMMusicDemo.this,
																								 1, 10);

																				 mUIHandler.obtainMessage(0, c)
																						   .sendToTarget();

																			 }
																		 }.start();

																		 break;
																	 case 1:
																		 showParameterDialog("chartCode",
																				 new ParameterCallback() {
																					 @Override
																					 public void callback(
																							 final String chartCode) {
																						 Log.i("TAG", "chartCode = "
																								 + chartCode);
																						 showProgressBar("数据加载中...");
																						 new Thread() {
																							 @Override
																							 public void run() {
																								 MusicListRsp m = MusicQueryInterface
																										 .getMusicsByChartId(
																												 CMMusicDemo.this,
																												 chartCode,
																												 1,
																												 5);

																								 mUIHandler
																										 .obtainMessage(
																												 0,
																												 m)
																										 .sendToTarget();
																							 }
																						 }.start();
																					 }
																				 });

																		 break;
																	 case 2:
																		 showProgressBar("数据加载中...");
																		 new Thread() {
																			 @Override
																			 public void run() {
																				 AlbumListRsp a = MusicQueryInterface
																						 .getAlbumsBySingerId(
																								 CMMusicDemo.this,
																								 "235", 1, 5);

																				 mUIHandler.obtainMessage(0, a)
																						   .sendToTarget();
																			 }
																		 }.start();
																		 break;
																	 case 3:
																		 showParameterDialog("专辑ID",
																				 new ParameterCallback() {
																					 @Override
																					 public void callback(
																							 final String albumId) {
																						 Log.w("TAG", "albumId = "
																								 + albumId);
																						 showProgressBar("数据加载中...");
																						 new Thread() {
																							 @Override
																							 public void run() {
																								 MusicListRsp m = MusicQueryInterface
																										 .getMusicsByAlbumId(
																												 CMMusicDemo.this,
																												 albumId,
																												 1,
																												 5);

																								 mUIHandler
																										 .obtainMessage(
																												 0,
																												 m)
																										 .sendToTarget();
																							 }
																						 }.start();
																					 }
																				 });
																		 break;
																	 case 4:
																		 showProgressBar("数据加载中...");
																		 new Thread() {
																			 @Override
																			 public void run() {
																				 MusicListRsp m = MusicQueryInterface
																						 .getMusicsBySingerId(
																								 CMMusicDemo.this,
																								 "235", 1, 5);

																				 mUIHandler.obtainMessage(0, m)
																						   .sendToTarget();

																			 }
																		 }.start();
																		 break;
																	 case 5:
																		 showProgressBar("数据加载中...");
																		 new Thread() {
																			 @Override
																			 public void run() {
																				 TagListRsp t = MusicQueryInterface
																						 .getTags(CMMusicDemo.this,
																								 "10", 1, 5);

																				 mUIHandler.obtainMessage(0, t)
																						   .sendToTarget();
																			 }
																		 }.start();
																		 break;
																	 case 6:
																		 showProgressBar("数据加载中...");
																		 new Thread() {
																			 @Override
																			 public void run() {
																				 MusicListRsp t = MusicQueryInterface
																						 .getMusicsByTagId(
																								 CMMusicDemo.this,
																								 "100", 1, 5);

																				 mUIHandler.obtainMessage(0, t)
																						   .sendToTarget();

																			 }
																		 }.start();
																		 break;
																	 case 7:
																		 showParameterDialog(new String[] { "关键字",
																						 "关键字类型" },
																				 new ParameterCallbacks() {
																					 @Override
																					 public void callback(
																							 final String[] parameters) {
																						 Log.i("TAG",
																								 "parameters = "
																										 + parameters);
																						 showProgressBar("数据加载中...");
																						 new Thread() {
																							 @Override
																							 public void run() {
																								 MusicListRsp t = null;
																								 t = MusicQueryInterface
																										 .getMusicsByKey(
																												 CMMusicDemo.this,
																												 URLEncoder
																														 .encode(parameters[0]),
																												 parameters[1],
																												 1,
																												 5);
																								 mUIHandler
																										 .obtainMessage(
																												 0,
																												 t)
																										 .sendToTarget();
																							 }
																						 }.start();
																					 }
																				 });
																		 break;
																	 case 8:
																		 showParameterDialog("歌曲ID",
																				 new ParameterCallback() {
																					 @Override
																					 public void callback(
																							 final String musicId) {
																						 Log.i("TAG", "musicId = "
																								 + musicId);
																						 showProgressBar("数据加载中...");
																						 new Thread() {
																							 @Override
																							 public void run() {
																								 super.run();

																								 MusicInfoResult result = MusicQueryInterface
																										 .getMusicInfoByMusicId(
																												 CMMusicDemo.this,
																												 musicId);

																								 mUIHandler
																										 .obtainMessage(
																												 0,
																												 result)
																										 .sendToTarget();

																							 }
																						 }.start();
																					 }
																				 });

																		 break;

																	 case 9:
																		 showParameterDialog("歌曲ID",
																				 new ParameterCallback() {
																					 @Override
																					 public void callback(
																							 final String musicId) {
																						 Log.i("TAG", "musicId = "
																								 + musicId);
																						 showProgressBar("数据加载中...");
																						 new Thread() {
																							 @Override
																							 public void run() {
																								 super.run();

																								 AlbumListRsp result = MusicQueryInterface
																										 .getAlbumsByMusicId(
																												 CMMusicDemo.this,
																												 musicId,
																												 1,
																												 5);

																								 mUIHandler
																										 .obtainMessage(
																												 0,
																												 result)
																										 .sendToTarget();

																							 }
																						 }.start();
																					 }
																				 });

																		 break;

																	 case 10:
																		 showParameterDialog("歌手ID",
																				 new ParameterCallback() {
																					 @Override
																					 public void callback(
																							 final String singerId) {
																						 Log.i("TAG", "singerId = "
																								 + singerId);
																						 showProgressBar("数据加载中...");
																						 new Thread() {
																							 @Override
																							 public void run() {
																								 super.run();

																								 SingerInfoRsp result = MusicQueryInterface
																										 .getSingerInfo(
																												 CMMusicDemo.this,
																												 singerId);

																								 mUIHandler
																										 .obtainMessage(
																												 0,
																												 result)
																										 .sendToTarget();

																							 }
																						 }.start();
																					 }
																				 });

																		 break;
																 }

																 // hideProgressBar();
															 }
														 }).create().show();
			}
		});

		// 彩铃订购
		Button buyRingback = (Button) this.findViewById(R.id.buyRingback);
		buyRingback.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showParameterDialog("musicId", new ParameterCallback() {
					@Override
					public void callback(final String musicId) {
						Log.i("TAG", "musicId = " + musicId);
						RingbackManagerInterface.buyRingBack(CMMusicDemo.this,
								musicId, new CMMusicCallback<OrderResult>() {
									@Override
									public void operationResult(OrderResult ret) {
										if (null != ret) {
											new AlertDialog.Builder(
													CMMusicDemo.this)
													.setTitle("buyRingBack")
													.setMessage(ret.toString())
													.setPositiveButton("确认",
															null).show();
										}
									}
								});
					}
				});
			}
		});

		// 振铃下载
		Button vRing = (Button) this.findViewById(R.id.vRing);
		vRing.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showParameterDialog("musicId", new ParameterCallback() {
					@Override
					public void callback(final String musicId) {
						Log.i("TAG", "musicId = " + musicId);
						VibrateRingManagerInterface
								.queryVibrateRingDownloadUrl(CMMusicDemo.this,
										musicId,
										new CMMusicCallback<OrderResult>() {
											@Override
											public void operationResult(
													final OrderResult downloadResult) {
												showDownloadAlertDialog(
														downloadResult,
														"queryVibrateRingDownloadUrl");

												Log.d(LOG_TAG,
														"vRing Download result is "
																+ downloadResult);
											}
										});
					}
				});

			}
		});

		// 开通会员
		Button openMem = (Button) this.findViewById(R.id.openMem);
		openMem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				UserManagerInterface.openMember(CMMusicDemo.this,
						new CMMusicCallback<Result>() {
							@Override
							public void operationResult(Result ret) {
								if (null != ret) {
									new AlertDialog.Builder(CMMusicDemo.this)
											.setTitle("openMember")
											.setMessage(ret.toString())
											.setPositiveButton("确认", null)
											.show();
								}
							}
						});
			}
		});

		// 获取在线听歌地址
		Button onlineLse = (Button) this.findViewById(R.id.onlineLse);
		onlineLse.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showParameterDialog(new String[] { "musicId", "codeRate" },
						new ParameterCallbacks() {
							@Override
							public void callback(final String[] parameters) {
								Log.i("TAG", "parameters = " + parameters);
								showProgressBar("数据加载中...");
								new Thread() {
									@Override
									public void run() {
										StreamRsp s = OnlineListenerMusicInterface
												.getStream(CMMusicDemo.this,
														parameters[0],
														parameters[1]);
										mUIHandler.obtainMessage(0, s)
												  .sendToTarget();
									}
								}.start();
							}
						});
			}
		});

		// 获取彩铃试听地址
		Button crbtPrelisten = (Button) this.findViewById(R.id.crbtPrelisten);
		crbtPrelisten.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showParameterDialog("musicId", new ParameterCallback() {
					@Override
					public void callback(final String musicId) {
						Log.i("TAG", "musicId = " + musicId);
						showProgressBar("数据加载中...");
						new Thread() {
							@Override
							public void run() {
								super.run();

								CrbtPrelistenRsp c = RingbackManagerInterface
										.getCrbtPrelisten(CMMusicDemo.this,
												musicId);

								mUIHandler.obtainMessage(0, c).sendToTarget();

							}
						}.start();
					}
				});
			}
		});

		// 查询个人铃音库
		Button crbtBox = (Button) this.findViewById(R.id.crbtBox);
		crbtBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showProgressBar("数据加载中...");
				new Thread() {
					@Override
					public void run() {
						CrbtListRsp c = RingbackManagerInterface
								.getCrbtBox(CMMusicDemo.this);

						mUIHandler.obtainMessage(0, c).sendToTarget();

					}
				}.start();
				// hideProgressBar();
			}
		});

		// 设置默认铃音
		Button setDefaultCrbt = (Button) this.findViewById(R.id.setDefaultCrbt);
		setDefaultCrbt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showParameterDialog("crbtId", new ParameterCallback() {
					@Override
					public void callback(final String crbtId) {
						Log.i("TAG", "crbtId = " + crbtId);
						showProgressBar("数据加载中...");
						new Thread() {
							@Override
							public void run() {
								Result c = RingbackManagerInterface
										.setDefaultCrbt(CMMusicDemo.this,
												crbtId);

								mUIHandler.obtainMessage(0, c).sendToTarget();
							}
						}.start();
					}
				});
			}
		});

		// 手机号查询默认铃音
		Button getDefaultCrbt = (Button) this.findViewById(R.id.getDefaultCrbt);
		getDefaultCrbt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showParameterDialog("msisdn", new ParameterCallback() {
					@Override
					public void callback(final String msisdn) {
						Log.i("TAG", "msisdn = " + msisdn);
						showProgressBar("数据加载中...");
						new Thread() {
							@Override
							public void run() {
								Result c = RingbackManagerInterface
										.getDefaultCrbt(CMMusicDemo.this,
												msisdn);

								mUIHandler.obtainMessage(0, c).sendToTarget();
							}
						}.start();
					}
				});
			}
		});

		// 振铃试听地址
		Button ringPrelisten = (Button) this.findViewById(R.id.ringPrelisten);
		ringPrelisten.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showParameterDialog("musicId", new ParameterCallback() {
					@Override
					public void callback(final String musicId) {
						Log.i("TAG", "musicId = " + musicId);
						showProgressBar("数据加载中...");
						new Thread() {
							@Override
							public void run() {
								DownloadResult c = VibrateRingManagerInterface
										.getRingPrelisten(CMMusicDemo.this,
												musicId);

								mUIHandler.obtainMessage(0, c).sendToTarget();
							}
						}.start();
					}
				});
			}
		});

		// 专属按次
		Button exclusive_net = (Button) this.findViewById(R.id.exclusive_net);
		exclusive_net.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showParameterDialog(new String[] { "serviceId", "definedseq" },
						new ParameterCallbacks() {
							@Override
							public void callback(final String[] parameters) {
								Log.i("TAG", "serviceId = " + parameters[0]);
								ExclusiveManagerInterface.exclusiveOnce(
										CMMusicDemo.this, parameters[0],
										parameters[1],
										new CMMusicCallback<OrderResult>() {
											@Override
											public void operationResult(
													OrderResult result) {
												mUIHandler.obtainMessage(0,
														result).sendToTarget();

												Log.d(LOG_TAG, "ret is "
														+ result);
											}
										});
							}
						});
			}
		});

		// 关联歌曲推荐
		Button associateSongRecommendBtn = (Button) this
				.findViewById(R.id.sr_associate_song_recommend);
		associateSongRecommendBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showParameterDialog("contentId", new ParameterCallback() {
					@Override
					public void callback(final String contentId) {
						Log.i("TAG", "contentId = " + contentId);

						SongRecommendManagerInterface.associateSongRecommend(
								CMMusicDemo.this, contentId,
								new CMMusicCallback<SongRecommendResult>() {

									@Override
									public void operationResult(
											SongRecommendResult ret) {
										if (null != ret) {
											new AlertDialog.Builder(
													CMMusicDemo.this)
													.setTitle("contentIds")
													.setMessage(ret.toString())
													.setPositiveButton("确认",
															null).show();
										}
									}
								});
					}
				});
			}

		});

		// 关联歌手歌曲推荐
		Button associateSingerSongRecommendBtn = (Button) this
				.findViewById(R.id.sr_associate_singer_song_recommend);
		associateSingerSongRecommendBtn
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						showParameterDialog("contentId",
								new ParameterCallback() {
									@Override
									public void callback(final String contentId) {
										Log.i("TAG", "contentId = " + contentId);
										SongRecommendManagerInterface
												.associateSingerSongRecommend(
														CMMusicDemo.this,
														contentId,
														new CMMusicCallback<SongRecommendResult>() {

															@Override
															public void operationResult(
																	SongRecommendResult ret) {
																if (null != ret) {
																	new AlertDialog.Builder(
																			CMMusicDemo.this)
																			.setTitle(
																					"contentIds")
																			.setMessage(
																					ret.toString())
																			.setPositiveButton(
																					"确认",
																					null)
																			.show();
																}
															}
														});
									}
								});
					}

				});

		// 偏好歌曲推荐
		Button likeSongRecommendBtn = (Button) this
				.findViewById(R.id.sr_like_song_recommend);
		likeSongRecommendBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SongRecommendManagerInterface.likeSongRecommend(
						CMMusicDemo.this,
						new CMMusicCallback<SongRecommendResult>() {

							@Override
							public void operationResult(SongRecommendResult ret) {
								if (null != ret) {
									new AlertDialog.Builder(CMMusicDemo.this)
											.setTitle("contentIds")
											.setMessage(ret.toString())
											.setPositiveButton("确认", null)
											.show();
								}
							}
						});
			}
		});

		// 基于用户的协同过滤推荐
		Button dealSongRecommendBtn = (Button) this
				.findViewById(R.id.sr_deal_song_recommend);
		dealSongRecommendBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showProgressBar("请稍候...");
				SongRecommendManagerInterface.likeSongRecommend(
						CMMusicDemo.this,
						new CMMusicCallback<SongRecommendResult>() {

							@Override
							public void operationResult(SongRecommendResult ret) {
								if (null != ret) {
									new AlertDialog.Builder(CMMusicDemo.this)
											.setTitle("contentIds")
											.setMessage(ret.toString())
											.setPositiveButton("确认", null)
											.show();
								}
							}
						});
			}
		});

		// 无界面接口
		Button noUIButton = (Button) this.findViewById(R.id.noUI);
		noUIButton.setOnClickListener(new OnClickListener() {
			String[] strs = new String[] { "彩铃订购（无界面）", "获取彩铃业务策略",
					"赠送彩铃（无界面）", "专属按次订购（无界面）", "获取专属业务信息", "", "", "查询全曲下载策略",
					"全曲下载（无界面）", "查询振铃下载策略", "振铃下载（无界面）", "CP专属包月订购（无界面）",
					"CP专属振铃按次下载（无界面）", "CP专属全曲按次下载（无界面）", "个性化彩铃包月订购（无界面）",
					"非彩铃用户一键订购个性化彩铃包月（无界面）", "个性化彩铃订购（无界面）", "个性彩铃包月关系查询",
					"个性化彩铃文字合成铃音", "个性化彩铃铃音文件上传" };

			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(CMMusicDemo.this).setTitle("无界面接口")
														 .setItems(strs, new DialogInterface.OnClickListener() {
															 public void onClick(DialogInterface dialog,
																				 int which) {
																 switch (which) {
																	 case 0:
																		 showParameterDialog(new String[] {
																						 "musicId", "bizCode", "bizType",
																						 "price", "memberType", "hold2" },
																				 new ParameterCallbacks() {
																					 @Override
																					 public void callback(
																							 final String[] parameters) {
																						 Log.i("TAG", "musicId = "
																								 + parameters[0]);
																						 RingbackManagerInterface
																								 .buyRingback(
																										 CMMusicDemo.this,
																										 parameters[0],
																										 parameters[1],
																										 parameters[2],
																										 parameters[3],
																										 parameters[4],
																										 parameters[5],
																										 new CMMusicCallback<OrderResult>() {
																											 @Override
																											 public void operationResult(
																													 OrderResult result) {
																												 mUIHandler
																														 .obtainMessage(
																																 0,
																																 result)
																														 .sendToTarget();
																											 }
																										 });
																					 }
																				 });

																		 break;
																	 case 1:
																		 showParameterDialog("crbtId",
																				 new ParameterCallback() {
																					 @Override
																					 public void callback(
																							 final String crbtId) {
																						 Log.i("TAG", "crbtId = "
																								 + crbtId);
																						 showProgressBar("数据加载中...");
																						 new Thread() {
																							 @Override
																							 public void run() {
																								 String t = RingbackManagerInterface
																										 .getRingbackPolicy(
																												 CMMusicDemo.this,
																												 crbtId);

																								 mUIHandler
																										 .obtainMessage(
																												 0,
																												 t)
																										 .sendToTarget();
																							 }
																						 }.start();
																					 }
																				 });
																		 break;

																	 case 2:
																		 showParameterDialog(new String[] {
																						 "phoneNum", "musicId", "bizCode",
																						 "bizType", "price", "memberType",
																						 "hold2" },
																				 new ParameterCallbacks() {
																					 @Override
																					 public void callback(
																							 String[] parameters) {
																						 Log.i("TAG", "musicId = "
																								 + parameters[0]);
																						 RingbackManagerInterface
																								 .giveRingbackByCustom(
																										 CMMusicDemo.this,
																										 parameters[0],
																										 parameters[1],
																										 parameters[2],
																										 parameters[3],
																										 parameters[4],
																										 parameters[5],
																										 parameters[6],
																										 new CMMusicCallback<OrderResult>() {
																											 @Override
																											 public void operationResult(
																													 OrderResult result) {
																												 mUIHandler
																														 .obtainMessage(
																																 0,
																																 result)
																														 .sendToTarget();

																												 Log.d(LOG_TAG,
																														 "ret is "
																																 + result);
																											 }
																										 });
																					 }
																				 });

																		 break;
																	 case 3:
																		 showParameterDialog(
																				 new String[] { "copyId",
																						 "serviceId", "definedseq",
																						 "price", "memberType" },
																				 new ParameterCallbacks() {
																					 @Override
																					 public void callback(
																							 final String[] parameters) {
																						 Log.i("TAG", "serviceId = "
																								 + parameters[0]);
																						 ExclusiveManagerInterface
																								 .exclusiveTimesOrder(
																										 CMMusicDemo.this,
																										 parameters[0],
																										 parameters[1],
																										 parameters[2],
																										 parameters[3],
																										 parameters[4],
																										 new CMMusicCallback<OrderResult>() {
																											 @Override
																											 public void operationResult(
																													 OrderResult result) {
																												 mUIHandler
																														 .obtainMessage(
																																 0,
																																 result)
																														 .sendToTarget();

																												 Log.d(LOG_TAG,
																														 "ret is "
																																 + result);
																											 }
																										 });
																					 }
																				 });
																		 break;
																	 case 4:
																		 showParameterDialog("copyId",
																				 new ParameterCallback() {
																					 @Override
																					 public void callback(
																							 final String copyId) {
																						 Log.i("TAG", "copyId = "
																								 + copyId);
																						 showProgressBar("数据加载中...");
																						 new Thread() {
																							 @Override
																							 public void run() {
																								 String t = ExclusiveManagerInterface
																										 .getServiceEx(
																												 CMMusicDemo.this,
																												 copyId);

																								 mUIHandler
																										 .obtainMessage(
																												 0,
																												 t)
																										 .sendToTarget();
																							 }
																						 }.start();
																					 }
																				 });
																		 break;
																	 case 5:
																		 // showParameterDialog(new String[] {
																		 // "musicId", "bizCode",
																		 // "saleBizCode", "bizType" },
																		 // new ParameterCallbacks() {
																		 // @Override
																		 // public void callback(
																		 // final String[] parameters) {
																		 // Log.i("TAG", "musicId = "
																		 // + parameters[0]);
																		 // RingbackManagerInterface
																		 // .buyRingbackByOpenRingBack(
																		 // CMMusicDemo.this,
																		 // parameters[0],
																		 // parameters[1],
																		 // parameters[2],
																		 // parameters[3],
																		 // new CMMusicCallback<Result>() {
																		 // @Override
																		 // public void operationResult(
																		 // Result ret) {
																		 // if (null != ret) {
																		 // new AlertDialog.Builder(
																		 // CMMusicDemo.this)
																		 // .setTitle(
																		 // "buyRingbackByOpenRingBack")
																		 // .setMessage(
																		 // ret.toString())
																		 // .setPositiveButton(
																		 // "确认",
																		 // null)
																		 // .show();
																		 // }
																		 // }
																		 // });
																		 // }
																		 // });

																		 break;
																	 case 6:
																		 // showParameterDialog(new String[] {
																		 // "musicId", "bizCode",
																		 // "saleBizCode", "bizType" },
																		 // new ParameterCallbacks() {
																		 // @Override
																		 // public void callback(
																		 // final String[] parameters) {
																		 // Log.i("TAG", "musicId = "
																		 // + parameters[0]);
																		 // RingbackManagerInterface
																		 // .buyRingbackByOpenMember(
																		 // CMMusicDemo.this,
																		 // parameters[0],
																		 // parameters[1],
																		 // parameters[2],
																		 // parameters[3],
																		 // new CMMusicCallback<Result>() {
																		 // @Override
																		 // public void operationResult(
																		 // Result ret) {
																		 // if (null != ret) {
																		 // new AlertDialog.Builder(
																		 // CMMusicDemo.this)
																		 // .setTitle(
																		 // "buyRingbackByOpenMember")
																		 // .setMessage(
																		 // ret.toString())
																		 // .setPositiveButton(
																		 // "确认",
																		 // null)
																		 // .show();
																		 // }
																		 // }
																		 // });
																		 // }
																		 // });

																		 break;

																	 case 7:
																		 showParameterDialog("musicId",
																				 new ParameterCallback() {
																					 @Override
																					 public void callback(
																							 final String musicId) {
																						 Log.i("TAG", "musicId = "
																								 + musicId);
																						 showProgressBar("数据加载中...");
																						 new Thread() {
																							 @Override
																							 public void run() {
																								 String t = FullSongManagerInterface
																										 .getFullSongPolicy(
																												 CMMusicDemo.this,
																												 musicId);

																								 mUIHandler
																										 .obtainMessage(
																												 0,
																												 t)
																										 .sendToTarget();
																							 }
																						 }.start();
																					 }
																				 });
																		 break;

																	 case 8:
																		 showParameterDialog(new String[] {
																						 "musicId", "bizCode", "bizType",
																						 "codeRate", "price", "memberType",
																						 "hold2" },
																				 new ParameterCallbacks() {
																					 @Override
																					 public void callback(
																							 String[] parameters) {
																						 Log.i("TAG", "musicId = "
																								 + parameters[0]);
																						 FullSongManagerInterface
																								 .getFullSongDownloadUrl(
																										 CMMusicDemo.this,
																										 parameters[0],
																										 parameters[1],
																										 parameters[2],
																										 parameters[3],
																										 parameters[4],
																										 parameters[5],
																										 parameters[6],
																										 new CMMusicCallback<OrderResult>() {
																											 @Override
																											 public void operationResult(
																													 OrderResult result) {
																												 mUIHandler
																														 .obtainMessage(
																																 1,
																																 result)
																														 .sendToTarget();

																												 Log.d(LOG_TAG,
																														 "ret is "
																																 + result);
																											 }
																										 });
																					 }
																				 });

																		 break;
																	 case 9:
																		 showParameterDialog("musicId",
																				 new ParameterCallback() {
																					 @Override
																					 public void callback(
																							 final String musicId) {
																						 Log.i("TAG", "musicId = "
																								 + musicId);
																						 showProgressBar("数据加载中...");
																						 new Thread() {
																							 @Override
																							 public void run() {
																								 String t = VibrateRingManagerInterface
																										 .getVibrateRingPolicy(
																												 CMMusicDemo.this,
																												 musicId);

																								 mUIHandler
																										 .obtainMessage(
																												 0,
																												 t)
																										 .sendToTarget();
																							 }
																						 }.start();
																					 }
																				 });
																		 break;

																	 case 10:
																		 showParameterDialog(new String[] {
																						 "musicId", "bizCode", "bizType",
																						 "codeRate", "price", "memberType",
																						 "hold2" },
																				 new ParameterCallbacks() {
																					 @Override
																					 public void callback(
																							 String[] parameters) {
																						 Log.i("TAG", "musicId = "
																								 + parameters[0]);
																						 VibrateRingManagerInterface
																								 .getVibrateRingDownloadUrl(
																										 CMMusicDemo.this,
																										 parameters[0],
																										 parameters[1],
																										 parameters[2],
																										 parameters[3],
																										 parameters[4],
																										 parameters[5],
																										 parameters[6],
																										 new CMMusicCallback<OrderResult>() {
																											 @Override
																											 public void operationResult(
																													 OrderResult result) {
																												 mUIHandler
																														 .obtainMessage(
																																 1,
																																 result)
																														 .sendToTarget();

																												 Log.d(LOG_TAG,
																														 "ret is "
																																 + result);
																											 }
																										 });
																					 }
																				 });

																		 break;

																	 case 11:
																		 showParameterDialog(new String[] {
																						 "serviceId", "definedseq" },
																				 new ParameterCallbacks() {
																					 @Override
																					 public void callback(
																							 final String[] parameters) {
																						 Log.i("TAG", "serviceId = "
																								 + parameters[0]);
																						 CPManagerInterface
																								 .openCpMonth(
																										 CMMusicDemo.this,
																										 parameters[0],
																										 parameters[1],
																										 new CMMusicCallback<OrderResult>() {
																											 @Override
																											 public void operationResult(
																													 OrderResult result) {
																												 mUIHandler
																														 .obtainMessage(
																																 0,
																																 result)
																														 .sendToTarget();
																											 }
																										 });
																					 }
																				 });

																		 break;

																	 case 12:
																		 showParameterDialog(
																				 new String[] { "musicId",
																						 "codeRate", "memberType" },
																				 new ParameterCallbacks() {
																					 @Override
																					 public void callback(
																							 final String[] parameters) {
																						 Log.i("TAG", "musicId = "
																								 + parameters[0]);
																						 CPManagerInterface
																								 .getCPVibrateRingTimeDownloadUrl(
																										 CMMusicDemo.this,
																										 parameters[0],
																										 parameters[1],
																										 parameters[2],
																										 new CMMusicCallback<OrderResult>() {
																											 @Override
																											 public void operationResult(
																													 OrderResult result) {
																												 mUIHandler
																														 .obtainMessage(
																																 1,
																																 result)
																														 .sendToTarget();
																											 }
																										 });
																					 }
																				 });

																		 break;

																	 case 13:
																		 showParameterDialog(
																				 new String[] { "musicId",
																						 "codeRate", "memberType" },
																				 new ParameterCallbacks() {
																					 @Override
																					 public void callback(
																							 final String[] parameters) {
																						 Log.i("TAG", "musicId = "
																								 + parameters[0]);
																						 CPManagerInterface
																								 .getCpFullSongTimeDownloadUrl(
																										 CMMusicDemo.this,
																										 parameters[0],
																										 parameters[1],
																										 parameters[2],
																										 new CMMusicCallback<OrderResult>() {
																											 @Override
																											 public void operationResult(
																													 OrderResult result) {
																												 mUIHandler
																														 .obtainMessage(
																																 1,
																																 result)
																														 .sendToTarget();
																											 }
																										 });
																					 }
																				 });

																		 break;
																	 case 14:
																		 showParameterDialog(
																				 new String[] { "monthType" },
																				 new ParameterCallbacks() {
																					 @Override
																					 public void callback(
																							 final String[] parameters) {
																						 Log.i("TAG", "monthType = "
																								 + parameters[0]);
																						 RingbackManagerInterface
																								 .ownRingMonth(
																										 CMMusicDemo.this,
																										 parameters[0],
																										 new CMMusicCallback<OrderResult>() {
																											 @Override
																											 public void operationResult(
																													 OrderResult result) {
																												 mUIHandler
																														 .obtainMessage(
																																 0,
																																 result)
																														 .sendToTarget();
																											 }
																										 });
																					 }
																				 });

																		 break;

																	 case 15:
																		 showParameterDialog(
																				 new String[] { "monthType" },
																				 new ParameterCallbacks() {
																					 @Override
																					 public void callback(
																							 final String[] parameters) {
																						 Log.i("TAG", "monthType = "
																								 + parameters[0]);
																						 RingbackManagerInterface
																								 .keyOrderOwnRingMonth(
																										 CMMusicDemo.this,
																										 parameters[0],
																										 new CMMusicCallback<OrderResult>() {
																											 @Override
																											 public void operationResult(
																													 OrderResult result) {
																												 mUIHandler
																														 .obtainMessage(
																																 0,
																																 result)
																														 .sendToTarget();
																											 }
																										 });
																					 }
																				 });

																		 break;

																	 case 16:
																		 showParameterDialog(
																				 new String[] { "crbtId" },
																				 new ParameterCallbacks() {
																					 @Override
																					 public void callback(
																							 final String[] parameters) {
																						 Log.i("TAG", "crbtId = "
																								 + parameters[0]);
																						 RingbackManagerInterface
																								 .orderOwnRingback(
																										 CMMusicDemo.this,
																										 parameters[0],
																										 new CMMusicCallback<OrderResult>() {
																											 @Override
																											 public void operationResult(
																													 OrderResult result) {
																												 mUIHandler
																														 .obtainMessage(
																																 0,
																																 result)
																														 .sendToTarget();
																											 }
																										 });
																					 }
																				 });

																		 break;

																	 case 17:
																		 showProgressBar("数据加载中...");
																		 new Thread() {
																			 @Override
																			 public void run() {
																				 Result t = RingbackManagerInterface
																						 .isOwnRingMonthUser(CMMusicDemo.this);

																				 mUIHandler.obtainMessage(0, t)
																						   .sendToTarget();
																			 }
																		 }.start();

																		 break;

																	 case 18:
																		 showParameterDialog(new String[] {
																						 "content", "bgMusicId", "sex",
																						 "ringName" },
																				 new ParameterCallbacks() {
																					 @Override
																					 public void callback(
																							 final String[] parameters) {
																						 Log.i("TAG", "content = "
																								 + parameters[0]);

																						 showProgressBar("数据加载中...");
																						 new Thread() {
																							 @Override
																							 public void run() {
																								 OwnRingRsp t = RingbackManagerInterface
																										 .createTextOwnRingback(
																												 CMMusicDemo.this,
																												 parameters[0],
																												 parameters[1],
																												 parameters[2],
																												 parameters[3]);

																								 mUIHandler
																										 .obtainMessage(
																												 0,
																												 t)
																										 .sendToTarget();
																							 }
																						 }.start();
																					 }
																				 });

																		 break;

																	 case 19:
																		 showParameterDialog(
																				 new String[] { "filepath",
																						 "ringName", "audioType" },
																				 new ParameterCallbacks() {
																					 @Override
																					 public void callback(
																							 final String[] parameters) {
																						 Log.i("TAG", "filepath = "
																								 + parameters[0]);
																						 new Thread() {
																							 @Override
																							 public void run() {
																								 OwnRingRsp t = RingbackManagerInterface
																										 .uploadOwnRingback(
																												 CMMusicDemo.this,
																												 new File(
																														 parameters[0]),
																												 parameters[1],
																												 Integer.parseInt(parameters[2]));

																								 mUIHandler
																										 .obtainMessage(
																												 0,
																												 t)
																										 .sendToTarget();
																							 }
																						 }.start();
																					 }
																				 });

																		 break;
																 }
															 }
														 }).create().show();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_cmmusic_demo, menu);
		return true;
	}

	@Override
	public void onClick(View arg0) {
		if(arg0.getId()==R.id.deletesong)
		{
			showParameterDialog("crbtId", new ParameterCallback() {

				@Override
				public void callback(final String crbtId) {
					Log.i("TAG", "crbtId = " + crbtId);
					showProgressBar("数据加载中...");
					new Thread() {
						@Override
						public void run() {
							Result t = RingbackManagerInterface
									.deletePersonRing(CMMusicDemo.this, crbtId);

							mUIHandler.obtainMessage(0, t).sendToTarget();
						}
					}.start();
				}
			});
		}
		// 全曲下载
		else if(arg0.getId()==R.id.fullsong)
		{
			showParameterDialog("musicId", new ParameterCallback() {
				@Override
				public void callback(final String musicId) {
					Log.i("TAG", "musicId = " + musicId);
					FullSongManagerInterface.getFullSongDownloadUrl(
							CMMusicDemo.this, musicId,
							new CMMusicCallback<OrderResult>() {
								@Override
								public void operationResult(
										final OrderResult downloadResult) {
									showDownloadAlertDialog(downloadResult,
											"getFullSongDownloadUrlByNet");

									Log.d(LOG_TAG,
											"FullSong Download result is "
													+ downloadResult);
								}
							});
				}
			});
		}
	}

	class T1 extends Thread {
		@Override
		public void run() {
			super.run();
			Looper.prepare();
			// if (!InitCmmInterface.initCheck(CMMusicDemo.this)) {
			Hashtable<String, String> b = InitCmmInterface
					.initCmmEnv(CMMusicDemo.this);
			Message m = new Message();
			m.what = 0;
			m.obj = b;
			mUIHandler.sendMessage(m);
			// } else {
			// if (null != dialog) {
			// dialog.dismiss();
			// }
			//
			// Toast.makeText(CMMusicDemo.this, "已初始化过",
			// Toast.LENGTH_LONG).show();
			// }
			Looper.loop();
		}
	}

	private ProgressDialog mProgress = null;

	void showProgressBar(final String msg) {
		Log.d(LOG_TAG, "showProgressBar invoked!");

		mUIHandler.post(new Runnable() {
			@Override
			public void run() {
				if (mProgress == null) {
					mProgress = new ProgressDialog(CMMusicDemo.this);
					mProgress.setMessage(msg);
					mProgress.setIndeterminate(false);
					mProgress.setCancelable(false);
					mProgress.show();
				}
			}
		});
	}

	void hideProgressBar() {
		Log.d(LOG_TAG, "hideProgressBar invoked!");
		mUIHandler.post(new Runnable() {
			@Override
			public void run() {
				if (mProgress != null) {
					mProgress.dismiss();
					mProgress = null;
				}
			}
		});
	}

	void showParameterDialog(String title, final ParameterCallback callback) {
		View view = View.inflate(CMMusicDemo.this, R.layout.parameter_dialog,
				null);
		final EditText edt = (EditText) view.findViewById(R.id.editText1);
		new AlertDialog.Builder(CMMusicDemo.this).setTitle(title).setView(view)
												 .setMessage("请输入参数:" + title).setNegativeButton("取消", null)
												 .setPositiveButton("确认", new DialogInterface.OnClickListener() {

													 @Override
													 public void onClick(DialogInterface dialog, int which) {
														 String parameter = edt.getText().toString();
														 if (callback != null) {
															 callback.callback(parameter);
														 }
													 }
												 }).show();
	}

	void showParameterDialog(String[] titles, final ParameterCallbacks callback) {
		String title = getStrForArray(titles);
		final MyGroupView view = new MyGroupView(CMMusicDemo.this);
		for (int i = 0; i < titles.length; i++) {
			EditText paramEdt = new EditText(CMMusicDemo.this);
			paramEdt.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT));
			paramEdt.setHint(titles[i]);
			view.addView(paramEdt);

		}
		new AlertDialog.Builder(CMMusicDemo.this).setTitle(title).setView(view)
												 .setMessage("请输入参数:" + title).setNegativeButton("取消", null)
												 .setPositiveButton("确认", new DialogInterface.OnClickListener() {

													 @Override
													 public void onClick(DialogInterface dialog, int which) {
														 int count = view.getChildCount();
														 String[] parameters = new String[count];
														 for (int i = 0; i < count; i++) {
															 View child = view.getChildAt(i);
															 if (child instanceof EditText) {
																 EditText et = (EditText) child;
																 parameters[i] = et.getText().toString();
															 }
														 }
														 if (callback != null) {
															 callback.callback(parameters);
														 }
													 }
												 }).show();
	}

	interface ParameterCallback {
		void callback(String parameter);
	}

	interface ParameterCallbacks {
		void callback(String[] parameters);
	}

	String getStrForArray(String[] strs) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < strs.length; i++) {
			sb.append(strs[i]);
			if (i < strs.length - 1) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			InitCmmInterface.exitApp(this);
			finish();

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
