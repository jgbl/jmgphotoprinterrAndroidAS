package com.jmg.jmgphotouploader;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.*;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.provider.MediaStore.Images;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.*;
import android.annotation.TargetApi;
import android.app.*;
import android.content.*;
import android.database.Cursor;
//import android.runtime.*;
import android.view.*;
import android.view.View.OnLongClickListener;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.jmg.jmgphotouploader.ImgFolder.Type;
import com.jmg.jmgphotouploader.Controls.ZoomExpandableListview;
//import com.facebook.*;
//import com.facebook.android.Facebook;
//import com.sromku.simple.*;

import com.microsoft.live.LiveAuthListener;
import com.microsoft.live.LiveAuthClient;
import com.microsoft.live.LiveAuthException;
import com.microsoft.live.LiveConnectClient;
import com.microsoft.live.LiveConnectSession;
import com.microsoft.live.LiveDownloadOperation;
import com.microsoft.live.LiveDownloadOperationListener;
import com.microsoft.live.LiveOperationException;
import com.microsoft.live.LiveStatus;


public class PhotoFolderAdapter extends BaseExpandableListAdapter implements LiveAuthListener
{
	public Activity context;
	private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 3);
	
	private LiveAuthClient auth;
	public LiveConnectClient client;
    public static int requestCode = 9997;
    public java.util.List<ImgFolder> rows;
	
	//private SynchronizationContext SyncContext;
	public PhotoFolderAdapter(Activity context, java.util.ArrayList<ImgFolder> List)
	{
		setThreadPolicy();
		this.context = context;
		this.rows = List;
		((_MainActivity)context).lv.setOnScrollListener(onScrollListener);
		//((_MainActivity)context).lv.setOverScrollMode(View.OVER_SCROLL_NEVER);
		//SyncContext = (SynchronizationContext.Current != null) ? SynchronizationContext.Current : new SynchronizationContext();
	}

		// Indexes are used for IDs:
	
	@Override
	protected void finalize() throws Throwable {
		executor.shutdown();
		super.finalize();
	};
	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}
	//---------------------------------------------------------------------------------------
	// Group methods:

	@Override
	public long getGroupId(int groupPosition)
	{
		// The index of the group is used as its ID:
		return groupPosition;
	}

		// Return the number of produce ("vegetables", "fruitimport com.microsoft.live.LiveOperationException;s", "herbs") objects:
	@Override
	public int getGroupCount()
	{
		return rows.size();
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		// Recycle a previous view if provided:
		//lib.LastgroupPosition= groupPosition;
		View view = convertView;
		boolean blnNew = false;
		// If no recycled view, inflate a new view as a simple expandable list item 1:
		if (view == null)
		{
			Object tempVar = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LayoutInflater inflater = (LayoutInflater)((tempVar instanceof LayoutInflater) ? tempVar : null);
			view = inflater.inflate(android.R.layout.simple_expandable_list_item_1, null);
			blnNew = true;
		}

		// Grab the produce object ("vegetables", "fruits", etc.) at the group position:
		ImgFolder imgFolder = rows.get(groupPosition);
		
		// Get the built-in first text view and insert the group name ("Vegetables", "Fruits", etc.):
		TextView textView = (TextView) view.findViewById(android.R.id.text1);
		
		if (imgFolder.type == ImgFolder.Type.OneDriveAlbum) 
			{
			textView.setTextColor(Color.CYAN);
			if (isExpanded==false && imgFolder.items != null && imgFolder.items.size()==0 && imgFolder.Name=="/" && imgFolder.fetched == false)
				{
					imgFolder.fetched = false;
					imgFolder.Name = "One Drive";
				}
			}
		else if(imgFolder.type == ImgFolder.Type.OneDriveFolder) 
		{
			textView.setTextColor(Color.GRAY);
		}
		else
		{
			textView.setTextColor(Color.WHITE);
		}
		textView.setText(imgFolder.Name);
		
		//if (blnNew) textView.setTextSize(lib.convertFromDp(context.getApplicationContext(), textView.getTextSize()));
		return view;
	}

	@Override
	public Object getGroup(int groupPosition)
	{
		return null;
	}

	//---------------------------------------------------------------------------------------
	// Child methods:

	@Override
	public long getChildId(int groupPosition, int childPosition)
	{
		// The index of the child is used as its ID:
		return childPosition;
	}

	@Override
	public int getChildrenCount(int groupPosition)
	{
		// Return the number of children (produce item objects) in the group (produce object):
		ImgFolder Folder = rows.get(groupPosition);
		GetFolderItems(Folder, groupPosition);		
		return Folder.items.size();
	}

	private static class ItemParams
	{
		public ImgListItem item;
		public ImageView img;
		public View view;
		
		public ItemParams(ImgListItem item, ImageView img, View view)
		{
			this.item = item;
			this.img = img;
			this.view = view;
		}
	}
	private static class ItemParamsSet
	{
		public ImageView IView;
		public Bitmap img;
		public View view;
		public ItemParamsSet(ImageView IView, Bitmap img, View view)
		{
			this.IView = IView;
			this.img = img;
			this.view = view;
		}
	}
	public android.database.Cursor ServiceCursor;
	@Override
	public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		// Recycle a previous view if provided:
		lib.setgstatus("GetChildview Start");
		View view = convertView;
		ImgFolder Folder = rows.get(groupPosition);
		lib.LastChildPosition= childPosition;
		//lib.LastgroupPosition = groupPosition;
		lib.LastisLastChild = isLastChild;
		lib.LastgroupPosition = groupPosition;
		GetFolderItems(Folder, groupPosition);
		final ImgListItem item = Folder.items.get(childPosition);
		// If no recycled view, inflate a new view as a simple expandable list item 2:
		boolean isNewView = false;
		try
		{
			if (view == null)
			{
				lib.setgstatus("GetChildview Inflate");
				view = context.getLayoutInflater().inflate(R.layout.customview, null);
				isNewView = true;
			}
			lib.setgstatus("GetChildview ImageView");
			if (isNewView) view.setMinimumHeight((int) lib.convertFromDp(context.getApplicationContext(), view.getHeight()));
			final ImageView Image = (ImageView)view.findViewById (R.id.Image);
			
			boolean ItemExists = false;
			if (view.getTag() != null)
			{
				ViewHolder holder = (ViewHolder)(view.getTag());
				ImgListItem ImgListItem = holder.item;
				if (ImgListItem.id != null && ImgListItem.id != "" && ImgListItem.id == item.id) ItemExists = true;
			}
			
			if (!ItemExists) 
			{
				Image.setImageBitmap(null);
				item.ThumbnailLoaded = false;
				view.setTag(new ViewHolder(item));
			}
			
			lib.setgstatus("GetChildview Thread");
			JMPPPApplication myApp = (JMPPPApplication) context.getApplication();
			lib.setClient(myApp.getConnectClient());
			context = (Activity) myApp.MainContext;
			boolean isOneDrive = false;
			if (item.type==ImgFolder.Type.OneDriveAlbum || item.type==ImgFolder.Type.OneDriveFolder)
			{
				LoadThumbnailOneDrive(item,Image);
				isOneDrive = true;
			}
			else
			{
				//BitmapWorkerAsyncTask Task = new BitmapWorkerAsyncTask(new ItemParams(item,Image,view), context);
				if (mIsScrolling)
				{
				}
				else
				{
				Runnable worker = new getBitmapWorkerThread(new ItemParams(item, Image, view), context);
				executor.submit(worker);
				//Task.execute();
				}
			}
			
			Image.setClickable(false);
			Image.setFocusable(false);
			Image.setFocusableInTouchMode(false);
			Image.setLongClickable(false);
			if (isNewView)
			{
				Image.setMaxHeight((int) lib.convertFromDp(context.getApplicationContext(), Image.getHeight()));
				Image.setMaxWidth((int) lib.convertFromDp(context.getApplicationContext(), Image.getWidth()));
			}
			//Image.Click+= (object sender, EventArgs e) => Console.WriteLine("IMG clicked");
			//view.FindViewById<ImageView> (Android.Resource.Id.Icon).ScaleX = 5;
			//view.FindViewById<ImageView> (Android.Resource.Id.Icon).ScaleY = 5;
			lib.setgstatus("GetChildview Text1");
			TextView Text1 = (TextView)view.findViewById (R.id.Text1);
			Text1.setText (item.FileName);
			Text1.setClickable(false);
			Text1.setFocusable(false);
			Text1.setFocusableInTouchMode(false);
			Text1.setLongClickable(false);
			if (isNewView) Text1.setTextSize(TypedValue.COMPLEX_UNIT_PX,lib.convertFromDp(context.getApplicationContext(), Text1.getTextSize()));
			//Text1.Click+= (object sender, EventArgs e) => Console.WriteLine("Text1 clicked");
			lib.setgstatus("GetChildview Text2");
			TextView Text2 = (TextView)view.findViewById (R.id.Text2);
			if (item.getsize() == null && item.getImg() != null)
			{
				Text2.setText((item.getsize() != null) ? item.getsize() : item.getImg().getWidth() 
						+ "*" + item.getImg().getHeight());
			}
			else
			{
				Text2.setText(item.getsize());
			}
			Text2.setClickable(false);
			Text2.setFocusable(false);
			Text2.setFocusableInTouchMode(false);
			Text2.setLongClickable(false);
			if (isNewView) Text2.setTextSize(TypedValue.COMPLEX_UNIT_PX,lib.convertFromDp(context.getApplicationContext(), Text2.getTextSize()));
				//Text2.Click += (object sender, EventArgs e) => Console.WriteLine("Text2 clicked");
			
			//if (item.Img != null) view.FindViewById<TextView> (Resource.Id.Text2).Text = (item.size != null) ? item.size : item.Img.Width + "*" + item.Img.Height;
			//view.FindViewById<TextView> (Android.Resource.Id.Text1).
			lib.setgstatus("GetChildview Tag");
			


			//view.ScaleX = 2;
			//view.ScaleY = 2;
			view.setClickable(false);
			view.setFocusable(false);
			view.setFocusableInTouchMode(false);
			view.setLongClickable(false);
			/*view.Click += (object sender, EventArgs e) => {
				System.Diagnostics.Debug.Print ("Click");
			};
			*/
			//view.Touch += new EventHandler<View.TouchEvent1Args> (touch);
			//RelativeLayout relLayout = (RelativeLayout)view.FindViewById (Resource.Id.ImageList);
			lib.setgstatus("GetChildview Start Combo");
			LinearLayout layout = (LinearLayout)view.findViewById(R.id.Text); // (context);
			//relLayout.AddView (layout);
			android.database.Cursor Cursor;
			lib.setgstatus("GetChildview Services");
			if (ServiceCursor == null)
			{
				do {
					Cursor = lib.dbpp.query("Select * FROM Services");
					ServiceCursor = Cursor;
					if (Cursor.getCount() == 3){
						try{
							lib.dbpp.DataBase.execSQL ("ALTER TABLE Services ADD COLUMN 'package' VARCHAR");
						}
						catch(Exception ex){
							System.out.print(ex.getMessage());
						}
						lib.dbpp.DataBase.execSQL("INSERT INTO Services ('Name','URL','package') VALUES('Pinterest','pinterest.com','com.pinterest')");
					}
				} while (Cursor.getCount() < 4);
			}
			else
			{
				Cursor = ServiceCursor;
			}
			if (Cursor.getCount() > 0)
			{
				boolean first = true;
				lib.setgstatus("GetChildview enumerate Services");
				while ((first) ? (Cursor.moveToFirst()) : (Cursor.moveToNext()))
				{
					first = false;
					if (Cursor.getString(Cursor.getColumnIndex("visible")).equalsIgnoreCase("true"))
					{
						if (item.Uri != null)
						{
							lib.setgstatus("GetChildview Select Files");
							try 
							{
								android.database.Cursor CursorItem;
								if (item.type == Type.OneDriveAlbum)
								{
									CursorItem = lib.dbpp.DataBase.query("Files", null, "FileName=?", new String[]{item.id}, null, null, null);
								}
								else
								{
									String uri = item.folder; //item.Uri.getPath();
									CursorItem = lib.dbpp.DataBase.query("Files", null, "URI=?", new String[]{uri}, null, null, null);
								}
								String Service = Cursor.getString(Cursor.getColumnIndex("Name"));
								CheckBox cb;
								if (isNewView)
								{
									lib.setgstatus("GetChildview Create CheckBox");
									cb = new CheckBox(context);
									cb.setId(Cursor.getInt(Cursor.getColumnIndex("_id")));
									cb.setText(Service);
									cb.setTextColor(Color.LTGRAY);
									//(cb.setScaleX(lib.convertFromDp(context.getApplicationContext(), 1.0f));
									//cb.setScaleY(lib.convertFromDp(context.getApplicationContext(), 1.0f));
									//cb.forceLayout();
									cb.setTextSize(TypedValue.COMPLEX_UNIT_PX,lib.convertFromDp(context.getApplicationContext(), 14.0f));
									//cb.SetPadding (1,1,1,1);//(5, 26, 5, 5);
									cb.setGravity(android.view.Gravity.CENTER_VERTICAL);
									float Weight = 0.49f / Cursor.getCount();
									LinearLayout.LayoutParams L = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.FILL_PARENT,Weight);
									L.gravity = cb.getGravity();
									/*
									if (cb.getScaleX() != 1.0f){
										L.setMargins(0,20,20,20);
										L.setMargins((int)(L.leftMargin / cb.getScaleX()),(int)( L.topMargin / cb.getScaleY()),(int) (L.rightMargin / cb.getScaleX()), (int)(L.bottomMargin / cb.getScaleY()));
										//L.leftMargin = - Math.abs(L.leftMargin);
										L.rightMargin = - Math.abs(L.rightMargin);
										L.topMargin = - Math.abs(L.topMargin);
										L.bottomMargin = - Math.abs(L.bottomMargin);
									}
									*/
									lib.setgstatus("GetChildview AddCheckbox to Layout");
									layout.addView(cb, L);
									//cb.setWidth((int)(cb.getWidth() / cb.getScaleX()));
									//cb.setHeight((int)(cb.getHeight() / cb.getScaleY()));
									//Console.WriteLine ("CB: Width: " + cb.Width + " Height: " + cb.Height + " Gravity: " + cb.Gravity.ToString());
	//C# TO JAVA CONVERTER TODO TASK: Java has no equivalent to C#-style event wireups:
									cb.setOnCheckedChangeListener(onCheckedChangedListener);
									cb.setOnLongClickListener(onLongClickListener);
									cb.setChecked(false);
								}
								else
								{
									lib.setgstatus("GetChildview Find Checkbox");
									cb = (CheckBox)view.findViewById(Cursor.getInt(Cursor.getColumnIndex("_id")));
									cb.setTag(null);
									cb.setChecked(false);
								}
								lib.setgstatus("GetChildview Set Checked");
								if (CursorItem.moveToFirst())
								{
									try
									{
										lib.setgstatus("GetChildview Query Uploads");
										try 
										{
											String ServiceID = Cursor.getString(Cursor.getColumnIndex("_id"));
											String FileID = CursorItem.getString(CursorItem.getColumnIndex("_id"));						
											android.database.Cursor CursorUploads = lib.dbpp.DataBase.query
													("Uploads", null, "ServiceID=? AND FileID=?", 
															(new String[] {ServiceID, 
																	FileID}), null, null, null);
											boolean Checked = (CursorUploads.getCount() > 0);
											cb.setChecked (Checked);
										}
										finally{
											
										}
									}
									catch (java.lang.Exception e)
									{
									}
								}
								lib.setgstatus("GetChildview Set cb.Tag");
								cb.setTag(new cbItemHolder(item, Cursor.getInt(Cursor.getColumnIndex("_id"))));
							}
							finally{
								
							}
						}
					}
				}
			}
			/*
			Text1.LayoutParameters.Width = 0;
			((LinearLayout.LayoutParams)Text1.LayoutParameters).Weight= 0.3f;
			Text2.LayoutParameters.Width = 0;
			((LinearLayout.LayoutParams)Text2.LayoutParameters).Weight= 0.2f;
			*/
			lib.setgstatus("GetChildview Finished");
			return view;
		}
		catch (RuntimeException ex)
		{
			lib.ShowException(context, ex);
			return view;
		}
	}
	private void LoadThumbnailOneDrive(final ImgListItem item, final ImageView Image)
	{
		String file = item.id + "/picture?type=thumbnail";
		if (item.ThumbnailLoaded) return;
		try 
		{
			
			try
			{
				/*
				mProgress = new ProgressDialog(context);
ZoomExpandableListview lv = (ZoomExpandableListview) ((_MainActivity) context).lv;				mProgress.setTitle("Download");
				mProgress.setMessage("Downloading Image");
				mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				mProgress.setMax(100);
				mProgress.show();
				*/
			}
			catch (Exception ex)
			{
				lib.ShowException(context, ex);
			}
			ZoomExpandableListview lv = (ZoomExpandableListview) ((_MainActivity) context).lv;
        	if (mIsScrolling || lv.getIsScaled() || !ItemExists(Image, item))
			{
        		/*
        		lib.ShowToast(context, "Item " + item.FileName 
        				+ " can not be displayed!" 
        				+ " IsScrolling " + mIsScrolling 
        				+ " IsScaled " + lv.getIsScaled() 
        				+ " ItemExists " + ItemExists(Image,item));
        		return;
        		*/
			}
			else
			{
				LiveDownloadOperationListener LDL;
				LDL = new LiveDownloadOperationListener() 
					{
			        public void onDownloadCompleted(LiveDownloadOperation operation)
			        {
			        	InputStream input = null;
			        	ZoomExpandableListview lv = (ZoomExpandableListview) ((_MainActivity) context).lv;
			        	if (mIsScrolling || lv.getIsScaled() || !ItemExists(Image, item))
						{
			        		/*
			        		lib.ShowToast(context, "Download interrupted " + item.FileName 
			        				+ " can not be displayed!" 
			        				+ " IsScrolling " + mIsScrolling 
			        				+ " IsScaled " + lv.getIsScaled() 
			        				+ " ItemExists " + ItemExists(Image,item));
			        		operation.cancel();
			        		return;
			        		*/
						}
			        	try {
			                //resultTextView.setText("Picture downloaded.");
			                input =  operation.getStream();
			                Bitmap bMap = null;
			                try
			                {
			                	int i = 0;
			                	while (bMap == null)
			                	{
			                		i++;
			                		bMap = BitmapFactory.decodeStream(input);
			                		if (i > 0) break;
			                	}
			                }
			                catch (Exception ex)
			                {
			                	ex.printStackTrace();
			                	lib.ShowToast(context, "Could not load " + item.FileName + " Error: " + ex.getClass().getName() + " " + ex.getMessage());
			                }
			                if (bMap != null)
			                {
			                	if (ItemExists(Image,item))
			                	{
			                		Image.setImageBitmap(bMap);
			                		item.ThumbnailLoaded = true;
			                	}
			                	else
			                	{
			                		lib.ShowToast(context, "Item " + item.FileName + " is no more visible!");
			                	}
			                	//SetImageViewBitmap(new ItemParamsSet(p.img, bMap));
			                	//p.item.setsize(bMap.getWidth() + "*" + bMap.getHeight());
			                }
			                else
			                {
			                	lib.ShowToast(context, "Could not load " + item.FileName);
			                }
			                if(input !=  null)
			                {
			                	try 
			                	{
									input.close();
								} 
			                	catch (IOException e) 
			                	{
									// TODO Auto-generated catch block
									e.printStackTrace();
								} 
			                }
			            }
			            catch(Exception ex) {
			                //resultTextView.setText("Error downloading picture: " + ex.getMessage());
			            	lib.ShowToast(context, "Could not load " + item.FileName + " Error: " + ex.getClass().getName() + " " + ex.getMessage() + (lib.getCauses(ex)));
			            }
			            finally
			            {
			            	//mProgress.dismiss();
			            }
			        }
			        			        
			        public void onDownloadFailed(LiveOperationException exception, LiveDownloadOperation operation)
			        {
			            //resultTextView.setText(exception.getMessage());
			        	//lib.ShowToast(context, "Failure! Could not load " + item.FileName);
			        	lib.ShowToast(context, "Could not load " + item.FileName + " Error: " + exception.getClass().getName() + " " + exception.getMessage() + (lib.getCauses(exception)));
			        	//mProgress.dismiss();
			        	/*
			        	if (exception.getClass() == LiveOperationException.class && exception.getMessage().contains("request_token_missing"))
			        	{
			        		Intent LoginLiveIntent = new Intent(context, LoginLiveActivity.class);
							LoginLiveIntent.putExtra("GroupPosition", 0);
							context.startActivity(LoginLiveIntent); //, LoginLiveActivity.requestCode);
							context.finish();
			        	}
			        	*/
			        }
			        public void onDownloadProgress(int totalBytes, int bytesRemaining, LiveDownloadOperation operation)
			        {
			            //resultTextView.setText("Downloading picture... " + bytesRemaining + " bytes downloaded " +
			              //  "(" + (bytesRemaining / totalBytes) * 100 + "%)");
			        	int percentCompleted = (int)((bytesRemaining/totalBytes) * 100);
			        	ZoomExpandableListview lv = (ZoomExpandableListview) ((_MainActivity) context).lv;
			        	if (mIsScrolling || lv.getIsScaled() || !ItemExists(Image, item))
			        	{
			        		lib.ShowToast(context, "Operation canceled! " + item.FileName 
			        				+ " can not be displayed!" 
			        				+ " IsScrolling " + mIsScrolling 
			        				+ " IsScaled " + lv.getIsScaled() 
			        				+ " ItemExists " + ItemExists(Image,item));
			        		operation.cancel();
			        	}
							
                        //mProgress.setProgress(percentCompleted);
			        }
			    };
			    lib.getClient(context).downloadAsync(file, LDL ,null);
			}
		}
		catch (Exception ex)
		{
			lib.ShowException(context, ex);
		}
	}
	private boolean ItemExists(ImageView Image, ImgListItem item)
    {
    	View view = (View) Image.getParent();
    	Boolean ItemExists = false;
    	if (view.getTag() != null)
		{
			ViewHolder holder = (ViewHolder)(view.getTag());
			ImgListItem ImgListItem = holder.item;
			if (ImgListItem.id != null && ImgListItem.id != "" && ImgListItem.id == item.id) ItemExists = true;
		}
    	return ItemExists;
    }

	private void GetFolderItems(ImgFolder Folder, int GroupPosition){
		if ((Folder.type == ImgFolder.Type.OneDriveAlbum || Folder.type == ImgFolder.Type.OneDriveFolder) && (Folder.Name != "/") && (Folder.items.size() == 0))
		{
			if (Folder.Name == "One Drive") 
			{
				Folder.Name = "/";	
			}
			if (Folder.items.size() == 0 && Folder.fetched == false) {
				lib.LastgroupPosition = GroupPosition;
				if (true)
				{
					
					if (lib.getClient(context) == null)
					{
						Folder.Name = "One Drive";
						//CountDownLatch Latch = new CountDownLatch(1);
						//lib.Latch= Latch;
						lib.BMList = new java.util.ArrayList<ImgListItem>();
						((_MainActivity)context).StartLoginLive(Folder);
						//context.finish();
					}
					else
					{
						try {
							if(Folder.fetched == false ){
								lib.BMList = new java.util.ArrayList<ImgListItem>();
								Folder.items= lib.BMList;
								lib.GetThumbnailsOneDrive(context,Folder.Name, Folder, GroupPosition, ((_MainActivity)context).lv);
								Folder.fetched=true;
							}
						} catch (LiveOperationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					/* 
					CountDownLatch LatchClient = new CountDownLatch(1);
					lib.LatchClient = LatchClient;
					this.auth = new LiveAuthClient(context, "0000000048135143");
			        final Iterable<String> scopes = Arrays.asList("wl.signin", "wl.basic", "wl.skydrive");
			        auth.login(context, scopes, (LiveAuthListener) this);
			        try {
						LatchClient.await(15, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				*/
				/*
		         try {
					//lib.GetThumbnailsOneDrive(context, lib.BMList);
					Latch.await(30, TimeUnit.SECONDS);
		         } catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
		         }
		         */
		         Folder.items= lib.BMList;
				}
			}
		}
	}

	public void onAuthComplete(LiveStatus status, LiveConnectSession session, Object userState) {
        if(status == LiveStatus.CONNECTED) {
            //lib.ShowMessage(this,"Signed in.");
            client = new LiveConnectClient(session);
            lib.setClient (client);
            //lib.LatchClient.countDown();
            //GetThumbnailsOneDriveAndSetLV(true);
            //this.setResult(1, this.getIntent());
            //this.finishActivity(requestCode);
            //this.finish();
        }
        else {
            lib.ShowMessage(context,"Not signed in.");
            client = null;
            //lib.LatchClient.countDown();
            //GetThumbnailsOneDriveAndSetLV(false);
            //this.setResult(0, getIntent());
            //this.finishActivity(requestCode);
        }        
    }

    public void onAuthError(LiveAuthException exception, Object userState) {
        lib.ShowMessage(context, "Error signing in: " + exception.getMessage());        
        client = null;
        //lib.LatchClient.countDown();
        //GetThumbnailsOneDriveAndSetLV(false);
    }


	public class getBitmapWorkerThread implements Runnable {
	     
	    private String command;
	    private ItemParams p; 
	    private Context Context;
	    public getBitmapWorkerThread(ItemParams p, Context Context){
	        this.p = p;
	        this.Context = Context;
	    }
	 
	    @Override
	    public void run() {
			Bitmap img = p.item.getImg();
			SetImageViewBitmap(new ItemParamsSet(p.img, img, p.view));
		}
	 
	    private synchronized void SetImageViewBitmap(ItemParamsSet p)
		{
			p.IView.setImageBitmap(p.img);
			//p.IView.invalidate();
			this.p.item.ThumbnailLoaded = true;
		}
	 
	    @Override
	    public String toString(){
	        return this.command;
	    }
	}
	public class BitmapWorkerAsyncTask extends AsyncTask {
	     
	    private String command;
	    private ItemParams p; 
	    private Context Context;
	    public BitmapWorkerAsyncTask(ItemParams p, Context Context){
	        this.p = p;
	        this.Context = Context;
	    }
	   
	    @Override
		protected Object doInBackground(Object... arg0) {
			// TODO Auto-generated method stub
				Bitmap img = p.item.getImg();
				SetImageViewBitmap(new ItemParamsSet(p.img, img, p.view));
				return null;
		}
	 
	    private synchronized void SetImageViewBitmap(ItemParamsSet p)
		{
			p.IView.setImageBitmap(p.img);
			//p.IView.invalidate();
			p.view.invalidate();
		}
	 
	    @Override
	    public String toString(){
	        return this.command;
	    }

		
	}
	
	private static class cbItemHolder extends java.lang.Object
	{
		public ImgListItem item;
		public int ServiceID;
		public cbItemHolder(ImgListItem item, int ServiceID)
		{
			this.item = item;
			this.ServiceID = ServiceID;
		}
	}
	public OnCheckedChangeListener onCheckedChangedListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// TODO Auto-generated method stub
			lib.setgstatus("cb_checkedChanged Start");
			CheckBox cb = (CheckBox)buttonView;
			if (cb.getTag() == null)
			{
				return;
			}
			cbItemHolder holder = (cbItemHolder)cb.getTag();
			ImgListItem item = holder.item;
			android.database.Cursor CursorItem;
			do
			{
				lib.setgstatus("cb_checkedChanged Query Files");
				//CursorItem = lib.dbpp.DataBase.query("Files", null, "URI=?", new String[]{item.Uri.getPath()}, null, null, null);
				boolean isOneDrive = false;
				if (item.type == Type.OneDriveAlbum)
				{
					CursorItem = lib.dbpp.DataBase.query("Files", null, "FileName=?", new String[]{item.id}, null, null, null);
					isOneDrive = true;
				}
				else
				{
					CursorItem = lib.dbpp.DataBase.query("Files", null, "URI=?", new String[]{item.folder}, null, null, null);
				}
				if (CursorItem.getCount() == 0)
				{
					ContentValues values = new ContentValues();
					if (isOneDrive)
					{
						values.put("URI", item.Uri.getPath());
					}
					else
					{
						values.put("URI", item.folder);
					}
					if (item.type == Type.OneDriveAlbum)
					{
						values.put("FileName", item.id);
					}
					else
					{
						values.put("FileName", item.FileName);
					}
					lib.setgstatus("cb_checkedChanged Insert Files");
					lib.dbpp.DataBase.insert("Files", null, values);
					//CursorItem = lib.dbpp.DataBase.Query ("Files", null, "URI=?", new string[]{ item.Uri.Path }, null, null, null);
				}
			} while (CursorItem.getCount() == 0);
			CursorItem.moveToFirst();
			lib.setgstatus("cb_checkedChanged GetFileID");
			int FileID = CursorItem.getInt(CursorItem.getColumnIndex("_id"));

			if (isChecked)
			{
				try
				{
					lib.setgstatus("cb_checkedChanged InsertUploads");
					ContentValues values = new ContentValues();
					values.put("ServiceID", holder.ServiceID);
					values.put("FileID", FileID);
					lib.dbpp.DataBase.insert("Uploads", "", values);
				}
				catch (java.lang.Exception e)
				{
				}
			}
			else
			{
				try
				{
					lib.setgstatus("cb_checkedChanged Delete Uploads " + holder.ServiceID + " " + FileID);
					//lib.ShowMessage(context, lib.getgstatus());
					//String[] WhereArgs = new String[]{"" + (holder.ServiceID), "" + (FileID)};
					lib.dbpp.DataBase.execSQL("DELETE FROM Uploads WHERE ServiceID = " 
							+ holder.ServiceID + " AND FileID = " + FileID);
					
					//lib.dbpp.DataBase.delete("Uploads", "ServiceID=?, FileID=?", 
					//		WhereArgs);
				}
				catch (java.lang.Exception e2)
				{
					lib.ShowException(context, e2);
				}
			}


		}
	};
	private int mFirstVisibleItem = -1;
	private int mVisibleItemCount = -1;
	private boolean mIsScrolling = false;
	
	public OnScrollListener onScrollListener = new OnScrollListener()
	{

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			// TODO Auto-generated method stub
			mFirstVisibleItem = firstVisibleItem;
			mVisibleItemCount = visibleItemCount;
			//if (totalItemCount > 0) mIsScrolling = true;
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub
			/*
			int 	SCROLL_STATE_FLING 	The user had previously been scrolling using touch and had performed a fling.
			int 	SCROLL_STATE_IDLE 	The view is not scrolling.
			int 	SCROLL_STATE_TOUCH_SCROLL 	The user is scrolling using touch, and their finger is still on the screen 
			*/
			if (scrollState == SCROLL_STATE_IDLE)
			{
				mIsScrolling = false;
				ExpandableListView lv = (ExpandableListView)view; 
				//PhotoFolderAdapter ppa = (PhotoFolderAdapter)(lv.getAdapter()); 
				int firstVis  = lv.getFirstVisiblePosition();
			    int lastVis = lv.getLastVisiblePosition();
			     int count = firstVis;

			    while (count <= lastVis)
			    {
			        long longposition = lv.getExpandableListPosition(count);
			        int type = ExpandableListView.getPackedPositionType(longposition);
			        int groupPosition = ExpandableListView.getPackedPositionGroup(longposition);
		            int childPosition = ExpandableListView.getPackedPositionChild(longposition);
		            if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			            boolean isLastChild = count == lastVis;
			            View ChildView = lv.getChildAt(count-firstVis);
			            if ((ChildView != null) && (ChildView.getTag() != null))
						{
							ViewHolder holder = (ViewHolder)(ChildView.getTag());
							if (holder != null)
							{
								ImgListItem item = holder.item;
								ImageView Image = (ImageView) (ChildView.findViewById (R.id.Image));
								//Bitmap bitmap = ((BitmapDrawable)Image.getDrawable()).getBitmap();
								if (item.type==ImgFolder.Type.OneDriveAlbum || item.type==ImgFolder.Type.OneDriveFolder)
								{
									LoadThumbnailOneDrive(item,Image);
								}
								else
								{
									//BitmapWorkerAsyncTask Task = new BitmapWorkerAsyncTask(new ItemParams(item,Image,view), context);
									Runnable worker = new getBitmapWorkerThread(new ItemParams(item, Image, view), context);
									executor.submit(worker);
									//Task.execute();
								}
							}
						}
			        }
			        else
			        {
			        	
			        }
			        count++;
			    }
			}
			else
			{
				mIsScrolling = true;
			}
		}
		
	};
	public OnLongClickListener onLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View arg0) {
			// TODO Auto-generated method stub
			try
			{
				CheckBox cb = (CheckBox) arg0;
				if (cb.isChecked()) {
					final String service = (String) cb.getText();
					final int id = cb.getId();
					View v = (View) cb.getParent().getParent();
					if (v.getTag() != null)
					{
						ViewHolder holder = (ViewHolder)(v.getTag());
						ImgListItem ImgListItem = holder.item;
						Uri uri = ImgListItem.Uri;
						if (ImgListItem.type== Type.OneDriveAlbum)
						{
							final String file = ImgListItem.id + "/picture?type=full";
							if (lib.getClient(context) != null)
							{
							}
							else
							{
								JMPPPApplication myApp = (JMPPPApplication) context.getApplication();
								lib.setClient (myApp.getConnectClient());
							}
							
							final ProgressDialog mProgress = new ProgressDialog(context);
							mProgress.setTitle("Download");
							mProgress.setMessage("Downloading Image");
							mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
							mProgress.setMax(100);
							mProgress.setIndeterminate(true);
							mProgress.show();
							
							lib.getClient(context).downloadAsync(file, new LiveDownloadOperationListener() {
								
								@Override
								public void onDownloadProgress(int arg0, int arg1,
										LiveDownloadOperation arg2) {
									// TODO Auto-generated method stub
									int percentCompleted = (int)((arg1/arg0) * 100);
		                            mProgress.setProgress(percentCompleted);
								}
								
								@Override
								public void onDownloadFailed(LiveOperationException arg0,
										LiveDownloadOperation arg1) {
									// TODO Auto-generated method stub
									mProgress.dismiss();
									lib.ShowToast(context, arg0.getMessage());
								}
								
								@Override
								public void onDownloadCompleted(LiveDownloadOperation arg0) {
									lib.ShowToast(context, "File downloaded.");
									InputStream s = null;
									Bitmap mBitmap = null;
									// TODO Auto-generated method stub
									try 
									  {
										s = arg0.getStream();
										mBitmap = BitmapFactory.decodeStream(s);
										if (mBitmap != null)
										{
											String path = Images.Media.insertImage(context.getContentResolver(), 
												    mBitmap, "Image Description", null);
											Uri newUri = Uri.parse(path);
											ShareUri(ServiceCursor, id, newUri);
										}
										s.close();
									  } 
									  catch (Exception e) 
									  {
										// TODO Auto-generated catch block
										e.printStackTrace();
										lib.ShowToast(context, e.getMessage());
									  }
									mProgress.dismiss();
		                            
								}
							});
							
							
							
							
						}
						else
						{
							ShareUri(ServiceCursor, id,uri);
						}
					}
				}
				return true;
			}
			catch (Exception ex)
			{
				lib.ShowToast(context, ex.getMessage());
				return false;
			}
			
		}
		
	};
	private void ShareUri(Cursor c, int id, Uri uri)
	{
		/*
		if (service.contains("Facebook")){
			lib.SharePictureOnFacebook(context , uri);
		}
		else if (service.contains("Twitter")){
			lib.SharePictureOnTwitter(context , uri);
		}
		else if (service.contains("Instagram")){
			lib.SharePictureOnInstagram(context , uri);
		}
		else if (service.contains("Pinterest")){
			lib.SharePictureOnPinterest(context , uri);
		}
		*/
		lib.SharePicture(context, uri, c, id);
	}
	
	public boolean onTouchEvent(MotionEvent event) 
	{
		//MainActivity a = (MainActivity)((context instanceof MainActivity) ? context : null);
		//a.lv.OnTouchEvent(e.Event);
		return false;
	}


	@Override
	public Object getChild(int groupPosition, int childPosition)
	{
		return null;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		return true;
	}

	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private void setThreadPolicy()
	{
		if (android.os.Build.VERSION.SDK_INT>8)
		{
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy); 
		}
	}
	
}