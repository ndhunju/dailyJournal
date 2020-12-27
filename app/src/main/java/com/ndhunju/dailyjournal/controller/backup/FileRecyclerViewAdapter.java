package com.ndhunju.dailyjournal.controller.backup;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.api.services.drive.model.File;
import com.ndhunju.dailyjournal.R;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FileRecyclerViewAdapter
        extends RecyclerView.Adapter<FileRecyclerViewAdapter.FileViewHolder> {

    // Member Variables
    private List<File> fileList = Collections.emptyList();
    private OnFileSelectListener onFileSelectListener;

    public void setFileList(List<File> fileList) {
        this.fileList = fileList;
        notifyDataSetChanged();
    }

    public void setOnFileSelectListener(OnFileSelectListener onFileSelectListener) {
        this.onFileSelectListener = onFileSelectListener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileViewHolder(
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.list_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        holder.onBind(getItem(position));
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public File getItem(int position) {
        return fileList.get(position);
    }

    class FileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        File file;
        TextView title;
        TextView description;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            title = itemView.findViewById(R.id.tvName);
            description = itemView.findViewById(R.id.tvDesc);
        }

        public void onBind(File file) {
            this.file = file;
            title.setText(file.getName());
            description.setText(description
                    .getContext()
                    .getString(
                            R.string.msg_file_description,
                            // Relative date
                            DateUtils.getRelativeDateTimeString(
                                    description.getContext(),
                                    file.getModifiedTime() != null
                                            ? file.getModifiedTime().getValue()
                                            : file.getCreatedTime().getValue(),
                                    0,
                                    0,
                                    0
                            ).toString(),
                            // Size of file in MB
                            file.getSize() != null ? String.valueOf(file.getSize()/1048576) : "(N/A)"
                    )
             );
        }

        @Override
        public void onClick(View view) {
            if (onFileSelectListener != null) {
                onFileSelectListener.onFileSelect(file);
            }
        }
    }

    interface OnFileSelectListener{
        /** Passed {@link File} instance was selected. */
        void onFileSelect(File file);
    }
}
