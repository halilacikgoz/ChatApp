package com.example.dolby.chatfirebase;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;

import com.example.dolby.chatfirebase.MessageImage;
import com.example.dolby.chatfirebase.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class MessagesImageAdapter
        extends RecyclerView.Adapter<MessagesImageAdapter.ViewHolder>
{

    private final Context context;

    private List<MessageImage> messages = new ArrayList<>();

    public MessagesImageAdapter( Context context )
    {
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType )
    {
        View view = LayoutInflater.from( parent.getContext() ).inflate(R.layout.message_single_layout, parent, false );
        return new ViewHolder( view );
    }

    @Override
    public void onBindViewHolder( ViewHolder viewHolder, int position )
    {
        MessageImage message = messages.get( position );

        if ( message.getImageUrl() != null )
        {
            viewHolder.imageView.setVisibility( View.VISIBLE );
            Picasso.with( context ).load( message.getImageUrl() ).into( viewHolder.imageView );
        }
        else
        {
        }
    }

    @Override
    public int getItemCount()
    {
        return messages.size();
    }

    public void addMessage( MessageImage message )
    {
        messages.add( 0, message );
        notifyDataSetChanged();
    }

    public void removeMessage( MessageImage message )
    {
        messages.remove( message );
        notifyDataSetChanged();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder
    {

        @Bind( R.id.image )
        ImageView imageView;

        ViewHolder( View view )
        {
            super( view );
            ButterKnife.bind( this, view );
        }
    }
}
