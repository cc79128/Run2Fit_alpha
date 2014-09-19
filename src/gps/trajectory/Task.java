package gps.trajectory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import android.util.Log;

public class Task  implements Runnable {
	public static interface taskListener {
		void onProgressChanged(final int progress,final String name);
	}
	private String desc;
	private String presentName;
	private int progress;
	private boolean finish;
	private Vector<Task.taskListener> listenerList;
	
	private Thread mthread;
	private FTPClient mFTPClient;
	private String ftpHost;
	private String ftpUserName;
	private String ftpPassword;  
	private String ftpRemoteDirectory;
	private String LocalDirectory;	
	private String Delete;
	private int port;
	private int num_files;
	private int finish_file;

	public Task(String desc,
			String ftpHost,String ftpUserName,String ftpPassword,
			String ftpRemoteDirectory,String LocalDirectory,String Delete,int port) {
		
		this.desc=desc;
		this.presentName=desc;
		progress=0;
		num_files=1;
		finish_file=0;
		finish=false;
		listenerList=new Vector<Task.taskListener>();
		
		this.ftpHost=ftpHost;
		this.ftpUserName=ftpUserName;
		this.ftpPassword=ftpPassword;
		this.ftpRemoteDirectory=ftpRemoteDirectory;
		this.LocalDirectory=LocalDirectory;
		this.Delete=Delete;
		this.port=port;
		
		mthread = new Thread(ftpclient);

		//Log.i("Thread","new Tread initial");
	}
	
	@Override
	public void run() {
		mthread.start();
	}

	public void addListener(taskListener l) {
		listenerList.add(l);
	}
	public void removeListener(taskListener l){
		listenerList.remove(l);
	}
	public int getProgress() {
		return progress > 100 ? 100 : progress;
	}

	public void setProgress(int progress) {
		this.progress = progress >= 100 ? 100 : progress;
		if(this.progress>=100){
			finish_file++;
			if(finish_file == num_files){
				finish=true;
			}
		}
		if (!listenerList.isEmpty()) {
			for (Task.taskListener listener : listenerList)
				listener.onProgressChanged(this.progress,this.presentName);
		}
	}
	
	public void setDesc(String name){
		desc=name;
	}
	public void setPresentDesc(String name){
		presentName=name;
	}
	
	public String getDesc() {
		return desc;
	}

	public boolean getFinish() {
		return finish;
	}

	
	//ftp 主功能
	private Runnable ftpclient = new Runnable() {
	      public void run() {
	          ftpConnect(ftpHost,ftpUserName,ftpPassword,port);
	        	      //Directory start
	          	  if( desc.charAt(desc.length()-1) == '/') { 	  
	          		  //創遠端目錄
		          	  String remoteDirName = desc.substring(0,desc.lastIndexOf("/"));  
		          	  remoteDirName = remoteDirName.substring(remoteDirName.lastIndexOf("/")+1,remoteDirName.length());
		          	  try {
		          		  mFTPClient.makeDirectory(ftpRemoteDirectory+"/"+remoteDirName);
					  } catch (IOException e) {
						  Log.i("ftp", "fail to create directory");
						  e.printStackTrace();
					  }
		          	  
	          		  //上傳目錄底下的檔案
			          File dir = new File(desc);
			          String[] child = dir.list();
			         
			          if(child == null){
			        	  	Log.i("ftp", "Error: There is no files in Local Directory" );
			        	  	num_files=0;
			          }
			          else{
			        	  	  num_files=child.length;
			              for(int i = 0; i < child.length; i++){
			                String fileName = child[i];
			                String presentName = desc +" : "+fileName+"  ["+String.valueOf(i+1)+"/"+String.valueOf(child.length)+"]";
			                setPresentDesc(presentName); //更改看到的textView:顯示目錄下有幾個檔案完成
					        ftpUpload(desc+fileName,fileName,ftpRemoteDirectory+"/"+remoteDirName);
					        if( Delete == "yes"){
					        		Deletefile(desc+fileName);
					        }
			              }
			          }
	          	  } //Directory end
	          	  
	          	  //File start 
	          	  else{
	          		
	          		String fileName = desc.substring(desc.lastIndexOf("/")+1,desc.length());	          		
	          		ftpUpload(desc,fileName,ftpRemoteDirectory);
	          		if( Delete == "yes"){
	          			Deletefile(desc+fileName);
	          		}
	          	  }//File end
	          
	          ftpDisconnect();
	      }
	      
	   };//Runable
	   
	   public boolean ftpConnect(String host, String username,String password, int port){
			try {
				mFTPClient = new FTPClient();
				// connecting to the host
				mFTPClient.connect(host,port);
				
				// now check the reply code, if positive mean connection success
				if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {
					boolean status = mFTPClient.login(username, password);
					mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
					mFTPClient.enterLocalPassiveMode();
					return status;
				}
			} catch(Exception e) {
				Log.i("ftp", "Error: could not connect to host " + host + ":" + String.valueOf(port) );
			}
		return false;
	 }
	 
	 public boolean ftpDisconnect()
	 {
	     try {
	         mFTPClient.logout();
	         mFTPClient.disconnect();
	         return true;
	     } catch (Exception e) {
	         Log.i("ftp", "Error occurred while disconnecting from ftp server.");
	     }

	     return false;
	 }
	 
	 public boolean ftpChangeDirectory(String directory_path){
	     try {
	         return mFTPClient.changeWorkingDirectory(directory_path);
	     } catch(Exception e) {
	         Log.i("ftp", "Error: could not change directory to " + directory_path);
	     }

	     return false;
	 }
	 
	 public boolean ftpUpload(String srcFilePath, String desFileName,String desDirectory){
		 boolean completed = false;

		 try {
			FileInputStream srcFileStream = new FileInputStream(srcFilePath);
			File file=new File(srcFilePath);
			// change working directory to the destination directory
			if (ftpChangeDirectory(desDirectory)) {
				InputStream inputStream = new FileInputStream(srcFilePath);
				OutputStream outputStream = mFTPClient.storeFileStream(desFileName);
	            byte[] bytesIn = new byte[4096];
	            int read = 0;
	            int now_progress=0;
	            while ((read = inputStream.read(bytesIn)) != -1) {
	                outputStream.write(bytesIn, 0, read);
	                now_progress+=read;
	                int complete_percent = (int)((now_progress*100)/(file.length()));
	                setProgress(complete_percent);
	            }
	            
	            inputStream.close();
	            outputStream.close();

	            completed = mFTPClient.completePendingCommand();
	            if (completed){
	                Log.i("ftp", srcFilePath+" is uploaded successfully.");
	            }

			}
			
			srcFileStream.close();
			return completed;
			
		} catch (Exception e) {
			Log.i("ftp", "upload failed "+srcFilePath);
		}
		
		return completed;
	 }
	 
	 public boolean Deletefile(String srcFilePath){
		boolean status = false;
		File file = new File(srcFilePath);
		status = file.delete();
		return status;
	 }
	
}
