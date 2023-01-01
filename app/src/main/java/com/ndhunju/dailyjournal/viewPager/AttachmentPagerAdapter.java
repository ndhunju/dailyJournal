package com.ndhunju.dailyjournal.viewPager;

import android.os.Build;
import androidx.viewpager.widget.PagerAdapter;
import androidx.core.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.ndhunju.dailyjournal.model.Attachment;
import com.ndhunju.dailyjournal.util.Utils;

import java.util.List;

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
        public View instantiateItem(ViewGroup container, final int position) {
            final PhotoView photoView = new PhotoView(container.getContext());
            photoView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override public void onGlobalLayout() {
                    // make sure PhotoView is laid out so that its dimensions are measured
                    if (ViewCompat.isLaidOut(photoView)) {
                        photoView.setImageBitmap(Utils.scaleBitmap(mAttachments.get(position).getPath(), photoView.getWidth(), photoView.getHeight()));
                        photoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
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
