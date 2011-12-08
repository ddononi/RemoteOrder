package kr.co.remoteorder;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MyListAdapter extends BaseAdapter {
	private ArrayList<Order> list;
	private Context context;
	public MyListAdapter(Context context, ArrayList<Order> list){
		this.context = context;
		this.list = list;
	}

	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewGroup item = getViewGroup(convertView, parent);
		TextView orderTV = (TextView)item.findViewById(R.id.item);
		TextView timeTV = (TextView)item.findViewById(R.id.time);
		orderTV.setText(getItem(position).toString());
		orderTV.setText(getItem(position).toString());
		
		return item;	
	}
	
	/*
	 * 뷰가 재사용 가능한지 체크
	 */
	private ViewGroup getViewGroup(View reuse, ViewGroup parent){
		if(reuse instanceof ViewGroup){	/
			return (ViewGroup)reuse;
		}
		
		Context context = parent.getContext();	// 컨택스트를 얻어온다.
		LayoutInflater inflater = LayoutInflater.from(context);
		// custom list를 만들어준다.
		ViewGroup item = (ViewGroup)inflater.inflate(R.layout.list, null);
		return item;
	}	

}