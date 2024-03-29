package gps.trajectory;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class MyAdapter extends BaseAdapter {
    public static HashMap<Integer, Boolean> isSelected;
    private Context context = null;
    private LayoutInflater inflater = null;
    private List<HashMap<String, Object>> list = null;
    private String keyString[] = null;
    private String itemString = null; // 记录每个item中textview的值
    private String upString = null; //紀錄有沒有被upload
    private int idValue[] = null;// id值

    public MyAdapter(Context context, List<HashMap<String, Object>> list,
            int resource, String[] from, int[] to) {
        this.context = context;
        this.list = list;
        keyString = new String[from.length];
        idValue = new int[to.length];
        System.arraycopy(from, 0, keyString, 0, from.length);
        System.arraycopy(to, 0, idValue, 0, to.length);
        inflater = LayoutInflater.from(context);
        init();
    }

    // 初始化 设置所有checkbox都为未选择
    public void init() {
        isSelected = new HashMap<Integer, Boolean>();
        for (int i = 0; i < list.size(); i++) {
            isSelected.put(i, false);
        }
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int arg0) {
        return list.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup arg2) {
        ViewHolder holder = null;
        if (holder == null) {
            holder = new ViewHolder();
            if (view == null) {
                view = inflater.inflate(R.layout.listviewitem, null);
            }
            holder.tv = (TextView) view.findViewById(R.id.item_tv);
            holder.cb = (CheckBox) view.findViewById(R.id.item_cb);
            holder.up = (ImageView) view.findViewById(R.id.imageView_up);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        HashMap<String, Object> map = list.get(position);
        if (map != null) {
            itemString = (String) map.get(keyString[0]);        
            upString = (String) map.get(keyString[2]);
            holder.tv.setText(itemString);
            if(upString.equals("UPLOAD")){
            	Log.d("upload","字串=upload");
            	holder.up.setImageResource(R.drawable.upload_ok);
            }
            else{
            	Log.d("upload","字串不等於upload");
            	holder.up.setImageResource(R.drawable.upload_not);
            }
        }
        holder.cb.setChecked(isSelected.get(position));
        return view;
    }

}