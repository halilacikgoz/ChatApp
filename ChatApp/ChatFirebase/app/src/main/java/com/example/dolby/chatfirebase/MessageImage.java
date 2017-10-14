package com.example.dolby.chatfirebase;

/**
 * Created by DOLBY on 13-Aug-17.
 */

public class MessageImage {

    String imageUrl;

    //Used by json parser
    public MessageImage()
    {
    }


    public MessageImage( String imageUrl )
    {
        this.imageUrl = imageUrl;
    }


    public String getImageUrl()
    {
        return imageUrl;
    }

}
