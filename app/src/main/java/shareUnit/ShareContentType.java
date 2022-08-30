package shareUnit;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@StringDef({ShareContentType.TEXT, ShareContentType.IMAGE,
        ShareContentType.AUDIO, ShareContentType.VIDEO, ShareContentType.FILE})
@Retention(RetentionPolicy.SOURCE)
public @interface ShareContentType {
   public final String TEXT = "text/plain";
   public final String IMAGE = "image/*";
   public final String AUDIO = "audio/*";
   public final String VIDEO = "video/*";
   public final String FILE = "*/*";
}
