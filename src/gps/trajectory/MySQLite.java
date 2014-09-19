package gps.trajectory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;




public class MySQLite extends SQLiteOpenHelper {
	
	SQLiteDatabase db; //資料庫物件
	private static final int VERSION = 2;//資料庫版本

	public MySQLite(Context context) {
		super(context, "/sdcard/gpslogger2.db", null, VERSION); //建新資料庫要改此
		db=this.getWritableDatabase();  //將db對應到/sdcard/gpslogger.db
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//建立資料表
		Log.d("HsinHsi","create table");
		String DATABASE_story = " story2" ;
		String DATABASE_CREATE_story = "create table" + DATABASE_story +"(_id TEXT,createday TEXT,title TEXT,desc TEXT,distance TEXT,upload INT);" ;
		db.execSQL(DATABASE_CREATE_story); //新增故事資料表
		}

	//若資料庫版本有改則自動進入此function
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { 
		if (newVersion > oldVersion) {
			   db.beginTransaction();//建立交易
			     
			    boolean success = false;//判斷參數
			        
			    //由之前不用的版本，可做不同的動作     
			    switch (oldVersion) {
			    case 1:           
			      db.execSQL("ALTER TABLE  story2 ADD COLUMN upload integer DEFAULT 0"); //插入新欄位
			     // db.execSQL("ALTER TABLE newMemorandum ADD COLUMN type VARCHAR");
			     // db.execSQL("ALTER TABLE newMemorandum ADD COLUMN memo VARCHAR");
			      oldVersion++;      
			     success = true;
			     break;
			    }
			                
			     if (success) {
			       db.setTransactionSuccessful();//正確交易才成功
			      }
			    db.endTransaction();
			  }
			  else {
			    onCreate(db);
			  }   
		//db.execSQL("DROP TABLE IF EXISTS story2");	//刪除舊有的資料表
		//onCreate(db);

	}
}
