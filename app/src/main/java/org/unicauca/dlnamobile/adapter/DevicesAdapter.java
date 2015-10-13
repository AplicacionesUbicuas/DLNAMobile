package org.unicauca.dlnamobile.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.unicauca.dlnamobile.R;

import java.util.List;

public class DevicesAdapter extends ArrayAdapter<String> {
	private Context context;
	private List<String> devices;

	public DevicesAdapter(Context context, int textViewResourceId,
			List<String> devices) {
		super(context, textViewResourceId, devices);
		this.context = context;
		this.setDevices(devices);
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		View rowView = convertView;

		if (rowView == null) {
			// ROW INFLATION
			Log.d(this.getClass().getSimpleName(),
					"Starting XML Row Inflation ... ");
			LayoutInflater inflater = (LayoutInflater) this.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.list_item_devices, parent,
					false);
			Log.d(this.getClass().getSimpleName(),
					"Successfully completed XML Row Inflation!");
		}

		// Advertisement a = advertisements.get(position);
		String device = devices.get(position);
		TextView titleTV = (TextView) rowView.findViewById(R.id.TvTitleDev);
		// String desc = a.getDescript();
		titleTV.setText(device);

		return rowView;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public List<String> getDevices() {
		return devices;
	}

	public void setDevices(List<String> devices) {
		this.devices = devices;
	}

}
