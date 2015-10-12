package com.ndhunju.dailyjournal.viewPager;

import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.ndhunju.dailyjournal.model.Attachment;

import java.util.List;

/**
 * Created by dhunju on 10/4/2015.
 */
class AttachmentPagerAdapter extends PagerAdapter {

        private List<Attachment> mAttachments = null;

        public AttachmentPagerAdapter(List<Attachment> attachments) {
            mAttachments = attachments;
        }

        @Override
        public int getCount() {
            return mAttachments.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());
            photoView.setImageDrawable(Drawable
                     .createFromPath(mAttachments.get(position).getPath()));
            // Now just add PhotoView to ViewPager and return it
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT,
                                         ViewGroup.LayoutParams.MATCH_PARENT);
            return photoView;
        }

        public Attachment getItem(int pos){
            return mAttachments.get(pos);
        }

        public void deleteItem(int pos){
            mAttachments.remove(pos);
        }

        public void addItem(Attachment attachment){
            mAttachments.add(attachment);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
}
