package gps.trajectory;

import gps.trajectory.Task.taskListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Tasklist extends ListActivity {

	//ftp
	Bundle prev_bData;
	String ftpHost;
    String ftpUserName;
    String ftpPassword;  
    String ftpRemoteDirectory;
    String LocalDirectory;	
    String Delete;
    int port;
    
    //task
    static Task[] myTasks; 
    static int now_task;
    static int total_task;
    static ArrayList<Task> tasksFtp;
    
    //同步
    static Object lock = new Object() ;
    
    //button
    Button btn_ok;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tasklist);
		
		//ftp
        ftpHost = this.getIntent().getStringExtra("ftpHost");
        ftpUserName = this.getIntent().getStringExtra("ftpUser");
        ftpPassword = this.getIntent().getStringExtra("ftpPasswd");
        ftpRemoteDirectory = this.getIntent().getStringExtra("ftpRemoteDir");
        LocalDirectory = this.getIntent().getStringExtra("localDir"); //list
        port = this.getIntent().getIntExtra("port",21);
        Delete = this.getIntent().getStringExtra("delete");

        //task
        now_task=0;
        myTasks=initTasks();
        tasksFtp = new ArrayList<Task>(Arrays.asList(myTasks));
		MyAdapter adapter = new MyAdapter(this,tasksFtp);
		getListView().setAdapter(adapter);
		
		//只從第一個開始跑
		new Thread(myTasks[now_task]).start();
		
		//button
		btn_ok = (Button) this.findViewById(R.id.button_ok);
		btn_ok.setOnClickListener(click_btnok);
		
	}
	
	//增加task 
	
	Task[] initTasks() { 
		String[] fileList = LocalDirectory.split(",");
		final int count=fileList.length;
		total_task=count;
		Task[] result= new Task[count];
		
		for (int i=0;i<count;i++){
			result[i]=new Task(fileList[i],ftpHost,ftpUserName,ftpPassword,
					ftpRemoteDirectory,LocalDirectory,Delete,port);  
		}
		
		return result;
	}
	
	

	public static class MyAdapter extends BaseAdapter {
		private final Context context;
		private static ArrayList<Task> tasks;
		private LayoutInflater inflater;

		final static Handler mHandler=new Handler();
		public MyAdapter(Context context,ArrayList<Task> tasks){	
			this.context=context;
			this.tasks=tasks;
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);			
		}
		
		@Override
		public View getView(final int position, View convertView, final ViewGroup parent) {
			final ViewHolder viewHolder;
			
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.custom_list_item, parent,false);
				viewHolder = new ViewHolder(convertView);
				convertView.setTag(viewHolder);
			} else {
				viewHolder=(ViewHolder) convertView.getTag();
			}

			final Task aTask= tasks.get(position);
			viewHolder.setNewTask(aTask);
			
			//Clear completed task
			/*
			viewHolder.btnAction.setOnClickListener(new Button.OnClickListener(){
				@Override
				public void onClick(View v) {
					if(aTask.getFinish()){
						tasks.remove(aTask);
						notifyDataSetChanged();
					}
				}
				
			});*/
				
			return convertView;
		}// getView()

		final static class ViewHolder {
			public ImageView imageType;
			public TextView tvTaskDesc;
			public ProgressBar pbTask;
			//public Button btnAction;
			
			public Task linkTask;
			public Task.taskListener l;
			
			public void removeListener() {
				if (linkTask!=null && l !=null )
					linkTask.removeListener(l);
			}
			public void addListener() {
				if (linkTask!=null )
					linkTask.addListener(l);
			}
			public void setNewTask(Task t) {
				removeListener();
				this.linkTask=t;
				this.tvTaskDesc.setText(t.getDesc());
				this.pbTask.setProgress(t.getProgress());
				addListener();
			}

			public ViewHolder(View convertView){
				//this.imageType = (ImageView) convertView
						//.findViewById(R.id.icon);
				this.tvTaskDesc = (TextView) convertView
						.findViewById(R.id.tvTaskDesc);
				this.pbTask = (ProgressBar) convertView
						.findViewById(R.id.pbTask);
				//this.btnAction = (Button) convertView
					//	.findViewById(R.id.btnActive);
				this.l = new taskListener() {				
					@Override
					public void onProgressChanged(final int progress,final String name) {
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								tvTaskDesc.setText(name);
								pbTask.setProgress(progress);
								if(now_task < total_task-1){ //最後一個task不用進判斷式
									if(myTasks[now_task].getFinish()){
										//next task
										now_task++;
										new Thread(myTasks[now_task]).start();
									}
								}
							}
						});//onProgress
					}//taskListener
					
				}; //ViewHolder
				
			}
		}

		@Override
		public int getCount() {
			return  tasks.size();
		}
		@Override
		public Task getItem(int position) {
			return getItem(position);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}

	}
	@Override
	public void onDestroy() {
		super.onDestroy();		 
	}
	
	//click button ok
		private View.OnClickListener click_btnok = new View.OnClickListener(){
			public void onClick(View v){
					
				Bundle tData = new Bundle();
				tData.putBoolean("finish", true);
				
				Intent it=new Intent();
				it.setClass(Tasklist.this, Mystory.class);
				it.putExtras(tData);
				setResult(RESULT_OK,it);
				//startActivity(it);
				Tasklist.this.finish();
				
		    }
		};
}
