
package com.elex.chatservice.view;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.elex.chatservice.R;
import com.elex.chatservice.model.ConfigManager;
import com.elex.chatservice.model.LanguageKeys;
import com.elex.chatservice.model.LanguageManager;
import com.elex.chatservice.model.UserInfo;
import com.elex.chatservice.model.UserManager;
import com.elex.chatservice.model.viewholder.ViewHolderHelper;
import com.elex.chatservice.util.FixedAspectRatioFrameLayout;
import com.elex.chatservice.util.ImageUtil;
import com.elex.chatservice.util.RoundImageView;
import com.elex.chatservice.util.ScaleUtil;
import com.elex.chatservice.view.actionbar.MyActionBarActivity;

/**
 * Replacement of ArrayAdapter with custom filtering conditions.
 * Was created for {@link android.widget.AutoCompleteTextView}, but may be used everywhere else.
 * Typical implementation might look like as shown:
 * <pre>
 * {@code
 *      FilteredArrayAdapter<Teacher> teachersAdapter =
 *      new FilteredArrayAdapter<Teacher>(this,
 *                                      android.R.layout.simple_dropdown_item_1line,
 *                                      getAllTeachers())
 *     {
 *          {@literal @}Override
 *          protected boolean isFilterCondition(final Teacher data, String constraint) {
 *              return data.getLastName().contains(constraint)
 *                      || data.getFirstName().contains(constraint);
 *          }
 *     };
 * }
 * </pre>
 * Based on {@link android.widget.ArrayAdapter}
 */
public abstract class FilteredArrayAdapter<T> extends BaseAdapter implements Filterable {
    /**
     * Contains the list of objects that represent the data of this ArrayAdapter.
     * The content of this list is referred to as "the array" in the documentation.
     */
    protected List<T> mObjects;

    /**
     * Lock used to modify the content of {@link #mObjects}. Any write operation
     * performed on the array should be synchronized on this lock. This lock is also
     * used by the filter (see {@link #getFilter()} to make a synchronized copy of
     * the original array of data.
     */
    protected final Object mLock = new Object();

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter.
     */
    protected int mResource;

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter in a drop down widget.
     */
    protected int mDropDownResource;

    /**
     * If the inflated resource is not a TextView, mFieldId is used to find
     * a TextView inside the inflated views hierarchy. This field must contain the
     * identifier that matches the one defined in the resource file.
     */
    protected int mFieldId = 0;

    /**
     * Indicates whether or not {@link #notifyDataSetChanged()} must be called whenever
     * {@link #mObjects} is modified.
     */
    protected boolean mNotifyOnChange = true;

    protected MyActionBarActivity mActivity;

    // A copy of the original mObjects array, initialized from and then used instead as soon as
    // the mFilter CustomFilter is used. mObjects will then only contain the filtered values.
    protected ArrayList<T> mOriginalValues;
    protected CustomFilter mFilter;

    protected LayoutInflater mInflater;


    public FilteredArrayAdapter(MyActionBarActivity context, int resource) {
        init(context, resource, 0, new ArrayList<T>());
    }

    public FilteredArrayAdapter(MyActionBarActivity context, int resource, int textViewResourceId) {
        init(context, resource, textViewResourceId, new ArrayList<T>());
    }

    public FilteredArrayAdapter(MyActionBarActivity context, int resource, T[] objects) {
        init(context, resource, 0, Arrays.asList(objects));
    }

    public FilteredArrayAdapter(MyActionBarActivity context, int resource, int textViewResourceId, T[] objects) {
        init(context, resource, textViewResourceId, Arrays.asList(objects));
    }

    public FilteredArrayAdapter(MyActionBarActivity context, int resource, List<T> objects) {
        init(context, resource, 0, objects);
    }

    public FilteredArrayAdapter(MyActionBarActivity context, int resource, int textViewResourceId, List<T> objects) {
        init(context, resource, textViewResourceId, objects);
    }

    /**
     * Adds the specified object at the end of the array.
     *
     * @param object The object to add at the end of the array.
     */
    public void add(T object) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.add(object);
            } else {
                mObjects.add(object);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Adds the specified Collection at the end of the array.
     *
     * @param collection The Collection to add at the end of the array.
     */
    public void addAll(Collection<? extends T> collection) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.addAll(collection);
            } else {
                mObjects.addAll(collection);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Adds the specified items at the end of the array.
     *
     * @param items The items to add at the end of the array.
     */
    public void addAll(T ... items) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                Collections.addAll(mOriginalValues, items);
            } else {
                Collections.addAll(mObjects, items);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Inserts the specified object at the specified index in the array.
     *
     * @param object The object to insert into the array.
     * @param index The index at which the object must be inserted.
     */
    public void insert(T object, int index) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.add(index, object);
            } else {
                mObjects.add(index, object);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Removes the specified object from the array.
     *
     * @param object The object to remove.
     */
    public void remove(T object) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.remove(object);
            } else {
                mObjects.remove(object);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Remove all elements from the list.
     */
    public void clear() {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.clear();
            } else {
                mObjects.clear();
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Sorts the content of this adapter using the specified comparator.
     *
     * @param comparator The comparator used to sort the objects contained
     *        in this adapter.
     */
    public void sort(Comparator<? super T> comparator) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                Collections.sort(mOriginalValues, comparator);
            } else {
                Collections.sort(mObjects, comparator);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        mNotifyOnChange = true;
    }

    /**
     * Control whether methods that change the list ({@link #add},
     * {@link #insert}, {@link #remove}, {@link #clear}) automatically call
     * {@link #notifyDataSetChanged}.  If set to false, caller must
     * manually call notifyDataSetChanged() to have the changes
     * reflected in the attached view.
     *
     * The default is true, and calling notifyDataSetChanged()
     * resets the flag to true.
     *
     * @param notifyOnChange if true, modifications to the list will
     *                       automatically call {@link
     *                       #notifyDataSetChanged}
     */
    public void setNotifyOnChange(boolean notifyOnChange) {
        mNotifyOnChange = notifyOnChange;
    }

    private void init(MyActionBarActivity context, int resource, int textViewResourceId, List<T> objects) {
    	mActivity = context;
    	if(context!=null)
    		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResource = mDropDownResource = resource;
        mObjects = objects;
        mFieldId = textViewResourceId;
    }
    
    public void refreshData(List<T> objects)
    {
    	mObjects = objects;
    	notifyDataSetChanged();
    }

    /**
     * Returns the context associated with this array adapter. The context is used
     * to create views from the resource passed to the constructor.
     *
     * @return The Context associated with this adapter.
     */
    public Context getContext() {
        return mActivity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        return mObjects.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getItem(int position) {
        return mObjects.get(position);
    }

    /**
     * Returns the position of the specified item in the array.
     *
     * @param item The item to retrieve the position of.
     *
     * @return The position of the specified item.
     */
    public int getPosition(T item) {
        return mObjects.indexOf(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mResource);
    }

	private void adjustSize(View convertView)
	{
		if (convertView != null && ConfigManager.getInstance().scaleFontandUI && ConfigManager.scaleRatio > 0 && mActivity!=null)
		{
			int length = (int) (ScaleUtil.dip2px(30) * ConfigManager.scaleRatio * mActivity.getScreenCorrectionFactor());
			FixedAspectRatioFrameLayout user_pic_layout = ViewHolderHelper.get(convertView, R.id.user_pic_layout);
			if (user_pic_layout != null)
			{
				RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) user_pic_layout.getLayoutParams();
				layoutParams.width = length;
				layoutParams.height = length;
				user_pic_layout.setLayoutParams(layoutParams);
			}
			
			TextView nameLabel = ViewHolderHelper.get(convertView, R.id.nameLabel);
			if (nameLabel != null)
				ScaleUtil.adjustTextSize(nameLabel, ConfigManager.scaleRatio);
		}
	}

    private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {

        if (convertView == null) {
        	convertView = mInflater.inflate(R.layout.cs__autocomplete_item, parent, false);
        	adjustSize(convertView);
        } 
        
        RoundImageView user_icon = ViewHolderHelper.get(convertView, R.id.user_icon);
        if(user_icon!=null)
        {
        	GradientDrawable bgShape = (GradientDrawable) user_icon.getBackground();
			if (bgShape != null)
				bgShape.setColor(0xFF2E3D59);
//        	if(position == 0)
//            {
//        		user_icon.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.icon_mail_name));
//            }
//        	else
//        	{
        		UserInfo user = (UserInfo)getItem(position);
        		if(user!=null)
        		{
        			if(mActivity!=null)
        				ImageUtil.setHeadImage(mActivity, user.headPic, user_icon, user);
        		}
//        	}
        }
        
        TextView nameLabel = ViewHolderHelper.get(convertView, R.id.nameLabel);
        if(nameLabel!=null)
        {
//        	if(position == 0)
//            {
//        		nameLabel.setText(LanguageManager.getLangByKey(LanguageKeys.NAME_ALL_USER));
//            }
//        	else
//        	{
        		UserInfo user = (UserInfo)getItem(position);
        		if(user!=null)
        		{
        			String showText = "";
        			if(user.isNpc())
        			{
        				showText = LanguageManager.getNPCName();
        			}
        			else
        			{
        				if(StringUtils.isNotEmpty(user.userName))
            				showText = user.userName;
            			else
            				showText = user.uid;
        			}
        			
        			if(!user.isNpc() && StringUtils.isNotEmpty(ChatFragmentNew.currentAtText) && StringUtils.isNotEmpty(showText))
        			{
        				SpannableStringBuilder style = new SpannableStringBuilder(showText);
        				int startPos = showText.indexOf(ChatFragmentNew.currentAtText);
        				if(startPos>=0)
        				{
        					style.setSpan(new ForegroundColorSpan(0xff07ba02),startPos, startPos+ChatFragmentNew.currentAtText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        					nameLabel.setText(style);
        				}
        				else
        					nameLabel.setText(showText);
        			}
        			else
        				nameLabel.setText(showText);
        		}
//        	}
        }
        return convertView;
    }

    /**
     * <p>Determines how one item will be applied to TextView.
     * If the method {@code T.toString()} is not suitable,
     * and it can not be rewritten,
     * then {@code @Override} this method as you wish </p>
     *
     * @param item the item to bind
     * @return String representation of item
     */
    protected String stringifyItem(T item) {
        return item.toString();
    }

    /**
     * <p>Sets the layout resource to create the drop down views.</p>
     *
     * @param resource the layout resource defining the drop down views
     * @see #getDropDownView(int, android.view.View, android.view.ViewGroup)
     */
    public void setDropDownViewResource(int resource) {
        this.mDropDownResource = resource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mDropDownResource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new CustomFilter();
        }
        return mFilter;
    }

    /**
     * <p>This method must be implemented in working subclasses.
     * During filtration this method is used for each item from the {@link #mOriginalValues}.
     * If condition returns {@code true}, then item will be added to FilterResults,
     * otherwise item will be omitted</p>
     *
     * @param item each item in collection
     * @param constraint text specified
     */
    protected abstract boolean isFilterCondition(final T item, String constraint);

    private class CustomFilter extends Filter {
        /**
         * {@inheritDoc}
         */
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (mOriginalValues == null) {
                synchronized (mLock) {
                    mOriginalValues = new ArrayList<T>(mObjects);
                }
            }

            if (constraint == null || constraint.length() == 0) {
                ArrayList<T> list;
                synchronized (mLock) {
                    list = new ArrayList<T>(mOriginalValues);
                }
                results.values = list;
                results.count = list.size();
            } else {
                final String stringConstraint = constraint.toString();
                ArrayList<T> values;
                synchronized (mLock) {
                    values = new ArrayList<T>(mOriginalValues);
                }

                final ArrayList<T> newValues = new ArrayList<T>();

                for (final T value : values) {
                    if (isFilterCondition(value, stringConstraint)) {
                        newValues.add(value);
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            mObjects = (List<T>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
