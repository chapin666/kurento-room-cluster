package tv.lycam.server.api.module.attachment;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tv.lycam.server.api.module.meeting.MeetingModel;
import tv.lycam.server.api.module.user.UserModel;
import tv.lycam.server.api.utils.AttachmentDeserializer;
import tv.lycam.server.api.utils.AttachmentSerializer;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by lycamandroid on 2017/6/19.
 */
@Document(collection = "attachment")
public class AttachmentModel implements Serializable {

    @Id
    private String id;
    private String title;

    @NotNull(message = "类型不能为空")
    @JsonSerialize(using=AttachmentSerializer.class)
    @JsonDeserialize(using=AttachmentDeserializer.class)
    private AttachmentType attachmentType;
    private String icon;
    private String link;
    private MeetingModel meeting;
    private UserModel createUser;
    private String createTime;
    private boolean isDistribution;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public AttachmentType getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(AttachmentType attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public MeetingModel getMeeting() {
        return meeting;
    }

    public void setMeeting(MeetingModel meeting) {
        this.meeting = meeting;
    }

    public UserModel getCreateUser() {
        return createUser;
    }

    public void setCreateUser(UserModel createUser) {
        this.createUser = createUser;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public boolean isDistribution() {
        return isDistribution;
    }

    public void setDistribution(boolean distribution) {
        isDistribution = distribution;
    }


    @Override
    public String toString() {
        return "AttachmentModel{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", attachmentType=" + attachmentType +
                ", icon='" + icon + '\'' +
                ", link='" + link + '\'' +
                ", meeting=" + meeting +
                ", createUser=" + createUser +
                ", createTime='" + createTime + '\'' +
                ", isDistribution=" + isDistribution +
                '}';
    }
}
