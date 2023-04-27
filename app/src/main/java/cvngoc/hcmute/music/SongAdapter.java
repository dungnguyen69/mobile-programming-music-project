package cvngoc.hcmute.music;


import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import cvngoc.hcmute.music.databinding.CustormMusicItemsBinding;


public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ImageViewHolder> {
    private Context mContext;
    private List<Song> listSongs;
    //    private AdapterView.OnItemClickListener mListener;
    private OnItemClickListener mListener;
    CustormMusicItemsBinding binding;

    public SongAdapter(Context context, List<Song> songs) {
        mContext = context;
        listSongs = songs;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View v = LayoutInflater.from(mContext).inflate(ImageItemBinding.class, parent, false);

        binding = CustormMusicItemsBinding.inflate(LayoutInflater.from(mContext), parent, false);
        View v = binding.getRoot();
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        Song uploadCurrent = listSongs.get(position);
        holder.tvTitle.setText(uploadCurrent.getTitle());
        holder.tvSingle.setText(uploadCurrent.getSingle());
        Picasso.get().load(uploadCurrent.getImage()).fit().centerCrop().into(holder.imgAvatar);
    }

    @Override
    public int getItemCount() {
        return listSongs.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        public TextView tvTitle;
        public TextView tvSingle;
        public ImageView imgAvatar;

        public ImageViewHolder(View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tv_titleSong_item);
            tvSingle = itemView.findViewById(R.id.tv_singleSong_item);
            imgAvatar = itemView.findViewById(R.id.img_song_item);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);


        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
//                    mListener.onItemClick(position);
                    mListener.onItemClick(position);
                }
            }
        }

        @Override
        public boolean onMenuItemClick(@NonNull MenuItem item) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {

                    switch (item.getItemId()) {
                        case 1:
                            mListener.onWhatEverClick(position);
                            return true;
                        case 2:
                            mListener.onDeleteClick(position);
                            return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Select Action");
            MenuItem doWhatever = menu.add(Menu.NONE, 1, 1, "Do whatever");
            MenuItem delete = menu.add(Menu.NONE, 2, 2, "Delete");

            doWhatever.setOnMenuItemClickListener(this);
            delete.setOnMenuItemClickListener(this);
        }
    }
    public interface OnItemClickListener {
        void onItemClick(int position);

        void onWhatEverClick(int position);

        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
}
