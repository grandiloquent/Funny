package euphoria.psycho.funny.server;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Note {

    @SerializedName("id")
    public Long ID;
    @SerializedName("title")
    public String Title;
    @SerializedName("content")
    public String Content;
    @SerializedName("updateTime")
    public Date UpdateTime;
    @SerializedName("createTime")
    public Date CreateTime;
}
