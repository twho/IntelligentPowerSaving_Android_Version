package com.tsungweiho.intelligentpowersaving.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tsungweiho.intelligentpowersaving.R;

/**
 * Class for setting side menu in InboxFragment
 * <p>
 * This class is the user interface of side menu in InboxFragment
 *
 * @author Tsung Wei Ho
 * @version 0218.2017
 * @since 1.0.0
 */
public class DrawerListAdapter extends BaseAdapter {
    private Context context;

    // Constants
    private int[] functions;
    private int[] drawables;

    /**
     * DrawerListAdapter constructor
     *
     * @param context   the context that uses this class
     * @param functions the function description in String resources
     * @param drawables the image of the functions in drawable resources
     */
    public DrawerListAdapter(Context context, int[] functions, int[] drawables) {
        this.context = context;
        this.functions = functions;
        this.drawables = drawables;
    }

    @Override
    public int getCount() {
        return functions.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.obj_drawer_list_item, null);

            viewHolder = new ViewHolder();
            viewHolder.tvFunction = convertView.findViewById(R.id.obj_drawer_list_item_tv);
            viewHolder.imageView = convertView.findViewById(R.id.obj_drawer_list_item_iv);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvFunction.setText(context.getResources().getString(functions[position]));
        Drawable d = context.getResources().getDrawable(drawables[position]);
        viewHolder.imageView.setImageDrawable(d);
        return convertView;
    }

    /**
     * All UI widgets used in DrawerListAdapter
     */
    private class ViewHolder {
        ImageView imageView;
        TextView tvFunction;
    }
}
