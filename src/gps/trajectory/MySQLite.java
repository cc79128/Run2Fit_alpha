package gps.trajectory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;




public class MySQLite extends SQLiteOpenHelper {
	
	SQLiteDatabase db; //��Ʈw����
	private static final int VERSION = 2;//��Ʈw����

	public MySQLite(Context context) {
		super(context, "/sdcard/gpslogger2.db", null, VERSION); //�طs��Ʈw�n�惡
		db=this.getWritableDatabase();  //�Ndb������/sdcard/gpslogger.db
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//�إ߸�ƪ�
		Log.d("HsinHsi","create table");
		String DATABASE_story = " story2" ;
		String DATABASE_CREATE_story = "create table" + DATABASE_story +"(_id TEXT,createday TEXT,title TEXT,desc TEXT,distance TEXT,upload INT);" ;
		db.execSQL(DATABASE_CREATE_story); //�s�W�G�Ƹ�ƪ�
		}

	//�Y��Ʈw��������h�۰ʶi�J��function
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { 
		if (newVersion > oldVersion) {
			   db.beginTransaction();//�إߥ��
			     
			    boolean success = false;//�P�_�Ѽ�
			        
			    //�Ѥ��e���Ϊ������A�i�����P���ʧ@     
			    switch (oldVersion) {
			    case 1:           
			      db.execSQL("ALTER TABLE  story2 ADD COLUMN upload integer DEFAULT 0"); //���J�s���
			     // db.execSQL("ALTER TABLE newMemorandum ADD COLUMN type VARCHAR");
			     // db.execSQL("ALTER TABLE newMemorandum ADD COLUMN memo VARCHAR");
			      oldVersion++;      
			     success = true;
			     break;
			    }
			                
			     if (success) {
			       db.setTransactionSuccessful();//���T����~���\
			      }
			    db.endTransaction();
			  }
			  else {
			    onCreate(db);
			  }   
		//db.execSQL("DROP TABLE IF EXISTS story2");	//�R���¦�����ƪ�
		//onCreate(db);

	}
}
